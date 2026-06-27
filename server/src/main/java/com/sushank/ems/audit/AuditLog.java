package com.sushank.ems.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    private String id;

    private String action; // e.g. "DELETE_EMPLOYEE", "LOGIN", "UPDATE_EMPLOYEE"
    private String performedBy; // Username / email / role of the user performing it
    private String details;

    @CreatedDate
    private Instant timestamp;
}
