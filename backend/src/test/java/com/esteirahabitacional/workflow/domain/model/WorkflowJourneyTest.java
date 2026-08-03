package com.esteirahabitacional.workflow.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WorkflowJourneyTest {
    private static final UUID ORGANIZATION = UUID.randomUUID();
    private static final UUID PROCESS = UUID.randomUUID();
    private static final UUID ACTOR = UUID.randomUUID();

    @Test
    void shouldAdvanceAndPreserveTransitionHistory() {
        WorkflowJourney journey = activeJourney(model(Set.of()));

        journey.advance(Set.of(), ACTOR, Instant.EPOCH.plusSeconds(1));

        assertThat(journey.currentStage().code()).isEqualTo("SECOND");
        assertThat(journey.stages().getFirst().status()).isEqualTo(StageStatus.COMPLETED);
        assertThat(journey.transitions()).extracting(StageTransition::type)
                .containsExactly(TransitionType.INITIALIZED, TransitionType.ADVANCED);
    }

    @Test
    void shouldRejectAdvanceWhenCriterionIsMissingOrStageIsBlocked() {
        WorkflowJourney missingCriterion = activeJourney(model(Set.of("CLIENT_IDENTIFIED")));
        assertThatThrownBy(() -> missingCriterion.advance(Set.of(), ACTOR, Instant.EPOCH.plusSeconds(1)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("criteria");

        WorkflowJourney blocked = activeJourney(model(Set.of()));
        blocked.blockCurrentStage("Aguardando validação", ACTOR, Instant.EPOCH.plusSeconds(1));
        assertThatThrownBy(() -> blocked.advance(Set.of(), ACTOR, Instant.EPOCH.plusSeconds(2)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Blocked");
        blocked.unblockCurrentStage("Validação concluída", ACTOR, Instant.EPOCH.plusSeconds(3));
        assertThat(blocked.currentStage().status()).isEqualTo(StageStatus.CURRENT);
    }

    @Test
    void shouldRequireJustificationForExceptionAndPreserveReturn() {
        WorkflowJourney journey = activeJourney(model(Set.of()));
        assertThatThrownBy(() -> journey.moveWithAuthorizedException(
                "SECOND", " ", ACTOR, Instant.EPOCH.plusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Justification");

        journey.advance(Set.of(), ACTOR, Instant.EPOCH.plusSeconds(1));
        journey.returnTo("FIRST", "Reanálise autorizada", ACTOR, Instant.EPOCH.plusSeconds(2));

        assertThat(journey.currentStage().code()).isEqualTo("FIRST");
        assertThat(journey.transitions().getLast().type()).isEqualTo(TransitionType.RETURNED);
        assertThat(journey.transitions().getLast().justification()).isEqualTo("Reanálise autorizada");
    }

    @Test
    void shouldRequireResponsibleForNextAction() {
        WorkflowJourney journey = activeJourney(model(Set.of()));
        assertThatThrownBy(() -> journey.defineNextAction(
                "Solicitar documentos", null, null, Instant.EPOCH.plusSeconds(1)))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("responsible");
    }

    @Test
    void shouldRejectWorkflowInitializationForDraft() {
        assertThatThrownBy(() -> WorkflowJourney.initializeForSubmission(UUID.randomUUID(), ORGANIZATION,
                PROCESS, ProcessLifecycleState.DRAFT, model(Set.of()), ACTOR, Instant.EPOCH))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Draft");
    }

    @Test
    void shouldBuildVersionedInitialMvpModelInOnePlace() {
        WorkflowModel model = WorkflowModel.initialMvp(UUID.randomUUID(), ORGANIZATION, Instant.EPOCH);
        assertThat(model.version()).isEqualTo(1);
        assertThat(model.stages()).hasSize(6);
        assertThat(model.stages()).extracting(WorkflowStageDefinition::position)
                .containsExactly(1, 2, 3, 4, 5, 6);
    }

    private WorkflowJourney activeJourney(WorkflowModel model) {
        return WorkflowJourney.initializeForSubmission(UUID.randomUUID(), ORGANIZATION, PROCESS,
                ProcessLifecycleState.ACTIVE, model, ACTOR, Instant.EPOCH);
    }

    private WorkflowModel model(Set<String> firstCriteria) {
        return new WorkflowModel(UUID.randomUUID(), ORGANIZATION, 1, "Test model", List.of(
                new WorkflowStageDefinition("FIRST", "First", 1, firstCriteria),
                new WorkflowStageDefinition("SECOND", "Second", 2, Set.of()),
                new WorkflowStageDefinition("THIRD", "Third", 3, Set.of())), Instant.EPOCH);
    }
}
