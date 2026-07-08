package com.ekwe_hub.zeeshopserver.productinventory.repository.interfaces;

import com.ekwe_hub.zeeshopserver.productinventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, ProductRepositoryCustom {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, UUID id);

    boolean existsByCategoryId(UUID categoryId);

    boolean existsByUnitId(UUID unitId);
}
