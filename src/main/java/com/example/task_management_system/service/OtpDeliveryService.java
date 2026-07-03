package com.example.task_management_system.service;

import com.example.task_management_system.exception.OtpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
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
import java.time.Duration;
import java.util.Base64;

@Service
public class OtpDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(OtpDeliveryService.class);

    private final JavaMailSender mailSender;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${app.otp.delivery-required:false}")
    private boolean deliveryRequired;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.from:}")
    private String mailFrom;

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:}")
    private String sendGridFromEmail;

    @Value("${sendgrid.from-name:Task Management System}")
    private String sendGridFromName;

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
        if (!isBlank(sendGridApiKey)) {
            sendEmailOtpWithSendGridApi(email, otp, expiryMinutes);
            return;
        }

        if (isBlank(mailFrom) && isBlank(mailUsername)) {
            handleMissingProvider("Email provider is not configured. OTP for " + email + ": " + otp);
            return;
        }

        String from = isBlank(mailFrom) ? mailUsername : mailFrom;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Task Management registration OTP");
        message.setText("Your Task Management registration OTP is " + otp
                + ". It will expire in " + expiryMinutes + " minutes.");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            handleDeliveryFailure("Failed to send OTP email. OTP for " + email + ": " + otp, e);
        }
    }

    private void sendEmailOtpWithSendGridApi(String email, String otp, int expiryMinutes) {
        if (isBlank(sendGridFromEmail)) {
            handleMissingProvider("SendGrid from email is not configured. OTP for " + email + ": " + otp);
            return;
        }

        String subject = "Task Management registration OTP";
        String content = "Your Task Management registration OTP is " + otp
                + ". It will expire in " + expiryMinutes + " minutes.";

        String body = "{"
                + "\"personalizations\":[{\"to\":[{\"email\":\"" + jsonEscape(email) + "\"}]}],"
                + "\"from\":{\"email\":\"" + jsonEscape(sendGridFromEmail) + "\",\"name\":\"" + jsonEscape(sendGridFromName) + "\"},"
                + "\"subject\":\"" + jsonEscape(subject) + "\","
                + "\"content\":[{\"type\":\"text/plain\",\"value\":\"" + jsonEscape(content) + "\"}]"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + sendGridApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                handleDeliveryFailure("Failed to send OTP email via SendGrid. Status " + response.statusCode()
                        + ". OTP for " + email + ": " + otp, null);
            }
        } catch (IOException e) {
            handleDeliveryFailure("Failed to send OTP email via SendGrid. OTP for " + email + ": " + otp, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleDeliveryFailure("Failed to send OTP email via SendGrid. OTP for " + email + ": " + otp, e);
        }
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
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                handleDeliveryFailure("Failed to send OTP SMS. Twilio status " + response.statusCode()
                        + ". OTP for " + phoneNumber + ": " + otp, null);
            }
        } catch (IOException e) {
            handleDeliveryFailure("Failed to send OTP SMS. OTP for " + phoneNumber + ": " + otp, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleDeliveryFailure("Failed to send OTP SMS. OTP for " + phoneNumber + ": " + otp, e);
        }
    }

    private String formField(String key, String value) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String jsonEscape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private void handleMissingProvider(String message) {
        if (deliveryRequired) {
            throw new OtpException("OTP delivery is not configured");
        }
        log.warn(message);
    }

    private void handleDeliveryFailure(String message, Exception exception) {
        if (deliveryRequired) {
            throw new OtpException("Failed to deliver OTP");
        }

        if (exception == null) {
            log.warn(message);
        } else {
            log.warn(message, exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
