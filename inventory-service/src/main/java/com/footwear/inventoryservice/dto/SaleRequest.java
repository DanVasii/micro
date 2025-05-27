package com.footwear.inventoryservice.dto;

import lombok.Data;

@Data
public class SaleRequest {
    private Long productId;
    private Long storeId;
    private String color;
    private Integer size;
    private Integer quantity;
}

