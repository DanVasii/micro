package com.footwear.inventoryservice.service;

import com.footwear.inventoryservice.adapter.ExternalServiceAdapter;
import com.footwear.inventoryservice.adapter.NotificationType;
import com.footwear.inventoryservice.dto.*;
import com.footwear.inventoryservice.entity.Inventory;
import com.footwear.inventoryservice.entity.Store;
import com.footwear.inventoryservice.repository.InventoryRepository;
import com.footwear.inventoryservice.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private JwtValidationService jwtValidationService;

    @Autowired
    private ExternalServiceAdapter notificationService;

    // PUBLIC METHODS - pentru căutări fără autentificare

    public ProductAvailabilityDto getProductAvailability(Long productId) {
        List<Inventory> inventoryItems = inventoryRepository.findByProductId(productId);

        ProductAvailabilityDto availability = new ProductAvailabilityDto();
        availability.setProductId(productId);

        // Grupăm după magazin
        Map<Long, List<Inventory>> itemsByStore = inventoryItems.stream()
                .filter(item -> item.getQuantity() > 0)
                .collect(Collectors.groupingBy(Inventory::getStoreId));

        List<StoreAvailabilityDto> storeAvailabilities = itemsByStore.entrySet().stream()
                .map(entry -> {
                    Long storeId = entry.getKey();
                    List<Inventory> storeItems = entry.getValue();

                    Store store = storeRepository.findById(storeId)
                            .orElseThrow(() -> new RuntimeException("Store not found"));

                    StoreAvailabilityDto storeAvailability = new StoreAvailabilityDto();
                    storeAvailability.setStoreId(storeId);
                    storeAvailability.setStoreName(store.getName());
                    storeAvailability.setCity(store.getCity());

                    List<InventoryItemDto> items = storeItems.stream()
                            .map(this::convertToInventoryItemDto)
                            .collect(Collectors.toList());

                    storeAvailability.setAvailableItems(items);
                    return storeAvailability;
                })
                .collect(Collectors.toList());

        availability.setStores(storeAvailabilities);
        return availability;
    }

    public StockStatusDto checkStockStatus(StockCheckRequest request) {
        List<Inventory> items = inventoryRepository.checkAvailabilityAcrossStores(
                request.getProductId(), request.getColor(), request.getSize());

        StockStatusDto status = new StockStatusDto();
        status.setProductId(request.getProductId());
        status.setColor(request.getColor());
        status.setSize(request.getSize());
        status.setAvailable(!items.isEmpty());
        status.setTotalQuantity(items.stream().mapToInt(Inventory::getQuantity).sum());
        status.setStoreCount(items.size());

        return status;
    }

    // EMPLOYEE METHODS - pentru angajați

    public List<InventoryDto> getStoreInventory(Long storeId, String token) {
        jwtValidationService.validateEmployeeRole(token);

        // Verificăm dacă angajatul lucrează în acest magazin
        Long employeeStoreId = jwtValidationService.getStoreIdFromToken(token);
        String role = jwtValidationService.getRoleFromToken(token);

        if (!"MANAGER".equals(role) && !"ADMIN".equals(role) && !storeId.equals(employeeStoreId)) {
            throw new RuntimeException("Access denied - Can only view inventory for your store");
        }

        List<Inventory> items = inventoryRepository.findByStoreId(storeId);
        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<InventoryDto> getFilteredStoreInventory(Long storeId, Long productId,
                                                        String color, Integer size,
                                                        Integer minQuantity, String token) {
        jwtValidationService.validateEmployeeRole(token);

        // Verificăm dacă angajatul lucrează în acest magazin
        Long employeeStoreId = jwtValidationService.getStoreIdFromToken(token);
        String role = jwtValidationService.getRoleFromToken(token);

        if (!"MANAGER".equals(role) && !"ADMIN".equals(role) && !storeId.equals(employeeStoreId)) {
            throw new RuntimeException("Access denied - Can only view inventory for your store");
        }

        List<Inventory> items = inventoryRepository.findInventoryWithFilters(
                storeId, productId, color, size, minQuantity);

        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public InventoryDto searchProductInStore(Long productId, String color, Integer size,
                                             Long storeId, String token) {
        jwtValidationService.validateEmployeeRole(token);

        // Verificăm dacă angajatul lucrează în acest magazin
        Long employeeStoreId = jwtValidationService.getStoreIdFromToken(token);
        String role = jwtValidationService.getRoleFromToken(token);

        if (!"MANAGER".equals(role) && !"ADMIN".equals(role) && !storeId.equals(employeeStoreId)) {
            throw new RuntimeException("Access denied - Can only search in your store");
        }

        Inventory item = inventoryRepository.findByProductIdAndStoreIdAndColorAndSize(
                        productId, storeId, color, size)
                .orElse(null);

        return item != null ? convertToDto(item) : null;
    }

    @Transactional
    public InventoryDto processSale(SaleRequest request, String token) {
        jwtValidationService.validateEmployeeRole(token);

        // Verificăm dacă angajatul lucrează în acest magazin
        Long employeeStoreId = jwtValidationService.getStoreIdFromToken(token);
        String role = jwtValidationService.getRoleFromToken(token);

        if (!"MANAGER".equals(role) && !"ADMIN".equals(role) && !request.getStoreId().equals(employeeStoreId)) {
            throw new RuntimeException("Access denied - Can only process sales for your store");
        }

        Inventory item = inventoryRepository.findByProductIdAndStoreIdAndColorAndSize(
                        request.getProductId(), request.getStoreId(), request.getColor(), request.getSize())
                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));

        if (item.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + item.getQuantity());
        }

        // Process the sale
        item.setQuantity(item.getQuantity() - request.getQuantity());
        item = inventoryRepository.save(item);

        // Check for low stock and send notification using Adapter Pattern
        if (item.getQuantity() <= item.getMinStock()) {
            Store store = storeRepository.findById(item.getStoreId()).orElse(null);
            if (store != null) {
                String productInfo = String.format("Product ID: %d, Color: %s, Size: %d - Current stock: %d (Min: %d)",
                        item.getProductId(), item.getColor(), item.getSize(), item.getQuantity(), item.getMinStock());

                // Use adapter to send notification
                boolean notificationSent = notificationService.sendLowStockAlert("manager@" + store.getName().toLowerCase() + ".com", productInfo);

                if (notificationSent) {
                    System.out.println("Low stock notification sent successfully for store: " + store.getName());
                } else {
                    System.out.println("Failed to send low stock notification for store: " + store.getName());
                }
            }
        }

        return convertToDto(item);
    }

    @Transactional
    public InventoryDto updateInventory(UpdateInventoryRequest request, String token) {
        jwtValidationService.validateEmployeeRole(token);

        // Verificăm dacă angajatul lucrează în acest magazin
        Long employeeStoreId = jwtValidationService.getStoreIdFromToken(token);
        String role = jwtValidationService.getRoleFromToken(token);

        if (!"MANAGER".equals(role) && !"ADMIN".equals(role) && !request.getStoreId().equals(employeeStoreId)) {
            throw new RuntimeException("Access denied - Can only update inventory for your store");
        }

        Inventory item = inventoryRepository.findByProductIdAndStoreIdAndColorAndSize(
                        request.getProductId(), request.getStoreId(), request.getColor(), request.getSize())
                .orElse(new Inventory());

        // Store previous quantity for comparison
        Integer previousQuantity = item.getQuantity() != null ? item.getQuantity() : 0;

        item.setProductId(request.getProductId());
        item.setStoreId(request.getStoreId());
        item.setColor(request.getColor());
        item.setSize(request.getSize());
        item.setQuantity(request.getQuantity());
        item.setMinStock(request.getMinStock());

        item = inventoryRepository.save(item);

        // Send restock notification if stock was increased significantly
        if (request.getQuantity() > previousQuantity + 10) {
            Store store = storeRepository.findById(item.getStoreId()).orElse(null);
            if (store != null) {
                String restockInfo = String.format("Product ID: %d, Color: %s, Size: %d - Restocked from %d to %d units",
                        item.getProductId(), item.getColor(), item.getSize(), previousQuantity, item.getQuantity());

                notificationService.sendNotification("inventory@" + store.getName().toLowerCase() + ".com",
                        "Inventory restocked: " + restockInfo, NotificationType.EMAIL);
            }
        }

        return convertToDto(item);
    }

    @Transactional
    public InventoryDto createInventoryItem(CreateInventoryRequest request, String token) {
        jwtValidationService.validateEmployeeRole(token);

        // Verificăm dacă angajatul lucrează în acest magazin
        Long employeeStoreId = jwtValidationService.getStoreIdFromToken(token);
        String role = jwtValidationService.getRoleFromToken(token);

        if (!"MANAGER".equals(role) && !"ADMIN".equals(role) && !request.getStoreId().equals(employeeStoreId)) {
            throw new RuntimeException("Access denied - Can only create inventory for your store");
        }

        // Verificăm dacă articolul există deja
        if (inventoryRepository.findByProductIdAndStoreIdAndColorAndSize(
                request.getProductId(), request.getStoreId(), request.getColor(), request.getSize()).isPresent()) {
            throw new RuntimeException("Inventory item already exists");
        }

        Inventory item = new Inventory();
        item.setProductId(request.getProductId());
        item.setStoreId(request.getStoreId());
        item.setColor(request.getColor());
        item.setSize(request.getSize());
        item.setQuantity(request.getQuantity());
        item.setMinStock(request.getMinStock());

        item = inventoryRepository.save(item);

        // Send notification about new inventory item
        Store store = storeRepository.findById(item.getStoreId()).orElse(null);
        if (store != null) {
            String newItemInfo = String.format("New inventory item created - Product ID: %d, Color: %s, Size: %d, Quantity: %d",
                    item.getProductId(), item.getColor(), item.getSize(), item.getQuantity());

            notificationService.sendNotification("inventory@" + store.getName().toLowerCase() + ".com",
                    "New inventory item: " + newItemInfo, NotificationType.EMAIL);
        }

        return convertToDto(item);
    }

    public List<InventoryDto> getLowStockItems(Long storeId, String token) {
        jwtValidationService.validateEmployeeRole(token);

        // Verificăm dacă angajatul lucrează în acest magazin
        Long employeeStoreId = jwtValidationService.getStoreIdFromToken(token);
        String role = jwtValidationService.getRoleFromToken(token);

        if (!"MANAGER".equals(role) && !"ADMIN".equals(role) && !storeId.equals(employeeStoreId)) {
            throw new RuntimeException("Access denied - Can only view low stock for your store");
        }

        List<Inventory> items = inventoryRepository.findLowStockItemsByStore(storeId);

        // Send consolidated low stock alert if there are multiple items
        if (items.size() >= 3) {
            Store store = storeRepository.findById(storeId).orElse(null);
            if (store != null) {
                String alertMessage = String.format("Multiple low stock items detected in %s: %d items need attention",
                        store.getName(), items.size());
                notificationService.sendNotification("manager@" + store.getName().toLowerCase() + ".com",
                        alertMessage, NotificationType.EMAIL);
            }
        }

        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // MANAGER METHODS - pentru manageri

    public List<InventoryDto> getAllInventory(String token) {
        jwtValidationService.validateManagerRole(token);

        List<Inventory> items = inventoryRepository.findAll();
        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<InventoryDto> getAllLowStockItems(String token) {
        jwtValidationService.validateManagerRole(token);

        List<Inventory> items = inventoryRepository.findAllLowStockItems();

        // Send management report about overall low stock situation
        if (items.size() >= 10) {
            String managementAlert = String.format("Company-wide low stock alert: %d items across all stores need immediate attention", items.size());
            notificationService.sendNotification("management@company.com", managementAlert, NotificationType.EMAIL);
        }

        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // STORE METHODS

    public List<StoreDto> getAllStores() {
        List<Store> stores = storeRepository.findByActiveTrue();
        return stores.stream()
                .map(this::convertToStoreDto)
                .collect(Collectors.toList());
    }

    public StoreDto getStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
        return convertToStoreDto(store);
    }

    // HELPER METHODS

    private InventoryDto convertToDto(Inventory inventory) {
        InventoryDto dto = new InventoryDto();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProductId());
        dto.setStoreId(inventory.getStoreId());

        // Adăugăm numele magazinului dacă store-ul este încărcat
        if (inventory.getStore() != null) {
            dto.setStoreName(inventory.getStore().getName());
        } else {
            // Încărcăm store-ul dacă nu este deja încărcat
            Store store = storeRepository.findById(inventory.getStoreId()).orElse(null);
            dto.setStoreName(store != null ? store.getName() : "Unknown Store");
        }

        dto.setColor(inventory.getColor());
        dto.setSize(inventory.getSize());
        dto.setQuantity(inventory.getQuantity());
        dto.setMinStock(inventory.getMinStock());
        return dto;
    }

    private InventoryItemDto convertToInventoryItemDto(Inventory inventory) {
        InventoryItemDto dto = new InventoryItemDto();
        dto.setColor(inventory.getColor());
        dto.setSize(inventory.getSize());
        dto.setQuantity(inventory.getQuantity());
        dto.setMinStock(inventory.getMinStock());
        return dto;
    }

    private StoreDto convertToStoreDto(Store store) {
        StoreDto dto = new StoreDto();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setAddress(store.getAddress());
        dto.setCity(store.getCity());
        dto.setPhone(store.getPhone());
        dto.setEmail(store.getEmail());
        dto.setActive(store.isActive());
        return dto;
    }
}