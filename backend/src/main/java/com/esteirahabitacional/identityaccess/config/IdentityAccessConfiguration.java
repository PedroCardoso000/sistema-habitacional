package com.esteirahabitacional.identityaccess.config;

import com.esteirahabitacional.identityaccess.adapter.in.web.DevHeaderCurrentActorProvider;
import com.esteirahabitacional.identityaccess.adapter.out.persistence.JdbcUserRepository;
import com.esteirahabitacional.identityaccess.application.port.in.GetCurrentUserContextUseCase;
import com.esteirahabitacional.identityaccess.ProvisionInitialAdministratorUseCase;
import com.esteirahabitacional.identityaccess.application.port.out.CurrentActorProvider;
import com.esteirahabitacional.identityaccess.application.port.out.UserRepository;
import com.esteirahabitacional.identityaccess.application.service.AuthorizationService;
import com.esteirahabitacional.identityaccess.application.service.GetCurrentUserContextService;
import com.esteirahabitacional.identityaccess.application.service.ProvisionInitialAdministratorService;
import com.esteirahabitacional.identityaccess.application.service.UserAdministrationService;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import com.esteirahabitacional.identityaccess.application.port.in.AssignRoleUseCase;
import com.esteirahabitacional.identityaccess.application.port.in.RegisterInternalUserUseCase;
import com.esteirahabitacional.identityaccess.application.port.in.RevokeAccessUseCase;
import com.esteirahabitacional.identityaccess.application.port.in.SuspendAccessUseCase;

@Configuration(proxyBeanMethods = false)
class IdentityAccessConfiguration {

    @Bean
    UserRepository userRepository(JdbcClient jdbc) {
        return new JdbcUserRepository(jdbc);
    }

    @Bean
    CurrentActorProvider currentActorProvider() {
        return new DevHeaderCurrentActorProvider();
    }

    @Bean
    AuthorizationService authorizationService(CurrentActorProvider actors, UserRepository users) {
        return new AuthorizationService(actors, users);
    }

    @Bean
    RegisterInternalUserUseCase registerInternalUserUseCase(
            AuthorizationService authorization,
            UserRepository users,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        UserAdministrationService service =
                new UserAdministrationService(authorization, users, identifiers, time);
        return command -> transactions.execute(status -> service.execute(command));
    }

    @Bean
    AssignRoleUseCase assignRoleUseCase(
            AuthorizationService authorization,
            UserRepository users,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        UserAdministrationService service =
                new UserAdministrationService(authorization, users, identifiers, time);
        return command -> transactions.executeWithoutResult(status -> service.execute(command));
    }

    @Bean
    SuspendAccessUseCase suspendAccessUseCase(
            AuthorizationService authorization,
            UserRepository users,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        UserAdministrationService service =
                new UserAdministrationService(authorization, users, identifiers, time);
        return command -> transactions.executeWithoutResult(status -> service.execute(command));
    }

    @Bean
    RevokeAccessUseCase revokeAccessUseCase(
            AuthorizationService authorization,
            UserRepository users,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time,
            TransactionTemplate transactions) {
        UserAdministrationService service =
                new UserAdministrationService(authorization, users, identifiers, time);
        return command -> transactions.executeWithoutResult(status -> service.execute(command));
    }

    @Bean
    GetCurrentUserContextUseCase getCurrentUserContextUseCase(
            CurrentActorProvider actors, UserRepository users) {
        return new GetCurrentUserContextService(actors, users);
    }

    @Bean
    ProvisionInitialAdministratorUseCase provisionInitialAdministratorUseCase(
            UserRepository users, IdentifierGenerator identifiers, CurrentTimeProvider time) {
        return new ProvisionInitialAdministratorService(users, identifiers, time);
    }
}
