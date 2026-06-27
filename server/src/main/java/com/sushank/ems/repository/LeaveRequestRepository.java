package com.sushank.ems.repository;

import com.sushank.ems.entity.LeaveRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends MongoRepository<LeaveRequest, String> {
    List<LeaveRequest> findByEmployeeId(String employeeId);
    List<LeaveRequest> findByStatus(String status);
    List<LeaveRequest> findByEmployeeIdAndStatus(String employeeId, String status);

    // Query to find leave requests of an employee that overlap with a project timeline
    @Query("{ 'employeeId': ?0, 'status': 'APPROVED', 'startDate': { $lte: ?2 }, 'endDate': { $gte: ?1 } }")
    List<LeaveRequest> findOverlappingLeaves(String employeeId, LocalDate projectStart, LocalDate projectEnd);

    // Query to find all approved leave requests on a particular date for a list of employees
    @Query("{ 'employeeId': { $in: ?0 }, 'status': 'APPROVED', 'startDate': { $lte: ?1 }, 'endDate': { $gte: ?1 } }")
    List<LeaveRequest> findApprovedLeavesOnDate(List<String> employeeIds, LocalDate date);

    // Query to find leaves for an employee in a specific month
    @Query("{ 'employeeId': ?0, 'status': 'APPROVED', 'startDate': { $lte: ?2 }, 'endDate': { $gte: ?1 } }")
    List<LeaveRequest> findApprovedLeavesForMonth(String employeeId, LocalDate monthStart, LocalDate monthEnd);
}
