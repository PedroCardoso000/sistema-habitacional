package com.esteirahabitacional.workflow.application.service;

import com.esteirahabitacional.financingprocess.FinancingProcessWorkflowLookup;
import com.esteirahabitacional.identityaccess.AuthorizeOrganizationUseCase;
import com.esteirahabitacional.identityaccess.InternalUserReferenceLookup;
import com.esteirahabitacional.shared.CurrentTimeProvider;
import com.esteirahabitacional.shared.DomainEventPublisher;
import com.esteirahabitacional.shared.IdentifierGenerator;
import com.esteirahabitacional.workflow.InitializeWorkflowForSubmissionUseCase;
import com.esteirahabitacional.workflow.application.port.in.EnsureInitialWorkflowModelUseCase;
import com.esteirahabitacional.workflow.application.port.in.ManageWorkflowJourneyUseCase;
import com.esteirahabitacional.workflow.application.port.in.QueryWorkflowJourneyUseCase;
import com.esteirahabitacional.workflow.application.port.out.WorkflowAudit;
import com.esteirahabitacional.workflow.application.port.out.WorkflowJourneyRepository;
import com.esteirahabitacional.workflow.application.port.out.WorkflowModelRepository;
import com.esteirahabitacional.workflow.domain.event.NextActionDefined;
import com.esteirahabitacional.workflow.domain.event.ProcessStageChanged;
import com.esteirahabitacional.workflow.domain.event.WorkflowInitialized;
import com.esteirahabitacional.workflow.domain.model.ProcessLifecycleState;
import com.esteirahabitacional.workflow.domain.model.StageTransition;
import com.esteirahabitacional.workflow.domain.model.WorkflowJourney;
import com.esteirahabitacional.workflow.domain.model.WorkflowModel;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class WorkflowService {
    private final AuthorizeOrganizationUseCase authorization;
    private final InternalUserReferenceLookup internalUsers;
    private final FinancingProcessWorkflowLookup processes;
    private final WorkflowModelRepository models;
    private final WorkflowJourneyRepository journeys;
    private final WorkflowAudit audit;
    private final DomainEventPublisher events;
    private final IdentifierGenerator identifiers;
    private final CurrentTimeProvider time;

    public WorkflowService(AuthorizeOrganizationUseCase authorization,
            InternalUserReferenceLookup internalUsers, FinancingProcessWorkflowLookup processes,
            WorkflowModelRepository models, WorkflowJourneyRepository journeys, WorkflowAudit audit,
            DomainEventPublisher events, IdentifierGenerator identifiers, CurrentTimeProvider time) {
        this.authorization = authorization;
        this.internalUsers = internalUsers;
        this.processes = processes;
        this.models = models;
        this.journeys = journeys;
        this.audit = audit;
        this.events = events;
        this.identifiers = identifiers;
        this.time = time;
    }

    public EnsureInitialWorkflowModelUseCase.Result ensureInitialModel(UUID organizationId) {
        authorization.require(organizationId, AuthorizeOrganizationUseCase.Action.MANAGE_PROCESSES);
        WorkflowModel model = ensureModel(organizationId);
        return new EnsureInitialWorkflowModelUseCase.Result(model.id(), model.version(), model.stages().size());
    }

    public InitializeWorkflowForSubmissionUseCase.Result initialize(
            InitializeWorkflowForSubmissionUseCase.Command command) {
        internalUsers.findActive(command.organizationId(), command.actorId());
        var process = processes.find(command.organizationId(), command.processId());
        WorkflowModel model = ensureModel(command.organizationId());
        Instant now = time.now();
        WorkflowJourney journey;
        try {
            journey = WorkflowJourney.initializeForSubmission(identifiers.generate(), command.organizationId(),
                    command.processId(), ProcessLifecycleState.valueOf(process.lifecycle().name()),
                    model, command.actorId(), now);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw WorkflowExceptions.invalid(exception.getMessage());
        }
        journey = journeys.insert(journey);
        audit.record(command.organizationId(), command.processId(), command.actorId(), "WORKFLOW_INITIALIZED", now);
        events.publish(List.of(new WorkflowInitialized(command.organizationId(), command.processId(), journey.id(),
                journey.workflowVersion(), command.actorId(), now)));
        return new InitializeWorkflowForSubmissionUseCase.Result(journey.id(), model.id(), model.version(),
                journey.currentStage().code());
    }

    public ManageWorkflowJourneyUseCase.Result advance(ManageWorkflowJourneyUseCase.AdvanceCommand command) {
        return mutate(command.organizationId(), command.processId(), command.expectedVersion(),
                AuthorizeOrganizationUseCase.Action.MANAGE_PROCESSES, "STAGE_ADVANCED",
                (journey, actor, now) -> journey.advance(command.satisfiedCriteria(), actor, now));
    }

    public ManageWorkflowJourneyUseCase.Result returnStage(ManageWorkflowJourneyUseCase.MoveCommand command) {
        return mutate(command.organizationId(), command.processId(), command.expectedVersion(),
                AuthorizeOrganizationUseCase.Action.AUTHORIZE_WORKFLOW_EXCEPTION, "STAGE_RETURNED",
                (journey, actor, now) -> journey.returnTo(
                        command.targetStageCode(), command.justification(), actor, now));
    }

    public ManageWorkflowJourneyUseCase.Result moveWithException(ManageWorkflowJourneyUseCase.MoveCommand command) {
        return mutate(command.organizationId(), command.processId(), command.expectedVersion(),
                AuthorizeOrganizationUseCase.Action.AUTHORIZE_WORKFLOW_EXCEPTION,
                "AUTHORIZED_STAGE_EXCEPTION", (journey, actor, now) -> journey.moveWithAuthorizedException(
                        command.targetStageCode(), command.justification(), actor, now));
    }

    public ManageWorkflowJourneyUseCase.Result block(ManageWorkflowJourneyUseCase.BlockCommand command) {
        return mutate(command.organizationId(), command.processId(), command.expectedVersion(),
                AuthorizeOrganizationUseCase.Action.MANAGE_PROCESSES, "STAGE_BLOCKED",
                (journey, actor, now) -> journey.blockCurrentStage(command.justification(), actor, now));
    }

    public ManageWorkflowJourneyUseCase.Result unblock(ManageWorkflowJourneyUseCase.BlockCommand command) {
        return mutate(command.organizationId(), command.processId(), command.expectedVersion(),
                AuthorizeOrganizationUseCase.Action.MANAGE_PROCESSES, "STAGE_UNBLOCKED",
                (journey, actor, now) -> journey.unblockCurrentStage(command.justification(), actor, now));
    }

    public ManageWorkflowJourneyUseCase.Result defineNextAction(
            ManageWorkflowJourneyUseCase.NextActionCommand command) {
        return mutateAction(command);
    }

    public QueryWorkflowJourneyUseCase.Journey find(UUID organizationId, UUID processId) {
        authorization.require(organizationId, AuthorizeOrganizationUseCase.Action.VIEW_PROCESSES);
        return result(load(organizationId, processId));
    }

    private ManageWorkflowJourneyUseCase.Result mutateAction(
            ManageWorkflowJourneyUseCase.NextActionCommand command) {
        var actor = authorization.require(
                command.organizationId(), AuthorizeOrganizationUseCase.Action.MANAGE_PROCESSES);
        WorkflowJourney journey = loadVersion(command.organizationId(), command.processId(), command.expectedVersion());
        internalUsers.findActive(command.organizationId(), command.responsibleUserId());
        Instant now = time.now();
        try {
            journey.defineNextAction(command.description(), command.responsibleUserId(), command.dueAt(), now);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw WorkflowExceptions.invalid(exception.getMessage());
        }
        journey = journeys.update(journey, command.expectedVersion());
        audit.record(command.organizationId(), command.processId(), actor.userId(), "NEXT_ACTION_DEFINED", now);
        events.publish(List.of(new NextActionDefined(command.organizationId(), command.processId(),
                command.responsibleUserId(), command.dueAt(), actor.userId(), now)));
        return managementResult(journey);
    }

    private ManageWorkflowJourneyUseCase.Result mutate(UUID organizationId, UUID processId,
            long expectedVersion, AuthorizeOrganizationUseCase.Action requiredAction,
            String action, Mutation mutation) {
        var actor = authorization.require(organizationId, requiredAction);
        WorkflowJourney journey = loadVersion(organizationId, processId, expectedVersion);
        Instant now = time.now();
        try {
            mutation.apply(journey, actor.userId(), now);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw WorkflowExceptions.invalid(exception.getMessage());
        }
        journey = journeys.update(journey, expectedVersion);
        StageTransition transition = journey.transitions().getLast();
        audit.record(organizationId, processId, actor.userId(), action, now);
        events.publish(List.of(new ProcessStageChanged(organizationId, processId,
                transition.fromStageCode(), transition.toStageCode(), transition.type(), actor.userId(), now)));
        return managementResult(journey);
    }

    private WorkflowModel ensureModel(UUID organizationId) {
        return models.findActive(organizationId).orElseGet(() -> models.insert(
                WorkflowModel.initialMvp(identifiers.generate(), organizationId, time.now())));
    }

    private WorkflowJourney loadVersion(UUID organizationId, UUID processId, long expectedVersion) {
        WorkflowJourney journey = load(organizationId, processId);
        if (journey.version() != expectedVersion) {
            throw WorkflowExceptions.conflict();
        }
        return journey;
    }

    private WorkflowJourney load(UUID organizationId, UUID processId) {
        return journeys.findByProcess(organizationId, processId).orElseThrow(WorkflowExceptions::notFound);
    }

    private static ManageWorkflowJourneyUseCase.Result managementResult(WorkflowJourney journey) {
        return new ManageWorkflowJourneyUseCase.Result(
                journey.id(), journey.currentStage().code(), journey.version());
    }

    private static QueryWorkflowJourneyUseCase.Journey result(WorkflowJourney journey) {
        var stages = journey.stages().stream().map(stage -> new QueryWorkflowJourneyUseCase.Stage(
                stage.code(), stage.name(), stage.position(), stage.status(),
                stage.startedAt(), stage.completedAt())).toList();
        var transitions = journey.transitions().stream().map(item -> new QueryWorkflowJourneyUseCase.Transition(
                item.sequence(), item.type(), item.fromStageCode(), item.toStageCode(),
                item.justification(), item.actorId(), item.occurredAt())).toList();
        QueryWorkflowJourneyUseCase.Action action = journey.nextAction() == null ? null
                : new QueryWorkflowJourneyUseCase.Action(journey.nextAction().description(),
                        journey.nextAction().responsibleUserId(), journey.nextAction().dueAt(),
                        journey.nextAction().definedAt());
        var current = journey.currentStage();
        return new QueryWorkflowJourneyUseCase.Journey(journey.id(), journey.processId(),
                journey.workflowModelId(), journey.workflowVersion(), new QueryWorkflowJourneyUseCase.Stage(
                        current.code(), current.name(), current.position(), current.status(),
                        current.startedAt(), current.completedAt()), stages, transitions,
                action, action == null, journey.version());
    }

    private interface Mutation {
        void apply(WorkflowJourney journey, UUID actorId, Instant occurredAt);
    }
}
