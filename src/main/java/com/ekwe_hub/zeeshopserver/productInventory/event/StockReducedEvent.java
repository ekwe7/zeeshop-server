package com.ekwe_hub.zeeshopserver.productInventory.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StockReducedEvent extends AbstractDomainEvent {

    private final UUID productId;
    private final int quantityReduced;
    private final int remainingQuantity;
    private final String reason;

    public StockReducedEvent(UUID productId, int quantityReduced, int remainingQuantity, String reason) {
        super(productId.toString());
        this.productId = productId;
        this.quantityReduced = quantityReduced;
        this.remainingQuantity = remainingQuantity;
        this.reason = reason;
    }

    @Override
    public String getEventType() {
        return "inventory.stock_reduced";
    }
}
