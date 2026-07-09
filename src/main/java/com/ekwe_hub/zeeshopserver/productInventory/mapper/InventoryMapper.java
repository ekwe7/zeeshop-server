package com.ekwe_hub.zeeshopserver.productInventory.mapper;

import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryAdjustmentResponse;
import com.ekwe_hub.zeeshopserver.productInventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.productInventory.entity.Inventory;
import com.ekwe_hub.zeeshopserver.productInventory.entity.InventoryAdjustment;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .sku(inventory.getProduct().getSku())
                .quantityOnHand(inventory.getQuantityOnHand())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .lowStock(inventory.getQuantityOnHand() <= inventory.getLowStockThreshold())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    public InventoryAdjustmentResponse toAdjustmentResponse(InventoryAdjustment adjustment) {
        return InventoryAdjustmentResponse.builder()
                .id(adjustment.getId())
                .productId(adjustment.getProduct().getId())
                .productName(adjustment.getProduct().getName())
                .sku(adjustment.getProduct().getSku())
                .quantityBefore(adjustment.getQuantityBefore())
                .quantityAfter(adjustment.getQuantityAfter())
                .change(adjustment.getQuantityAfter() - adjustment.getQuantityBefore())
                .reason(adjustment.getReason())
                .adjustedBy(adjustment.getCreatedBy())
                .adjustedAt(adjustment.getCreatedAt())
                .build();
    }
}
