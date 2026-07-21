package com.ekwe_hub.zeeshopserver.supplierpurchase.repository.interfaces;

import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, UUID> {

    Optional<PurchaseItem> findByIdAndPurchaseId(UUID id, UUID purchaseId);
}
