package com.ekwe_hub.zeeshopserver.productInventory.service.interfaces;

import com.ekwe_hub.zeeshopserver.productInventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.SetLowStockThresholdRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryAdjustmentResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Stock-level operations on top of Inventory. Inventory rows are provisioned
 * and retired by ProductService alongside their owning Product, so this
 * interface deliberately exposes no create/delete — only reading stock levels
 * and adjusting them by a signed delta.
 */
public interface InventoryService {

    List<InventoryResponse> getAllInventory();

    InventoryResponse getInventoryByProduct(UUID productId);

    InventoryResponse adjustStock(UUID productId, AdjustInventoryRequest request);

    List<InventoryResponse> getLowStockInventory();

    InventoryResponse updateLowStockThreshold(UUID productId, SetLowStockThresholdRequest request);

    PageResponse<InventoryAdjustmentResponse> getInventoryHistory(UUID productId, Pageable pageable);
}
