package com.esteirahabitacional.workflow.adapter.out.persistence;

import com.esteirahabitacional.shared.ApplicationException;
import com.esteirahabitacional.workflow.application.port.out.WorkflowJourneyRepository;
import com.esteirahabitacional.workflow.application.port.out.WorkflowModelRepository;
import com.esteirahabitacional.workflow.domain.model.NextAction;
import com.esteirahabitacional.workflow.domain.model.ProcessStage;
import com.esteirahabitacional.workflow.domain.model.StageStatus;
import com.esteirahabitacional.workflow.domain.model.StageTransition;
import com.esteirahabitacional.workflow.domain.model.TransitionType;
import com.esteirahabitacional.workflow.domain.model.WorkflowJourney;
import com.esteirahabitacional.workflow.domain.model.WorkflowModel;
import com.esteirahabitacional.workflow.domain.model.WorkflowStageDefinition;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;

public final class JdbcWorkflowRepository implements WorkflowModelRepository, WorkflowJourneyRepository {
    private final JdbcClient jdbc;
    public JdbcWorkflowRepository(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Override
    public Optional<WorkflowModel> findActive(UUID organizationId) {
        return jdbc.sql("SELECT id, organization_id, version, name, created_at FROM workflow_models "
                        + "WHERE organization_id = :organizationId AND active")
                .param("organizationId", organizationId).query((result, row) -> mapModel(result)).optional();
    }

    @Override
    public WorkflowModel insert(WorkflowModel model) {
        int inserted = jdbc.sql("INSERT INTO workflow_models (id, organization_id, version, name, active, created_at) "
                            + "VALUES (:id, :organizationId, :version, :name, TRUE, :createdAt) "
                            + "ON CONFLICT DO NOTHING")
                    .param("id", model.id()).param("organizationId", model.organizationId())
                    .param("version", model.version()).param("name", model.name())
                    .param("createdAt", time(model.createdAt())).update();
        if (inserted == 0) {
            return findActive(model.organizationId()).orElseThrow();
        }
        for (WorkflowStageDefinition stage : model.stages()) {
            jdbc.sql("INSERT INTO workflow_stage_definitions (model_id, organization_id, stage_code, "
                            + "stage_name, position, required_exit_criteria) VALUES (:modelId, "
                            + ":organizationId, :code, :name, :position, :criteria)")
                    .param("modelId", model.id()).param("organizationId", model.organizationId())
                    .param("code", stage.code()).param("name", stage.name()).param("position", stage.position())
                    .param("criteria", stage.requiredExitCriteria().toArray(String[]::new)).update();
        }
        return model;
    }

    @Override
    public Optional<WorkflowJourney> findByProcess(UUID organizationId, UUID processId) {
        return jdbc.sql("SELECT id, organization_id, process_id, workflow_model_id, workflow_version, "
                        + "next_action_description, next_action_responsible_id, next_action_due_at, "
                        + "next_action_defined_at, version, initialized_at, updated_at FROM workflow_journeys "
                        + "WHERE organization_id = :organizationId AND process_id = :processId")
                .param("organizationId", organizationId).param("processId", processId)
                .query((result, row) -> mapJourney(result)).optional();
    }

    @Override
    public WorkflowJourney insert(WorkflowJourney journey) {
        try {
            jdbc.sql("INSERT INTO workflow_journeys (id, organization_id, process_id, workflow_model_id, "
                            + "workflow_version, version, initialized_at, updated_at) VALUES (:id, "
                            + ":organizationId, :processId, :modelId, :modelVersion, 0, :initializedAt, :updatedAt)")
                    .param("id", journey.id()).param("organizationId", journey.organizationId())
                    .param("processId", journey.processId()).param("modelId", journey.workflowModelId())
                    .param("modelVersion", journey.workflowVersion()).param("initializedAt", time(journey.initializedAt()))
                    .param("updatedAt", time(journey.updatedAt())).update();
            persistChildren(journey);
            return journey;
        } catch (DuplicateKeyException exception) {
            throw new ApplicationException(409, "workflow-already-initialized", "Jornada já inicializada",
                    "O processo já possui uma jornada operacional.");
        }
    }

    @Override
    public WorkflowJourney update(WorkflowJourney journey, long expectedVersion) {
        NextAction action = journey.nextAction();
        int changed = jdbc.sql("UPDATE workflow_journeys SET next_action_description = :description, "
                        + "next_action_responsible_id = :responsible, next_action_due_at = :dueAt, "
                        + "next_action_defined_at = :definedAt, version = version + 1, updated_at = :updatedAt "
                        + "WHERE organization_id = :organizationId AND process_id = :processId "
                        + "AND version = :expectedVersion")
                .param("description", action == null ? null : action.description())
                .param("responsible", action == null ? null : action.responsibleUserId())
                .param("dueAt", action == null || action.dueAt() == null ? null : time(action.dueAt()))
                .param("definedAt", action == null ? null : time(action.definedAt()))
                .param("updatedAt", time(journey.updatedAt())).param("organizationId", journey.organizationId())
                .param("processId", journey.processId()).param("expectedVersion", expectedVersion).update();
        if (changed == 0) {
            throw new ApplicationException(409, "workflow-version-conflict", "Conflito de versão",
                    "A jornada foi alterada por outra operação. Recarregue os dados e tente novamente.");
        }
        persistChildren(journey);
        journey.persistedAtVersion(expectedVersion + 1);
        return journey;
    }

    private WorkflowModel mapModel(ResultSet result) throws SQLException {
        UUID modelId = result.getObject("id", UUID.class);
        UUID organizationId = result.getObject("organization_id", UUID.class);
        return new WorkflowModel(modelId, organizationId, result.getInt("version"), result.getString("name"),
                definitions(modelId), instant(result, "created_at"));
    }

    private WorkflowJourney mapJourney(ResultSet result) throws SQLException {
        UUID journeyId = result.getObject("id", UUID.class);
        UUID organizationId = result.getObject("organization_id", UUID.class);
        UUID modelId = result.getObject("workflow_model_id", UUID.class);
        List<ProcessStage> stages = jdbc.sql("SELECT stage_code, stage_name, position, status, started_at, "
                        + "completed_at FROM workflow_process_stages WHERE journey_id = :journeyId "
                        + "AND organization_id = :organizationId ORDER BY position")
                .param("journeyId", journeyId).param("organizationId", organizationId)
                .query((row, number) -> new ProcessStage(row.getString("stage_code"),
                        row.getString("stage_name"), row.getInt("position"),
                        StageStatus.valueOf(row.getString("status")), nullableInstant(row, "started_at"),
                        nullableInstant(row, "completed_at"))).list();
        List<StageTransition> transitions = jdbc.sql("SELECT sequence, transition_type, from_stage_code, "
                        + "to_stage_code, justification, actor_id, occurred_at FROM workflow_stage_transitions "
                        + "WHERE journey_id = :journeyId AND organization_id = :organizationId ORDER BY sequence")
                .param("journeyId", journeyId).param("organizationId", organizationId)
                .query((row, number) -> new StageTransition(row.getInt("sequence"),
                        TransitionType.valueOf(row.getString("transition_type")),
                        row.getString("from_stage_code"), row.getString("to_stage_code"),
                        row.getString("justification"), row.getObject("actor_id", UUID.class),
                        instant(row, "occurred_at"))).list();
        String description = result.getString("next_action_description");
        NextAction action = description == null ? null : new NextAction(description,
                result.getObject("next_action_responsible_id", UUID.class),
                nullableInstant(result, "next_action_due_at"), instant(result, "next_action_defined_at"));
        return WorkflowJourney.restore(journeyId, organizationId, result.getObject("process_id", UUID.class),
                modelId, result.getInt("workflow_version"), definitions(modelId), stages, transitions, action,
                result.getLong("version"), instant(result, "initialized_at"), instant(result, "updated_at"));
    }

    private List<WorkflowStageDefinition> definitions(UUID modelId) {
        return jdbc.sql("SELECT stage_code, stage_name, position, required_exit_criteria "
                        + "FROM workflow_stage_definitions WHERE model_id = :modelId ORDER BY position")
                .param("modelId", modelId).query((result, row) -> new WorkflowStageDefinition(
                        result.getString("stage_code"), result.getString("stage_name"), result.getInt("position"),
                        stringSet(result.getArray("required_exit_criteria")))).list();
    }

    private void persistChildren(WorkflowJourney journey) {
        for (ProcessStage stage : journey.stages()) {
            jdbc.sql("INSERT INTO workflow_process_stages (journey_id, organization_id, stage_code, "
                            + "stage_name, position, status, started_at, completed_at) VALUES (:journeyId, "
                            + ":organizationId, :code, :name, :position, :status, :startedAt, :completedAt) "
                            + "ON CONFLICT (journey_id, stage_code) DO UPDATE SET status = EXCLUDED.status, "
                            + "started_at = EXCLUDED.started_at, completed_at = EXCLUDED.completed_at")
                    .param("journeyId", journey.id()).param("organizationId", journey.organizationId())
                    .param("code", stage.code()).param("name", stage.name()).param("position", stage.position())
                    .param("status", stage.status().name())
                    .param("startedAt", stage.startedAt() == null ? null : time(stage.startedAt()))
                    .param("completedAt", stage.completedAt() == null ? null : time(stage.completedAt())).update();
        }
        for (StageTransition transition : journey.transitions()) {
            jdbc.sql("INSERT INTO workflow_stage_transitions (journey_id, organization_id, sequence, "
                            + "transition_type, from_stage_code, to_stage_code, justification, actor_id, occurred_at) "
                            + "VALUES (:journeyId, :organizationId, :sequence, :type, :fromStage, :toStage, "
                            + ":justification, :actorId, :occurredAt) ON CONFLICT DO NOTHING")
                    .param("journeyId", journey.id()).param("organizationId", journey.organizationId())
                    .param("sequence", transition.sequence()).param("type", transition.type().name())
                    .param("fromStage", transition.fromStageCode()).param("toStage", transition.toStageCode())
                    .param("justification", transition.justification()).param("actorId", transition.actorId())
                    .param("occurredAt", time(transition.occurredAt())).update();
        }
    }

    private static Set<String> stringSet(Array array) throws SQLException {
        if (array == null) {
            return Set.of();
        }
        return new LinkedHashSet<>(Arrays.asList((String[]) array.getArray()));
    }
    private static Instant instant(ResultSet result, String column) throws SQLException {
        return result.getObject(column, OffsetDateTime.class).toInstant();
    }
    private static Instant nullableInstant(ResultSet result, String column) throws SQLException {
        OffsetDateTime value = result.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }
    private static OffsetDateTime time(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
