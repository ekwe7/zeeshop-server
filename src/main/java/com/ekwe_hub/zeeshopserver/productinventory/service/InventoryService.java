package com.ekwe_hub.zeeshopserver.productinventory.service;

import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.productinventory.entity.Inventory;
import com.ekwe_hub.zeeshopserver.productinventory.mapper.InventoryMapper;
import com.ekwe_hub.zeeshopserver.productinventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Stock-level operations on top of Inventory. Inventory rows are provisioned
 * and retired by ProductService alongside their owning Product, so this
 * service deliberately exposes no create/delete — only reading stock levels
 * and adjusting them by a signed delta.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    public InventoryResponse getInventoryByProduct(UUID productId) {
        return inventoryMapper.toResponse(findInventoryOrThrow(productId));
    }

    @Transactional
    public InventoryResponse adjustStock(UUID productId, AdjustInventoryRequest request) {
        Inventory inventory = findInventoryOrThrow(productId);

        int newQuantity = inventory.getQuantityOnHand() + request.quantity();
        if (newQuantity < 0) {
            throw new BusinessRuleViolationException(
                    "Cannot reduce stock below zero: current quantity is %d, requested change is %d"
                            .formatted(inventory.getQuantityOnHand(), request.quantity()));
        }

        inventory.setQuantityOnHand(newQuantity);
        inventory = inventoryRepository.save(inventory);
        return inventoryMapper.toResponse(inventory);
    }

    private Inventory findInventoryOrThrow(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", productId));
    }
}
