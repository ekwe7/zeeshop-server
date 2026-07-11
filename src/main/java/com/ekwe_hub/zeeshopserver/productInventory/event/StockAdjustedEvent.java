package com.ekwe_hub.zeeshopserver.productInventory.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class StockAdjustedEvent extends AbstractDomainEvent {

    private final UUID productId;
    private final int quantityBefore;
    private final int quantityAfter;
    private final String reason;

    public StockAdjustedEvent(UUID productId, int quantityBefore, int quantityAfter, String reason) {
        super(productId.toString());
        this.productId = productId;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.reason = reason;
    }

    @Override
    public String getEventType() {
        return "inventory.stock_adjusted";
    }
}
