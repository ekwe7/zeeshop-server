package com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * JpaSpecificationExecutor adds findAll(Specification, Pageable) — the query
 * that powers product search/filtering (see ProductSpecifications in
 * repository/impl for how the filter itself is built). No custom fragment
 * or hand-written implementation is needed for that: it's a standard
 * Spring Data interface, implemented by the framework the same way
 * JpaRepository's own methods are.
 */
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, UUID id);

    boolean existsByCategoryId(UUID categoryId);

    boolean existsByUnitId(UUID unitId);
}
