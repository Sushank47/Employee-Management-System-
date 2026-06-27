package com.sushank.ems.controller;

import com.sushank.ems.entity.Payroll;
import com.sushank.ems.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @PostMapping("/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Payroll> calculatePayroll(
            @RequestParam String employeeId,
            @RequestParam String monthYear,
            @RequestParam(required = false) Double bonus) {
        Payroll payroll = payrollService.calculatePayroll(employeeId, monthYear, bonus);
        return ResponseEntity.ok(payroll);
    }

    @PostMapping("/{payrollId}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Payroll> paySalary(@PathVariable String payrollId) {
        Payroll payroll = payrollService.paySalary(payrollId);
        return ResponseEntity.ok(payroll);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or (#employeeId == authentication.principal.employeeId)")
    public ResponseEntity<List<Payroll>> getPayrollHistory(@PathVariable String employeeId) {
        List<Payroll> history = payrollService.getPayrollHistory(employeeId);
        return ResponseEntity.ok(history);
    }
}
