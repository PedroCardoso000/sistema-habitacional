package com.esteirahabitacional.organizations.config;

import com.esteirahabitacional.organizations.adapter.out.persistence.JdbcOrganizationRepository;
import com.esteirahabitacional.organizations.CreateOrganizationUseCase;
import com.esteirahabitacional.organizations.ProvisionFirstOrganizationUseCase;
import com.esteirahabitacional.organizations.application.port.out.OrganizationRepository;
import com.esteirahabitacional.organizations.application.service.CreateOrganizationService;
import com.esteirahabitacional.organizations.application.service.ProvisionFirstOrganizationService;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
class OrganizationsConfiguration {

    @Bean
    OrganizationRepository organizationRepository(JdbcClient jdbc) {
        return new JdbcOrganizationRepository(jdbc);
    }

    @Bean
    CreateOrganizationUseCase createOrganizationUseCase(
            OrganizationRepository repository,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        CreateOrganizationService service = new CreateOrganizationService(repository, identifiers, time);
        return command -> transactions.execute(status -> service.execute(command));
    }

    @Bean
    ProvisionFirstOrganizationUseCase provisionFirstOrganizationUseCase(
            OrganizationRepository repository,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        ProvisionFirstOrganizationService service =
                new ProvisionFirstOrganizationService(repository, identifiers, time);
        return command -> transactions.execute(status -> service.execute(command));
    }
}
