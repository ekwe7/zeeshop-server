package com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces;

import com.ekwe_hub.zeeshopserver.productInventory.entity.InventoryAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, UUID> {

    Page<InventoryAdjustment> findByProductId(UUID productId, Pageable pageable);
}
