package com.esteirahabitacional.parties.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PartyDomainTest {

    private static final Instant REGISTERED_AT = Instant.parse("2026-08-03T15:00:00Z");

    @Test
    void shouldNormalizeSensitiveIdentifiersAndContacts() {
        Client client = Client.register(
                UUID.randomUUID(), UUID.randomUUID(), new Cpf("529.982.247-25"),
                "Client Name", new ContactInfo(" CLIENT@EXAMPLE.COM ", "(85) 99999-9999"), REGISTERED_AT);

        assertThat(client.cpf().value()).isEqualTo("52998224725");
        assertThat(client.contact().email()).isEqualTo("client@example.com");
        assertThat(client.contact().phone()).isEqualTo("85999999999");
    }

    @Test
    void shouldRejectInvalidCpfAndCnpj() {
        assertThatThrownBy(() -> new Cpf("111.111.111-11"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cnpj("11.111.111/1111-11"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRequireAtLeastOneValidContact() {
        assertThatThrownBy(() -> new ContactInfo(null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldPreventInactiveBrokerFromOriginatingProcess() {
        Broker broker = broker();
        broker.deactivate(REGISTERED_AT.plusSeconds(60));

        assertThatThrownBy(broker::ensureCanOriginateProcess)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldAllowReactivatedBrokerToOriginateProcess() {
        Broker broker = broker();
        broker.deactivate(REGISTERED_AT.plusSeconds(60));
        broker.activate(REGISTERED_AT.plusSeconds(120));

        broker.ensureCanOriginateProcess();

        assertThat(broker.status()).isEqualTo(PartyStatus.ACTIVE);
    }

    @Test
    void shouldAssociateBrokerWithAgency() {
        Broker broker = broker();
        UUID agencyId = UUID.randomUUID();

        broker.associateWithAgency(agencyId, REGISTERED_AT.plusSeconds(60));

        assertThat(broker.realEstateAgencyId()).isEqualTo(agencyId);
    }

    private Broker broker() {
        return Broker.register(
                UUID.randomUUID(), UUID.randomUUID(), new Cpf("52998224725"),
                "Broker Name", new ContactInfo("broker@example.com", null), REGISTERED_AT);
    }
}
