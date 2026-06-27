package com.sushank.ems.service;
import com.sushank.ems.audit.AuditLogService;
import com.sushank.ems.audit.AuditLog;

import com.sushank.ems.dto.LeaveApplyRequest;
import com.sushank.ems.dto.ResourceRecommendationRequest;
import com.sushank.ems.dto.ResourceRecommendationResponse;
import com.sushank.ems.entity.Employee;
import com.sushank.ems.entity.LeaveRequest;
import com.sushank.ems.entity.ProjectAssignment;
import com.sushank.ems.repository.EmployeeRepository;
import com.sushank.ems.repository.LeaveRequestRepository;
import com.sushank.ems.repository.ProjectAssignmentRepository;
import com.sushank.ems.repository.ProjectRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EmsServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private LeaveService leaveService;

    @InjectMocks
    private ProjectService projectService;

    // ==========================================
    // SMART LEAVE ENGINE TESTS
    // ==========================================

    @Test
    public void testLeaveApply_AutoApprove_WhenDurationLessThanOrEqualToTwoDays() {
        // Arrange: leave duration of 2 days (Jun 25 - Jun 26)
        LeaveApplyRequest request = new LeaveApplyRequest();
        request.setEmployeeId("EMP001");
        request.setStartDate(LocalDate.of(2026, 6, 25));
        request.setEndDate(LocalDate.of(2026, 6, 26));
        request.setReason("Doctor visit");

        Employee employee = Employee.builder()
                .employeeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .status("ACTIVE")
                .build();

        Mockito.when(employeeRepository.findByEmployeeId("EMP001")).thenReturn(Optional.of(employee));
        Mockito.when(leaveRequestRepository.save(Mockito.any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeaveRequest leaveResult = leaveService.applyLeave(request);

        // Assert
        Assertions.assertEquals("APPROVED", leaveResult.getStatus());
        Assertions.assertEquals("SYSTEM_AUTO", leaveResult.getApprovedBy());
        Mockito.verify(notificationService).sendNotification(Mockito.eq("EMP001"), Mockito.anyString());
    }

    @Test
    public void testLeaveApply_AutoReject_WhenDepartmentCapacityExceeded() {
        // Arrange: leave duration of 3 days (Jun 25 - Jun 27). Requires department leave capacity check.
        LeaveApplyRequest request = new LeaveApplyRequest();
        request.setEmployeeId("EMP001");
        request.setStartDate(LocalDate.of(2026, 6, 25));
        request.setEndDate(LocalDate.of(2026, 6, 27));
        request.setReason("Long Vacation");

        Employee applicant = Employee.builder()
                .employeeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .departmentId("DEP001")
                .status("ACTIVE")
                .build();

        Employee peer1 = Employee.builder().employeeId("EMP002").departmentId("DEP001").status("ACTIVE").build();
        Employee peer2 = Employee.builder().employeeId("EMP003").departmentId("DEP001").status("ACTIVE").build();

        List<Employee> deptEmployees = Arrays.asList(applicant, peer1, peer2); // total active in dept = 3

        Mockito.when(employeeRepository.findByEmployeeId("EMP001")).thenReturn(Optional.of(applicant));
        Mockito.when(employeeRepository.findByDepartmentId("DEP001")).thenReturn(deptEmployees);

        // Simulate that 1 peer is already on approved leave on Jun 26.
        // 1 peer on approved leave / 3 total employees = 33.3% (> 30% capacity). Triggering auto-reject!
        List<LeaveRequest> approvedLeavesOnDay = Collections.singletonList(new LeaveRequest());
        
        // Mock check for the first day (no overlap)
        Mockito.when(leaveRequestRepository.findApprovedLeavesOnDate(Mockito.anyList(), Mockito.eq(LocalDate.of(2026, 6, 25))))
                .thenReturn(Collections.emptyList());
        // Mock check for the second day (has overlap, triggers reject)
        Mockito.when(leaveRequestRepository.findApprovedLeavesOnDate(Mockito.anyList(), Mockito.eq(LocalDate.of(2026, 6, 26))))
                .thenReturn(approvedLeavesOnDay);

        Mockito.when(leaveRequestRepository.save(Mockito.any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeaveRequest leaveResult = leaveService.applyLeave(request);

        // Assert
        Assertions.assertEquals("REJECTED", leaveResult.getStatus());
        Assertions.assertEquals("SYSTEM_AUTO", leaveResult.getApprovedBy());
        Mockito.verify(notificationService).sendNotification(Mockito.eq("EMP001"), Mockito.contains("auto-rejected"));
    }

    // ==========================================
    // SMART RESOURCE ALLOCATION TESTS
    // ==========================================

    @Test
    public void testRecommendResources_SortsByOverallScoreCorrectly() {
        // Arrange: Project requires ["Java", "Spring Boot"]
        ResourceRecommendationRequest request = new ResourceRecommendationRequest();
        request.setRequiredSkills(Arrays.asList("Java", "Spring Boot"));
        request.setStartDate(LocalDate.of(2026, 7, 1));
        request.setEndDate(LocalDate.of(2026, 9, 30));

        // Employee A: has both skills ("Java", "Spring Boot") -> 100% skill match. Has 0 active assignments -> 100% availability.
        Employee empA = Employee.builder()
                .employeeId("EMPA")
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@company.com")
                .skills(Arrays.asList("Java", "Spring Boot"))
                .status("ACTIVE")
                .build();

        // Employee B: has only "Java" -> 50% skill match. Has 1 active assignment -> 75% availability.
        Employee empB = Employee.builder()
                .employeeId("EMPB")
                .firstName("Bob")
                .lastName("Jones")
                .email("bob@company.com")
                .skills(Collections.singletonList("Java"))
                .status("ACTIVE")
                .build();

        Mockito.when(employeeRepository.findByStatus("ACTIVE")).thenReturn(Arrays.asList(empA, empB));
        
        // Mock workloads:
        Mockito.when(projectAssignmentRepository.countByEmployeeId("EMPA")).thenReturn(0L); // 100% availability
        Mockito.when(projectAssignmentRepository.countByEmployeeId("EMPB")).thenReturn(1L); // 75% availability

        // Mock overlapping leaves: none
        Mockito.when(leaveRequestRepository.findOverlappingLeaves(Mockito.anyString(), Mockito.any(LocalDate.class), Mockito.any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<ResourceRecommendationResponse> recs = projectService.recommendResources(request);

        // Assert
        Assertions.assertEquals(2, recs.size());
        
        // Emp A Score: (100 * 0.6) + (100 * 0.4) = 100.0
        // Emp B Score: (50 * 0.6) + (75 * 0.4) = 30.0 + 30.0 = 60.0
        Assertions.assertEquals("EMPA", recs.get(0).getEmployeeId());
        Assertions.assertEquals(100.0, recs.get(0).getOverallScore());
        
        Assertions.assertEquals("EMPB", recs.get(1).getEmployeeId());
        Assertions.assertEquals(60.0, recs.get(1).getOverallScore());
    }
}
