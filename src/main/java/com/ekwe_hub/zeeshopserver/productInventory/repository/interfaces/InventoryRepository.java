package com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces;

import com.ekwe_hub.zeeshopserver.productInventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProductId(UUID productId);
}
