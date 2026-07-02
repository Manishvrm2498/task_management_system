package com.example.task_management_system.dto;

import com.example.task_management_system.Enum.TaskStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonPropertyOrder({ "id", "title", "description", "status", "userId", "userName", "userEmail", "createdAt", "updatedAt"})
@Schema(description = "Task details returned by the API")
public class TaskResponse {

    @Schema(description = "Task ID", example = "1")
    private Long id;

    @Schema(description = "Task title", example = "Learn Spring Boot")
    private String title;

    @Schema(description = "Task description", example = "Practice building secure REST APIs with Spring Boot")
    private String description;

    @Schema(description = "Task status", example = "PENDING")
    private TaskStatus status;

    @Schema(description = "Owner user ID", example = "1")
    private Long userId;

    @Schema(description = "Owner full name", example = "Manish Verma")
    private String userName;

    @Schema(description = "Owner email address", example = "manish@example.com")
    private String userEmail;

    @Schema(description = "Task creation timestamp", example = "2026-07-02T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-07-02T11:00:00")
    private LocalDateTime updatedAt;
}
