package com.footwear.userservice.controller;

import com.footwear.userservice.dto.*;
import com.footwear.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // Test endpoint
    @GetMapping("/test")
    public String test() {
        return "User Service is running!";
    }

    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"UP\",\"service\":\"user-service\"}";
    }

    // Autentificare
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(userService.login(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Înregistrare
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(userService.register(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Validare token
    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestBody TokenValidationRequest request) {
        return ResponseEntity.ok(userService.validateToken(request.getToken()));
    }

    // Admin - listă utilizatori
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(userService.getAllUsers(token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }
}
