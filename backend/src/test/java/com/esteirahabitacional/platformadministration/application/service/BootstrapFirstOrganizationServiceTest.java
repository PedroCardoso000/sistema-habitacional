package com.esteirahabitacional.platformadministration.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.esteirahabitacional.identityaccess.ProvisionInitialAdministratorUseCase;
import com.esteirahabitacional.organizations.ProvisionFirstOrganizationUseCase;
import com.esteirahabitacional.platformadministration.application.port.in.BootstrapFirstOrganizationUseCase;
import com.esteirahabitacional.platformadministration.application.port.out.PlatformAdministrationSettings;
import com.esteirahabitacional.shared.ApplicationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BootstrapFirstOrganizationServiceTest {

    @Test
    void shouldRejectUnauthorizedBootstrapWithoutCallingProvisioning() {
        RecordingProvisioning provisioning = new RecordingProvisioning();
        BootstrapFirstOrganizationService service = new BootstrapFirstOrganizationService(
                new FixedSettings(false), provisioning, provisioning);

        assertThatThrownBy(() -> service.execute(command()))
                .isInstanceOf(ApplicationException.class);
        assertThat(provisioning.organizationCalls).isZero();
        assertThat(provisioning.administratorCalls).isZero();
    }

    @Test
    void shouldProvisionOrganizationBeforeItsAdministrator() {
        RecordingProvisioning provisioning = new RecordingProvisioning();
        BootstrapFirstOrganizationService service = new BootstrapFirstOrganizationService(
                new FixedSettings(true), provisioning, provisioning);

        BootstrapFirstOrganizationUseCase.Result result = service.execute(command());

        assertThat(result.organizationId()).isEqualTo(provisioning.organizationId);
        assertThat(result.administratorUserId()).isEqualTo(provisioning.userId);
        assertThat(provisioning.organizationCalls).isEqualTo(1);
        assertThat(provisioning.administratorCalls).isEqualTo(1);
    }

    private BootstrapFirstOrganizationUseCase.Command command() {
        return new BootstrapFirstOrganizationUseCase.Command(
                "secret", "Organization", "admin@example.com", "Administrator");
    }

    private record FixedSettings(boolean acceptsSecret) implements PlatformAdministrationSettings {

        @Override
        public boolean acceptsBootstrapSecret(String suppliedSecret) {
            return acceptsSecret;
        }

        @Override
        public boolean isOrganizationCreationEnabled() {
            return false;
        }
    }

    private static final class RecordingProvisioning
            implements ProvisionFirstOrganizationUseCase, ProvisionInitialAdministratorUseCase {

        private final UUID organizationId = UUID.randomUUID();
        private final UUID userId = UUID.randomUUID();
        private int organizationCalls;
        private int administratorCalls;

        @Override
        public ProvisionFirstOrganizationUseCase.Result execute(
                ProvisionFirstOrganizationUseCase.Command command) {
            organizationCalls++;
            return new ProvisionFirstOrganizationUseCase.Result(organizationId, command.name());
        }

        @Override
        public ProvisionInitialAdministratorUseCase.Result execute(
                ProvisionInitialAdministratorUseCase.Command command) {
            administratorCalls++;
            return new ProvisionInitialAdministratorUseCase.Result(userId);
        }
    }
}
