package com.footwear.userservice.service;

import com.footwear.userservice.dto.*;
import com.footwear.userservice.entity.User;
import com.footwear.userservice.entity.UserRole;
import com.footwear.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        String token = jwtService.generateToken(user.getUsername(),
                user.getRole().name(),
                user.getId(),
                user.getStoreId());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRole(user.getRole().name());
        response.setUserId(user.getId());
        response.setStoreId(user.getStoreId());
        response.setUsername(user.getUsername());

        return response;
    }

    public UserDto register(RegisterRequest request) {
        // Verifică dacă username-ul există deja
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.CLIENT); // Doar clienți prin înregistrare publică

        user = userRepository.save(user);
        return convertToDto(user);
    }

    public TokenValidationResponse validateToken(String token) {
        TokenValidationResponse response = new TokenValidationResponse();

        try {
            var claims = jwtService.extractClaims(token);
            response.setValid(true);
            response.setUsername(claims.getSubject());
            response.setRole(claims.get("role", String.class));
            response.setUserId(claims.get("userId", Long.class));
            response.setStoreId(claims.get("storeId", Long.class));
        } catch (Exception e) {
            response.setValid(false);
        }

        return response;
    }

    public List<UserDto> getAllUsers(String token) {
        validateAdminRole(token);
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void validateAdminRole(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        var claims = jwtService.extractClaims(token);
        String role = claims.get("role", String.class);

        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Access denied - Admin role required");
        }
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setStoreId(user.getStoreId());
        dto.setActive(user.isActive());
        return dto;
    }
}
