package com.footwear.inventoryservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductAvailabilityDto {
    private Long productId;
    private String productModel;
    private List<StoreAvailabilityDto> stores;
}

