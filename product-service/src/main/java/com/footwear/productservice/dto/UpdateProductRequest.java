package com.footwear.productservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductRequest {
    private String model;
    private String manufacturer;
    private String category;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private List<String> imageUrls;
    private List<String> availableColors;
    private String description;
    private Boolean active;
}
