package com.sushank.ems.controller;

import com.sushank.ems.dto.EmployeeCreateRequest;
import com.sushank.ems.dto.MessageResponse;
import com.sushank.ems.entity.Employee;
import com.sushank.ems.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        Employee employee = employeeService.createEmployee(request);
        return new ResponseEntity<>(employee, HttpStatus.CREATED);
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable String employeeId, @RequestBody Employee updateDetails) {
        Employee employee = employeeService.updateEmployee(employeeId, updateDetails);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteEmployee(@PathVariable String employeeId) {
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok(new MessageResponse("Employee deleted successfully"));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Employee> getEmployeeByEmployeeId(@PathVariable String employeeId) {
        Employee employee = employeeService.getEmployeeByEmployeeId(employeeId);
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeId") String sort) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<Employee> employees = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    public ResponseEntity<Page<Employee>> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String designation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeId") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<Employee> results = employeeService.searchEmployees(name, email, departmentId, skill, designation, pageable);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{employeeId}/skills")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Employee> addSkill(@PathVariable String employeeId, @RequestParam String skill) {
        Employee employee = employeeService.addSkill(employeeId, skill);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{employeeId}/skills")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Employee> removeSkill(@PathVariable String employeeId, @RequestParam String skill) {
        Employee employee = employeeService.removeSkill(employeeId, skill);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/skill/{skill}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    public ResponseEntity<List<Employee>> getEmployeesBySkill(@PathVariable String skill) {
        List<Employee> list = employeeService.searchBySkill(skill);
        return ResponseEntity.ok(list);
    }
}
