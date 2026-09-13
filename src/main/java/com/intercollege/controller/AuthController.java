package com.intercollege.controller;

import com.intercollege.dto.ApiResponse;
import com.intercollege.dto.AuthRequests.*;
import com.intercollege.model.User;
import com.intercollege.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller handling student and coordinator registration, login, logout,
 * and current session status.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_USER_KEY = "LOGGED_IN_USER";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<User>> registerStudent(@RequestBody RegisterStudentRequest req, HttpSession session) {
        User user = userService.registerStudent(
                req.getFullName(),
                req.getUsername(),
                req.getPassword(),
                req.getConfirmPassword(),
                req.getCollege(),
                req.getLocation(),
                req.getInterests()
        );
        // Automatically sign in the registered student into the current session
        session.setAttribute(SESSION_USER_KEY, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Student registration successful!", user.toSafeUser()));
    }

    @PostMapping("/register/coordinator")
    public ResponseEntity<ApiResponse<User>> registerCoordinator(@RequestBody RegisterCoordinatorRequest req, HttpSession session) {
        User user = userService.registerCoordinator(
                req.getFullName(),
                req.getUsername(),
                req.getPassword(),
                req.getConfirmPassword(),
                req.getCollege()
        );
        // Automatically sign in the registered coordinator
        session.setAttribute(SESSION_USER_KEY, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Coordinator registration successful!", user.toSafeUser()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody LoginRequest req, HttpSession session) {
        User user = userService.authenticate(req.getUsername(), req.getPassword());
        session.setAttribute(SESSION_USER_KEY, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Login successful!", user.toSafeUser()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.removeAttribute(SESSION_USER_KEY);
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(HttpSession session) {
        String userId = (String) session.getAttribute(SESSION_USER_KEY);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }

        return userService.findById(userId)
                .map(u -> ResponseEntity.ok(ApiResponse.success(u.toSafeUser())))
                .orElseGet(() -> {
                    session.removeAttribute(SESSION_USER_KEY);
                    return ResponseEntity.ok(ApiResponse.success(null));
                });
    }
}
