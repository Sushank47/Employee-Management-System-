package com.sushank.ems.repository;

import com.sushank.ems.entity.ProjectAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProjectAssignmentRepository extends MongoRepository<ProjectAssignment, String> {
    List<ProjectAssignment> findByProjectId(String projectId);
    List<ProjectAssignment> findByEmployeeId(String employeeId);
    boolean existsByProjectIdAndEmployeeId(String projectId, String employeeId);
    void deleteByProjectIdAndEmployeeId(String projectId, String employeeId);
    long countByEmployeeId(String employeeId);
}
