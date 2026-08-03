package com.esteirahabitacional.organizations.application.service;

import com.esteirahabitacional.organizations.ProvisionFirstOrganizationUseCase;
import com.esteirahabitacional.organizations.application.port.out.OrganizationRepository;
import com.esteirahabitacional.organizations.domain.model.Organization;
import com.esteirahabitacional.shared.ApplicationException;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;

public class ProvisionFirstOrganizationService implements ProvisionFirstOrganizationUseCase {

    private final OrganizationRepository repository;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public ProvisionFirstOrganizationService(
            OrganizationRepository repository, IdentifierGenerator identifiers, CurrentTimeProvider time) {
        this.repository = repository;
        this.identifiers = identifiers;
        this.time = time;
    }

    @Override
    public Result execute(Command command) {
        repository.acquireBootstrapLock();
        if (repository.existsAny()) {
            throw new ApplicationException(409, "bootstrap-already-used", "Bootstrap indisponível",
                    "O provisionamento inicial não pode ser repetido.");
        }
        Organization organization = Organization.create(identifiers.generate(), command.name(), time.now());
        repository.save(organization);
        return new Result(organization.id(), organization.name());
    }
}
