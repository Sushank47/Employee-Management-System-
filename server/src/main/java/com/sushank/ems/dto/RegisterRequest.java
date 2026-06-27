package com.sushank.ems.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Set;

@Data
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotEmpty
    private Set<String> roles; // ADMIN, HR, MANAGER, EMPLOYEE

    private String employeeId; // links to existing Employee profile if pre-created
}
