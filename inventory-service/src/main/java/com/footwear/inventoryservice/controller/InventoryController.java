package com.footwear.inventoryservice.controller;

import com.footwear.inventoryservice.dto.*;
import com.footwear.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // Test endpoints
    @GetMapping("/test")
    public String test() {
        return "Inventory Service is running!";
    }

    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"UP\",\"service\":\"inventory-service\"}";
    }

    // PUBLIC ENDPOINTS - fără autentificare

    @GetMapping("/public/product/{productId}/availability")
    public ResponseEntity<ProductAvailabilityDto> getProductAvailability(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductAvailability(productId));
    }

    @PostMapping("/public/stock/check")
    public ResponseEntity<StockStatusDto> checkStockStatus(@RequestBody StockCheckRequest request) {
        return ResponseEntity.ok(inventoryService.checkStockStatus(request));
    }

    @GetMapping("/public/stores")
    public ResponseEntity<List<StoreDto>> getAllStores() {
        return ResponseEntity.ok(inventoryService.getAllStores());
    }

    @GetMapping("/public/stores/{storeId}")
    public ResponseEntity<StoreDto> getStore(@PathVariable Long storeId) {
        try {
            return ResponseEntity.ok(inventoryService.getStore(storeId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // EMPLOYEE ENDPOINTS - necesită autentificare ca angajat

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<InventoryDto>> getStoreInventory(
            @PathVariable Long storeId,
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(inventoryService.getStoreInventory(storeId, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @GetMapping("/store/{storeId}/filtered")
    public ResponseEntity<List<InventoryDto>> getFilteredStoreInventory(
            @PathVariable Long storeId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer minQuantity,
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(inventoryService.getFilteredStoreInventory(
                    storeId, productId, color, size, minQuantity, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @GetMapping("/store/{storeId}/search")
    public ResponseEntity<InventoryDto> searchProductInStore(
            @PathVariable Long storeId,
            @RequestParam Long productId,
            @RequestParam String color,
            @RequestParam Integer size,
            @RequestHeader("Authorization") String token) {
        try {
            InventoryDto result = inventoryService.searchProductInStore(productId, color, size, storeId, token);
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @PostMapping("/sale")
    public ResponseEntity<InventoryDto> processSale(
            @RequestBody SaleRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(inventoryService.processSale(request, token));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<InventoryDto> updateInventory(
            @RequestBody UpdateInventoryRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(inventoryService.updateInventory(request, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<InventoryDto> createInventoryItem(
            @RequestBody CreateInventoryRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(inventoryService.createInventoryItem(request, token));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @GetMapping("/store/{storeId}/low-stock")
    public ResponseEntity<List<InventoryDto>> getLowStockItems(
            @PathVariable Long storeId,
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(inventoryService.getLowStockItems(storeId, token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    // MANAGER ENDPOINTS - necesită autentificare ca manager

    @GetMapping("/all")
    public ResponseEntity<List<InventoryDto>> getAllInventory(
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(inventoryService.getAllInventory(token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDto>> getAllLowStockItems(
            @RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(inventoryService.getAllLowStockItems(token));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(null);
        }
    }
}