package com.esteirahabitacional.shared.config;

import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.DomainEventPublisher;
import com.esteirahabitacional.shared.IdentifierGenerator;
import com.esteirahabitacional.shared.adapter.out.SpringDomainEventPublisher;
import com.esteirahabitacional.shared.adapter.out.SystemCurrentTimeProvider;
import com.esteirahabitacional.shared.adapter.out.UuidIdentifierGenerator;
import java.time.Clock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SharedConfiguration {

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }

    @Bean
    CurrentTimeProvider currentTimeProvider(Clock clock) {
        return new SystemCurrentTimeProvider(clock);
    }

    @Bean
    IdentifierGenerator identifierGenerator() {
        return new UuidIdentifierGenerator();
    }

    @Bean
    DomainEventPublisher domainEventPublisher(ApplicationEventPublisher publisher) {
        return new SpringDomainEventPublisher(publisher);
    }
}
