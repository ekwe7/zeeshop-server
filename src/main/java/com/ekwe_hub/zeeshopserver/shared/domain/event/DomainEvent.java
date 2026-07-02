package com.ekwe_hub.zeeshopserver.shared.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Marker interface for all domain events in ZeeShop.
 *
 * A domain event represents something that already happened inside the domain —
 * e.g. OrderPlaced, PaymentConfirmed, StockReserved. They are facts, not commands.
 *
 * Why use domain events?
 * They decouple modules. When an order is placed, the Order module raises OrderPlaced.
 * The Inventory module listens and reserves stock. Neither module knows the other exists.
 * This lets each module evolve independently and makes the system easier to test.
 *
 * All events are published through DomainEventPublisher and dispatched by Spring's
 * ApplicationEventPublisher. Listeners use @EventListener or @TransactionalEventListener.
 */
public interface DomainEvent {

    // Unique ID for this specific event occurrence — used for idempotency and deduplication
    UUID getEventId();

    // Human-readable type name, e.g. "order.placed", "payment.confirmed"
    String getEventType();

    LocalDateTime getOccurredAt();

    // ID of the aggregate that raised this event (e.g. the Order ID for an OrderPlaced event)
    String getAggregateId();
}
