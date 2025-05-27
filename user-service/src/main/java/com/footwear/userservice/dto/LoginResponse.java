package com.footwear.userservice.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String role;
    private Long userId;
    private Long storeId;
    private String username;
}