package com.example.task_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "User details returned to admin users")
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "First name", example = "FirstName")
    private String firstName;

    @Schema(description = "Last name", example = "LastName")
    private String lastName;

    @Schema(description = "Email address", example = "example@example.com")
    private String email;

    @Schema(description = "User role", example = "USER")
    private String role;

    @Schema(description = "Whether the user account is enabled", example = "true")
    private boolean enabled;

    @Schema(description = "Account creation timestamp", example = "2026-07-02T10:30:00")
    private LocalDateTime createdAt;
}
