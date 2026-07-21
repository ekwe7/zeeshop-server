package com.ekwe_hub.zeeshopserver.supplierpurchase.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class PurchaseCompletedEvent extends AbstractDomainEvent {

    private final UUID purchaseId;
    private final UUID supplierId;
    private final BigDecimal totalAmount;

    public PurchaseCompletedEvent(UUID purchaseId, UUID supplierId, BigDecimal totalAmount) {
        super(purchaseId.toString());
        this.purchaseId = purchaseId;
        this.supplierId = supplierId;
        this.totalAmount = totalAmount;
    }

    @Override
    public String getEventType() {
        return "purchase.completed";
    }
}
