package com.ekwe_hub.zeeshopserver.supplierpurchase.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.util.UUID;

/**
 * Raised once per purchase item line received (see PurchaseService.receiveStock),
 * not once per API call — a single request can receive several lines.
 */
@Getter
public class StockReceivedEvent extends AbstractDomainEvent {

    private final UUID purchaseId;
    private final UUID productId;
    private final int quantityReceived;

    public StockReceivedEvent(UUID purchaseId, UUID productId, int quantityReceived) {
        super(purchaseId.toString());
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.quantityReceived = quantityReceived;
    }

    @Override
    public String getEventType() {
        return "purchase.stock_received";
    }
}
