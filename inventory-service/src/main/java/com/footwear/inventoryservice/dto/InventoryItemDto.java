package com.footwear.inventoryservice.dto;

import lombok.Data;

@Data
public class InventoryItemDto {
    private String color;
    private Integer size;
    private Integer quantity;
    private Integer minStock;
}
