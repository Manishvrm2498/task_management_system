package com.example.task_management_system.controller;

import com.example.task_management_system.dto.ApiErrorResponse;
import com.example.task_management_system.dto.TaskRequest;
import com.example.task_management_system.dto.TaskResponse;
import com.example.task_management_system.security.OpenApiConfig;
import com.example.task_management_system.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Operations for managing tasks of the authenticated user")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(
            summary = "Create a new task",
            description = "Creates a new task for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(request));
    }

    @GetMapping
    @Operation(
            summary = "Get all my tasks",
            description = "Returns all tasks for the authenticated user. Admin users receive all tasks in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<TaskResponse>> getMyTasks() {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getMyTasks());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get task by ID",
            description = "Returns a task by ID. Users can access only their own tasks; admins can access any task."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> getTaskById(
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long id) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getTaskById(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update task",
            description = "Updates a task. Users can update only their own tasks; admins can update any task."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete task",
            description = "Deletes a task. Users can delete only their own tasks; admins can delete any task."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task deleted successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<String> deleteTask(
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long id) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.deleteTask(id));
    }
}
