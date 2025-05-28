package com.footwear.userservice.strategy;

// Strategy interface
public interface UserValidationStrategy {
    ValidationResult validate(Object userRequest);
}

