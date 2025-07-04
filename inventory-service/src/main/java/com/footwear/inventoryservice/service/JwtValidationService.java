package com.footwear.inventoryservice.service;

import com.footwear.userservice.config.JwtConfigurationManager;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class JwtValidationService {

    private final JwtConfigurationManager config;

    public JwtValidationService() {
        this.config = JwtConfigurationManager.getInstance();
    }

    public Claims validateToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return Jwts.parserBuilder()
                .setSigningKey(config.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void validateEmployeeRole(String token) {
        Claims claims = validateToken(token);
        String role = claims.get("role", String.class);

        if (!"EMPLOYEE".equals(role) && !"MANAGER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("Access denied - Employee role required");
        }
    }

    public void validateManagerRole(String token) {
        Claims claims = validateToken(token);
        String role = claims.get("role", String.class);

        if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("Access denied - Manager role required");
        }
    }

    public Long getStoreIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("storeId", Long.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("role", String.class);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("userId", Long.class);
    }

    // Additional helper methods
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // Method to get token expiration for logging/debugging
    public Date getTokenExpiration(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }
}