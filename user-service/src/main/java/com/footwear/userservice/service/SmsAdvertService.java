// user-service/src/main/java/com/footwear/userservice/service/SmsAdvertService.java
package com.footwear.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SmsAdvertService {

    private final RestTemplate restTemplate;

    @Value("${sms.api.url:https://www.smsadvert.ro/api}")
    private String apiUrl;

    @Value("${sms.api.key:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2M2ZiYWUyMzhmODYyMDAwMDJiMDM2NzcifQ.TWTpn8QkKPwoLgPbJFK6C7QU3DAvEGWSOW4EhMLWfcQ}")
    private String apiKey;

    public SmsAdvertService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Trimite SMS folosind SmsAdvert API
     * API Documentation: https://www.smsadvert.ro/api/sms/
     */
    public boolean sendSMS(String phoneNumber, String message) {
        try {
            // Formatează numărul de telefon
            String formattedPhone = formatPhoneNumber(phoneNumber);

            if (formattedPhone == null) {
                log.warn("Invalid phone number: {}", phoneNumber);
                return false;
            }

                    String escapedPhone = formattedPhone.replace("\"", "\\\"");
            String escapedMessage = message.replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\\", "\\\\");

            // Hardcode JSON body as string
            String jsonBody = String.format(
                    "{\"phone\":\"%s\",\"shortTextMessage\":\"%s\"}",
                    escapedPhone,
                    escapedMessage
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            System.out.println(apiKey);
            headers.set("Authorization", apiKey);
            // Remove duplicate Content-Type header

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            String endpoint = apiUrl + "/sms/";  // Endpoint corect
            log.info("Sending SMS to {} via SmsAdvert API", formattedPhone);
            log.debug("SMS API URL: {}", endpoint);
            log.debug("SMS Request body: {}", jsonBody);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    request,
                    String.class
            );


            log.info("SmsAdvert API response status: {}", response.getStatusCode());
            log.debug("SmsAdvert API response body: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent successfully to: {}", formattedPhone);
                return true;
            } else {
                log.error("SmsAdvert API returned error status: {} for phone: {}",
                        response.getStatusCode(), formattedPhone);
                log.error("Response body: {}", response.getBody());
                return false;
            }

        } catch (Exception e) {
            log.error("Error sending SMS to: {} via SmsAdvert API", formatPhoneNumber(phoneNumber), e);
            return false;
        }
    }

    /**
     * Verifică statusul API-ului SmsAdvert
     */
    public boolean checkApiStatus() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKey);

            HttpEntity<String> request = new HttpEntity<>(headers);

            // Test cu un endpoint simplu sau ping
            String endpoint = apiUrl + "/ping"; // Sau orice endpoint de status

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Error checking SmsAdvert API status", e);
            return false;
        }
    }

    /**
     * Testează API-ul cu un SMS de test
     */
    public Map<String, Object> testApi(String testPhoneNumber) {
        Map<String, Object> result = new HashMap<>();

        try {
            String testMessage = "Test message from FootwearChain - " + java.time.LocalDateTime.now();
            boolean success = sendSMS(testPhoneNumber, testMessage);

            result.put("success", success);
            result.put("phone", testPhoneNumber);
            result.put("message", testMessage);
            result.put("timestamp", System.currentTimeMillis());

            if (success) {
                result.put("status", "SMS sent successfully");
            } else {
                result.put("status", "SMS sending failed");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }

        return result;
    }

    /**
     * Formatează numărul de telefon pentru România
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }

        // Elimină spațiile și caracterele speciale
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");

        if (cleaned.isEmpty()) {
            return null;
        }

        // Dacă începe cu 0, înlocuiește cu +40
        if (cleaned.startsWith("0")) {
            cleaned = "+40" + cleaned.substring(1);
        }

        // Dacă începe cu 40, adaugă +
        if (cleaned.startsWith("40") && !cleaned.startsWith("+40")) {
            cleaned = "+" + cleaned;
        }

        // Dacă nu are prefix și are 10 cifre, presupunem că e număr românesc
        if (!cleaned.startsWith("+") && cleaned.length() == 10) {
            cleaned = "+40" + cleaned;
        }

        // Validează lungimea finală (ar trebui să fie +40 + 9 cifre = 12 caractere)
        if (cleaned.startsWith("+40") && cleaned.length() != 12) {
            log.warn("Invalid Romanian phone number length: {}", cleaned);
            return null;
        }

        return cleaned;
    }

    /**
     * Validează dacă numărul de telefon este valid
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        String formatted = formatPhoneNumber(phoneNumber);
        return formatted != null;
    }

    /**
     * Returnează informații despre configurația API
     */
    public Map<String, Object> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("apiUrl", apiUrl);
        info.put("hasApiKey", apiKey != null && !apiKey.trim().isEmpty() && !"your-api-key".equals(apiKey));
        info.put("timestamp", System.currentTimeMillis());
        return info;
    }
}