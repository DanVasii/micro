// user-service/src/main/java/com/footwear/userservice/config/JwtConfigurationManager.java
package com.footwear.userservice.config;

import io.jsonwebtoken.security.Keys;
import java.security.Key;

/**
 * Singleton Pattern Implementation
 * Manages JWT configuration globally across the application
 * Thread-safe implementation using double-checked locking
 */
public class JwtConfigurationManager {

    private static volatile JwtConfigurationManager instance;
    private static final Object lock = new Object();

    private final String secretKey;
    private final long expirationTime;
    private final Key signingKey;

    // Private constructor to prevent instantiation
    private JwtConfigurationManager() {
        this.secretKey = "mySecretKeyForFootwearApplicationThatIsLongEnough";
        this.expirationTime = 86400000; // 24 hours
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Thread-safe singleton instance getter
    public static JwtConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new JwtConfigurationManager();
                }
            }
        }
        return instance;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public Key getSigningKey() {
        return signingKey;
    }

    // Configuration validation
    public boolean isConfigurationValid() {
        return secretKey != null &&
                secretKey.length() >= 32 &&
                expirationTime > 0;
    }

    // Get token expiration in hours for display
    public long getExpirationInHours() {
        return expirationTime / (1000 * 60 * 60);
    }
}