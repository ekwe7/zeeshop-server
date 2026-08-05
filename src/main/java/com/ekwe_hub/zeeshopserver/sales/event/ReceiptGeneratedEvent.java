package com.ekwe_hub.zeeshopserver.sales.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ReceiptGeneratedEvent extends AbstractDomainEvent {

    private final UUID saleId;
    private final String referenceNumber;

    public ReceiptGeneratedEvent(UUID saleId, String referenceNumber) {
        super(saleId.toString());
        this.saleId = saleId;
        this.referenceNumber = referenceNumber;
    }

    @Override
    public String getEventType() {
        return "receipt.generated";
    }
}
