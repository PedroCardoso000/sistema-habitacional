package com.esteirahabitacional.workflow.adapter.in.web;

import com.esteirahabitacional.workflow.application.port.in.EnsureInitialWorkflowModelUseCase;
import com.esteirahabitacional.workflow.application.port.in.ManageWorkflowJourneyUseCase;
import com.esteirahabitacional.workflow.application.port.in.QueryWorkflowJourneyUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations/{organizationId}")
class WorkflowController {
    private final EnsureInitialWorkflowModelUseCase ensureModel;
    private final ManageWorkflowJourneyUseCase management;
    private final QueryWorkflowJourneyUseCase queries;

    WorkflowController(EnsureInitialWorkflowModelUseCase ensureModel,
            ManageWorkflowJourneyUseCase management, QueryWorkflowJourneyUseCase queries) {
        this.ensureModel = ensureModel;
        this.management = management;
        this.queries = queries;
    }

    @PutMapping("/workflow/models/initial")
    ModelResponse ensureInitialModel(@PathVariable UUID organizationId) {
        return ModelResponse.from(ensureModel.execute(organizationId));
    }

    @GetMapping("/processes/{processId}/workflow")
    JourneyResponse find(
            @PathVariable UUID organizationId, @PathVariable UUID processId) {
        return JourneyResponse.from(queries.find(organizationId, processId));
    }

    @PatchMapping("/processes/{processId}/workflow/advance")
    MutationResponse advance(@PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody AdvanceRequest request) {
        return MutationResponse.from(management.advance(new ManageWorkflowJourneyUseCase.AdvanceCommand(
                organizationId, processId, request.satisfiedCriteria(), request.expectedVersion())));
    }

    @PatchMapping("/processes/{processId}/workflow/return")
    MutationResponse returnStage(
            @PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody MoveRequest request) {
        return MutationResponse.from(management.returnStage(new ManageWorkflowJourneyUseCase.MoveCommand(
                organizationId, processId, request.targetStageCode(), request.justification(),
                request.expectedVersion())));
    }

    @PatchMapping("/processes/{processId}/workflow/exception")
    MutationResponse exception(
            @PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody MoveRequest request) {
        return MutationResponse.from(management.moveWithException(new ManageWorkflowJourneyUseCase.MoveCommand(
                organizationId, processId, request.targetStageCode(), request.justification(),
                request.expectedVersion())));
    }

    @PatchMapping("/processes/{processId}/workflow/block")
    MutationResponse block(@PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody JustificationRequest request) {
        return MutationResponse.from(management.block(new ManageWorkflowJourneyUseCase.BlockCommand(
                organizationId, processId, request.justification(), request.expectedVersion())));
    }

    @PatchMapping("/processes/{processId}/workflow/unblock")
    MutationResponse unblock(@PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody JustificationRequest request) {
        return MutationResponse.from(management.unblock(new ManageWorkflowJourneyUseCase.BlockCommand(
                organizationId, processId, request.justification(), request.expectedVersion())));
    }

    @PutMapping("/processes/{processId}/workflow/next-action")
    MutationResponse defineNextAction(
            @PathVariable UUID organizationId, @PathVariable UUID processId,
            @Valid @RequestBody NextActionRequest request) {
        return MutationResponse.from(management.defineNextAction(new ManageWorkflowJourneyUseCase.NextActionCommand(
                organizationId, processId, request.description(), request.responsibleUserId(),
                request.dueAt(), request.expectedVersion())));
    }

    record AdvanceRequest(@NotNull Set<String> satisfiedCriteria, @PositiveOrZero long expectedVersion) {}
    record MoveRequest(@NotBlank String targetStageCode, @NotBlank String justification,
                       @PositiveOrZero long expectedVersion) {}
    record JustificationRequest(@NotBlank String justification, @PositiveOrZero long expectedVersion) {}
    record NextActionRequest(@NotBlank String description, @NotNull UUID responsibleUserId,
                             Instant dueAt, @PositiveOrZero long expectedVersion) {}
    record ModelResponse(UUID modelId, int version, int stageCount) {
        static ModelResponse from(EnsureInitialWorkflowModelUseCase.Result result) {
            return new ModelResponse(result.modelId(), result.version(), result.stageCount());
        }
    }
    record MutationResponse(UUID journeyId, String currentStageCode, long version) {
        static MutationResponse from(ManageWorkflowJourneyUseCase.Result result) {
            return new MutationResponse(result.journeyId(), result.currentStageCode(), result.version());
        }
    }
    record StageResponse(String code, String name, int position, String status,
                         Instant startedAt, Instant completedAt) {
        static StageResponse from(QueryWorkflowJourneyUseCase.Stage stage) {
            return new StageResponse(stage.code(), stage.name(), stage.position(), stage.status().name(),
                    stage.startedAt(), stage.completedAt());
        }
    }
    record TransitionResponse(int sequence, String type, String fromStageCode, String toStageCode,
                              String justification, UUID actorId, Instant occurredAt) {
        static TransitionResponse from(QueryWorkflowJourneyUseCase.Transition transition) {
            return new TransitionResponse(transition.sequence(), transition.type().name(),
                    transition.fromStageCode(), transition.toStageCode(), transition.justification(),
                    transition.actorId(), transition.occurredAt());
        }
    }
    record ActionResponse(String description, UUID responsibleUserId, Instant dueAt, Instant definedAt) {
        static ActionResponse from(QueryWorkflowJourneyUseCase.Action action) {
            return action == null ? null : new ActionResponse(action.description(), action.responsibleUserId(),
                    action.dueAt(), action.definedAt());
        }
    }
    record JourneyResponse(UUID id, UUID processId, UUID workflowModelId, int workflowVersion,
                           StageResponse currentStage, List<StageResponse> stages,
                           List<TransitionResponse> transitions, ActionResponse nextAction,
                           boolean missingNextAction, long version) {
        static JourneyResponse from(QueryWorkflowJourneyUseCase.Journey journey) {
            return new JourneyResponse(journey.id(), journey.processId(), journey.workflowModelId(),
                    journey.workflowVersion(), StageResponse.from(journey.currentStage()),
                    journey.stages().stream().map(StageResponse::from).toList(),
                    journey.transitions().stream().map(TransitionResponse::from).toList(),
                    ActionResponse.from(journey.nextAction()), journey.missingNextAction(), journey.version());
        }
    }
}
