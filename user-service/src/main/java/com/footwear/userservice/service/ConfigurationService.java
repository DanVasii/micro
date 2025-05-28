package com.footwear.userservice.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Singleton Pattern Implementation
 * Provides global configuration management across the application
 */
@Service
@Slf4j
public class ConfigurationService {

    private static volatile ConfigurationService instance;
    private final Map<String, String> configurations;

    // Private constructor to prevent instantiation
    private ConfigurationService() {
        this.configurations = new ConcurrentHashMap<>();
        loadDefaultConfigurations();
        log.info("ConfigurationService singleton instance created");
    }

    /**
     * Thread-safe singleton instance retrieval using double-checked locking
     */
    public static ConfigurationService getInstance() {
        if (instance == null) {
            synchronized (ConfigurationService.class) {
                if (instance == null) {
                    instance = new ConfigurationService();
                }
            }
        }
        return instance;
    }

    private void loadDefaultConfigurations() {
        // Application configurations
        configurations.put("app.name", "FootwearChain");
        configurations.put("app.version", "1.0.0");
        configurations.put("max.products.per.page", "20");
        configurations.put("default.currency", "RON");
        configurations.put("session.timeout.minutes", "30");
        configurations.put("inventory.low.stock.threshold", "5");

        // Email configurations
        configurations.put("email.smtp.host", "smtp.hostinger.com");
        configurations.put("email.smtp.port", "465");
        configurations.put("email.from.name", "FootwearChain Support");

        // SMS configurations
        configurations.put("sms.api.enabled", "true");
        configurations.put("sms.api.timeout.seconds", "30");

        log.info("Loaded {} default configurations", configurations.size());
    }


    /**
     * Get configuration value with default fallback
     */
    public String getConfiguration(String key, String defaultValue) {
        return configurations.getOrDefault(key, defaultValue);
    }

    /**
     * Get configuration as integer
     */
    public int getConfigurationAsInt(String key, int defaultValue) {
        try {
            String value = configurations.get(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("Invalid integer configuration for key: {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }



}