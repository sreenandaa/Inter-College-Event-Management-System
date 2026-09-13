package com.intercollege.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intercollege.model.Event;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * File-backed repository for Event records stored in data/events.json.
 */
@Repository
public class EventRepository {

    private static final String FILE_NAME = "events.json";
    private final JsonFileManager jsonFileManager;
    private final List<Event> eventCache = new CopyOnWriteArrayList<>();

    public EventRepository(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }

    @PostConstruct
    public synchronized void loadFromDisk() {
        List<Event> loaded = jsonFileManager.readList(FILE_NAME, new TypeReference<List<Event>>() {});
        eventCache.clear();
        if (loaded != null) {
            eventCache.addAll(loaded);
        }
    }

    private synchronized void persistToDisk() {
        jsonFileManager.writeList(FILE_NAME, new ArrayList<>(eventCache));
    }

    public List<Event> findAll() {
        return new ArrayList<>(eventCache);
    }

    public Optional<Event> findById(String id) {
        if (id == null) return Optional.empty();
        return eventCache.stream()
                .filter(e -> id.equals(e.getId()))
                .findFirst();
    }

    public List<Event> findByCollege(String college) {
        if (college == null) return new ArrayList<>();
        return eventCache.stream()
                .filter(e -> college.equalsIgnoreCase(e.getCollege()))
                .collect(Collectors.toList());
    }

    public synchronized Event save(Event event) {
        if (event.getId() == null || event.getId().trim().isEmpty()) {
            event.setId(UUID.randomUUID().toString());
            event.setCreatedAt(LocalDateTime.now());
            event.setUpdatedAt(LocalDateTime.now());
            eventCache.add(event);
        } else {
            event.setUpdatedAt(LocalDateTime.now());
            int existingIndex = -1;
            for (int i = 0; i < eventCache.size(); i++) {
                if (eventCache.get(i).getId().equals(event.getId())) {
                    existingIndex = i;
                    break;
                }
            }
            if (existingIndex >= 0) {
                // Preserve original createdAt
                if (event.getCreatedAt() == null) {
                    event.setCreatedAt(eventCache.get(existingIndex).getCreatedAt());
                }
                eventCache.set(existingIndex, event);
            } else {
                eventCache.add(event);
            }
        }
        persistToDisk();
        return event;
    }

    public synchronized boolean deleteById(String id) {
        boolean removed = eventCache.removeIf(e -> id.equals(e.getId()));
        if (removed) {
            persistToDisk();
        }
        return removed;
    }

    public synchronized void saveAll(List<Event> events) {
        eventCache.clear();
        eventCache.addAll(events);
        persistToDisk();
    }
}
