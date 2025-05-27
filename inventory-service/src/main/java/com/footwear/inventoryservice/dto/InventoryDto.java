package com.footwear.inventoryservice.dto;

import lombok.Data;

@Data
public class InventoryDto {
    private Long id;
    private Long productId;
    private Long storeId;
    private String storeName;
    private String color;
    private Integer size;
    private Integer quantity;
    private Integer minStock;
}
