package com.example.task_management_system.service;

import com.example.task_management_system.dto.TaskRequest;
import com.example.task_management_system.dto.TaskResponse;
import com.example.task_management_system.entity.Task;
import com.example.task_management_system.entity.UserEntity;
import com.example.task_management_system.repository.TaskRepository;
import com.example.task_management_system.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private UserEntity getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    private boolean isCurrentUserAdmin() {
        return "ADMIN".equalsIgnoreCase(getCurrentUser().getRole())
                || "ROLE_ADMIN".equalsIgnoreCase(getCurrentUser().getRole());
    }

    private Task findTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task Not Found"));

        UserEntity currentUser = getCurrentUser();

        if (!isCurrentUserAdmin() && !task.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access Denied");
        }

        return task;
    }

    private TaskResponse mapToResponse(Task task) {
        UserEntity user = task.getUser();
        String userName = user.getFirstName() + " " + user.getLastName();

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .userId(user.getId())
                .userName(userName.trim())
                .userEmail(user.getEmail())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public TaskResponse createTask(TaskRequest request) {
        UserEntity currentUser = getCurrentUser();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .user(currentUser)
                .build();

        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask);
    }

    public List<TaskResponse> getMyTasks() {
        if (isCurrentUserAdmin()) {
            return getAllTasks();
        }

        return taskRepository.findByUser(getCurrentUser())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {
        return mapToResponse(findTask(id));
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTask(id);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());

        return mapToResponse(taskRepository.save(task));
    }

    public String deleteTask(Long id) {
        Task task = findTask(id);

        taskRepository.delete(task);

        return "Task deleted successfully";
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse getAnyTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task Not Found"));

        return mapToResponse(task);
    }

    public String deleteAnyTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task Not Found"));

        taskRepository.delete(task);

        return "Task deleted successfully";
    }
}
