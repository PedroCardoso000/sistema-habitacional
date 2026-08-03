package com.esteirahabitacional.platformadministration.application.service;

import com.esteirahabitacional.identityaccess.AuthorizePlatformAdministrationUseCase;
import com.esteirahabitacional.organizations.CreateOrganizationUseCase;
import com.esteirahabitacional.platformadministration.application.port.in.CreateAuthorizedOrganizationUseCase;
import com.esteirahabitacional.platformadministration.application.port.out.PlatformAdministrationSettings;
import com.esteirahabitacional.platformadministration.application.port.out.PlatformAdministrationAudit;
import com.esteirahabitacional.shared.ApplicationException;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;

public class CreateAuthorizedOrganizationService implements CreateAuthorizedOrganizationUseCase {

    private final PlatformAdministrationSettings settings;
    private final AuthorizePlatformAdministrationUseCase authorization;
    private final CreateOrganizationUseCase organizations;
    private final PlatformAdministrationAudit audit;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public CreateAuthorizedOrganizationService(
            PlatformAdministrationSettings settings,
            AuthorizePlatformAdministrationUseCase authorization,
            CreateOrganizationUseCase organizations,
            PlatformAdministrationAudit audit,
            IdentifierGenerator identifiers,
            CurrentTimeProvider time) {
        this.settings = settings;
        this.authorization = authorization;
        this.organizations = organizations;
        this.audit = audit;
        this.identifiers = identifiers;
        this.time = time;
    }

    @Override
    public Result execute(Command command) {
        if (!settings.isOrganizationCreationEnabled()) {
            throw new ApplicationException(403, "organization-creation-disabled", "Criação desabilitada",
                    "A criação de empresas pela administração da plataforma está desabilitada.");
        }
        AuthorizePlatformAdministrationUseCase.AuthorizedActor actor =
                authorization.requireOrganizationCreationPermission();
        CreateOrganizationUseCase.Result result = organizations.execute(
                new CreateOrganizationUseCase.Command(command.name()));
        audit.recordOrganizationCreated(
                identifiers.generate(), actor.organizationId(), actor.userId(), result.id(), time.now());
        return new Result(result.id(), result.name());
    }
}
