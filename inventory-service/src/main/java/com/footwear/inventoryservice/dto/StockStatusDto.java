package com.footwear.inventoryservice.dto;

import lombok.Data;

@Data
public class StockStatusDto {
    private Long productId;
    private String color;
    private Integer size;
    private boolean available;
    private Integer totalQuantity;
    private Integer storeCount;
}