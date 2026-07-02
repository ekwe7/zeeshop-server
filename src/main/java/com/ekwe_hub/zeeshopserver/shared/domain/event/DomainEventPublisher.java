package com.ekwe_hub.zeeshopserver.shared.domain.event;

import java.util.Collection;

/**
 * Port (interface) for publishing domain events out of the domain layer.
 *
 * Lives in the domain package so domain classes can depend on it without
 * importing any Spring or infrastructure types. The actual implementation
 * (SpringDomainEventPublisher) lives in the infrastructure layer and wires
 * Spring's ApplicationEventPublisher behind this interface.
 *
 * This separation means the domain can be tested without a Spring context —
 * just pass a mock or a simple in-memory collector.
 */
public interface DomainEventPublisher {

    // Publish a single domain event immediately
    void publish(DomainEvent event);

    // Publish a batch of events — e.g. all events accumulated during a transaction
    void publishAll(Collection<? extends DomainEvent> events);
}
