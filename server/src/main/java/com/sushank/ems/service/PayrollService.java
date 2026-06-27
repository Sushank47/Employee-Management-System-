package com.sushank.ems.service;
import com.sushank.ems.audit.AuditLogService;
import com.sushank.ems.audit.AuditLog;

import com.sushank.ems.entity.Employee;
import com.sushank.ems.entity.LeaveRequest;
import com.sushank.ems.entity.Payroll;
import com.sushank.ems.exception.BadRequestException;
import com.sushank.ems.exception.ResourceNotFoundException;
import com.sushank.ems.repository.EmployeeRepository;
import com.sushank.ems.repository.LeaveRequestRepository;
import com.sushank.ems.repository.PayrollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${app.payroll.taxRate:0.10}")
    private double taxRate;

    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MM-yyyy");

    public Payroll calculatePayroll(String employeeId, String monthYear, Double bonus) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        // Check if payroll already processed
        Optional<Payroll> existing = payrollRepository.findByEmployeeIdAndMonthYear(employeeId, monthYear);
        if (existing.isPresent()) {
            throw new BadRequestException("Payroll already processed for employee " + employeeId + " in month " + monthYear);
        }

        double basicSalary = employee.getSalary() != null ? employee.getSalary() : 0.0;
        double inputBonus = bonus != null ? bonus : 0.0;

        // Parse month range
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(monthYear, MONTH_YEAR_FORMATTER);
        } catch (Exception e) {
            throw new BadRequestException("Invalid monthYear format. Must be MM-yyyy (e.g. 06-2026)");
        }
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        // 1. Calculate leave days overlapping this month
        List<LeaveRequest> leaves = leaveRequestRepository.findApprovedLeavesForMonth(employeeId, monthStart, monthEnd);
        long leaveDays = 0;
        for (LeaveRequest leave : leaves) {
            LocalDate start = leave.getStartDate().isAfter(monthStart) ? leave.getStartDate() : monthStart;
            LocalDate end = leave.getEndDate().isBefore(monthEnd) ? leave.getEndDate() : monthEnd;
            if (!start.isAfter(end)) {
                leaveDays += ChronoUnit.DAYS.between(start, end) + 1;
            }
        }

        // 2. Compute components
        double leaveDeduction = (basicSalary / 30.0) * leaveDays;
        double tax = basicSalary * taxRate;
        double netSalary = Math.max(0.0, basicSalary + inputBonus - tax - leaveDeduction);

        Payroll payroll = Payroll.builder()
                .employeeId(employeeId)
                .monthYear(monthYear)
                .basicSalary(basicSalary)
                .bonus(inputBonus)
                .tax(tax)
                .leaveDeduction(leaveDeduction)
                .netSalary(netSalary)
                .status("PENDING")
                .processedDate(LocalDate.now())
                .build();

        Payroll saved = payrollRepository.save(payroll);
        auditLogService.log("PAYROLL_CALCULATE", "SYSTEM", 
                "Calculated payroll for " + employeeId + " for " + monthYear + " Net: " + netSalary);

        return saved;
    }

    public Payroll paySalary(String payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with ID: " + payrollId));

        if ("PAID".equals(payroll.getStatus())) {
            throw new BadRequestException("Payroll already marked as PAID!");
        }

        payroll.setStatus("PAID");
        payroll.setProcessedDate(LocalDate.now());
        Payroll saved = payrollRepository.save(payroll);

        auditLogService.log("PAYROLL_PAY", "SYSTEM", "Paid salary record " + payrollId + " to employee " + payroll.getEmployeeId());
        return saved;
    }

    public List<Payroll> getPayrollHistory(String employeeId) {
        if (!employeeRepository.existsByEmployeeId(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        return payrollRepository.findByEmployeeId(employeeId);
    }
}
