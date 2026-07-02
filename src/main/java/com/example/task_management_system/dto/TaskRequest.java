package com.example.task_management_system.dto;


import com.example.task_management_system.Enum.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request body for creating or updating a task")
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Schema(description = "Short task title", example = "Learn Spring Boot")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    @Schema(description = "Detailed task description", example = "Practice building secure REST APIs with Spring Boot")
    private String description;

    @NotNull(message = "Task status is required")
    @Schema(description = "Current task status", example = "PENDING", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED"})
    private TaskStatus status;
}
