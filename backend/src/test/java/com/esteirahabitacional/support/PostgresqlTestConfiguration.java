package com.esteirahabitacional.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class PostgresqlTestConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresqlContainer() {
        return new PostgreSQLContainer("postgres:18-alpine")
                .withDatabaseName("esteira_habitacional")
                .withUsername("esteira")
                .withPassword("esteira");
    }
}
