package com.ekwe_hub.zeeshopserver.productinventory.service.impl;

import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.productinventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productinventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.productinventory.entity.Inventory;
import com.ekwe_hub.zeeshopserver.productinventory.mapper.InventoryMapper;
import com.ekwe_hub.zeeshopserver.productinventory.repository.interfaces.InventoryRepository;
import com.ekwe_hub.zeeshopserver.productinventory.service.interfaces.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Override
    public InventoryResponse getInventoryByProduct(UUID productId) {
        return inventoryMapper.toResponse(findInventoryOrThrow(productId));
    }

    @Override
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
