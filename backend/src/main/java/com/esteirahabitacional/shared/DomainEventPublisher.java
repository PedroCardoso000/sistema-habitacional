package com.esteirahabitacional.shared;

import java.util.Collection;

public interface DomainEventPublisher {

    void publish(Collection<? extends DomainEvent> events);
}

