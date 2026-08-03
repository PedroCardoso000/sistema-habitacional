package com.esteirahabitacional.parties.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.parties.application.port.in.RegisterClientUseCase;
import com.esteirahabitacional.parties.application.port.out.BrokerRepository;
import com.esteirahabitacional.parties.application.port.out.ClientRepository;
import com.esteirahabitacional.parties.application.port.out.PartyAudit;
import com.esteirahabitacional.parties.domain.model.Broker;
import com.esteirahabitacional.parties.domain.model.Client;
import com.esteirahabitacional.parties.domain.model.ContactInfo;
import com.esteirahabitacional.parties.domain.model.Cpf;
import com.esteirahabitacional.shared.ApplicationException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PartyApplicationServiceTest {

    private static final UUID ORGANIZATION_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();

    @Test
    void shouldRejectDuplicateClientBeforePersistingOrAuditing() {
        RecordingClientRepository clients = new RecordingClientRepository(true);
        RecordingAudit audit = new RecordingAudit();
        ClientManagementService service = new ClientManagementService(
                authorization(), clients, audit, UUID::randomUUID, () -> Instant.EPOCH);

        assertThatThrownBy(() -> service.execute(new RegisterClientUseCase.Command(
                        ORGANIZATION_ID, "52998224725", "Client", "client@example.com", null)))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).status())
                .isEqualTo(409);
        assertThat(clients.saved).isNull();
        assertThat(audit.action).isNull();
    }

    @Test
    void shouldReturnMinimalClientResultAndAuditSuccessfulRegistration() {
        RecordingClientRepository clients = new RecordingClientRepository(false);
        RecordingAudit audit = new RecordingAudit();
        ClientManagementService service = new ClientManagementService(
                authorization(), clients, audit, UUID::randomUUID, () -> Instant.EPOCH);

        RegisterClientUseCase.Result result = service.execute(new RegisterClientUseCase.Command(
                ORGANIZATION_ID, "52998224725", "Client", "client@example.com", null));

        assertThat(result.fullName()).isEqualTo("Client");
        assertThat(clients.saved).isNotNull();
        assertThat(audit.action.actorUserId()).isEqualTo(ACTOR_ID);
        assertThat(audit.action.organizationId()).isEqualTo(ORGANIZATION_ID);
    }

    @Test
    void shouldRejectInactiveBrokerReferenceForNewProcess() {
        Broker broker = Broker.register(
                UUID.randomUUID(), ORGANIZATION_ID, new Cpf("52998224725"), "Broker",
                new ContactInfo("broker@example.com", null), Instant.EPOCH);
        broker.deactivate(Instant.EPOCH.plusSeconds(1));
        PartyReferenceService service = new PartyReferenceService(
                new RecordingClientRepository(false), new FixedBrokerRepository(broker));

        assertThatThrownBy(() -> service.findActive(ORGANIZATION_ID, broker.id()))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).status())
                .isEqualTo(422);
    }

    private AuthorizeOrganizationUseCase authorization() {
        return (organizationId, action) -> new AuthorizeOrganizationUseCase.AuthorizedActor(
                ACTOR_ID, organizationId);
    }

    private static final class RecordingClientRepository implements ClientRepository {

        private final boolean duplicate;
        private Client saved;

        private RecordingClientRepository(boolean duplicate) {
            this.duplicate = duplicate;
        }

        @Override public boolean existsByCpf(UUID organizationId, Cpf cpf) { return duplicate; }
        @Override public Optional<Client> findByCpf(UUID organizationId, Cpf cpf) { return Optional.empty(); }
        @Override public Optional<Client> findById(UUID organizationId, UUID clientId) { return Optional.empty(); }
        @Override public void save(Client client) { saved = client; }
    }

    private static final class RecordingAudit implements PartyAudit {

        private Action action;

        @Override public void record(Action value) { action = value; }
    }

    private record FixedBrokerRepository(Broker broker) implements BrokerRepository {

        @Override public boolean existsByCpf(UUID organizationId, Cpf cpf) { return false; }
        @Override public Optional<Broker> findById(UUID organizationId, UUID brokerId) {
            return broker.organizationId().equals(organizationId) && broker.id().equals(brokerId)
                    ? Optional.of(broker) : Optional.empty();
        }
        @Override public void save(Broker value) {}
    }
}
