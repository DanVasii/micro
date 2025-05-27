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

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id,
                                               @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(userService.getUserById(id, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    // Admin - Update user
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                              @RequestBody UpdateUserRequest request,
                                              @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, request, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    // Admin - Create user
    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request,
                                              @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(userService.createUser(request, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    // Admin - Delete user (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @RequestHeader("Authorization") String token) {
        try {
            userService.deleteUser(id, token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }

    // Admin - Reactivate user
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<UserDto> reactivateUser(@PathVariable Long id,
                                                  @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(userService.reactivateUser(id, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }
}
