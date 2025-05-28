// user-service/src/main/java/com/footwear/userservice/config/MailConfig.java
package com.footwear.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host:smtp.hostinger.com}")
    private String host;

    @Value("${spring.mail.port:465}")
    private int port;

    @Value("${spring.mail.username:dan@web-dev.dev}")
    private String username;

    @Value("${spring.mail.password:u!Z2E1~rc}")
    private String password;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        if (port == 465) {
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");           // SSL în loc de STARTTLS
            props.put("mail.smtp.ssl.trust", host);              // Trust host-ul
            props.put("mail.smtp.socketFactory.port", "465");    // Socket factory port
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.debug", "true");                     // Pentru debugging

            // Timeout settings
            props.put("mail.smtp.connectiontimeout", "10000");   // 10 secunde
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");
        }

        return mailSender;
    }
}