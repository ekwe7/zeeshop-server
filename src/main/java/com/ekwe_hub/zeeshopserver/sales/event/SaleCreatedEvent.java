package com.ekwe_hub.zeeshopserver.sales.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class SaleCreatedEvent extends AbstractDomainEvent {

    private final UUID saleId;
    private final BigDecimal totalAmount;

    public SaleCreatedEvent(UUID saleId, BigDecimal totalAmount) {
        super(saleId.toString());
        this.saleId = saleId;
        this.totalAmount = totalAmount;
    }

    @Override
    public String getEventType() {
        return "sale.created";
    }
}

