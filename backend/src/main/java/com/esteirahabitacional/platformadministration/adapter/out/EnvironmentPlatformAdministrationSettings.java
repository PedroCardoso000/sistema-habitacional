package com.esteirahabitacional.platformadministration.adapter.out;

import com.esteirahabitacional.platformadministration.application.port.out.PlatformAdministrationSettings;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class EnvironmentPlatformAdministrationSettings implements PlatformAdministrationSettings {

    private final boolean bootstrapEnabled;
    private final String bootstrapSecret;
    private final boolean organizationCreationEnabled;

    public EnvironmentPlatformAdministrationSettings(
            boolean bootstrapEnabled, String bootstrapSecret, boolean organizationCreationEnabled) {
        this.bootstrapEnabled = bootstrapEnabled;
        this.bootstrapSecret = bootstrapSecret;
        this.organizationCreationEnabled = organizationCreationEnabled;
    }

    @Override
    public boolean acceptsBootstrapSecret(String suppliedSecret) {
        if (!bootstrapEnabled || bootstrapSecret == null || bootstrapSecret.isBlank() || suppliedSecret == null) {
            return false;
        }
        return MessageDigest.isEqual(
                bootstrapSecret.getBytes(StandardCharsets.UTF_8),
                suppliedSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean isOrganizationCreationEnabled() {
        return organizationCreationEnabled;
    }
}
