package com.sushank.ems.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "payroll")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {
    @Id
    private String id;

    private String employeeId; // references Employee.employeeId
    private String monthYear;  // e.g. "06-2026"
    private Double basicSalary;
    private Double bonus;
    private Double tax;
    private Double leaveDeduction;
    private Double netSalary;
    private String status;     // PAID, PENDING
    private LocalDate processedDate;
}
