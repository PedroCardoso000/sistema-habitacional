package com.esteirahabitacional.platformadministration.application.service;

import com.esteirahabitacional.identityaccess.ProvisionInitialAdministratorUseCase;
import com.esteirahabitacional.organizations.ProvisionFirstOrganizationUseCase;
import com.esteirahabitacional.platformadministration.application.port.in.BootstrapFirstOrganizationUseCase;
import com.esteirahabitacional.platformadministration.application.port.out.PlatformAdministrationSettings;
import com.esteirahabitacional.shared.ApplicationException;

public class BootstrapFirstOrganizationService implements BootstrapFirstOrganizationUseCase {

    private final PlatformAdministrationSettings settings;
    private final ProvisionFirstOrganizationUseCase organizations;
    private final ProvisionInitialAdministratorUseCase administrators;

    public BootstrapFirstOrganizationService(
            PlatformAdministrationSettings settings,
            ProvisionFirstOrganizationUseCase organizations,
            ProvisionInitialAdministratorUseCase administrators) {
        this.settings = settings;
        this.organizations = organizations;
        this.administrators = administrators;
    }

    @Override
    public Result execute(Command command) {
        if (!settings.acceptsBootstrapSecret(command.secret())) {
            throw new ApplicationException(403, "bootstrap-not-authorized", "Bootstrap não autorizado",
                    "O ambiente ou o segredo não autoriza o provisionamento inicial.");
        }
        ProvisionFirstOrganizationUseCase.Result organization = organizations.execute(
                new ProvisionFirstOrganizationUseCase.Command(command.organizationName()));
        ProvisionInitialAdministratorUseCase.Result administrator = administrators.execute(
                new ProvisionInitialAdministratorUseCase.Command(
                        organization.id(), command.administratorEmail(), command.administratorDisplayName()));
        return new Result(organization.id(), administrator.userId());
    }
}
