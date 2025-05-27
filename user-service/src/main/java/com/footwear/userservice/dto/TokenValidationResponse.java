package com.footwear.userservice.dto;

import lombok.Data;

@Data
public class TokenValidationResponse {
    private boolean valid;
    private String username;
    private String role;
    private Long userId;
    private Long storeId;
}