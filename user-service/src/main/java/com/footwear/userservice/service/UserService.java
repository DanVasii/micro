package com.footwear.userservice.service;

import com.footwear.userservice.dto.*;
import com.footwear.userservice.entity.User;
import com.footwear.userservice.entity.UserRole;
import com.footwear.userservice.repository.UserRepository;
import com.footwear.userservice.strategy.UserValidationContext;
import com.footwear.userservice.strategy.ValidationResult;
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

    @Autowired
    private UserValidationContext validationContext;

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
        // Use Strategy Pattern for validation
        ValidationResult validation = validationContext.validateUserRegistration(request);
        if (!validation.isValid()) {
            throw new RuntimeException("Validation failed: " + String.join(", ", validation.getErrors()));
        }

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

    public UserDto getUserById(Long id, String token) {
        validateAdminRole(token);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }

    public UserDto updateUser(Long id, UpdateUserRequest request, String token) {
        validateAdminRole(token);

        // Use Strategy Pattern for validation
        ValidationResult validation = validationContext.validateEmployeeUpdate(request);
        if (!validation.isValid()) {
            throw new RuntimeException("Validation failed: " + String.join(", ", validation.getErrors()));
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields if provided
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            // Check if username already exists for another user
            userRepository.findByUsername(request.getUsername())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new RuntimeException("Username already exists");
                        }
                    });
            user.setUsername(request.getUsername().trim());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail().trim());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone().trim());
        }

        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            try {
                UserRole role = UserRole.valueOf(request.getRole().toUpperCase());
                user.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + request.getRole());
            }
        }

        if (request.getStoreId() != null) {
            user.setStoreId(request.getStoreId());
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);
        return convertToDto(user);
    }

    public UserDto createUser(CreateUserRequest request, String token) {
        validateAdminRole(token);

        // Use Strategy Pattern for validation
        ValidationResult validation = validationContext.validateAdminUserCreation(request);
        if (!validation.isValid()) {
            throw new RuntimeException("Validation failed: " + String.join(", ", validation.getErrors()));
        }

        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername().trim()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);

        // Set role (default to CLIENT if not specified)
        UserRole role = UserRole.CLIENT;
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            try {
                role = UserRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + request.getRole());
            }
        }
        user.setRole(role);

        user.setStoreId(request.getStoreId());
        user.setActive(true);

        user = userRepository.save(user);
        return convertToDto(user);
    }

    public void deleteUser(Long id, String token) {
        validateAdminRole(token);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Soft delete - just set active to false
        user.setActive(false);
        userRepository.save(user);
    }

    public UserDto reactivateUser(Long id, String token) {
        validateAdminRole(token);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(true);
        user = userRepository.save(user);
        return convertToDto(user);
    }

    // Additional method to demonstrate strategy pattern usage
    public ValidationResult validateUserData(Object userRequest, String operationType) {
        return validationContext.getValidationStrategy(operationType).validate(userRequest);
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