package com.sushank.ems.controller;

import com.sushank.ems.dto.MessageResponse;
import com.sushank.ems.entity.Department;
import com.sushank.ems.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        Department created = departmentService.createDepartment(department);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> updateDepartment(@PathVariable String departmentId, @Valid @RequestBody Department details) {
        Department updated = departmentService.updateDepartment(departmentId, details);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteDepartment(@PathVariable String departmentId) {
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.ok(new MessageResponse("Department deleted successfully"));
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Department> getDepartmentByDepartmentId(@PathVariable String departmentId) {
        Department department = departmentService.getDepartmentByDepartmentId(departmentId);
        return ResponseEntity.ok(department);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> list = departmentService.getAllDepartments();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{departmentId}/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getDepartmentStats(@PathVariable String departmentId) {
        Map<String, Object> stats = departmentService.getDepartmentStats(departmentId);
        return ResponseEntity.ok(stats);
    }
}
