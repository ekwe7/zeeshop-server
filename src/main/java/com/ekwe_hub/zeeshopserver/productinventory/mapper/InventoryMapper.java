package com.ekwe_hub.zeeshopserver.productinventory.mapper;

import com.ekwe_hub.zeeshopserver.productinventory.dto.response.InventoryResponse;
import com.ekwe_hub.zeeshopserver.productinventory.entity.Inventory;
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
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
