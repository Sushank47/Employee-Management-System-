package com.sushank.ems.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOverviewResponse {
    private long totalEmployees;
    private long activeEmployees;
    private long employeesOnLeave;
    private long departments;
    private long projects;

    private Map<String, Long> departmentWiseEmployees;
    private Map<String, Long> salaryDistribution; // e.g. "0-50000", "50000-100000", "100000+"
    private Map<String, Long> attendanceTrends; // e.g. "PRESENT", "LATE", "ABSENT"
    private Map<String, Long> leaveStatistics;  // e.g. "PENDING", "APPROVED", "REJECTED"
    private Map<String, Double> performanceStatistics; // e.g. departmentName to avgRating
    private double attritionRate;
}
