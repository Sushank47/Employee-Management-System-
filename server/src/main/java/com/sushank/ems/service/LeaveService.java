package com.sushank.ems.service;
import com.sushank.ems.audit.AuditLogService;
import com.sushank.ems.audit.AuditLog;

import com.sushank.ems.dto.LeaveApplyRequest;
import com.sushank.ems.entity.Employee;
import com.sushank.ems.entity.LeaveRequest;
import com.sushank.ems.exception.BadRequestException;
import com.sushank.ems.exception.ResourceNotFoundException;
import com.sushank.ems.repository.EmployeeRepository;
import com.sushank.ems.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sushank.ems.util.DateUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditLogService auditLogService;

    public LeaveRequest applyLeave(LeaveApplyRequest request) {
        Employee employee = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date!");
        }

        long durationDays = DateUtils.calculateDurationDays(request.getStartDate(), request.getEndDate());

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employeeId(request.getEmployeeId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status("PENDING")
                .build();

        // --- Smart Leave Engine Rules ---
        
        // 1. Auto Approve Rule: <= 2 days
        if (durationDays <= 2) {
            leaveRequest.setStatus("APPROVED");
            leaveRequest.setApprovedBy("SYSTEM_AUTO");
            LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

            notificationService.sendNotification(request.getEmployeeId(), 
                    "Your leave request of " + durationDays + " day(s) was auto-approved by the Smart Leave Engine.");
            auditLogService.log("LEAVE_AUTO_APPROVE", "SYSTEM", 
                    "Auto-approved leave request " + saved.getId() + " for employee " + request.getEmployeeId());
            return saved;
        }

        // 2. Auto Reject Rule: Department leave capacity check (> 30% on leave on any requested day)
        String deptId = employee.getDepartmentId();
        if (deptId != null && !deptId.isBlank()) {
            List<Employee> deptEmployees = employeeRepository.findByDepartmentId(deptId);
            long activeDeptCount = deptEmployees.stream()
                    .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                    .count();

            if (activeDeptCount > 0) {
                List<String> deptEmpIds = deptEmployees.stream()
                        .map(Employee::getEmployeeId)
                        .collect(Collectors.toList());

                LocalDate currentDay = request.getStartDate();
                boolean shouldReject = false;

                while (!currentDay.isAfter(request.getEndDate())) {
                    List<LeaveRequest> approvedLeavesOnDay = leaveRequestRepository.findApprovedLeavesOnDate(deptEmpIds, currentDay);
                    long approvedCount = approvedLeavesOnDay.size();

                    double leavePercentage = ((double) approvedCount / activeDeptCount) * 100.0;
                    if (leavePercentage >= 30.0) {
                        shouldReject = true;
                        break;
                    }
                    currentDay = currentDay.plusDays(1);
                }

                if (shouldReject) {
                    leaveRequest.setStatus("REJECTED");
                    leaveRequest.setApprovedBy("SYSTEM_AUTO");
                    LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

                    notificationService.sendNotification(request.getEmployeeId(), 
                            "Your leave request was auto-rejected as the department's leave capacity limit (30%) is exceeded.");
                    auditLogService.log("LEAVE_AUTO_REJECT", "SYSTEM", 
                            "Auto-rejected leave request " + saved.getId() + " due to department capacity overload for employee " + request.getEmployeeId());
                    return saved;
                }
            }
        }

        // 3. Manual Approval Required (Otherwise)
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        auditLogService.log("LEAVE_APPLY", request.getEmployeeId(), "Applied for leave request " + saved.getId());
        
        // Notify manager if employee has managerId
        if (employee.getManagerId() != null && !employee.getManagerId().isBlank()) {
            notificationService.sendNotification(employee.getManagerId(), 
                    "Employee " + employee.getFirstName() + " " + employee.getLastName() + " applied for leave. Action required.");
        }

        return saved;
    }

    public LeaveRequest approveLeave(String leaveRequestId, String approvedByEmployeeId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveRequestId));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new BadRequestException("Leave request has already been processed!");
        }

        leaveRequest.setStatus("APPROVED");
        leaveRequest.setApprovedBy(approvedByEmployeeId);
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        notificationService.sendNotification(leaveRequest.getEmployeeId(), "Your leave request was APPROVED by manager " + approvedByEmployeeId);
        auditLogService.log("LEAVE_APPROVE", approvedByEmployeeId, "Approved leave request " + leaveRequestId);

        return saved;
    }

    public LeaveRequest rejectLeave(String leaveRequestId, String rejectedByEmployeeId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveRequestId));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new BadRequestException("Leave request has already been processed!");
        }

        leaveRequest.setStatus("REJECTED");
        leaveRequest.setApprovedBy(rejectedByEmployeeId);
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        notificationService.sendNotification(leaveRequest.getEmployeeId(), "Your leave request was REJECTED by manager " + rejectedByEmployeeId);
        auditLogService.log("LEAVE_REJECT", rejectedByEmployeeId, "Rejected leave request " + leaveRequestId);

        return saved;
    }

    public void cancelLeave(String leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveRequestId));

        if ("APPROVED".equals(leaveRequest.getStatus())) {
            throw new BadRequestException("Approved leave requests cannot be cancelled!");
        }

        leaveRequestRepository.delete(leaveRequest);
        auditLogService.log("LEAVE_CANCEL", leaveRequest.getEmployeeId(), "Cancelled leave request " + leaveRequestId);
    }

    public List<LeaveRequest> getLeavesByEmployee(String employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAll();
    }
}
