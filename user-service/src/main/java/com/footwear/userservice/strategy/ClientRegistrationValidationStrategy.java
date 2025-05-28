package com.footwear.userservice.strategy;

import com.footwear.userservice.dto.RegisterRequest;

import java.util.List;
import java.util.regex.Pattern;

// Strategy for user registration (public)
public class ClientRegistrationValidationStrategy implements UserValidationStrategy {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9]{10,15}$");

    @Override
    public ValidationResult validate(Object userRequest) {
        if (!(userRequest instanceof RegisterRequest)) {
            return new ValidationResult(false, List.of("Invalid request type for client registration"));
        }

        RegisterRequest request = (RegisterRequest) userRequest;
        ValidationResult result = new ValidationResult(true);

        // Username validation
        if (request.getUsername() == null || request.getUsername().trim().length() < 3) {
            result.addError("Username must be at least 3 characters long");
        }

        // Password validation
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            result.addError("Password must be at least 6 characters long");
        }

        if (request.getPassword() != null && !containsDigit(request.getPassword())) {
            result.addError("Password must contain at least one digit");
        }

        // Email validation
        if (request.getEmail() == null || !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            result.addError("Valid email address is required");
        }

        // Phone validation (optional but if provided must be valid)
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(request.getPhone().replaceAll("[\\s-()]", "")).matches()) {
                result.addError("Phone number format is invalid");
            }
        }

        return result;
    }

    private boolean containsDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }
}

