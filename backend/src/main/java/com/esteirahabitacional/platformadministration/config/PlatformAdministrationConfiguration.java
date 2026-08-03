package com.esteirahabitacional.platformadministration.config;

import com.esteirahabitacional.identityaccess.AuthorizePlatformAdministrationUseCase;
import com.esteirahabitacional.identityaccess.ProvisionInitialAdministratorUseCase;
import com.esteirahabitacional.organizations.CreateOrganizationUseCase;
import com.esteirahabitacional.organizations.ProvisionFirstOrganizationUseCase;
import com.esteirahabitacional.platformadministration.adapter.in.bootstrap.BootstrapCommandRunner;
import com.esteirahabitacional.platformadministration.adapter.out.EnvironmentPlatformAdministrationSettings;
import com.esteirahabitacional.platformadministration.adapter.out.JdbcPlatformAdministrationAudit;
import com.esteirahabitacional.platformadministration.application.port.in.BootstrapFirstOrganizationUseCase;
import com.esteirahabitacional.platformadministration.application.port.in.CreateAuthorizedOrganizationUseCase;
import com.esteirahabitacional.platformadministration.application.port.out.PlatformAdministrationSettings;
import com.esteirahabitacional.platformadministration.application.port.out.PlatformAdministrationAudit;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import com.esteirahabitacional.platformadministration.application.service.BootstrapFirstOrganizationService;
import com.esteirahabitacional.platformadministration.application.service.CreateAuthorizedOrganizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration(proxyBeanMethods = false)
class PlatformAdministrationConfiguration {

    @Bean
    PlatformAdministrationSettings platformAdministrationSettings(
            @Value("${app.bootstrap.enabled:false}") boolean bootstrapEnabled,
            @Value("${app.bootstrap.expected-secret:}") String bootstrapSecret,
            @Value("${app.platform-administration.organization-creation-enabled:false}")
                    boolean organizationCreationEnabled) {
        return new EnvironmentPlatformAdministrationSettings(
                bootstrapEnabled, bootstrapSecret, organizationCreationEnabled);
    }

    @Bean
    BootstrapFirstOrganizationUseCase bootstrapFirstOrganizationUseCase(
            PlatformAdministrationSettings settings,
            ProvisionFirstOrganizationUseCase organizations,
            ProvisionInitialAdministratorUseCase administrators,
            TransactionTemplate transactions) {
        BootstrapFirstOrganizationService service =
                new BootstrapFirstOrganizationService(settings, organizations, administrators);
        return command -> transactions.execute(status -> service.execute(command));
    }

    @Bean
    CreateAuthorizedOrganizationUseCase createAuthorizedOrganizationUseCase(
            PlatformAdministrationSettings settings,
            AuthorizePlatformAdministrationUseCase authorization,
            CreateOrganizationUseCase organizations,
            PlatformAdministrationAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        CreateAuthorizedOrganizationService service = new CreateAuthorizedOrganizationService(
                settings, authorization, organizations, audit, identifiers, time);
        return command -> transactions.execute(status -> service.execute(command));
    }

    @Bean
    PlatformAdministrationAudit platformAdministrationAudit(JdbcClient jdbc) {
        return new JdbcPlatformAdministrationAudit(jdbc);
    }

    @Bean
    BootstrapCommandRunner bootstrapCommandRunner(
            @Value("${app.bootstrap.execute:false}") boolean execute,
            @Value("${app.bootstrap.supplied-secret:}") String suppliedSecret,
            @Value("${app.bootstrap.organization-name:}") String organizationName,
            @Value("${app.bootstrap.administrator-email:}") String administratorEmail,
            @Value("${app.bootstrap.administrator-display-name:}") String administratorDisplayName,
            BootstrapFirstOrganizationUseCase bootstrap) {
        return new BootstrapCommandRunner(
                execute,
                suppliedSecret,
                organizationName,
                administratorEmail,
                administratorDisplayName,
                bootstrap);
    }
}
