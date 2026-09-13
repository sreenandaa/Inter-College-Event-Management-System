package com.intercollege.controller;

import com.intercollege.dto.ApiResponse;
import com.intercollege.model.Event;
import com.intercollege.model.Role;
import com.intercollege.model.User;
import com.intercollege.service.EventService;
import com.intercollege.service.NotificationService;
import com.intercollege.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * REST Controller for discovering, searching, filtering, and managing events.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final NotificationService notificationService;

    public EventController(EventService eventService, UserService userService, NotificationService notificationService) {
        this.eventService = eventService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    private User getAuthenticatedUser(HttpSession session) {
        String userId = (String) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) {
            return null;
        }
        return userService.findById(userId).orElse(null);
    }

    /**
     * Comprehensive event discovery endpoint with searching, filtering, and sorting.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Event>>> getEvents(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) Double maxDistanceKm,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon,
            @RequestParam(required = false, defaultValue = "date_asc") String sortBy) {

        List<Event> events = eventService.searchEvents(
                query, college, category, city, dateFilter, maxDistanceKm, userLat, userLon, sortBy);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> getEventById(
            @PathVariable String id,
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon) {

        return eventService.getEventById(id, userLat, userLon)
                .map(e -> ResponseEntity.ok(ApiResponse.success(e)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Event could not be found.")));
    }

    @GetMapping("/happening-soon")
    public ResponseEntity<ApiResponse<List<Event>>> getHappeningSoon(
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon) {

        List<Event> events = eventService.getHappeningSoonEvents(userLat, userLon);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<Event>>> getNearbyEvents(
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon,
            @RequestParam(required = false, defaultValue = "50") Double maxDistanceKm) {

        List<Event> events = eventService.getNearbyEvents(userLat, userLon, maxDistanceKm);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<Event>>> getRecommendedEvents(
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon,
            HttpSession session) {

        User user = getAuthenticatedUser(session);
        List<Event> events = eventService.getRecommendedEvents(user, userLat, userLon);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Returns events belonging to the logged-in coordinator's college.
     */
    @GetMapping("/my-college")
    public ResponseEntity<ApiResponse<List<Event>>> getMyCollegeEvents(
            @RequestParam(required = false) Double userLat,
            @RequestParam(required = false) Double userLon,
            HttpSession session) {

        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole() != Role.COORDINATOR) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only coordinators can access this page."));
        }

        List<Event> events = eventService.getEventsByCollege(user.getCollege(), userLat, userLon);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    /**
     * Coordinator creates a new event. The system automatically associates
     * the event with the coordinator's registered college.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Event>> createEvent(@RequestBody Event event, HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole() != Role.COORDINATOR) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only coordinators can create events."));
        }

        Event created = eventService.createEvent(event, user);
        notificationService.notifyNewEvent(created);
        return ResponseEntity.ok(ApiResponse.success("Event created successfully!", created));
    }

    /**
     * Coordinator edits an existing event.
     * Backend strictly verifies the event belongs to the coordinator's college.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Event>> updateEvent(
            @PathVariable String id,
            @RequestBody Event event,
            HttpSession session) {

        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole() != Role.COORDINATOR) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only coordinators can edit events."));
        }

        Event updated = eventService.updateEvent(id, event, user);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully!", updated));
    }

    /**
     * Coordinator deletes an event.
     * Backend strictly verifies ownership before deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable String id, HttpSession session) {
        User user = getAuthenticatedUser(session);
        if (user == null || user.getRole() != Role.COORDINATOR) {
            return ResponseEntity.status(403).body(ApiResponse.error("Only coordinators can delete events."));
        }

        boolean deleted = eventService.deleteEvent(id, user);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Event deleted successfully.", null));
        } else {
            return ResponseEntity.status(404).body(ApiResponse.error("Event not found."));
        }
    }

    /**
     * Helper endpoint to get all distinct colleges in the system.
     */
    @GetMapping("/colleges")
    public ResponseEntity<ApiResponse<Set<String>>> getColleges() {
        Set<String> colleges = eventService.searchEvents(null, null, null, null, null, null, null, null, "date_asc")
                .stream()
                .map(Event::getCollege)
                .filter(c -> c != null && !c.trim().isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
        return ResponseEntity.ok(ApiResponse.success(colleges));
    }

    /**
     * Helper endpoint to get distinct cities/locations in the system for autocomplete.
     */
    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<Set<String>>> getCities() {
        Set<String> cities = eventService.searchEvents(null, null, null, null, null, null, null, null, "date_asc")
                .stream()
                .map(Event::getCity)
                .filter(c -> c != null && !c.trim().isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
        return ResponseEntity.ok(ApiResponse.success(cities));
    }

    /**
     * Summary statistics for coordinator or student dashboards.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(HttpSession session) {
        User user = getAuthenticatedUser(session);
        List<Event> all = eventService.searchEvents(null, null, null, null, null, null, null, null, "date_asc");

        long total = all.size();
        long happeningSoon = all.stream().filter(e -> "Happening Soon".equalsIgnoreCase(e.getStatus().getDisplayName()) || "Happening Today".equalsIgnoreCase(e.getStatus().getDisplayName())).count();
        long upcoming = all.stream().filter(e -> "Upcoming".equalsIgnoreCase(e.getStatus().getDisplayName())).count();
        long completed = all.stream().filter(e -> "Completed".equalsIgnoreCase(e.getStatus().getDisplayName())).count();

        long myCollegeEvents = 0;
        if (user != null && user.getRole() == Role.COORDINATOR) {
            myCollegeEvents = all.stream().filter(e -> user.getCollege().equalsIgnoreCase(e.getCollege())).count();
        }

        Map<String, Object> stats = Map.of(
                "total", total,
                "happeningSoon", happeningSoon,
                "upcoming", upcoming,
                "completed", completed,
                "myCollegeEvents", myCollegeEvents
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
