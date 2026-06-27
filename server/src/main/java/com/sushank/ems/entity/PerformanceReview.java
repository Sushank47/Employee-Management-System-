package com.sushank.ems.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "performance_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceReview {
    @Id
    private String id;

    private String employeeId; // references Employee.employeeId
    private Double rating;
    private String feedback;
    private String reviewedBy; // employeeId of the reviewing manager
    private String reviewPeriod; // e.g. "Q1 2026", "2026"
    private LocalDate reviewDate;
}
