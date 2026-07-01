package com.example.task_management_system.controller;

import com.example.task_management_system.dto.TaskResponse;
import com.example.task_management_system.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TaskService taskService;

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getAllTasks());
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.getAnyTaskById(id));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.deleteAnyTask(id));
    }
}
