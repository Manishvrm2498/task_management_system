package com.example.task_management_system.service;

import com.example.task_management_system.dto.UpdateUserRequest;
import com.example.task_management_system.dto.UserResponse;
import com.example.task_management_system.entity.UserEntity;
import com.example.task_management_system.repository.TaskRepository;
import com.example.task_management_system.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public AdminUserService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    private UserEntity findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String normalizeRole(String role) {
        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        return normalizedRole.startsWith("ROLE_")
                ? normalizedRole.substring("ROLE_".length())
                : normalizedRole;
    }

    private UserResponse mapToResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return mapToResponse(findUser(id));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        UserEntity user = findUser(id);
        String requestedRole = normalizeRole(request.getRole());

        if (user.getEmail().equalsIgnoreCase(currentEmail()) && !request.getEnabled()) {
            throw new RuntimeException("Admin cannot disable own account");
        }

        if (user.getEmail().equalsIgnoreCase(currentEmail()) && !"ADMIN".equals(requestedRole)) {
            throw new RuntimeException("Admin cannot remove own admin role");
        }

        user.setRole(requestedRole);
        user.setEnabled(request.getEnabled());

        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public String deleteUser(Long id) {
        UserEntity user = findUser(id);

        if (user.getEmail().equalsIgnoreCase(currentEmail())) {
            throw new RuntimeException("Admin cannot delete own account");
        }

        taskRepository.deleteByUser(user);
        userRepository.delete(user);

        return "User deleted successfully";
    }
}
