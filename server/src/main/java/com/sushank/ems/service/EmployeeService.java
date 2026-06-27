package com.sushank.ems.service;
import com.sushank.ems.audit.AuditLogService;
import com.sushank.ems.audit.AuditLog;

import com.sushank.ems.mapper.EmployeeMapper;
import com.sushank.ems.dto.EmployeeCreateRequest;
import com.sushank.ems.entity.Employee;
import com.sushank.ems.entity.User;
import com.sushank.ems.exception.BadRequestException;
import com.sushank.ems.exception.ResourceNotFoundException;
import com.sushank.ems.repository.EmployeeRepository;
import com.sushank.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmployeeMapper employeeMapper;

    public Employee createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee record with ID " + request.getEmployeeId() + " already exists!");
        }
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Employee record with email " + request.getEmail() + " already exists!");
        }

        Employee employee = employeeMapper.toEntity(request);

        Employee saved = employeeRepository.save(employee);
        auditLogService.log("CREATE_EMPLOYEE", "Created employee record " + request.getEmployeeId());
        return saved;
    }

    public Employee updateEmployee(String employeeId, Employee updateDetails) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        employee.setFirstName(updateDetails.getFirstName());
        employee.setLastName(updateDetails.getLastName());
        employee.setPhone(updateDetails.getPhone());
        employee.setDepartmentId(updateDetails.getDepartmentId());
        employee.setManagerId(updateDetails.getManagerId());
        employee.setDesignation(updateDetails.getDesignation());
        employee.setSalary(updateDetails.getSalary());
        employee.setStatus(updateDetails.getStatus());
        if (updateDetails.getSkills() != null) {
            employee.setSkills(updateDetails.getSkills());
        }

        Employee saved = employeeRepository.save(employee);
        auditLogService.log("UPDATE_EMPLOYEE", "Updated employee record " + employeeId);
        return saved;
    }

    public void deleteEmployee(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        employeeRepository.delete(employee);

        // Delete linked user credentials if present
        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> employeeId.equals(u.getEmployeeId()))
                .findFirst();
        userOpt.ifPresent(user -> userRepository.delete(user));

        auditLogService.log("DELETE_EMPLOYEE", "Deleted employee record " + employeeId);
    }

    public Employee getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));
    }

    public Employee getEmployeeById(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with DB ID: " + id));
    }

    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    public Page<Employee> searchEmployees(String name, String email, String departmentId, String skill, String designation, Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Criteria> criteriaList = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            Criteria nameCriteria = new Criteria().orOperator(
                    Criteria.where("firstName").regex(name, "i"),
                    Criteria.where("lastName").regex(name, "i")
            );
            criteriaList.add(nameCriteria);
        }

        if (email != null && !email.isBlank()) {
            criteriaList.add(Criteria.where("email").regex(email, "i"));
        }

        if (departmentId != null && !departmentId.isBlank()) {
            criteriaList.add(Criteria.where("departmentId").is(departmentId));
        }

        if (designation != null && !designation.isBlank()) {
            criteriaList.add(Criteria.where("designation").regex(designation, "i"));
        }

        if (skill != null && !skill.isBlank()) {
            criteriaList.add(Criteria.where("skills").regex(skill, "i"));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Employee.class);
        List<Employee> list = mongoTemplate.find(query, Employee.class);

        return new PageImpl<>(list, pageable, total);
    }

    public Employee addSkill(String employeeId, String skill) {
        Employee employee = getEmployeeByEmployeeId(employeeId);
        if (employee.getSkills() == null) {
            employee.setSkills(new ArrayList<>());
        }
        if (!employee.getSkills().contains(skill)) {
            employee.getSkills().add(skill);
            employeeRepository.save(employee);
            auditLogService.log("ADD_SKILL", "Added skill " + skill + " to employee " + employeeId);
        }
        return employee;
    }

    public Employee removeSkill(String employeeId, String skill) {
        Employee employee = getEmployeeByEmployeeId(employeeId);
        if (employee.getSkills() != null && employee.getSkills().contains(skill)) {
            employee.getSkills().remove(skill);
            employeeRepository.save(employee);
            auditLogService.log("REMOVE_SKILL", "Removed skill " + skill + " from employee " + employeeId);
        }
        return employee;
    }

    public List<Employee> searchBySkill(String skill) {
        return employeeRepository.findBySkill(skill);
    }
}
