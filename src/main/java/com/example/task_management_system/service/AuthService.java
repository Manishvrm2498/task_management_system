package com.example.task_management_system.service;

import com.example.task_management_system.Enum.Role;
import com.example.task_management_system.dto.LoginRequest;
import com.example.task_management_system.dto.RegisterRequest;
import com.example.task_management_system.entity.UserEntity;
import com.example.task_management_system.exception.AccountNotVerifiedException;
import com.example.task_management_system.exception.DuplicateResourceException;
import com.example.task_management_system.exception.InvalidCredentialsException;
import com.example.task_management_system.repository.UserRepository;
import com.example.task_management_system.util.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public String register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("Email already exists");
        }

        UserEntity user = new UserEntity();

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(String.valueOf(Role.USER));
        user.setEnabled(true);
        userRepository.save(user);

        return "Registration successful";
    }

    public String login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
            );
            return jwtUtil.generateToken(authentication);

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (DisabledException e) {
            throw new AccountNotVerifiedException("Account is not verified. Please verify your OTP first.");
        } catch (LockedException e) {
            throw new RuntimeException("Your account is locked. Please contact support.");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

}