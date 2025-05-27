package com.footwear.productservice.service;

import com.footwear.productservice.dto.*;
import com.footwear.productservice.entity.Product;
import com.footwear.productservice.entity.ProductCategory;
import com.footwear.productservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtValidationService jwtValidationService;

    // Public endpoints (no authentication required)
    public List<ProductDto> getAllProductsPublic(String category) {
        List<Product> products;
        if (category != null) {
            try {
                ProductCategory categoryEnum = ProductCategory.valueOf(category.toUpperCase());
                products = productRepository.findByCategory(categoryEnum);
            } catch (IllegalArgumentException e) {
                products = productRepository.findByActiveTrue();
            }
        } else {
            products = productRepository.findByActiveTrue();
        }

        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> searchProductsByModel(String model) {
        List<Product> products = productRepository.findByModelContainingIgnoreCase(model);
        return products.stream()
                .filter(Product::isActive)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Authenticated endpoints
    public List<ProductDto> getFilteredProducts(String category, String manufacturer,
                                                String priceMin, String priceMax, String token) {
        jwtValidationService.validateToken(token);

        ProductCategory categoryEnum = null;
        if (category != null) {
            try {
                categoryEnum = ProductCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid category, ignore
            }
        }

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        try {
            if (priceMin != null) minPrice = new BigDecimal(priceMin);
            if (priceMax != null) maxPrice = new BigDecimal(priceMax);
        } catch (NumberFormatException e) {
            // Invalid price format, ignore
        }

        List<Product> products = productRepository.findProductsWithFilters(
                categoryEnum, manufacturer, minPrice, maxPrice);

        return products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDto(product);
    }

    // Manager operations
    public ProductDto createProduct(CreateProductRequest request, String token) {
        jwtValidationService.validateManagerRole(token);

        Product product = new Product();
        product.setModel(request.getModel());
        product.setManufacturer(request.getManufacturer());
        product.setCategory(ProductCategory.valueOf(request.getCategory().toUpperCase()));
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setImageUrls(request.getImageUrls());
        product.setAvailableColors(request.getAvailableColors());
        product.setDescription(request.getDescription());

        product = productRepository.save(product);
        return convertToDto(product);
    }

    public ProductDto updateProduct(Long id, UpdateProductRequest request, String token) {
        jwtValidationService.validateManagerRole(token);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (request.getModel() != null) product.setModel(request.getModel());
        if (request.getManufacturer() != null) product.setManufacturer(request.getManufacturer());
        if (request.getCategory() != null) {
            product.setCategory(ProductCategory.valueOf(request.getCategory().toUpperCase()));
        }
        if (request.getPurchasePrice() != null) product.setPurchasePrice(request.getPurchasePrice());
        if (request.getSalePrice() != null) product.setSalePrice(request.getSalePrice());
        if (request.getImageUrls() != null) product.setImageUrls(request.getImageUrls());
        if (request.getAvailableColors() != null) product.setAvailableColors(request.getAvailableColors());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getActive() != null) product.setActive(request.getActive());

        product = productRepository.save(product);
        return convertToDto(product);
    }

    public void deleteProduct(Long id, String token) {
        jwtValidationService.validateManagerRole(token);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setActive(false); // Soft delete
        productRepository.save(product);
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setModel(product.getModel());
        dto.setManufacturer(product.getManufacturer());
        dto.setCategory(product.getCategory().name());
        dto.setPurchasePrice(product.getPurchasePrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setImageUrls(product.getImageUrls());
        dto.setAvailableColors(product.getAvailableColors());
        dto.setDescription(product.getDescription());
        dto.setActive(product.isActive());
        return dto;
    }
}
