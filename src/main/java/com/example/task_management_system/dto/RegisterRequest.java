package com.example.task_management_system.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder
@Schema(description = "Request body for creating a new user account")
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Schema(description = "User first name", example = "FirstName")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "User last name", example = "LastName")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Please provide a valid email address (e.g. user@example.com)"
    )
    @Schema(description = "Unique user email address", example = "example@example.com")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?[1-9][0-9]{9,14}$",
            message = "Please provide a valid phone number with country code, e.g. +919876543210"
    )
    @Schema(description = "Unique phone number with country code", example = "+919876543210")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least one digit, one uppercase, one lowercase, and one special character (@#$%^&+=)"
    )
    @Schema(description = "Strong password with uppercase, lowercase, number, and special character", example = "Password@123")
    private String password;

    @Schema(description = "Ignored during public registration; users are registered with USER role", example = "USER")
    private String role;
}
