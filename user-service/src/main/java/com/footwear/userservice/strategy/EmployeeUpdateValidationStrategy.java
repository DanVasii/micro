package com.footwear.userservice.strategy;

// Strategy for employee profile updates
public class EmployeeUpdateValidationStrategy implements UserValidationStrategy {
    @Override
    public ValidationResult validate(Object userRequest) {
        // This could be an UpdateUserRequest or similar
        ValidationResult result = new ValidationResult(true);

        // Less strict validation for updates
        // Only validate fields that are being changed

        return result;
    }
}
