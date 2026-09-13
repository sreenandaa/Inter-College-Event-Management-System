package com.intercollege.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intercollege.model.User;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * File-backed repository for User records stored in data/users.json.
 *
 * NOTE FOR FIRST-YEAR COMPUTER SCIENCE STUDENTS:
 * In a traditional database system, we would execute SQL queries such as:
 *   SELECT * FROM users WHERE username = '...'
 * Here, we demonstrate File I/O by reading and writing records to a JSON file.
 * We also keep an in-memory collection so queries are fast and responsive.
 */
@Repository
public class UserRepository {

    private static final String FILE_NAME = "users.json";
    private final JsonFileManager jsonFileManager;
    private final List<User> userCache = new CopyOnWriteArrayList<>();

    public UserRepository(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }

    @PostConstruct
    public synchronized void loadFromDisk() {
        List<User> loaded = jsonFileManager.readList(FILE_NAME, new TypeReference<List<User>>() {});
        userCache.clear();
        if (loaded != null) {
            userCache.addAll(loaded);
        }
    }

    private synchronized void persistToDisk() {
        jsonFileManager.writeList(FILE_NAME, new ArrayList<>(userCache));
    }

    public List<User> findAll() {
        return new ArrayList<>(userCache);
    }

    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return userCache.stream()
                .filter(u -> id.equals(u.getId()))
                .findFirst();
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return userCache.stream()
                .filter(u -> username.equalsIgnoreCase(u.getUsername()))
                .findFirst();
    }

    public boolean existsByUsername(String username) {
        if (username == null) return false;
        return userCache.stream()
                .anyMatch(u -> username.equalsIgnoreCase(u.getUsername()));
    }

    public synchronized User save(User user) {
        if (user.getId() == null || user.getId().trim().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
            userCache.add(user);
        } else {
            int existingIndex = -1;
            for (int i = 0; i < userCache.size(); i++) {
                if (userCache.get(i).getId().equals(user.getId())) {
                    existingIndex = i;
                    break;
                }
            }
            if (existingIndex >= 0) {
                userCache.set(existingIndex, user);
            } else {
                userCache.add(user);
            }
        }
        persistToDisk();
        return user;
    }

    public synchronized boolean deleteById(String id) {
        boolean removed = userCache.removeIf(u -> id.equals(u.getId()));
        if (removed) {
            persistToDisk();
        }
        return removed;
    }

    public synchronized void saveAll(List<User> users) {
        userCache.clear();
        userCache.addAll(users);
        persistToDisk();
    }
}
