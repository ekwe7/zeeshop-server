package com.ekwe_hub.zeeshopserver.productinventory.service.interfaces;

import com.ekwe_hub.zeeshopserver.productinventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.response.InventoryResponse;

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
}
