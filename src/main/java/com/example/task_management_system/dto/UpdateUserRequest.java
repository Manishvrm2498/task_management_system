package com.example.task_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Request body for updating a user as admin")
public class UpdateUserRequest {

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(USER|ADMIN|ROLE_USER|ROLE_ADMIN)$", message = "Role must be USER, ADMIN, ROLE_USER, or ROLE_ADMIN")
    @Schema(description = "User role", example = "USER", allowableValues = {"USER", "ADMIN", "ROLE_USER", "ROLE_ADMIN"})
    private String role;

    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether the user account is enabled", example = "true")
    private Boolean enabled;
}
