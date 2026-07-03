package com.example.task_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Successful login response containing the JWT access token")
public class LoginResponse {

    @Schema(description = "Login status message", example = "Login successful!")
    private String message;

    @Schema(description = "JWT token to use in the Swagger Authorize dialog", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
}
