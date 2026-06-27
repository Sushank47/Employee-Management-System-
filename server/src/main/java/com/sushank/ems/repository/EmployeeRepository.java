package com.sushank.ems.repository;

import com.sushank.ems.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
    Optional<Employee> findByEmployeeId(String employeeId);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByDepartmentId(String departmentId);
    List<Employee> findByManagerId(String managerId);
    List<Employee> findByStatus(String status);
    
    // Custom query to find employees by matching skill (case-insensitive)
    @Query("{'skills': { $regex: ?0, $options: 'i' }}")
    List<Employee> findBySkill(String skill);

    boolean existsByEmployeeId(String employeeId);
    boolean existsByEmail(String email);
}
