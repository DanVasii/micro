package com.footwear.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Integer size;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "min_stock")
    private Integer minStock = 0;

    // Relationarea cu Store
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", insertable = false, updatable = false)
    private Store store;
}