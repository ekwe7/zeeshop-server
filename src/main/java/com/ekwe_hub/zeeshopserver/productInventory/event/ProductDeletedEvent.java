package com.ekwe_hub.zeeshopserver.productInventory.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ProductDeletedEvent extends AbstractDomainEvent {

    private final UUID productId;
    private final String sku;

    public ProductDeletedEvent(UUID productId, String sku) {
        super(productId.toString());
        this.productId = productId;
        this.sku = sku;
    }

    @Override
    public String getEventType() {
        return "product.deleted";
    }
}
