package com.example.task_management_system.service;

import com.example.task_management_system.exception.OtpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class OtpDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(OtpDeliveryService.class);

    private final JavaMailSender mailSender;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${app.otp.delivery-required:false}")
    private boolean deliveryRequired;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number:}")
    private String twilioPhoneNumber;

    public OtpDeliveryService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRegistrationOtp(String email, String phoneNumber, String otp, int expiryMinutes) {
        sendEmailOtp(email, otp, expiryMinutes);
        sendSmsOtp(phoneNumber, otp, expiryMinutes);
    }

    private void sendEmailOtp(String email, String otp, int expiryMinutes) {
        if (isBlank(mailFrom)) {
            handleMissingProvider("Email SMTP is not configured. OTP for " + email + ": " + otp);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Task Management registration OTP");
        message.setText("Your Task Management registration OTP is " + otp
                + ". It will expire in " + expiryMinutes + " minutes.");
        mailSender.send(message);
    }

    private void sendSmsOtp(String phoneNumber, String otp, int expiryMinutes) {
        if (isBlank(twilioAccountSid) || isBlank(twilioAuthToken) || isBlank(twilioPhoneNumber)) {
            handleMissingProvider("Twilio SMS is not configured. OTP for " + phoneNumber + ": " + otp);
            return;
        }

        String body = formField("To", phoneNumber)
                + "&" + formField("From", twilioPhoneNumber)
                + "&" + formField("Body", "Your Task Management registration OTP is " + otp
                + ". It expires in " + expiryMinutes + " minutes.");

        String credentials = twilioAccountSid + ":" + twilioAuthToken;
        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OtpException("Failed to send OTP SMS");
            }
        } catch (IOException e) {
            throw new OtpException("Failed to send OTP SMS");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OtpException("Failed to send OTP SMS");
        }
    }

    private String formField(String key, String value) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void handleMissingProvider(String message) {
        if (deliveryRequired) {
            throw new OtpException("OTP delivery is not configured");
        }
        log.warn(message);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
