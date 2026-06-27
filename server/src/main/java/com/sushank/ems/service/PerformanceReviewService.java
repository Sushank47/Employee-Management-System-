package com.sushank.ems.service;
import com.sushank.ems.audit.AuditLogService;
import com.sushank.ems.audit.AuditLog;

import com.sushank.ems.dto.PerformanceReviewRequest;
import com.sushank.ems.entity.Employee;
import com.sushank.ems.entity.PerformanceReview;
import com.sushank.ems.exception.ResourceNotFoundException;
import com.sushank.ems.repository.EmployeeRepository;
import com.sushank.ems.repository.PerformanceReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PerformanceReviewService {

    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditLogService auditLogService;

    public PerformanceReview createReview(PerformanceReviewRequest request) {
        Employee employee = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        String managerUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        PerformanceReview review = PerformanceReview.builder()
                .employeeId(request.getEmployeeId())
                .rating(request.getRating())
                .feedback(request.getFeedback())
                .reviewedBy(managerUsername)
                .reviewPeriod(request.getReviewPeriod())
                .reviewDate(LocalDate.now())
                .build();

        PerformanceReview saved = performanceReviewRepository.save(review);

        // Notify employee
        notificationService.sendNotification(request.getEmployeeId(), 
                "A new performance review for period " + request.getReviewPeriod() + " was added with rating " + request.getRating());
        auditLogService.log("PERFORMANCE_REVIEW_ADD", managerUsername, 
                "Added performance review for employee " + request.getEmployeeId() + " Rating: " + request.getRating());

        return saved;
    }

    public List<PerformanceReview> getReviewsByEmployee(String employeeId) {
        if (!employeeRepository.existsByEmployeeId(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        return performanceReviewRepository.findByEmployeeId(employeeId);
    }

    public Map<String, Object> getRatingAnalytics(String employeeId) {
        List<PerformanceReview> reviews = getReviewsByEmployee(employeeId);

        double avgRating = reviews.stream()
                .mapToDouble(PerformanceReview::getRating)
                .average()
                .orElse(0.0);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("employeeId", employeeId);
        analytics.put("totalReviewsCount", reviews.size());
        analytics.put("averageRating", avgRating);
        analytics.put("history", reviews);

        return analytics;
    }
}
