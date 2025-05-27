package com.footwear.productservice.repository;

import com.footwear.productservice.entity.Product;
import com.footwear.productservice.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findByCategory(ProductCategory category);
    List<Product> findByManufacturer(String manufacturer);
    List<Product> findByModelContainingIgnoreCase(String model);

    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:manufacturer IS NULL OR LOWER(p.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) " +
            "AND (:minPrice IS NULL OR p.salePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.salePrice <= :maxPrice)")
    List<Product> findProductsWithFilters(@Param("category") ProductCategory category,
                                          @Param("manufacturer") String manufacturer,
                                          @Param("minPrice") BigDecimal minPrice,
                                          @Param("maxPrice") BigDecimal maxPrice);
}