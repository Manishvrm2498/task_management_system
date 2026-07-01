package com.example.task_management_system.controller;

import com.example.task_management_system.dto.TaskRequest;
import com.example.task_management_system.dto.TaskResponse;
import com.example.task_management_system.entity.Task;
import com.example.task_management_system.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getMyTasks() {

        return ResponseEntity.status(HttpStatus.OK).body(taskService.getMyTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id) {

        return ResponseEntity.status(HttpStatus.OK).body(taskService.getTaskById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {

        return ResponseEntity.status(HttpStatus.OK).body(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(taskService.deleteTask(id));
    }
}