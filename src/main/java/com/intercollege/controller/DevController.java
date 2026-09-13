package com.intercollege.controller;

import com.intercollege.dto.ApiResponse;
import com.intercollege.service.DataInitializationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Development utility controller to reset sample data back to default state.
 */
@RestController
@RequestMapping("/api/dev")
public class DevController {

    private final DataInitializationService dataInitializationService;

    public DevController(DataInitializationService dataInitializationService) {
        this.dataInitializationService = dataInitializationService;
    }

    @PostMapping("/reset-data")
    public ResponseEntity<ApiResponse<Void>> resetData() {
        dataInitializationService.resetAllData();
        return ResponseEntity.ok(ApiResponse.success("All sample users, events, and notifications have been reset.", null));
    }
}
