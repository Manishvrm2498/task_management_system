package com.example.task_management_system.dto;

import com.example.task_management_system.Enum.TaskStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonPropertyOrder({ "title", "description", "status", "createdAt", "updatedAt"})
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}