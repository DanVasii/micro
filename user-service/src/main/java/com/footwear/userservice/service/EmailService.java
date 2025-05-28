// user-service/src/main/java/com/footwear/userservice/service/EmailService.java
package com.footwear.userservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.from.email:noreply@footwearchain.com}")
    private String fromEmail;

    @Value("${mail.from.name:FootwearChain}")
    private String fromName;

    /**
     * Trimite email HTML
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content

            mailSender.send(message);
            System.out.println("sendHtmlEmail");
            log.info("HTML email sent successfully to: {}", to);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Trimite email text simplu
     */
    public void sendTextEmail(String to, String subject, String textContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, false); // false = plain text

            mailSender.send(message);
            log.info("Text email sent successfully to: {}", to);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send text email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}