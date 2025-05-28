package com.footwear.userservice.service;

import com.footwear.userservice.config.JwtConfigurationManager;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final JwtConfigurationManager config;

    public JwtService() {
        this.config = JwtConfigurationManager.getInstance();
    }

    public String generateToken(String username, String role, Long userId, Long storeId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        if (storeId != null) {
            claims.put("storeId", storeId);
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + config.getExpirationTime()))
                .signWith(config.getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(config.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Additional method to check configuration health
    public boolean isConfigurationHealthy() {
        return config.isConfigurationValid();
    }

    // Get token expiration info for debugging
    public String getTokenInfo() {
        return String.format("JWT Config - Expiration: %d hours, Key length: %d",
                config.getExpirationInHours(),
                config.getSecretKey().length());
    }
}