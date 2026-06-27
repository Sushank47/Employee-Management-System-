package com.sushank.ems.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttendanceRequest {
    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    private LocalDate date;
    private LocalTime time; // exact timestamp of punch-in/out
}
