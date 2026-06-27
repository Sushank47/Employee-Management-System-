package com.sushank.ems.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collection = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {
    @Id
    private String id;

    private String employeeId; // references Employee.employeeId
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    
    private String approvedBy; // references Employee.employeeId (the reviewer)

    @CreatedDate
    private Instant createdAt;
}
