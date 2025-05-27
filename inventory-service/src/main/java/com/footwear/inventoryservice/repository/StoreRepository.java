package com.footwear.inventoryservice.repository;

import com.footwear.inventoryservice.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByActiveTrue();
    List<Store> findByCity(String city);
}