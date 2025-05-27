package com.footwear.inventoryservice.dto;

import lombok.Data;

@Data
public class StockCheckRequest {
    private Long productId;
    private String color;
    private Integer size;
}
