package com.esteirahabitacional.organizations.application.port.out;

import com.esteirahabitacional.organizations.domain.model.Organization;

public interface OrganizationRepository {

    void acquireBootstrapLock();

    boolean existsAny();

    void save(Organization organization);
}
