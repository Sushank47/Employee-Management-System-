package com.sushank.ems.analytics;

import com.sushank.ems.analytics.DashboardOverviewResponse;
import com.sushank.ems.entity.Department;
import com.sushank.ems.entity.Employee;
import com.sushank.ems.entity.LeaveRequest;
import com.sushank.ems.entity.PerformanceReview;
import com.sushank.ems.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    public DashboardOverviewResponse getDashboardOverview() {
        List<Employee> employees = employeeRepository.findAll();
        long totalEmployees = employees.size();
        
        long activeEmployees = employees.stream()
                .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                .count();

        long inactiveEmployees = totalEmployees - activeEmployees;

        // Employees on approved leave today
        LocalDate today = LocalDate.now();
        List<String> activeEmpIds = employees.stream()
                .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                .map(Employee::getEmployeeId)
                .collect(Collectors.toList());
        long employeesOnLeave = leaveRequestRepository.findApprovedLeavesOnDate(activeEmpIds, today).size();

        long departmentsCount = departmentRepository.count();
        long projectsCount = projectRepository.count();

        // 1. Department-wise Employees
        Map<String, Long> departmentWise = new HashMap<>();
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            long count = employees.stream()
                    .filter(e -> dept.getDepartmentId().equals(e.getDepartmentId()))
                    .count();
            departmentWise.put(dept.getDepartmentName(), count);
        }

        // 2. Salary Distribution
        Map<String, Long> salaryDistribution = new HashMap<>();
        salaryDistribution.put("Under 50k", 0L);
        salaryDistribution.put("50k - 100k", 0L);
        salaryDistribution.put("100k - 150k", 0L);
        salaryDistribution.put("Over 150k", 0L);

        for (Employee emp : employees) {
            double sal = emp.getSalary() != null ? emp.getSalary() : 0.0;
            if (sal < 50000) {
                salaryDistribution.put("Under 50k", salaryDistribution.get("Under 50k") + 1);
            } else if (sal <= 100000) {
                salaryDistribution.put("50k - 100k", salaryDistribution.get("50k - 100k") + 1);
            } else if (sal <= 150000) {
                salaryDistribution.put("100k - 150k", salaryDistribution.get("100k - 150k") + 1);
            } else {
                salaryDistribution.put("Over 150k", salaryDistribution.get("Over 150k") + 1);
            }
        }

        // 3. Attendance Trends (Today's status counts)
        Map<String, Long> attendanceTrends = attendanceRepository.findByDate(today).stream()
                .collect(Collectors.groupingBy(a -> a.getStatus() != null ? a.getStatus() : "PRESENT", Collectors.counting()));

        // 4. Leave Statistics (Total status counts)
        Map<String, Long> leaveStatistics = leaveRequestRepository.findAll().stream()
                .collect(Collectors.groupingBy(LeaveRequest::getStatus, Collectors.counting()));

        // 5. Performance Statistics (Average rating by department)
        Map<String, Double> performanceStatistics = new HashMap<>();
        List<PerformanceReview> reviews = performanceReviewRepository.findAll();
        for (Department dept : departments) {
            List<String> deptEmpIds = employees.stream()
                    .filter(e -> dept.getDepartmentId().equals(e.getDepartmentId()))
                    .map(Employee::getEmployeeId)
                    .collect(Collectors.toList());

            double avgRating = reviews.stream()
                    .filter(r -> deptEmpIds.contains(r.getEmployeeId()))
                    .mapToDouble(PerformanceReview::getRating)
                    .average()
                    .orElse(0.0);

            performanceStatistics.put(dept.getDepartmentName(), avgRating);
        }

        // 6. Attrition Rate calculation (Inactive / Total) * 100
        double attritionRate = totalEmployees > 0 
                ? ((double) inactiveEmployees / totalEmployees) * 100.0 
                : 0.0;

        return DashboardOverviewResponse.builder()
                .totalEmployees(totalEmployees)
                .activeEmployees(activeEmployees)
                .employeesOnLeave(employeesOnLeave)
                .departments(departmentsCount)
                .projects(projectsCount)
                .departmentWiseEmployees(departmentWise)
                .salaryDistribution(salaryDistribution)
                .attendanceTrends(attendanceTrends)
                .leaveStatistics(leaveStatistics)
                .performanceStatistics(performanceStatistics)
                .attritionRate(attritionRate)
                .build();
    }
}
