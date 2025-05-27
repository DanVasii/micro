package com.footwear.inventoryservice.dto;

import lombok.Data;

@Data
public class StoreDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String phone;
    private String email;
    private boolean active;
}
