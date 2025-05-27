// user-service/src/main/java/com/footwear/userservice/dto/UpdateUserRequest.java
package com.footwear.userservice.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String email;
    private String phone;
    private String role;
    private Long storeId;
    private Boolean active;
    private String password;
}
