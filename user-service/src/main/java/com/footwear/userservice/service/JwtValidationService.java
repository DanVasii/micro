package com.footwear.productservice.service;

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

    public void validateManagerRole(String token) {
        Claims claims = validateToken(token);
        String role = claims.get("role", String.class);

        if (!"MANAGER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("Access denied - Manager role required");
        }
    }

    // Additional helper methods using singleton config
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }
}