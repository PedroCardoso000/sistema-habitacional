package com.esteirahabitacional.shared.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;

import com.esteirahabitacional.shared.DomainEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class SpringDomainEventPublisherTest {

    @Test
    void shouldPublishEveryDomainEvent() {
        var publishedEvents = new ArrayList<Object>();
        ApplicationEventPublisher applicationPublisher = publishedEvents::add;
        var publisher = new SpringDomainEventPublisher(applicationPublisher);
        var first = new TestEvent(Instant.parse("2026-08-03T12:00:00Z"));
        var second = new TestEvent(Instant.parse("2026-08-03T12:01:00Z"));

        publisher.publish(List.of(first, second));

        assertThat(publishedEvents).containsExactly(first, second);
    }

    private record TestEvent(Instant occurredAt) implements DomainEvent {}
}
