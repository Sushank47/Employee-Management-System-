package com.sushank.ems.scheduler;

import com.sushank.ems.entity.Attendance;
import com.sushank.ems.entity.Employee;
import com.sushank.ems.repository.AttendanceRepository;
import com.sushank.ems.repository.EmployeeRepository;
import com.sushank.ems.service.PayrollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class HrmsScheduler {
    private static final Logger logger = LoggerFactory.getLogger(HrmsScheduler.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private PayrollService payrollService;

    // Runs daily at 11:59 PM to mark employees who didn't check-in as ABSENT
    @Scheduled(cron = "0 59 23 * * *")
    public void runDailyAttendanceSummary() {
        logger.info("Starting Daily Attendance Summary Scheduler...");
        LocalDate today = LocalDate.now();
        List<Employee> activeEmployees = employeeRepository.findByStatus("ACTIVE");

        for (Employee emp : activeEmployees) {
            Optional<Attendance> recordOpt = attendanceRepository.findByEmployeeIdAndDate(emp.getEmployeeId(), today);
            if (recordOpt.isEmpty()) {
                Attendance absentRecord = Attendance.builder()
                        .employeeId(emp.getEmployeeId())
                        .date(today)
                        .status("ABSENT")
                        .build();
                attendanceRepository.save(absentRecord);
                logger.info("Marked employee {} as ABSENT for today", emp.getEmployeeId());
            }
        }
        logger.info("Daily Attendance Summary Scheduler completed.");
    }

    // Runs on the 1st of every month at 12:00 AM to auto-generate payroll slips for the previous month
    @Scheduled(cron = "0 0 0 1 * *")
    public void runMonthlyPayrollGeneration() {
        logger.info("Starting Monthly Payroll Generation Scheduler...");
        
        // Find previous month (e.g. if today is July 1st, previous month is June "06-2026")
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        String monthYearStr = previousMonth.format(DateTimeFormatter.ofPattern("MM-yyyy"));

        List<Employee> activeEmployees = employeeRepository.findByStatus("ACTIVE");
        for (Employee emp : activeEmployees) {
            try {
                // Generate payroll with 0 bonus by default
                payrollService.calculatePayroll(emp.getEmployeeId(), monthYearStr, 0.0);
                logger.info("Automatically generated payroll for employee {} for month {}", emp.getEmployeeId(), monthYearStr);
            } catch (Exception e) {
                logger.error("Failed to auto-generate payroll for employee {}: {}", emp.getEmployeeId(), e.getMessage());
            }
        }
        logger.info("Monthly Payroll Generation Scheduler completed.");
    }
}
