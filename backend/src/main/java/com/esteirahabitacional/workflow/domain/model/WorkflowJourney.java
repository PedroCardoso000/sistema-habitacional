package com.esteirahabitacional.workflow.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class WorkflowJourney {
    private final UUID id;
    private final UUID organizationId;
    private final UUID processId;
    private final UUID workflowModelId;
    private final int workflowVersion;
    private final List<WorkflowStageDefinition> definitions;
    private final List<ProcessStage> stages;
    private final List<StageTransition> transitions;
    private final Instant initializedAt;
    private NextAction nextAction;
    private long version;
    private Instant updatedAt;

    private WorkflowJourney(UUID id, UUID organizationId, UUID processId, UUID workflowModelId,
            int workflowVersion, List<WorkflowStageDefinition> definitions, List<ProcessStage> stages,
            List<StageTransition> transitions, NextAction nextAction, long version,
            Instant initializedAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.processId = Objects.requireNonNull(processId, "processId is required");
        this.workflowModelId = Objects.requireNonNull(workflowModelId, "workflowModelId is required");
        this.workflowVersion = workflowVersion;
        this.definitions = List.copyOf(definitions);
        this.stages = new ArrayList<>(stages);
        this.transitions = new ArrayList<>(transitions);
        this.nextAction = nextAction;
        this.version = version;
        this.initializedAt = Objects.requireNonNull(initializedAt, "initializedAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static WorkflowJourney initializeForSubmission(UUID id, UUID organizationId, UUID processId,
            ProcessLifecycleState processState, WorkflowModel model, UUID actorId, Instant occurredAt) {
        if (processState == ProcessLifecycleState.DRAFT) {
            throw new IllegalStateException("Draft process cannot receive a workflow");
        }
        List<ProcessStage> stages = model.stages().stream()
                .map(stage -> new ProcessStage(stage.code(), stage.name(), stage.position(),
                        stage.position() == 1 ? StageStatus.CURRENT : StageStatus.PENDING,
                        stage.position() == 1 ? occurredAt : null, null))
                .toList();
        StageTransition initialized = new StageTransition(1, TransitionType.INITIALIZED, null,
                stages.getFirst().code(), null, actorId, occurredAt);
        return new WorkflowJourney(id, organizationId, processId, model.id(), model.version(),
                model.stages(), stages, List.of(initialized), null, 0, occurredAt, occurredAt);
    }

    public static WorkflowJourney restore(UUID id, UUID organizationId, UUID processId,
            UUID workflowModelId, int workflowVersion, List<WorkflowStageDefinition> definitions,
            List<ProcessStage> stages, List<StageTransition> transitions, NextAction nextAction,
            long version, Instant initializedAt, Instant updatedAt) {
        return new WorkflowJourney(id, organizationId, processId, workflowModelId, workflowVersion,
                definitions, stages, transitions, nextAction, version, initializedAt, updatedAt);
    }

    public void advance(Set<String> satisfiedCriteria, UUID actorId, Instant occurredAt) {
        ProcessStage current = currentStage();
        ensureNotBlocked(current);
        WorkflowStageDefinition definition = definition(current.code());
        if (!definition.canExitWith(Set.copyOf(satisfiedCriteria))) {
            throw new IllegalStateException("Required exit criteria are not satisfied");
        }
        int currentIndex = current.position() - 1;
        if (currentIndex == stages.size() - 1) {
            throw new IllegalStateException("Workflow is already at its final stage");
        }
        ProcessStage next = stages.get(currentIndex + 1);
        stages.set(currentIndex, current.withStatus(StageStatus.COMPLETED, occurredAt));
        stages.set(currentIndex + 1, next.withStatus(StageStatus.CURRENT, occurredAt));
        nextAction = null;
        transition(TransitionType.ADVANCED, current.code(), next.code(), null, actorId, occurredAt);
    }

    public void returnTo(String targetStageCode, String justification, UUID actorId, Instant occurredAt) {
        ProcessStage current = currentStage();
        ProcessStage target = stage(targetStageCode);
        if (target.position() >= current.position()) {
            throw new IllegalStateException("Return target must be before the current stage");
        }
        String reason = requireJustification(justification);
        moveTo(target, occurredAt);
        transition(TransitionType.RETURNED, current.code(), target.code(), reason, actorId, occurredAt);
    }

    public void moveWithAuthorizedException(
            String targetStageCode, String justification, UUID actorId, Instant occurredAt) {
        ProcessStage current = currentStage();
        ProcessStage target = stage(targetStageCode);
        if (target.code().equals(current.code())) {
            throw new IllegalStateException("Exception target must differ from current stage");
        }
        String reason = requireJustification(justification);
        moveTo(target, occurredAt);
        transition(TransitionType.AUTHORIZED_EXCEPTION, current.code(), target.code(), reason, actorId, occurredAt);
    }

    public void blockCurrentStage(String justification, UUID actorId, Instant occurredAt) {
        ProcessStage current = currentStage();
        if (current.status() == StageStatus.BLOCKED) {
            throw new IllegalStateException("Current stage is already blocked");
        }
        String reason = requireJustification(justification);
        stages.set(current.position() - 1, current.withStatus(StageStatus.BLOCKED, occurredAt));
        transition(TransitionType.BLOCKED, current.code(), current.code(), reason, actorId, occurredAt);
    }

    public void unblockCurrentStage(String justification, UUID actorId, Instant occurredAt) {
        ProcessStage current = currentStage();
        if (current.status() != StageStatus.BLOCKED) {
            throw new IllegalStateException("Current stage is not blocked");
        }
        String reason = requireJustification(justification);
        stages.set(current.position() - 1, current.withStatus(StageStatus.CURRENT, occurredAt));
        transition(TransitionType.UNBLOCKED, current.code(), current.code(), reason, actorId, occurredAt);
    }

    public void defineNextAction(String description, UUID responsibleUserId, Instant dueAt, Instant occurredAt) {
        nextAction = new NextAction(description, responsibleUserId, dueAt, occurredAt);
        updatedAt = occurredAt;
    }

    public ProcessStage currentStage() {
        return stages.stream().filter(stage -> stage.status() == StageStatus.CURRENT
                        || stage.status() == StageStatus.BLOCKED).findFirst()
                .orElseThrow(() -> new IllegalStateException("Workflow has no current stage"));
    }

    private void moveTo(ProcessStage target, Instant occurredAt) {
        for (int index = 0; index < stages.size(); index++) {
            ProcessStage stage = stages.get(index);
            StageStatus status = stage.position() < target.position() ? StageStatus.COMPLETED
                    : stage.position() == target.position() ? StageStatus.CURRENT : StageStatus.PENDING;
            stages.set(index, stage.withStatus(status, occurredAt));
        }
        nextAction = null;
    }

    private void transition(TransitionType type, String from, String to, String justification,
            UUID actorId, Instant occurredAt) {
        transitions.add(new StageTransition(transitions.size() + 1, type, from, to,
                justification, actorId, occurredAt));
        updatedAt = occurredAt;
    }

    private WorkflowStageDefinition definition(String code) {
        return definitions.stream().filter(item -> item.code().equals(code)).findFirst().orElseThrow();
    }
    private ProcessStage stage(String code) {
        return stages.stream().filter(item -> item.code().equals(code)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown process stage: " + code));
    }
    private void ensureNotBlocked(ProcessStage stage) {
        if (stage.status() == StageStatus.BLOCKED) {
            throw new IllegalStateException("Blocked stage cannot advance");
        }
    }
    private String requireJustification(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Justification is required");
        }
        return value.trim();
    }

    public UUID id() { return id; }
    public UUID organizationId() { return organizationId; }
    public UUID processId() { return processId; }
    public UUID workflowModelId() { return workflowModelId; }
    public int workflowVersion() { return workflowVersion; }
    public List<WorkflowStageDefinition> definitions() { return definitions; }
    public List<ProcessStage> stages() { return List.copyOf(stages); }
    public List<StageTransition> transitions() { return List.copyOf(transitions); }
    public NextAction nextAction() { return nextAction; }
    public long version() { return version; }
    public Instant initializedAt() { return initializedAt; }
    public Instant updatedAt() { return updatedAt; }
    public void persistedAtVersion(long persistedVersion) { version = persistedVersion; }
}
