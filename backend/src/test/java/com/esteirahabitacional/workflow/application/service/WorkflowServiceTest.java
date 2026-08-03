package com.esteirahabitacional.workflow.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.esteirahabitacional.financingprocess.FinancingProcessWorkflowLookup;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.InternalUserReferenceLookup;
import com.esteirahabitacional.shared.ApplicationException;
import com.esteirahabitacional.shared.DomainEvent;
import com.esteirahabitacional.shared.DomainEventPublisher;
import com.esteirahabitacional.workflow.InitializeWorkflowForSubmissionUseCase;
import com.esteirahabitacional.workflow.application.port.in.ManageWorkflowJourneyUseCase;
import com.esteirahabitacional.workflow.application.port.out.WorkflowAudit;
import com.esteirahabitacional.workflow.application.port.out.WorkflowJourneyRepository;
import com.esteirahabitacional.workflow.application.port.out.WorkflowModelRepository;
import com.esteirahabitacional.workflow.domain.event.ProcessStageChanged;
import com.esteirahabitacional.workflow.domain.model.ProcessLifecycleState;
import com.esteirahabitacional.workflow.domain.model.WorkflowJourney;
import com.esteirahabitacional.workflow.domain.model.WorkflowModel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WorkflowServiceTest {
    private static final UUID ORGANIZATION = UUID.randomUUID();
    private static final UUID PROCESS = UUID.randomUUID();
    private static final UUID ACTOR = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-08-03T12:00:00Z");

    @Test
    void shouldRejectDraftInitializationWithoutPersistingJourney() {
        RecordingRepository repository = new RecordingRepository();
        WorkflowService service = service(repository, FinancingProcessWorkflowLookup.Lifecycle.DRAFT,
                new RecordingAudit(), new RecordingEvents());

        assertThatThrownBy(() -> service.initialize(
                new InitializeWorkflowForSubmissionUseCase.Command(ORGANIZATION, PROCESS, ACTOR)))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).status()).isEqualTo(422);
        assertThat(repository.journey).isNull();
    }

    @Test
    void shouldPersistAuditAndPublishEventForValidAdvance() {
        RecordingRepository repository = new RecordingRepository();
        WorkflowModel model = WorkflowModel.initialMvp(UUID.randomUUID(), ORGANIZATION, NOW);
        repository.model = model;
        repository.journey = WorkflowJourney.initializeForSubmission(UUID.randomUUID(), ORGANIZATION, PROCESS,
                ProcessLifecycleState.ACTIVE, model, ACTOR, NOW.minusSeconds(1));
        RecordingAudit audit = new RecordingAudit();
        RecordingEvents events = new RecordingEvents();
        WorkflowService service = service(repository, FinancingProcessWorkflowLookup.Lifecycle.ACTIVE, audit, events);

        ManageWorkflowJourneyUseCase.Result result = service.advance(
                new ManageWorkflowJourneyUseCase.AdvanceCommand(ORGANIZATION, PROCESS, Set.of(), 0));

        assertThat(result.currentStageCode()).isEqualTo("BUYER_DOCUMENTS");
        assertThat(result.version()).isEqualTo(1);
        assertThat(audit.action).isEqualTo("STAGE_ADVANCED");
        assertThat(events.values).singleElement().isInstanceOf(ProcessStageChanged.class);
    }

    private WorkflowService service(RecordingRepository repository,
            FinancingProcessWorkflowLookup.Lifecycle lifecycle, RecordingAudit audit, RecordingEvents events) {
        AuthorizeOrganizationUseCase authorization = (organizationId, action) ->
                new AuthorizeOrganizationUseCase.AuthorizedActor(ACTOR, organizationId);
        InternalUserReferenceLookup users = (organizationId, userId) ->
                new InternalUserReferenceLookup.Reference(userId, organizationId, "Internal User");
        FinancingProcessWorkflowLookup processes = (organizationId, processId) ->
                new FinancingProcessWorkflowLookup.Reference(processId, organizationId, lifecycle);
        return new WorkflowService(authorization, users, processes, repository, repository, audit,
                events, UUID::randomUUID, () -> NOW);
    }

    private static final class RecordingRepository
            implements WorkflowModelRepository, WorkflowJourneyRepository {
        private WorkflowModel model;
        private WorkflowJourney journey;
        @Override public Optional<WorkflowModel> findActive(UUID organizationId) { return Optional.ofNullable(model); }
        @Override public WorkflowModel insert(WorkflowModel value) {
            model = value;
            return value;
        }
        @Override public Optional<WorkflowJourney> findByProcess(UUID organizationId, UUID processId) {
            return Optional.ofNullable(journey);
        }
        @Override public WorkflowJourney insert(WorkflowJourney value) {
            journey = value;
            return value;
        }
        @Override public WorkflowJourney update(WorkflowJourney value, long expectedVersion) {
            value.persistedAtVersion(expectedVersion + 1);
            journey = value;
            return value;
        }
    }

    private static final class RecordingAudit implements WorkflowAudit {
        private String action;
        @Override public void record(UUID organizationId, UUID processId, UUID actorId,
                String value, Instant occurredAt) { action = value; }
    }

    private static final class RecordingEvents implements DomainEventPublisher {
        private final List<DomainEvent> values = new ArrayList<>();
        @Override public void publish(Collection<? extends DomainEvent> events) { values.addAll(events); }
    }
}
