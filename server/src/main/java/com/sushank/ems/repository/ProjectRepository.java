package com.sushank.ems.repository;

import com.sushank.ems.entity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> findByProjectId(String projectId);
    Optional<Project> findByProjectName(String projectName);
    boolean existsByProjectId(String projectId);
}
