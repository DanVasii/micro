// api-gateway/src/main/java/com/footwear/apigateway/factory/ResponseFactory.java
package com.footwear.apigateway.factory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Factory Method Pattern Implementation
 * Creates different types of HTTP responses based on the outcome
 */
public abstract class ResponseFactory {

    public abstract ResponseEntity<String> createResponse(String message, Object data);

    public static ResponseFactory getFactory(ResponseType type) {
        switch (type) {
            case SUCCESS:
                return new SuccessResponseFactory();
            case ERROR:
                return new ErrorResponseFactory();
            case VALIDATION_ERROR:
                return new ValidationErrorResponseFactory();
            default:
                return new SuccessResponseFactory();
        }
    }
}


// Success Response Factory
class SuccessResponseFactory extends ResponseFactory {
    @Override
    public ResponseEntity<String> createResponse(String message, Object data) {
        String jsonResponse = String.format(
                "{\"status\":\"success\",\"message\":\"%s\",\"data\":%s}",
                message,
                data != null ? data.toString() : "null"
        );
        return ResponseEntity.ok(jsonResponse);
    }
}

// Error Response Factory  
class ErrorResponseFactory extends ResponseFactory {
    @Override
    public ResponseEntity<String> createResponse(String message, Object data) {
        String jsonResponse = String.format(
                "{\"status\":\"error\",\"message\":\"%s\",\"error_code\":\"%s\"}",
                message,
                data != null ? data.toString() : "UNKNOWN_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonResponse);
    }
}

// Validation Error Response Factory
class ValidationErrorResponseFactory extends ResponseFactory {
    @Override
    public ResponseEntity<String> createResponse(String message, Object data) {
        String jsonResponse = String.format(
                "{\"status\":\"validation_error\",\"message\":\"%s\",\"field_errors\":%s}",
                message,
                data != null ? data.toString() : "[]"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResponse);
    }
}