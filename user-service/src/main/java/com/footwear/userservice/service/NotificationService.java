// user-service/src/main/java/com/footwear/userservice/service/NotificationService.java
package com.footwear.userservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    private final RestTemplate restTemplate;
    private final EmailService emailService;
    private final SmsAdvertService smsAdvertService;

    @Value("${sms.api.url:https://smsadvert.ro/api}")
    private String smsApiUrl;

    @Value("${sms.api.key:your-sms-api-key}")
    private String smsApiKey;

    public NotificationService(RestTemplate restTemplate, EmailService emailService, SmsAdvertService smsAdvertService) {
        this.restTemplate = restTemplate;
        this.emailService = emailService;
        this.smsAdvertService = smsAdvertService;
    }

    /**
     * Trimite email de notificare către utilizator
     */
    public void sendUserUpdateEmail(String userEmail, String username, String changeDescription) {
        try {
            String subject = "Account Update Notification - FootwearChain";
            String htmlContent = buildEmailContent(username, changeDescription);

            // Folosim un serviciu simplu de email (poți integra cu SendGrid, Amazon SES, etc.)
            sendEmail(userEmail, subject, htmlContent);

            log.info("Email notification sent successfully to: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send email notification to: {}", userEmail, e);
        }
    }

    /**
     * Trimite SMS de notificare către utilizator folosind smsAdvert API
     */
    public void sendUserUpdateSMS(String phoneNumber, String username, String changeDescription) {
        try {
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                log.warn("Phone number is empty for user: {}", username);
                return;
            }

            String message = buildSMSContent(username, changeDescription);
            sendSMS(phoneNumber, message);

            log.info("SMS notification sent successfully to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS notification to: {}", phoneNumber, e);
        }
    }

    /**
     * Trimite notificări complete (email + SMS)
     */
    public void sendUserUpdateNotifications(String userEmail, String phoneNumber, String username, String changeDescription) {
        // Trimitere email
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            System.out.println("SEND MAIL to "+userEmail);
            sendUserUpdateEmail(userEmail, username, changeDescription);
        }

        // Trimitere SMS
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            sendUserUpdateSMS(phoneNumber, username, changeDescription);
        }
    }

    /**
     * Construiește conținutul email-ului
     */
    private String buildEmailContent(String username, String changeDescription) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Account Update</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .footer { padding: 15px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>FootwearChain</h1>
                        <h2>Account Update Notification</h2>
                    </div>
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Your account has been updated with the following changes:</p>
                        <div style="background-color: white; padding: 15px; border-left: 4px solid #2563eb; margin: 15px 0;">
                            <p><strong>Changes made:</strong> %s</p>
                        </div>
                        <p>If you have any questions about these changes, please contact our support team.</p>
                        <p>Best regards,<br>The FootwearChain Team</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message. Please do not reply to this email.</p>
                        <p>&copy; 2025 FootwearChain. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, username, changeDescription);
    }

    /**
     * Construiește conținutul SMS-ului
     */
    private String buildSMSContent(String username, String changeDescription) {
        return String.format(
                "Salut %s! Contul tau a fost schimbat.",
                username
        );
    }

    /**
     * Trimite email folosind EmailService
     */
    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            System.out.println("sendEmail");
            emailService.sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            log.error("Error sending email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Trimite SMS folosind SmsAdvertService
     */
    private void sendSMS(String phoneNumber, String message) {
        try {
            boolean success = smsAdvertService.sendSMS(phoneNumber, message);
            if (!success) {
                throw new RuntimeException("SMS sending failed");
            }
        } catch (Exception e) {
            log.error("Error sending SMS to: {}", phoneNumber, e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    /**
     * Generează descrierea modificărilor
     */
    public String generateChangeDescription(Map<String, Object> changes) {
        if (changes.isEmpty()) {
            return "Account information updated";
        }

        StringBuilder description = new StringBuilder();

        if (changes.containsKey("username")) {
            description.append("Username changed");
        }
        if (changes.containsKey("email")) {
            if (description.length() > 0) description.append(", ");
            description.append("Email updated");
        }
        if (changes.containsKey("phone")) {
            if (description.length() > 0) description.append(", ");
            description.append("Phone number updated");
        }
        if (changes.containsKey("role")) {
            if (description.length() > 0) description.append(", ");
            description.append("Role changed to ").append(changes.get("role"));
        }
        if (changes.containsKey("storeId")) {
            if (description.length() > 0) description.append(", ");
            description.append("Store assignment updated");
        }
        if (changes.containsKey("active")) {
            if (description.length() > 0) description.append(", ");
            description.append("Account status changed to ")
                    .append((Boolean) changes.get("active") ? "Active" : "Inactive");
        }
        if (changes.containsKey("password")) {
            if (description.length() > 0) description.append(", ");
            description.append("Password updated");
        }

        return description.toString();
    }
}