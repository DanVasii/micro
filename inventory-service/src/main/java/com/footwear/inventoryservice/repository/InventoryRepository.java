package com.footwear.inventoryservice.repository;

import com.footwear.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Găsește inventory pentru un produs specific în toate magazinele
    List<Inventory> findByProductId(Long productId);

    // Găsește inventory pentru un magazin specific
    List<Inventory> findByStoreId(Long storeId);

    // Găsește inventory pentru un produs în un magazin specific
    List<Inventory> findByProductIdAndStoreId(Long productId, Long storeId);

    // Găsește un item specific de inventory
    Optional<Inventory> findByProductIdAndStoreIdAndColorAndSize(
            Long productId, Long storeId, String color, Integer size);

    // Găsește toate articolele cu stoc mic
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minStock AND i.storeId = :storeId")
    List<Inventory> findLowStockItemsByStore(@Param("storeId") Long storeId);

    // Găsește toate articolele cu stoc mic din toate magazinele
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.minStock")
    List<Inventory> findAllLowStockItems();

    // Găsește toate articolele disponibile pentru un produs și culoare
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId AND i.color = :color AND i.quantity > 0")
    List<Inventory> findAvailableByProductIdAndColor(@Param("productId") Long productId, @Param("color") String color);

    // Găsește articolele disponibile într-un magazin cu filtrare
    @Query("SELECT i FROM Inventory i WHERE i.storeId = :storeId " +
            "AND (:productId IS NULL OR i.productId = :productId) " +
            "AND (:color IS NULL OR i.color = :color) " +
            "AND (:size IS NULL OR i.size = :size) " +
            "AND (:minQuantity IS NULL OR i.quantity >= :minQuantity)")
    List<Inventory> findInventoryWithFilters(@Param("storeId") Long storeId,
                                             @Param("productId") Long productId,
                                             @Param("color") String color,
                                             @Param("size") Integer size,
                                             @Param("minQuantity") Integer minQuantity);

    // Verifică disponibilitatea unui produs în toate magazinele
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId " +
            "AND i.color = :color AND i.size = :size AND i.quantity > 0")
    List<Inventory> checkAvailabilityAcrossStores(@Param("productId") Long productId,
                                                  @Param("color") String color,
                                                  @Param("size") Integer size);
}
