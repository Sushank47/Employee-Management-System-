package com.sushank.ems.repository;

import com.sushank.ems.entity.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface DepartmentRepository extends MongoRepository<Department, String> {
    Optional<Department> findByDepartmentId(String departmentId);
    Optional<Department> findByDepartmentName(String departmentName);
    boolean existsByDepartmentId(String departmentId);
    boolean existsByDepartmentName(String departmentName);
}
