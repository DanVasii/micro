package com.footwear.userservice.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String role; // ADMIN can set any role
    private Long storeId;
}