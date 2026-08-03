package com.esteirahabitacional.organizations.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrganizationTest {

    @Test
    void shouldCreateOrganizationWithNormalizedName() {
        Organization organization = Organization.create(UUID.randomUUID(), "  Correspondente Alfa  ", Instant.EPOCH);

        assertThat(organization.name()).isEqualTo("Correspondente Alfa");
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> Organization.create(UUID.randomUUID(), " ", Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
