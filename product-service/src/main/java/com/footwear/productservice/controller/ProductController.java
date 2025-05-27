package com.footwear.productservice.controller;

import com.footwear.productservice.dto.*;
import com.footwear.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Test endpoints
    @GetMapping("/test")
    public String test() {
        return "Product Service is running!";
    }

    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"UP\",\"service\":\"product-service\"}";
    }

    // Public endpoints (no authentication required)
    @GetMapping("/public")
    public ResponseEntity<List<ProductDto>> getAllProductsPublic(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(productService.getAllProductsPublic(category));
    }

    @GetMapping("/public/search")
    public ResponseEntity<List<ProductDto>> searchProductsPublic(
            @RequestParam String model) {
        return ResponseEntity.ok(productService.searchProductsByModel(model));
    }

    // Authenticated endpoints
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String priceMin,
            @RequestParam(required = false) String priceMax,
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(productService.getFilteredProducts(category, manufacturer, priceMin, priceMax, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Manager operations
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody CreateProductRequest request,
                                                    @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(productService.createProduct(request, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id,
                                                    @RequestBody UpdateProductRequest request,
                                                    @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(productService.updateProduct(id, request, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id,
                                              @RequestHeader("Authorization") String token) {
        try {
            productService.deleteProduct(id, token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }
}
