package com.sushank.ems.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    @Id
    private String id;

    @Indexed(unique = true)
    private String departmentId; // e.g. DEP001

    @Indexed(unique = true)
    private String departmentName;

    private String description;
    private String managerId; // employeeId of the manager
}
