package com.intercollege.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for hashing and verifying passwords using SHA-256 with cryptographic salt.
 *
 * NOTE FOR FIRST-YEAR COMPUTER SCIENCE STUDENTS:
 * 1. Why don't we store plain-text passwords?
 *    If an attacker or unauthorized person accesses the data file, plain-text passwords
 *    would immediately compromise all user accounts.
 *
 * 2. What is a cryptographic Hash?
 *    A one-way mathematical function that converts arbitrary text into a fixed-length string.
 *    You can easily compute hash(password), but it is computationally infeasible to reverse it.
 *
 * 3. What is a "Salt"?
 *    A random string added to the password before hashing. Even if two users choose the
 *    same password (e.g., "password123"), their salts are different, resulting in completely
 *    different hashes. This defeats pre-computed dictionary attacks (Rainbow Tables).
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a cryptographically strong random salt encoded in Base64.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Computes the SHA-256 hash of the password concatenated with the given salt.
     */
    public static String hashPassword(String plainPassword, String salt) {
        if (plainPassword == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Combine salt + password
            String input = salt + plainPassword;
            byte[] hashedBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies whether the provided plain password matches the stored hash when hashed with salt.
     */
    public static boolean verifyPassword(String plainPassword, String salt, String expectedHash) {
        if (plainPassword == null || salt == null || expectedHash == null) {
            return false;
        }
        String calculatedHash = hashPassword(plainPassword, salt);
        return calculatedHash.equals(expectedHash);
    }
}
