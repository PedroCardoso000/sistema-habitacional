package com.esteirahabitacional.platformadministration.application.port.out;

public interface PlatformAdministrationSettings {

    boolean acceptsBootstrapSecret(String suppliedSecret);

    boolean isOrganizationCreationEnabled();
}
