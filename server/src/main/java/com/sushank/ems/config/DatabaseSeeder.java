package com.sushank.ems.config;

import com.sushank.ems.entity.*;
import com.sushank.ems.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only seed if there are no departments in the database
        if (departmentRepository.count() > 0) {
            System.out.println("Database already contains data, skipping seeder.");
            return;
        }

        System.out.println("Seeding database with demo records...");

        // 1. Seed Departments
        Department engineering = Department.builder()
                .departmentId("DEP001")
                .departmentName("Engineering")
                .description("Software Development and Systems Engineering")
                .build();
        Department hr = Department.builder()
                .departmentId("DEP002")
                .departmentName("Human Resources")
                .description("People Operations and Recruitment")
                .build();
        Department sales = Department.builder()
                .departmentId("DEP003")
                .departmentName("Sales & Marketing")
                .description("Client Relations and Outreach")
                .build();

        departmentRepository.saveAll(Arrays.asList(engineering, hr, sales));

        // 2. Seed Employees
        Employee emp1 = Employee.builder()
                .employeeId("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("admin@ems.com")
                .phone("+1 555-0101")
                .departmentId("DEP001")
                .managerId("")
                .designation("Director of Engineering")
                .salary(150000.0)
                .skills(Arrays.asList("Java", "Spring Boot", "MongoDB", "Architecture"))
                .status("ACTIVE")
                .build();

        Employee emp2 = Employee.builder()
                .employeeId("EMP002")
                .firstName("Jane")
                .lastName("Smith")
                .email("hr@ems.com")
                .phone("+1 555-0102")
                .departmentId("DEP002")
                .managerId("EMP001")
                .designation("HR Lead")
                .salary(95000.0)
                .skills(Arrays.asList("Recruiting", "Employee Relations", "Payroll"))
                .status("ACTIVE")
                .build();

        Employee emp3 = Employee.builder()
                .employeeId("EMP003")
                .firstName("Bob")
                .lastName("Johnson")
                .email("manager@ems.com")
                .phone("+1 555-0103")
                .departmentId("DEP001")
                .managerId("EMP001")
                .designation("Engineering Manager")
                .salary(120000.0)
                .skills(Arrays.asList("Agile", "Scrum", "Java", "Cloud"))
                .status("ACTIVE")
                .build();

        Employee emp4 = Employee.builder()
                .employeeId("EMP004")
                .firstName("Alice")
                .lastName("Williams")
                .email("employee@ems.com")
                .phone("+1 555-0104")
                .departmentId("DEP001")
                .managerId("EMP003")
                .designation("Software Engineer")
                .salary(95000.0)
                .skills(Arrays.asList("React", "Vite", "JavaScript", "CSS"))
                .status("ACTIVE")
                .build();

        Employee emp5 = Employee.builder()
                .employeeId("EMP005")
                .firstName("Charlie")
                .lastName("Brown")
                .email("charlie@ems.com")
                .phone("+1 555-0105")
                .departmentId("DEP003")
                .managerId("EMP001")
                .designation("Sales Executive")
                .salary(80000.0)
                .skills(Arrays.asList("Negotiation", "CRM", "Salesforce"))
                .status("ACTIVE")
                .build();

        Employee emp6 = Employee.builder()
                .employeeId("EMP006")
                .firstName("David")
                .lastName("Miller")
                .email("david@ems.com")
                .phone("+1 555-0106")
                .departmentId("DEP001")
                .managerId("EMP003")
                .designation("QA Engineer")
                .salary(85000.0)
                .skills(Arrays.asList("Selenium", "Testing", "JUnit"))
                .status("INACTIVE") // Set to INACTIVE for Attrition statistics demo
                .build();

        employeeRepository.saveAll(Arrays.asList(emp1, emp2, emp3, emp4, emp5, emp6));

        // 3. Seed Users (Pre-registered credentials)
        User adminUser = User.builder()
                .username("admin@ems.com")
                .password(passwordEncoder.encode("admin123"))
                .roles(new HashSet<>(Collections.singletonList(Role.ROLE_ADMIN)))
                .employeeId("EMP001")
                .build();

        User hrUser = User.builder()
                .username("hr@ems.com")
                .password(passwordEncoder.encode("hr123"))
                .roles(new HashSet<>(Collections.singletonList(Role.ROLE_HR)))
                .employeeId("EMP002")
                .build();

        User managerUser = User.builder()
                .username("manager@ems.com")
                .password(passwordEncoder.encode("manager123"))
                .roles(new HashSet<>(Collections.singletonList(Role.ROLE_MANAGER)))
                .employeeId("EMP003")
                .build();

        User employeeUser = User.builder()
                .username("employee@ems.com")
                .password(passwordEncoder.encode("employee123"))
                .roles(new HashSet<>(Collections.singletonList(Role.ROLE_EMPLOYEE)))
                .employeeId("EMP004")
                .build();

        userRepository.saveAll(Arrays.asList(adminUser, hrUser, managerUser, employeeUser));

        // 4. Seed Projects
        Project project1 = Project.builder()
                .projectId("PRJ001")
                .projectName("Workforce Portal Upgrade")
                .description("Upgrading internal employee tool suite")
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(3))
                .status("IN_PROGRESS")
                .requiredSkills(Arrays.asList("React", "Spring Boot", "MongoDB"))
                .build();

        Project project2 = Project.builder()
                .projectId("PRJ002")
                .projectName("Talent Sourcing Automation")
                .description("Automate recruitment candidate screening pipeline")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .status("IN_PROGRESS")
                .requiredSkills(Arrays.asList("Python", "NLP", "HR"))
                .build();

        projectRepository.saveAll(Arrays.asList(project1, project2));

        // 5. Seed Project Assignments
        ProjectAssignment pa1 = ProjectAssignment.builder()
                .projectId("PRJ001")
                .employeeId("EMP004")
                .assignedDate(LocalDate.now())
                .build();

        ProjectAssignment pa2 = ProjectAssignment.builder()
                .projectId("PRJ001")
                .employeeId("EMP003")
                .assignedDate(LocalDate.now())
                .build();

        projectAssignmentRepository.saveAll(Arrays.asList(pa1, pa2));

        // 6. Seed Attendance logs for today
        Attendance att1 = Attendance.builder()
                .employeeId("EMP001")
                .date(LocalDate.now())
                .checkIn(java.time.LocalTime.of(9, 0))
                .checkOut(java.time.LocalTime.of(17, 0))
                .status("PRESENT")
                .build();

        Attendance att2 = Attendance.builder()
                .employeeId("EMP002")
                .date(LocalDate.now())
                .checkIn(java.time.LocalTime.of(8, 30))
                .status("PRESENT")
                .build();

        attendanceRepository.saveAll(Arrays.asList(att1, att2));

        // 7. Seed Leave Requests
        LeaveRequest leave1 = LeaveRequest.builder()
                .employeeId("EMP004")
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().minusDays(8))
                .reason("Family Trip")
                .status("APPROVED")
                .approvedBy("EMP003")
                .build();

        LeaveRequest leave2 = LeaveRequest.builder()
                .employeeId("EMP005")
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(5))
                .reason("Medical Checkup")
                .status("PENDING")
                .build();

        LeaveRequest leave3 = LeaveRequest.builder()
                .employeeId("EMP004")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .reason("Conference")
                .status("PENDING")
                .build();

        leaveRequestRepository.saveAll(Arrays.asList(leave1, leave2, leave3));

        // 8. Seed Performance Reviews
        PerformanceReview review1 = PerformanceReview.builder()
                .employeeId("EMP004")
                .reviewedBy("EMP003")
                .reviewPeriod("Q1 2026")
                .reviewDate(LocalDate.now().minusMonths(2))
                .rating(4.5)
                .feedback("Excellent work delivering React features ahead of schedule. Great teamwork.")
                .build();

        PerformanceReview review2 = PerformanceReview.builder()
                .employeeId("EMP005")
                .reviewedBy("EMP001")
                .reviewPeriod("Q1 2026")
                .reviewDate(LocalDate.now().minusMonths(1))
                .rating(3.8)
                .feedback("Good sales volume. Needs to align on engineering team updates.")
                .build();

        performanceReviewRepository.saveAll(Arrays.asList(review1, review2));

        System.out.println("Seeding completed successfully!");
    }
}
