package com.footwear.userservice.strategy;

import java.util.ArrayList;
import java.util.List;

// Validation result class
public class ValidationResult {
    private boolean valid;
    private List<String> errors;

    public ValidationResult(boolean valid) {
        this.valid = valid;
        this.errors = new ArrayList<>();
    }

    public ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors;
    }

    public boolean isValid() { return valid; }
    public List<String> getErrors() { return errors; }

    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }
}
