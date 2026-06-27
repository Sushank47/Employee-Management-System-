package com.sushank.ems.service;
import com.sushank.ems.audit.AuditLogService;
import com.sushank.ems.audit.AuditLog;

import com.sushank.ems.dto.ResourceRecommendationRequest;
import com.sushank.ems.dto.ResourceRecommendationResponse;
import com.sushank.ems.entity.Employee;
import com.sushank.ems.entity.Project;
import com.sushank.ems.entity.ProjectAssignment;
import com.sushank.ems.exception.BadRequestException;
import com.sushank.ems.exception.ResourceNotFoundException;
import com.sushank.ems.repository.EmployeeRepository;
import com.sushank.ems.repository.LeaveRequestRepository;
import com.sushank.ems.repository.ProjectAssignmentRepository;
import com.sushank.ems.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditLogService auditLogService;

    public Project createProject(Project project) {
        if (projectRepository.existsByProjectId(project.getProjectId())) {
            throw new BadRequestException("Project with ID " + project.getProjectId() + " already exists!");
        }
        Project saved = projectRepository.save(project);
        auditLogService.log("CREATE_PROJECT", "Created project " + project.getProjectId());
        return saved;
    }

    public Project updateProject(String projectId, Project details) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        project.setProjectName(details.getProjectName());
        project.setDescription(details.getDescription());
        project.setStartDate(details.getStartDate());
        project.setEndDate(details.getEndDate());
        project.setStatus(details.getStatus());
        project.setRequiredSkills(details.getRequiredSkills());

        Project saved = projectRepository.save(project);
        auditLogService.log("UPDATE_PROJECT", "Updated project " + projectId);
        return saved;
    }

    public void deleteProject(String projectId) {
        Project project = projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        projectRepository.delete(project);
        
        // Remove all assignments for this project
        List<ProjectAssignment> assignments = projectAssignmentRepository.findByProjectId(projectId);
        projectAssignmentRepository.deleteAll(assignments);

        auditLogService.log("DELETE_PROJECT", "Deleted project " + projectId);
    }

    public Project getProjectByProjectId(String projectId) {
        return projectRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public ProjectAssignment assignEmployeeToProject(String projectId, String employeeId) {
        Project project = getProjectByProjectId(projectId);
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (projectAssignmentRepository.existsByProjectIdAndEmployeeId(projectId, employeeId)) {
            throw new BadRequestException("Employee is already assigned to this project!");
        }

        ProjectAssignment assignment = ProjectAssignment.builder()
                .projectId(projectId)
                .employeeId(employeeId)
                .assignedDate(LocalDate.now())
                .build();

        ProjectAssignment saved = projectAssignmentRepository.save(assignment);
        
        // Notify employee
        notificationService.sendNotification(employeeId, "You have been assigned to project: " + project.getProjectName());
        auditLogService.log("PROJECT_ASSIGN", "Assigned employee " + employeeId + " to project " + projectId);
        
        return saved;
    }

    public void removeEmployeeFromProject(String projectId, String employeeId) {
        if (!projectRepository.existsByProjectId(projectId)) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }
        if (!employeeRepository.existsByEmployeeId(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        if (!projectAssignmentRepository.existsByProjectIdAndEmployeeId(projectId, employeeId)) {
            throw new BadRequestException("Employee is not assigned to this project!");
        }

        projectAssignmentRepository.deleteByProjectIdAndEmployeeId(projectId, employeeId);
        
        // Notify employee
        notificationService.sendNotification(employeeId, "You have been removed from project: " + projectId);
        auditLogService.log("PROJECT_UNASSIGN", "Removed employee " + employeeId + " from project " + projectId);
    }

    public List<Employee> getEmployeesInProject(String projectId) {
        List<ProjectAssignment> assignments = projectAssignmentRepository.findByProjectId(projectId);
        List<String> empIds = assignments.stream().map(ProjectAssignment::getEmployeeId).collect(Collectors.toList());
        return employeeRepository.findAll().stream()
                .filter(e -> empIds.contains(e.getEmployeeId()))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getProjectUtilizationReport() {
        List<Project> projects = projectRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (Project project : projects) {
            List<Employee> assigned = getEmployeesInProject(project.getProjectId());
            Map<String, Object> projectReport = new HashMap<>();
            projectReport.put("projectId", project.getProjectId());
            projectReport.put("projectName", project.getProjectName());
            projectReport.put("status", project.getStatus());
            projectReport.put("assignedCount", assigned.size());
            projectReport.put("teamMembers", assigned.stream()
                    .map(e -> Map.of("employeeId", e.getEmployeeId(), "name", e.getFirstName() + " " + e.getLastName(), "role", e.getDesignation()))
                    .collect(Collectors.toList()));
            report.add(projectReport);
        }
        return report;
    }

    public List<ResourceRecommendationResponse> recommendResources(ResourceRecommendationRequest request) {
        List<String> requiredSkills;
        LocalDate startDate;
        LocalDate endDate;

        if (request.getProjectId() != null && !request.getProjectId().isBlank()) {
            Project project = getProjectByProjectId(request.getProjectId());
            requiredSkills = project.getRequiredSkills();
            startDate = project.getStartDate() != null ? project.getStartDate() : LocalDate.now();
            endDate = project.getEndDate() != null ? project.getEndDate() : LocalDate.now().plusMonths(3);
        } else {
            requiredSkills = request.getRequiredSkills();
            startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
            endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now().plusMonths(3);
        }

        if (requiredSkills == null || requiredSkills.isEmpty()) {
            throw new BadRequestException("Required skills are missing for resource recommendation!");
        }

        List<Employee> allEmployees = employeeRepository.findByStatus("ACTIVE");
        List<ResourceRecommendationResponse> recommendations = new ArrayList<>();

        for (Employee employee : allEmployees) {
            // 1. Skill Match Score
            double skillMatch = 0.0;
            List<String> empSkills = employee.getSkills() != null ? employee.getSkills() : Collections.emptyList();
            if (!empSkills.isEmpty()) {
                long matching = requiredSkills.stream()
                        .filter(req -> empSkills.stream().anyMatch(emp -> emp.equalsIgnoreCase(req)))
                        .count();
                skillMatch = ((double) matching / requiredSkills.size()) * 100.0;
            }

            // 2. Availability / Workload Score
            long activeAssignments = projectAssignmentRepository.countByEmployeeId(employee.getEmployeeId());
            double availabilityScore = Math.max(0.0, 100.0 - (activeAssignments * 25.0));

            // 3. Leave Conflict Check
            boolean leaveConflict = !leaveRequestRepository.findOverlappingLeaves(
                    employee.getEmployeeId(), startDate, endDate).isEmpty();

            if (leaveConflict) {
                availabilityScore = 0.0; // on leave during project means unavailable
            }

            // 4. Combined Score calculation (60% Skill Match + 40% Availability)
            double overallScore = (skillMatch * 0.6) + (availabilityScore * 0.4);

            recommendations.add(ResourceRecommendationResponse.builder()
                    .employeeId(employee.getEmployeeId())
                    .firstName(employee.getFirstName())
                    .lastName(employee.getLastName())
                    .email(employee.getEmail())
                    .skills(employee.getSkills())
                    .skillMatchPercentage(skillMatch)
                    .availabilityScore(availabilityScore)
                    .hasOverlappingLeave(leaveConflict)
                    .overallScore(overallScore)
                    .build());
        }

        // Sort by overall score descending
        recommendations.sort(Comparator.comparingDouble(ResourceRecommendationResponse::getOverallScore).reversed());
        return recommendations;
    }
}
