package com.sushank.ems.service;
import com.sushank.ems.audit.AuditLogService;
import com.sushank.ems.audit.AuditLog;

import com.sushank.ems.entity.Attendance;
import com.sushank.ems.exception.BadRequestException;
import com.sushank.ems.exception.ResourceNotFoundException;
import com.sushank.ems.repository.AttendanceRepository;
import com.sushank.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    private static final LocalTime LATE_THRESHOLD = LocalTime.of(9, 30);

    public Attendance checkIn(String employeeId, LocalTime checkInTime) {
        if (!employeeRepository.existsByEmployeeId(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);
        if (existing.isPresent()) {
            throw new BadRequestException("Employee already checked in for today: " + today);
        }

        LocalTime time = checkInTime != null ? checkInTime : LocalTime.now();
        String status = time.isAfter(LATE_THRESHOLD) ? "LATE" : "PRESENT";

        Attendance attendance = Attendance.builder()
                .employeeId(employeeId)
                .date(today)
                .checkIn(time)
                .status(status)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        auditLogService.log("CHECK_IN", employeeId, "Checked in at " + time + " on " + today + " Status: " + status);
        notificationService.sendNotification(employeeId, "Check-In registered at " + time + " (" + status + ")");
        return saved;
    }

    public Attendance checkOut(String employeeId, LocalTime checkOutTime) {
        if (!employeeRepository.existsByEmployeeId(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new BadRequestException("Cannot check-out without checking-in first for today: " + today));

        if (attendance.getCheckOut() != null) {
            throw new BadRequestException("Employee already checked out for today!");
        }

        LocalTime time = checkOutTime != null ? checkOutTime : LocalTime.now();
        attendance.setCheckOut(time);

        Attendance saved = attendanceRepository.save(attendance);
        auditLogService.log("CHECK_OUT", employeeId, "Checked out at " + time + " on " + today);
        notificationService.sendNotification(employeeId, "Check-Out registered at " + time);
        return saved;
    }

    public List<Attendance> getMonthlyAttendance(String employeeId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end);
    }

    public Map<String, Object> getMonthlyAttendanceReport(String employeeId, int year, int month) {
        List<Attendance> list = getMonthlyAttendance(employeeId, year, month);
        
        long present = 0;
        long late = 0;
        long halfDay = 0;
        long absent = 0;

        for (Attendance record : list) {
            switch (record.getStatus()) {
                case "PRESENT":
                    present++;
                    break;
                case "LATE":
                    late++;
                    break;
                case "HALFDAY":
                    halfDay++;
                    break;
                case "ABSENT":
                    absent++;
                    break;
            }
        }

        Map<String, Object> report = new HashMap<>();
        report.put("employeeId", employeeId);
        report.put("year", year);
        report.put("month", month);
        report.put("totalRecordedDays", list.size());
        report.put("presentCount", present);
        report.put("lateArrivalsCount", late);
        report.put("halfDayCount", halfDay);
        report.put("absentCount", absent);
        report.put("details", list);

        return report;
    }
}
