package com.intercollege.service;

import com.intercollege.model.Role;
import com.intercollege.model.User;
import com.intercollege.repository.UserRepository;
import com.intercollege.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service handling user registration, authentication, and profile management.
 *
 * NOTE FOR FIRST-YEAR COMPUTER SCIENCE STUDENTS:
 * The Service layer sits between Controllers (which handle HTTP requests)
 * and Repositories (which handle data storage). It contains business logic,
 * such as validating user inputs, encrypting passwords, and checking business rules.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new student user.
     */
    public User registerStudent(String fullName, String username, String password, String confirmPassword,
                                String college, String location, List<String> interests) {
        validateCommonRegistration(fullName, username, password, confirmPassword, college);

        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required for students.");
        }

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);

        User user = new User();
        user.setFullName(fullName.trim());
        user.setUsername(username.trim().toLowerCase());
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);
        user.setCollege(college.trim());
        user.setRole(Role.STUDENT);
        user.setLocation(location.trim());
        user.setInterests(interests);

        return userRepository.save(user);
    }

    /**
     * Registers a new coordinator user.
     */
    public User registerCoordinator(String fullName, String username, String password, String confirmPassword,
                                    String college) {
        validateCommonRegistration(fullName, username, password, confirmPassword, college);

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);

        User user = new User();
        user.setFullName(fullName.trim());
        user.setUsername(username.trim().toLowerCase());
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);
        user.setCollege(college.trim());
        user.setRole(Role.COORDINATOR);

        return userRepository.save(user);
    }

    private void validateCommonRegistration(String fullName, String username, String password,
                                            String confirmPassword, String college) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (college == null || college.trim().isEmpty()) {
            throw new IllegalArgumentException("College name is required.");
        }
        if (userRepository.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("Username already exists. Please choose a different username.");
        }
    }

    /**
     * Authenticates a user by checking their username and verifying their hashed password.
     */
    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        Optional<User> optionalUser = userRepository.findByUsername(username.trim());
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        User user = optionalUser.get();
        boolean valid = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!valid) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        return user;
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Updates profile details for a student.
     */
    public User updateStudentProfile(String userId, String fullName, String college, String location, List<String> interests) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        if (college != null && !college.trim().isEmpty()) {
            user.setCollege(college.trim());
        }
        if (location != null && !location.trim().isEmpty()) {
            user.setLocation(location.trim());
        }
        if (interests != null) {
            user.setInterests(interests);
        }

        return userRepository.save(user);
    }
}
