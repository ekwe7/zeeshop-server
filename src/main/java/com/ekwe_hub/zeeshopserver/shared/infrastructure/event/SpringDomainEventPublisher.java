package com.ekwe_hub.zeeshopserver.shared.infrastructure.event;

import com.ekwe_hub.zeeshopserver.shared.domain.event.DomainEvent;
import com.ekwe_hub.zeeshopserver.shared.domain.event.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Infrastructure adapter that fulfils the DomainEventPublisher port using
 * Spring's ApplicationEventPublisher.
 *
 * Spring dispatches events synchronously within the same thread by default.
 * Listeners can use @TransactionalEventListener(phase = AFTER_COMMIT) to ensure
 * side effects (e.g. sending emails, updating inventory) only run after the
 * originating transaction commits — preventing partial-update bugs where the
 * transaction rolls back but the side effect already fired.
 *
 * To switch to async dispatch later, add @Async to individual listener methods
 * and @EnableAsync to the application class — no changes needed here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: type={}, aggregateId={}, eventId={}",
                event.getEventType(), event.getAggregateId(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishAll(Collection<? extends DomainEvent> events) {
        events.forEach(this::publish);
    }
}
