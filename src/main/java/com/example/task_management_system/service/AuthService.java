package com.example.task_management_system.service;

import com.example.task_management_system.Enum.Role;
import com.example.task_management_system.dto.LoginRequest;
import com.example.task_management_system.dto.RegisterRequest;
import com.example.task_management_system.dto.ResendRegistrationOtpRequest;
import com.example.task_management_system.dto.VerifyRegistrationOtpRequest;
import com.example.task_management_system.entity.UserEntity;
import com.example.task_management_system.exception.AccountNotVerifiedException;
import com.example.task_management_system.exception.DuplicateResourceException;
import com.example.task_management_system.exception.InvalidCredentialsException;
import com.example.task_management_system.exception.OtpException;
import com.example.task_management_system.repository.UserRepository;
import com.example.task_management_system.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final OtpDeliveryService otpDeliveryService;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       OtpDeliveryService otpDeliveryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.otpDeliveryService = otpDeliveryService;
    }

    @Transactional
    public String register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("Email already exists");
        }

        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new DuplicateResourceException("Phone number already exists");
        }

        UserEntity user = new UserEntity();
        String otp = generateOtp();

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(String.valueOf(Role.USER));
        user.setEnabled(false);
        applyOtp(user, otp);
        userRepository.save(user);
        otpDeliveryService.sendRegistrationOtp(email, phoneNumber, otp, otpExpiryMinutes);

        return "Registration successful. OTP sent to email and phone number.";
    }

    @Transactional
    public String verifyRegistrationOtp(VerifyRegistrationOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OtpException("Invalid email or OTP"));

        if (user.isEnabled()) {
            return "Account already verified";
        }

        if (user.getRegistrationOtpHash() == null || user.getRegistrationOtpExpiresAt() == null) {
            throw new OtpException("OTP was not generated. Please request a new OTP.");
        }

        if (LocalDateTime.now().isAfter(user.getRegistrationOtpExpiresAt())) {
            throw new OtpException("OTP expired. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(request.getOtp(), user.getRegistrationOtpHash())) {
            throw new OtpException("Invalid OTP");
        }

        user.setEnabled(true);
        user.setRegistrationOtpHash(null);
        user.setRegistrationOtpExpiresAt(null);
        userRepository.save(user);

        return "Account verified successfully. You can login now.";
    }

    @Transactional
    public String resendRegistrationOtp(ResendRegistrationOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new OtpException("User not found"));

        if (user.isEnabled()) {
            return "Account is already verified";
        }

        String otp = generateOtp();
        applyOtp(user, otp);
        userRepository.save(user);
        otpDeliveryService.sendRegistrationOtp(user.getEmail(), user.getPhoneNumber(), otp, otpExpiryMinutes);

        return "OTP resent to email and phone number.";
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

    private void applyOtp(UserEntity user, String otp) {
        user.setRegistrationOtpHash(passwordEncoder.encode(otp));
        user.setRegistrationOtpExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.trim().replaceAll("\\s+", "");
    }

}
