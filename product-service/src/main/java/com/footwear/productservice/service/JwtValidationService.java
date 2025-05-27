package com.footwear.productservice.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import java.security.Key;

@Service
public class JwtValidationService {
    private static final String SECRET = "mySecretKeyForFootwearApplicationThatIsLongEnough";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public Claims validateToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
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
}
