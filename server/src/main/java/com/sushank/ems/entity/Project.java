package com.sushank.ems.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    private String id;

    @Indexed(unique = true)
    private String projectId; // e.g. PRJ001

    @Indexed(unique = true)
    private String projectName;

    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, INACTIVE

    private List<String> requiredSkills; // required for Smart Resource Allocation
}
