package com.ekwe_hub.zeeshopserver.supplierpurchase.repository.interfaces;

import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.Purchase;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Hand-written fragment for purchase history filtering, mirroring
 * ProductRepositoryCustom. Implemented by PurchaseRepositoryCustomImpl, kept
 * in this same package so Spring Data can resolve it by package + name.
 */
public interface PurchaseRepositoryCustom {

    Page<Purchase> search(UUID supplierId, PurchaseStatus status, Pageable pageable);
}
