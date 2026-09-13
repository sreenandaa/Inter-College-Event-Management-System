package com.intercollege.service;

import com.intercollege.model.Event;
import com.intercollege.model.EventStatus;
import com.intercollege.model.User;
import com.intercollege.repository.EventRepository;
import com.intercollege.util.GeoUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing event discovery, location calculations, urgency rules,
 * and coordinator authorization.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Calculates dynamic status and distance for an event relative to current date/time
     * and optional user coordinates.
     */
    public Event populateDynamicFields(Event event, Double userLat, Double userLon) {
        if (event == null) return null;

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDate eventDate = event.getDate() != null ? event.getDate() : today;

        long daysDiff = ChronoUnit.DAYS.between(today, eventDate);

        if (daysDiff < 0) {
            // Past event
            event.setStatus(EventStatus.COMPLETED);
            event.setUrgencyLabel("Completed");
        } else if (daysDiff == 0) {
            // Scheduled for today
            LocalTime start = event.getStartTime();
            LocalTime end = event.getEndTime();

            if (end != null && now.isAfter(end)) {
                event.setStatus(EventStatus.COMPLETED);
                event.setUrgencyLabel("Completed Today");
            } else if (start != null && now.isAfter(start)) {
                event.setStatus(EventStatus.ONGOING);
                event.setUrgencyLabel("Ongoing Now");
            } else {
                event.setStatus(EventStatus.TODAY);
                event.setUrgencyLabel("Happening Today");
            }
        } else if (daysDiff == 1) {
            event.setStatus(EventStatus.HAPPENING_SOON);
            event.setUrgencyLabel("Happening Tomorrow");
        } else if (daysDiff <= 3) {
            event.setStatus(EventStatus.HAPPENING_SOON);
            event.setUrgencyLabel("In " + daysDiff + " Days");
        } else if (daysDiff <= 7) {
            event.setStatus(EventStatus.UPCOMING);
            event.setUrgencyLabel("This Week");
        } else {
            event.setStatus(EventStatus.UPCOMING);
            event.setUrgencyLabel("Upcoming");
        }

        // Calculate distance if both user and event coordinates are available
        if (userLat != null && userLon != null && event.getLatitude() != null && event.getLongitude() != null) {
            double distance = GeoUtil.calculateDistanceKm(userLat, userLon, event.getLatitude(), event.getLongitude());
            event.setDistanceKm(distance);
        } else {
            event.setDistanceKm(null);
        }

        return event;
    }

    /**
     * Comprehensive search, filter, and sort for event discovery.
     */
    public List<Event> searchEvents(String query, String college, String category, String city,
                                   String dateFilter, Double maxDistanceKm,
                                   Double userLat, Double userLon, String sortBy) {

        List<Event> all = eventRepository.findAll();

        // Populate dynamic status and distance for each event
        List<Event> processed = all.stream()
                .map(e -> populateDynamicFields(e, userLat, userLon))
                .collect(Collectors.toList());

        return processed.stream()
                .filter(e -> {
                    // Search query matches name, college, city, or description
                    if (query != null && !query.trim().isEmpty()) {
                        String q = query.trim().toLowerCase();
                        boolean matchesName = e.getName() != null && e.getName().toLowerCase().contains(q);
                        boolean matchesCollege = e.getCollege() != null && e.getCollege().toLowerCase().contains(q);
                        boolean matchesCity = e.getCity() != null && e.getCity().toLowerCase().contains(q);
                        boolean matchesDesc = e.getDescription() != null && e.getDescription().toLowerCase().contains(q);
                        boolean matchesCat = e.getCategory() != null && e.getCategory().toLowerCase().contains(q);
                        if (!matchesName && !matchesCollege && !matchesCity && !matchesDesc && !matchesCat) {
                            return false;
                        }
                    }

                    // College filter
                    if (college != null && !college.trim().isEmpty() && !"all".equalsIgnoreCase(college.trim())) {
                        if (e.getCollege() == null || !e.getCollege().equalsIgnoreCase(college.trim())) {
                            return false;
                        }
                    }

                    // Category filter
                    if (category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category.trim())) {
                        String catFilter = category.trim().toLowerCase();
                        boolean primaryMatch = e.getCategory() != null && e.getCategory().toLowerCase().contains(catFilter);
                        boolean tagMatch = e.getCategories() != null && e.getCategories().stream().anyMatch(t -> t.toLowerCase().contains(catFilter));
                        if (!primaryMatch && !tagMatch) {
                            return false;
                        }
                    }

                    // City filter
                    if (city != null && !city.trim().isEmpty() && !"all".equalsIgnoreCase(city.trim())) {
                        if (e.getCity() == null || !e.getCity().equalsIgnoreCase(city.trim())) {
                            return false;
                        }
                    }

                    // Distance radius filter
                    if (maxDistanceKm != null && maxDistanceKm > 0) {
                        if (e.getDistanceKm() == null || e.getDistanceKm() > maxDistanceKm) {
                            return false;
                        }
                    }

                    // Date filter
                    if (dateFilter != null && !dateFilter.trim().isEmpty()) {
                        LocalDate today = LocalDate.now();
                        LocalDate d = e.getDate();
                        if (d == null) return false;

                        switch (dateFilter.trim().toLowerCase()) {
                            case "today":
                                if (!d.isEqual(today)) return false;
                                break;
                            case "tomorrow":
                                if (!d.isEqual(today.plusDays(1))) return false;
                                break;
                            case "soon":
                            case "happening_soon":
                                if (d.isBefore(today) || d.isAfter(today.plusDays(3))) return false;
                                break;
                            case "this_week":
                                if (d.isBefore(today) || d.isAfter(today.plusDays(7))) return false;
                                break;
                            case "upcoming":
                                if (d.isBefore(today)) return false;
                                break;
                            case "completed":
                            case "past":
                                if (!d.isBefore(today) && e.getStatus() != EventStatus.COMPLETED) return false;
                                break;
                        }
                    }

                    return true;
                })
                .sorted(getComparator(sortBy))
                .collect(Collectors.toList());
    }

    private Comparator<Event> getComparator(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty() || "date_asc".equalsIgnoreCase(sortBy)) {
            // Default: upcoming dates first, completed last
            return Comparator.comparing((Event e) -> e.getStatus() == EventStatus.COMPLETED ? 1 : 0)
                    .thenComparing(Event::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Event::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("date_desc".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(Event::getDate, Comparator.nullsLast(Comparator.reverseOrder()));
        } else if ("distance".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(Event::getDistanceKm, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("name".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(Event::getName, String.CASE_INSENSITIVE_ORDER);
        }
        return Comparator.comparing(Event::getDate, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    /**
     * Retrieves events categorized as "Happening Soon" (today or within next 3 days).
     */
    public List<Event> getHappeningSoonEvents(Double userLat, Double userLon) {
        return searchEvents(null, null, null, null, "soon", null, userLat, userLon, "date_asc");
    }

    /**
     * Retrieves nearby events sorted by distance.
     */
    public List<Event> getNearbyEvents(Double userLat, Double userLon, Double maxDistanceKm) {
        if (userLat == null || userLon == null) {
            return new ArrayList<>();
        }
        double radius = (maxDistanceKm != null && maxDistanceKm > 0) ? maxDistanceKm : 50.0;
        return searchEvents(null, null, null, null, "upcoming", radius, userLat, userLon, "distance");
    }

    /**
     * Recommends events tailored to a student's selected interests.
     */
    public List<Event> getRecommendedEvents(User student, Double userLat, Double userLon) {
        List<String> interests = student != null && student.getInterests() != null ? student.getInterests() : new ArrayList<>();
        List<Event> upcoming = searchEvents(null, null, null, null, "upcoming", null, userLat, userLon, "date_asc");

        if (interests.isEmpty()) {
            return upcoming.stream().limit(6).collect(Collectors.toList());
        }

        // Rank events by how many interests match their categories
        return upcoming.stream()
                .sorted((e1, e2) -> {
                    long match1 = countInterestMatches(e1, interests);
                    long match2 = countInterestMatches(e2, interests);
                    return Long.compare(match2, match1); // Higher matches first
                })
                .filter(e -> countInterestMatches(e, interests) > 0)
                .limit(8)
                .collect(Collectors.toList());
    }

    private long countInterestMatches(Event event, List<String> interests) {
        long count = 0;
        for (String interest : interests) {
            String norm = interest.trim().toLowerCase();
            if (event.getCategory() != null && event.getCategory().toLowerCase().contains(norm)) {
                count++;
            }
            if (event.getCategories() != null) {
                for (String cat : event.getCategories()) {
                    if (cat.toLowerCase().contains(norm)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Retrieves all events belonging to a specific college (for coordinators).
     */
    public List<Event> getEventsByCollege(String college, Double userLat, Double userLon) {
        return eventRepository.findByCollege(college).stream()
                .map(e -> populateDynamicFields(e, userLat, userLon))
                .sorted(Comparator.comparing(Event::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public Optional<Event> getEventById(String id, Double userLat, Double userLon) {
        return eventRepository.findById(id)
                .map(e -> populateDynamicFields(e, userLat, userLon));
    }

    /**
     * Creates a new event for a coordinator's college.
     * Automatically locks the college to the coordinator's registered college.
     */
    public Event createEvent(Event event, User coordinator) {
        validateEventDetails(event);

        // Security rule: Lock the event's college strictly to the coordinator's college
        event.setCollege(coordinator.getCollege());
        event.setOrganizerName(coordinator.getFullName());

        // If coordinates weren't supplied, attempt lookup from city
        if ((event.getLatitude() == null || event.getLongitude() == null) && event.getCity() != null) {
            double[] coords = GeoUtil.getCoordinatesForCity(event.getCity());
            if (coords != null) {
                event.setLatitude(coords[0]);
                event.setLongitude(coords[1]);
            }
        }

        // Set default poster if none provided
        if (event.getPosterImage() == null || event.getPosterImage().trim().isEmpty()) {
            event.setPosterImage("https://images.unsplash.com/photo-1511578314322-379afb476865?w=800&auto=format&fit=crop&q=80");
        }

        return eventRepository.save(event);
    }

    /**
     * Updates an existing event.
     * Enforces that the coordinator can ONLY edit events belonging to their own college.
     */
    public Event updateEvent(String eventId, Event updatedData, User coordinator) {
        Event existing = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        // Security check: Verify coordinator's college matches existing event's college
        if (!existing.getCollege().equalsIgnoreCase(coordinator.getCollege())) {
            throw new SecurityException("You don't have permission to modify events from other colleges.");
        }

        validateEventDetails(updatedData);

        existing.setName(updatedData.getName());
        existing.setDescription(updatedData.getDescription());
        existing.setCategory(updatedData.getCategory());
        existing.setCategories(updatedData.getCategories());
        existing.setDate(updatedData.getDate());
        existing.setStartTime(updatedData.getStartTime());
        existing.setEndTime(updatedData.getEndTime());
        existing.setVenue(updatedData.getVenue());
        existing.setCity(updatedData.getCity());
        existing.setRegistrationLink(updatedData.getRegistrationLink());
        existing.setContactInfo(updatedData.getContactInfo());
        existing.setMaxParticipants(updatedData.getMaxParticipants());

        if (updatedData.getPosterImage() != null && !updatedData.getPosterImage().trim().isEmpty()) {
            existing.setPosterImage(updatedData.getPosterImage());
        }

        if (updatedData.getLatitude() != null && updatedData.getLongitude() != null) {
            existing.setLatitude(updatedData.getLatitude());
            existing.setLongitude(updatedData.getLongitude());
        } else if (updatedData.getCity() != null) {
            double[] coords = GeoUtil.getCoordinatesForCity(updatedData.getCity());
            if (coords != null) {
                existing.setLatitude(coords[0]);
                existing.setLongitude(coords[1]);
            }
        }

        return eventRepository.save(existing);
    }

    /**
     * Deletes an event after verifying ownership.
     */
    public boolean deleteEvent(String eventId, User coordinator) {
        Event existing = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

        // Security check
        if (!existing.getCollege().equalsIgnoreCase(coordinator.getCollege())) {
            throw new SecurityException("You don't have permission to delete events from other colleges.");
        }

        return eventRepository.deleteById(eventId);
    }

    private void validateEventDetails(Event event) {
        if (event.getName() == null || event.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Event name is required.");
        }
        if (event.getDescription() == null || event.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Event description is required.");
        }
        if (event.getCategory() == null || event.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Event category is required.");
        }
        if (event.getDate() == null) {
            throw new IllegalArgumentException("Event date is required.");
        }
        if (event.getVenue() == null || event.getVenue().trim().isEmpty()) {
            throw new IllegalArgumentException("Venue is required.");
        }
        if (event.getCity() == null || event.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City/Location is required.");
        }
        if (event.getRegistrationLink() == null || !isValidUrl(event.getRegistrationLink())) {
            throw new IllegalArgumentException("Please enter a valid registration link (must start with http:// or https://).");
        }
    }

    private boolean isValidUrl(String url) {
        if (url == null) return false;
        String lower = url.trim().toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }
}
