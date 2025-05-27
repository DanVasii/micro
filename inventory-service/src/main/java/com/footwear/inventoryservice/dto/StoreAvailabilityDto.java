package com.footwear.inventoryservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class StoreAvailabilityDto {
    private Long storeId;
    private String storeName;
    private String city;
    private List<InventoryItemDto> availableItems;
}
