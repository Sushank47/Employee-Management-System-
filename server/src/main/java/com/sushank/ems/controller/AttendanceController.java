package com.sushank.ems.controller;

import com.sushank.ems.dto.AttendanceRequest;
import com.sushank.ems.entity.Attendance;
import com.sushank.ems.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sushank.ems.security.UserDetailsImpl;

import java.time.LocalTime;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/checkin")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Attendance> checkIn(@Valid @RequestBody AttendanceRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isStaff = userDetails.getAuthorities().stream()
                .anyMatch(a -> java.util.Arrays.asList("ROLE_ADMIN", "ROLE_HR", "ROLE_MANAGER").contains(a.getAuthority()));
        if (!isStaff && !request.getEmployeeId().equals(userDetails.getEmployeeId())) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to check-in/out for another employee!");
        }
        LocalTime time = request.getTime() != null ? request.getTime() : LocalTime.now();
        Attendance record = attendanceService.checkIn(request.getEmployeeId(), time);
        return ResponseEntity.ok(record);
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<Attendance> checkOut(@Valid @RequestBody AttendanceRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isStaff = userDetails.getAuthorities().stream()
                .anyMatch(a -> java.util.Arrays.asList("ROLE_ADMIN", "ROLE_HR", "ROLE_MANAGER").contains(a.getAuthority()));
        if (!isStaff && !request.getEmployeeId().equals(userDetails.getEmployeeId())) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to check-in/out for another employee!");
        }
        LocalTime time = request.getTime() != null ? request.getTime() : LocalTime.now();
        Attendance record = attendanceService.checkOut(request.getEmployeeId(), time);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or (#employeeId == authentication.principal.employeeId)")
    public ResponseEntity<Map<String, Object>> getReport(
            @RequestParam String employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Object> report = attendanceService.getMonthlyAttendanceReport(employeeId, year, month);
        return ResponseEntity.ok(report);
    }
}
