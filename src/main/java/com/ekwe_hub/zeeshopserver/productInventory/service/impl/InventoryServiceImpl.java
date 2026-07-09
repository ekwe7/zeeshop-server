package com.ekwe_hub.zeeshopserver.productInventory.service.impl;

import com.ekwe_hub.zeeshopserver.shared.api.exception.BusinessRuleViolationException;
import com.ekwe_hub.zeeshopserver.shared.api.exception.ResourceNotFoundException;
import com.ekwe_hub.zeeshopserver.shared.api.response.PageResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.AdjustInventoryRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.request.SetLowStockThresholdRequest;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryAdjustmentResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Inventory;
import com.ekwe_hub.zeeshopserver.productInventory.entity.InventoryAdjustment;
import com.ekwe_hub.zeeshopserver.productInventory.mapper.InventoryMapper;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.InventoryAdjustmentRepository;
import com.ekwe_hub.zeeshopserver.productInventory.repository.interfaces.InventoryRepository;
import com.ekwe_hub.zeeshopserver.productInventory.service.interfaces.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryAdjustmentRepository inventoryAdjustmentRepository;
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

        int quantityBefore = inventory.getQuantityOnHand();
        int quantityAfter = quantityBefore + request.quantity();
        if (quantityAfter < 0) {
            throw new BusinessRuleViolationException(
                    "Cannot reduce stock below zero: current quantity is %d, requested change is %d"
                            .formatted(quantityBefore, request.quantity()));
        }

        inventory.setQuantityOnHand(quantityAfter);
        inventory = inventoryRepository.save(inventory);

        inventoryAdjustmentRepository.save(InventoryAdjustment.builder()
                .product(inventory.getProduct())
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .reason(request.reason())
                .build());

        return inventoryMapper.toResponse(inventory);
    }

    @Override
    public List<InventoryResponse> getLowStockInventory() {
        return inventoryRepository.findLowStock().stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InventoryResponse updateLowStockThreshold(UUID productId, SetLowStockThresholdRequest request) {
        Inventory inventory = findInventoryOrThrow(productId);
        inventory.setLowStockThreshold(request.threshold());
        inventory = inventoryRepository.save(inventory);
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    public PageResponse<InventoryAdjustmentResponse> getInventoryHistory(UUID productId, Pageable pageable) {
        findInventoryOrThrow(productId);
        Page<InventoryAdjustmentResponse> history = inventoryAdjustmentRepository
                .findByProductId(productId, pageable)
                .map(inventoryMapper::toAdjustmentResponse);
        return PageResponse.from(history);
    }

    private Inventory findInventoryOrThrow(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", productId));
    }
}
