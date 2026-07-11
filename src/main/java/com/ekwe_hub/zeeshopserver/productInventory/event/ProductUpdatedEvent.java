package com.ekwe_hub.zeeshopserver.productInventory.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ProductUpdatedEvent extends AbstractDomainEvent {

    private final UUID productId;
    private final String sku;
    private final String name;

    public ProductUpdatedEvent(UUID productId, String sku, String name) {
        super(productId.toString());
        this.productId = productId;
        this.sku = sku;
        this.name = name;
    }

    @Override
    public String getEventType() {
        return "product.updated";
    }
}
