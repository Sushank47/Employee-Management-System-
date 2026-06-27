package com.sushank.ems.controller;

import com.sushank.ems.dto.MessageResponse;
import com.sushank.ems.dto.ResourceRecommendationRequest;
import com.sushank.ems.dto.ResourceRecommendationResponse;
import com.sushank.ems.entity.Employee;
import com.sushank.ems.entity.Project;
import com.sushank.ems.entity.ProjectAssignment;
import com.sushank.ems.service.ProjectService;
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
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project project) {
        Project created = projectService.createProject(project);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Project> updateProject(@PathVariable String projectId, @Valid @RequestBody Project details) {
        Project updated = projectService.updateProject(projectId, details);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProject(@PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok(new MessageResponse("Project deleted successfully"));
    }

    @PostMapping("/{projectId}/assign/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ProjectAssignment> assignEmployee(@PathVariable String projectId, @PathVariable String employeeId) {
        ProjectAssignment assignment = projectService.assignEmployeeToProject(projectId, employeeId);
        return new ResponseEntity<>(assignment, HttpStatus.CREATED);
    }

    @DeleteMapping("/{projectId}/remove/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<MessageResponse> removeEmployee(@PathVariable String projectId, @PathVariable String employeeId) {
        projectService.removeEmployeeFromProject(projectId, employeeId);
        return ResponseEntity.ok(new MessageResponse("Employee removed from project successfully"));
    }

    @GetMapping("/{projectId}/employees")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    public ResponseEntity<List<Employee>> getEmployeesInProject(@PathVariable String projectId) {
        List<Employee> list = projectService.getEmployeesInProject(projectId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/utilization")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    public ResponseEntity<List<Map<String, Object>>> getProjectUtilizationReport() {
        List<Map<String, Object>> report = projectService.getProjectUtilizationReport();
        return ResponseEntity.ok(report);
    }

    @PostMapping("/recommend-resources")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ResourceRecommendationResponse>> recommendResources(
            @RequestBody ResourceRecommendationRequest request) {
        List<ResourceRecommendationResponse> recommendations = projectService.recommendResources(request);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> list = projectService.getAllProjects();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<Project> getProjectByProjectId(@PathVariable String projectId) {
        Project project = projectService.getProjectByProjectId(projectId);
        return ResponseEntity.ok(project);
    }
}
