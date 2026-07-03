package com.example.task_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Schema(description = "Standard error response returned by the API")
public class ApiErrorResponse {

    @Schema(description = "Error timestamp", example = "2026-07-02T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "Validation Failed")
    private String error;

    @Schema(description = "Human-readable error message", example = "Input data is invalid")
    private String message;

    @Schema(description = "Field-level validation errors")
    private Map<String, String> errors;
}
