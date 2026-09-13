package com.intercollege.controller;

import com.intercollege.dto.ApiResponse;
import com.intercollege.dto.AuthRequests.UpdateProfileRequest;
import com.intercollege.model.User;
import com.intercollege.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for student and coordinator profile retrieval and updates.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(HttpSession session) {
        String userId = (String) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Please log in to view your profile."));
        }

        return userService.findById(userId)
                .map(u -> ResponseEntity.ok(ApiResponse.success(u.toSafeUser())))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("User not found.")));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @RequestBody UpdateProfileRequest req,
            HttpSession session) {

        String userId = (String) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Please log in to update your profile."));
        }

        User updated = userService.updateStudentProfile(
                userId, req.getFullName(), req.getCollege(), req.getLocation(), req.getInterests());

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully!", updated.toSafeUser()));
    }
}
