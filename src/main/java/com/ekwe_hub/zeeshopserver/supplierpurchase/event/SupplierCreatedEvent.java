package com.ekwe_hub.zeeshopserver.supplierpurchase.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SupplierCreatedEvent extends AbstractDomainEvent {

    private final UUID supplierId;
    private final String name;

    public SupplierCreatedEvent(UUID supplierId, String name) {
        super(supplierId.toString());
        this.supplierId = supplierId;
        this.name = name;
    }

    @Override
    public String getEventType() {
        return "supplier.created";
    }
}
