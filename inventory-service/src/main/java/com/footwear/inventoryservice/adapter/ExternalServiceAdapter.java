// inventory-service/src/main/java/com/footwear/inventoryservice/adapter/ExternalServiceAdapter.java
package com.footwear.inventoryservice.adapter;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Adapter Pattern Implementation
 * Adapts external service responses to our internal format
 */

// Target interface - what our application expects
interface NotificationService {
    boolean sendNotification(String recipient, String message, NotificationType type);
    boolean sendLowStockAlert(String storeManager, String productInfo);
}


// Adaptee - External Email Service with different interface
class ExternalEmailService {
    public void sendEmail(String to, String subject, String body) {
        // Simulate external email service
        System.out.println("External Email Service:");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }

    public boolean isEmailValid(String email) {
        return email != null && email.contains("@");
    }
}

// Adaptee - External SMS Service with different interface  
class ExternalSMSGateway {
    private RestTemplate restTemplate = new RestTemplate();

    public String sendSMS(String phoneNumber, String text) {
        // Simulate external SMS gateway
        System.out.println("SMS Gateway:");
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Message: " + text);
        return "SMS_SENT_OK";
    }
}

// Adapter - Adapts external services to our NotificationService interface
@Component
public class ExternalServiceAdapter implements NotificationService {

    private final ExternalEmailService emailService;
    private final ExternalSMSGateway smsGateway;

    public ExternalServiceAdapter() {
        this.emailService = new ExternalEmailService();
        this.smsGateway = new ExternalSMSGateway();
    }

    @Override
    public boolean sendNotification(String recipient, String message, NotificationType type) {
        try {
            switch (type) {
                case EMAIL:
                    if (emailService.isEmailValid(recipient)) {
                        emailService.sendEmail(recipient, "Inventory Notification", message);
                        return true;
                    }
                    return false;

                case SMS:
                    String result = smsGateway.sendSMS(recipient, message);
                    return "SMS_SENT_OK".equals(result);

                case PUSH:
                    // Could adapt another push notification service here
                    System.out.println("Push notification: " + message + " to " + recipient);
                    return true;

                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendLowStockAlert(String storeManager, String productInfo) {
        String alertMessage = String.format(
                "LOW STOCK ALERT: %s requires immediate attention. Please restock as soon as possible.",
                productInfo
        );

        // Try multiple notification methods for critical alerts
        boolean emailSent = sendNotification(storeManager, alertMessage, NotificationType.EMAIL);
        boolean smsSent = sendNotification(storeManager.replaceAll("@.*", ""), alertMessage, NotificationType.SMS);

        return emailSent || smsSent;
    }
}