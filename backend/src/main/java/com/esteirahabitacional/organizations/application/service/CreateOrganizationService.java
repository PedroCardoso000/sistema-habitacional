package com.esteirahabitacional.organizations.application.service;

import com.esteirahabitacional.organizations.CreateOrganizationUseCase;
import com.esteirahabitacional.organizations.application.port.out.OrganizationRepository;
import com.esteirahabitacional.organizations.domain.model.Organization;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.IdentifierGenerator;

public class CreateOrganizationService implements CreateOrganizationUseCase {

    private final OrganizationRepository repository;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public CreateOrganizationService(
            OrganizationRepository repository, IdentifierGenerator identifiers, CurrentTimeProvider time) {
        this.repository = repository;
        this.identifiers = identifiers;
        this.time = time;
    }

    @Override
    public Result execute(Command command) {
        Organization organization = Organization.create(identifiers.generate(), command.name(), time.now());
        repository.save(organization);
        return new Result(organization.id(), organization.name());
    }
}
