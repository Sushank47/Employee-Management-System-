package com.sushank.ems.service;
import com.sushank.ems.audit.AuditLogService;
import com.sushank.ems.audit.AuditLog;

import com.sushank.ems.dto.*;
import com.sushank.ems.entity.Role;
import com.sushank.ems.entity.User;
import com.sushank.ems.exception.BadRequestException;
import com.sushank.ems.exception.ResourceNotFoundException;
import com.sushank.ems.repository.EmployeeRepository;
import com.sushank.ems.repository.UserRepository;
import com.sushank.ems.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuditLogService auditLogService;

    public MessageResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getEmail())) {
            throw new BadRequestException("Error: Email is already in use!");
        }

        // Verify employeeId exists if provided
        if (registerRequest.getEmployeeId() != null && !registerRequest.getEmployeeId().isBlank()) {
            if (!employeeRepository.existsByEmployeeId(registerRequest.getEmployeeId())) {
                throw new ResourceNotFoundException("Error: Employee record with ID " + registerRequest.getEmployeeId() + " not found!");
            }
        }

        Set<String> strRoles = registerRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            roles.add(Role.ROLE_EMPLOYEE);
        } else {
            strRoles.forEach(role -> {
                switch (role.toUpperCase()) {
                    case "ADMIN":
                        roles.add(Role.ROLE_ADMIN);
                        break;
                    case "HR":
                        roles.add(Role.ROLE_HR);
                        break;
                    case "MANAGER":
                        roles.add(Role.ROLE_MANAGER);
                        break;
                    default:
                        roles.add(Role.ROLE_EMPLOYEE);
                }
            });
        }

        User user = User.builder()
                .username(registerRequest.getEmail())
                .password(encoder.encode(registerRequest.getPassword()))
                .roles(roles)
                .employeeId(registerRequest.getEmployeeId())
                .build();

        userRepository.save(user);
        auditLogService.log("USER_REGISTER", registerRequest.getEmail(), "Registered new user: " + registerRequest.getEmail() + " with roles " + roles);

        return new MessageResponse("User registered successfully!");
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        auditLogService.log("LOGIN", loginRequest.getUsername(), "Successfully logged in");

        return new JwtResponse(jwt);
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(Instant.now().plus(15, ChronoUnit.MINUTES));
        userRepository.save(user);

        // Print/log the token for verification
        logger.info("Password reset token generated for {}: {}", request.getEmail(), token);
        auditLogService.log("PASSWORD_RESET_REQUEST", request.getEmail(), "Password reset request generated token");

        // Simulate sending email: in real systems, trigger EmailService here
        System.out.println("Email Service: Send Password Reset Link with token: " + token);

        return new MessageResponse("Password reset token sent to your email. Active for 15 minutes.");
    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        // Query users by resetToken. Since Spring Data doesn't auto-resolve this custom query without definition,
        // let's define it or write a simple lookup. Wait, we can implement it using UserRepository query methods.
        // Let's first make sure UserRepository supports finding by resetToken.
        // Let's do it using MongoTemplate or define the repository method.
        // UserRepository findByResetToken is easy. Let's add that to UserRepository to avoid runtime errors.
        
        // Let's lookup user directly. We will query user repository. Wait, let's look up how to get it.
        // We will add it to UserRepository soon.
        Optional<User> userOpt = userRepository.findByResetToken(request.getToken());

        if (userOpt.isEmpty()) {
            throw new BadRequestException("Invalid or expired password reset token.");
        }

        User user = userOpt.get();
        if (user.getResetTokenExpiry().isBefore(Instant.now())) {
            throw new BadRequestException("Password reset token has expired.");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        auditLogService.log("PASSWORD_RESET", user.getUsername(), "Successfully reset password");

        return new MessageResponse("Password has been reset successfully!");
    }
}
