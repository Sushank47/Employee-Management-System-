package com.sushank.ems.controller;

import com.sushank.ems.dto.LeaveApplyRequest;
import com.sushank.ems.dto.MessageResponse;
import com.sushank.ems.entity.LeaveRequest;
import com.sushank.ems.security.UserDetailsImpl;
import com.sushank.ems.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequest> applyLeave(@Valid @RequestBody LeaveApplyRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isStaff = userDetails.getAuthorities().stream()
                .anyMatch(a -> java.util.Arrays.asList("ROLE_ADMIN", "ROLE_HR", "ROLE_MANAGER").contains(a.getAuthority()));
        if (!isStaff && !request.getEmployeeId().equals(userDetails.getEmployeeId())) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to apply leave for another employee!");
        }
        LeaveRequest leave = leaveService.applyLeave(request);
        return new ResponseEntity<>(leave, HttpStatus.CREATED);
    }

    @PostMapping("/{leaveRequestId}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequest> approveLeave(@PathVariable String leaveRequestId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String reviewerId = userDetails.getEmployeeId() != null ? userDetails.getEmployeeId() : userDetails.getUsername();
        LeaveRequest leave = leaveService.approveLeave(leaveRequestId, reviewerId);
        return ResponseEntity.ok(leave);
    }

    @PostMapping("/{leaveRequestId}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequest> rejectLeave(@PathVariable String leaveRequestId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String reviewerId = userDetails.getEmployeeId() != null ? userDetails.getEmployeeId() : userDetails.getUsername();
        LeaveRequest leave = leaveService.rejectLeave(leaveRequestId, reviewerId);
        return ResponseEntity.ok(leave);
    }

    @DeleteMapping("/{leaveRequestId}/cancel")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> cancelLeave(@PathVariable String leaveRequestId) {
        leaveService.cancelLeave(leaveRequestId);
        return ResponseEntity.ok(new MessageResponse("Leave request cancelled successfully"));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or (#employeeId == authentication.principal.employeeId)")
    public ResponseEntity<List<LeaveRequest>> getLeavesByEmployee(@PathVariable String employeeId) {
        List<LeaveRequest> list = leaveService.getLeavesByEmployee(employeeId);
        return ResponseEntity.ok(list);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')")
    public ResponseEntity<List<LeaveRequest>> getAllLeaves() {
        List<LeaveRequest> list = leaveService.getAllLeaves();
        return ResponseEntity.ok(list);
    }
}
