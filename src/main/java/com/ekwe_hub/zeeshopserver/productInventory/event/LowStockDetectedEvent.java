package com.ekwe_hub.zeeshopserver.productInventory.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class LowStockDetectedEvent extends AbstractDomainEvent {

    private final UUID productId;
    private final int quantityOnHand;
    private final int lowStockThreshold;

    public LowStockDetectedEvent(UUID productId, int quantityOnHand, int lowStockThreshold) {
        super(productId.toString());
        this.quantityOnHand = quantityOnHand;
        this.productId = productId;
        this.lowStockThreshold = lowStockThreshold;
    }

    @Override
    public String getEventType() {
        return "inventory.low_stock_detected";
    }
}
