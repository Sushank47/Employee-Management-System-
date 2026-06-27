package com.sushank.ems.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id
    private String id;

    private String employeeId; // references Employee.employeeId
    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;
    
    @Builder.Default
    private String status = "PRESENT"; // PRESENT, LATE, ABSENT, HALFDAY
}
