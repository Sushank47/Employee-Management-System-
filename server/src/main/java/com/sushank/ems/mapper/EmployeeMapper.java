package com.sushank.ems.mapper;

import com.sushank.ems.dto.EmployeeCreateRequest;
import com.sushank.ems.entity.Employee;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeCreateRequest request) {
        if (request == null) {
            return null;
        }

        return Employee.builder()
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .departmentId(request.getDepartmentId())
                .managerId(request.getManagerId())
                .designation(request.getDesignation())
                .salary(request.getSalary())
                .skills(request.getSkills() != null ? request.getSkills() : new ArrayList<>())
                .status("ACTIVE")
                .build();
    }
}
