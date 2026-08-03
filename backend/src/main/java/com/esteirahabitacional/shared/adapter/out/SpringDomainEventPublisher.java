package com.esteirahabitacional.shared.adapter.out;

import com.esteirahabitacional.shared.DomainEvent;
import com.esteirahabitacional.shared.DomainEventPublisher;
import java.util.Collection;
import org.springframework.context.ApplicationEventPublisher;

public final class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(Collection<? extends DomainEvent> events) {
        events.forEach(publisher::publishEvent);
    }
}
