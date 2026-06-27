package com.sushank.ems.service;
import com.sushank.ems.audit.AuditLogService;
import com.sushank.ems.audit.AuditLog;

import com.sushank.ems.entity.Department;
import com.sushank.ems.entity.Employee;
import com.sushank.ems.exception.BadRequestException;
import com.sushank.ems.exception.ResourceNotFoundException;
import com.sushank.ems.repository.DepartmentRepository;
import com.sushank.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    public Department createDepartment(Department department) {
        if (departmentRepository.existsByDepartmentId(department.getDepartmentId())) {
            throw new BadRequestException("Department with ID " + department.getDepartmentId() + " already exists!");
        }
        if (departmentRepository.existsByDepartmentName(department.getDepartmentName())) {
            throw new BadRequestException("Department with name " + department.getDepartmentName() + " already exists!");
        }
        Department saved = departmentRepository.save(department);
        auditLogService.log("CREATE_DEPARTMENT", "Created department " + department.getDepartmentId());
        return saved;
    }

    public Department updateDepartment(String departmentId, Department details) {
        Department department = departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        department.setDepartmentName(details.getDepartmentName());
        department.setDescription(details.getDescription());
        department.setManagerId(details.getManagerId());

        Department saved = departmentRepository.save(department);
        auditLogService.log("UPDATE_DEPARTMENT", "Updated department " + departmentId);
        return saved;
    }

    public void deleteDepartment(String departmentId) {
        Department department = departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        departmentRepository.delete(department);
        auditLogService.log("DELETE_DEPARTMENT", "Deleted department " + departmentId);
    }

    public Department getDepartmentByDepartmentId(String departmentId) {
        return departmentRepository.findByDepartmentId(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Map<String, Object> getDepartmentStats(String departmentId) {
        Department department = getDepartmentByDepartmentId(departmentId);
        List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);

        long count = employees.size();
        double sumSalary = employees.stream()
                .mapToDouble(e -> e.getSalary() != null ? e.getSalary() : 0.0)
                .sum();
        double avgSalary = count > 0 ? sumSalary / count : 0.0;

        String managerName = "None";
        if (department.getManagerId() != null && !department.getManagerId().isBlank()) {
            managerName = employeeRepository.findByEmployeeId(department.getManagerId())
                    .map(e -> e.getFirstName() + " " + e.getLastName())
                    .orElse("Unknown Manager (" + department.getManagerId() + ")");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("departmentId", department.getDepartmentId());
        stats.put("departmentName", department.getDepartmentName());
        stats.put("description", department.getDescription());
        stats.put("managerId", department.getManagerId());
        stats.put("managerName", managerName);
        stats.put("employeeCount", count);
        stats.put("averageSalary", avgSalary);

        return stats;
    }
}
