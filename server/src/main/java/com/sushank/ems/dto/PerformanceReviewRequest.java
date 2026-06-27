package com.sushank.ems.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PerformanceReviewRequest {
    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Double rating;

    @NotBlank(message = "Feedback is required")
    private String feedback;

    @NotBlank(message = "Review period is required")
    private String reviewPeriod;
}
