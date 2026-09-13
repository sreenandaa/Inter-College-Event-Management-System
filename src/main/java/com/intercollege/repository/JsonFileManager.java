package com.intercollege.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Core JSON File Manager that handles all file-based persistence for the application.
 *
 * NOTE FOR FIRST-YEAR COMPUTER SCIENCE STUDENTS:
 * Real-world systems usually use relational databases (like PostgreSQL or MySQL).
 * However, learning how file handling works in Java is a fundamental computer science concept!
 *
 * This class uses:
 * 1. Java NIO (Paths, Files) to check, create, and manage directories and files.
 * 2. Jackson ObjectMapper to serialize (Java Objects -> JSON text) and
 *    deserialize (JSON text -> Java Objects).
 * 3. ReentrantReadWriteLock to ensure thread-safety: multiple threads can read safely,
 *    but writes are exclusive to prevent corrupting the JSON files.
 * 4. Atomic file writing (writing to a temporary file, then replacing) to prevent data loss
 *    if a process is interrupted while writing.
 */
@Component
public class JsonFileManager {

    @Value("${intercollege.data.dir:data}")
    private String dataDirName;

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    public JsonFileManager() {
        this.objectMapper = new ObjectMapper();
        // Register JavaTimeModule to handle LocalDate and LocalTime
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Pretty print JSON so students can easily open and inspect the files in a text editor!
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void init() {
        try {
            Path dirPath = Paths.get(dataDirName);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                System.out.println("Created application data directory at: " + dirPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
    }

    private ReentrantReadWriteLock getLock(String fileName) {
        return locks.computeIfAbsent(fileName, k -> new ReentrantReadWriteLock());
    }

    public Path getFilePath(String fileName) {
        return Paths.get(dataDirName, fileName);
    }

    /**
     * Reads a list of items of type T from the specified JSON file.
     * If the file does not exist, returns an empty list.
     */
    public <T> List<T> readList(String fileName, TypeReference<List<T>> typeReference) {
        ReentrantReadWriteLock lock = getLock(fileName);
        lock.readLock().lock();
        try {
            File file = getFilePath(fileName).toFile();
            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, typeReference);
        } catch (IOException e) {
            System.err.println("Error reading JSON file " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Atomically writes a list of items of type T to the specified JSON file.
     */
    public <T> void writeList(String fileName, List<T> list) {
        ReentrantReadWriteLock lock = getLock(fileName);
        lock.writeLock().lock();
        try {
            Path targetPath = getFilePath(fileName);
            Path parentDir = targetPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // Atomic file write: write to temp file first, then atomically move
            Path tempPath = Paths.get(dataDirName, fileName + ".tmp");
            objectMapper.writeValue(tempPath.toFile(), list);
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Error writing JSON file " + fileName + ": " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
