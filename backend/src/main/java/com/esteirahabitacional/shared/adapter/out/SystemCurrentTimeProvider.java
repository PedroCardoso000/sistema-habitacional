package com.esteirahabitacional.shared.adapter.out;

import com.esteirahabitacional.shared.CurrentTimeProvider;
import java.time.Clock;
import java.time.Instant;

public final class SystemCurrentTimeProvider implements CurrentTimeProvider {

    private final Clock clock;

    public SystemCurrentTimeProvider(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant now() {
        return clock.instant();
    }
}
