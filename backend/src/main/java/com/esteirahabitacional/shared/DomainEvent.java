package com.esteirahabitacional.shared;

import java.time.Instant;

public interface DomainEvent {

    Instant occurredAt();
}

