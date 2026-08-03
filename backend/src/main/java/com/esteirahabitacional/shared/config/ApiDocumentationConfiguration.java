package com.esteirahabitacional.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ApiDocumentationConfiguration {

    @Bean
    OpenAPI esteiraHabitacionalOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Esteira Habitacional API")
                .version("v1")
                .description("Contrato HTTP do backend da Esteira Habitacional."));
    }
}

