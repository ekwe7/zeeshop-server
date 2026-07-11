package com.ekwe_hub.zeeshopserver.shared.domain.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fills in the bookkeeping every DomainEvent needs (event ID, timestamp) so
 * concrete events only have to declare their own payload and event type.
 */
@Getter
public abstract class AbstractDomainEvent implements DomainEvent {

    private final UUID eventId = UUID.randomUUID();
    private final LocalDateTime occurredAt = LocalDateTime.now();
    private final String aggregateId;

    protected AbstractDomainEvent(String aggregateId) {
        this.aggregateId = aggregateId;
    }
}
