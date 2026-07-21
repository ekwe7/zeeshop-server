package com.ekwe_hub.zeeshopserver.supplierpurchase.service.interfaces;

import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.CreatePurchaseRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.ReceiveStockRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.request.UpdatePurchaseRequest;
import com.ekwe_hub.zeeshopserver.supplierpurchase.dto.response.PurchaseResponse;
import com.ekwe_hub.zeeshopserver.supplierpurchase.entity.PurchaseStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PurchaseService {

    PageResponse<PurchaseResponse> getAllPurchases(UUID supplierId, PurchaseStatus status, Pageable pageable);

    PurchaseResponse getPurchase(UUID id);

    PurchaseResponse createPurchase(CreatePurchaseRequest request);

    PurchaseResponse updatePurchase(UUID id, UpdatePurchaseRequest request);

    void deletePurchase(UUID id);

    PurchaseResponse receiveStock(UUID id, ReceiveStockRequest request);

    PurchaseResponse cancelPurchase(UUID id);
}
