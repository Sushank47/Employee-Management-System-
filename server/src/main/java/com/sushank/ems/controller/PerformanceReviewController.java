package com.sushank.ems.controller;

import com.sushank.ems.dto.PerformanceReviewRequest;
import com.sushank.ems.entity.PerformanceReview;
import com.sushank.ems.service.PerformanceReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/performance-reviews")
public class PerformanceReviewController {

    @Autowired
    private PerformanceReviewService performanceReviewService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    public ResponseEntity<PerformanceReview> createReview(@Valid @RequestBody PerformanceReviewRequest request) {
        PerformanceReview review = performanceReviewService.createReview(request);
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or (#employeeId == authentication.principal.employeeId)")
    public ResponseEntity<List<PerformanceReview>> getReviewsByEmployee(@PathVariable String employeeId) {
        List<PerformanceReview> reviews = performanceReviewService.getReviewsByEmployee(employeeId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/employee/{employeeId}/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or (#employeeId == authentication.principal.employeeId)")
    public ResponseEntity<Map<String, Object>> getRatingAnalytics(@PathVariable String employeeId) {
        Map<String, Object> analytics = performanceReviewService.getRatingAnalytics(employeeId);
        return ResponseEntity.ok(analytics);
    }
}
