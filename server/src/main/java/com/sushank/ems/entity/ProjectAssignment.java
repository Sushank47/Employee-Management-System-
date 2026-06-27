package com.sushank.ems.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "project_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAssignment {
    @Id
    private String id;

    private String projectId;  // references Project.projectId
    private String employeeId; // references Employee.employeeId
    private LocalDate assignedDate;
}
