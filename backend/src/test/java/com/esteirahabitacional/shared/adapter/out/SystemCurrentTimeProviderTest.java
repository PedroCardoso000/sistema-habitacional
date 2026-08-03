package com.esteirahabitacional.shared.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SystemCurrentTimeProviderTest {

    @Test
    void shouldReturnTimeFromInjectedClock() {
        Instant expected = Instant.parse("2026-08-03T12:00:00Z");
        var provider = new SystemCurrentTimeProvider(Clock.fixed(expected, ZoneOffset.UTC));

        assertThat(provider.now()).isEqualTo(expected);
    }
}

