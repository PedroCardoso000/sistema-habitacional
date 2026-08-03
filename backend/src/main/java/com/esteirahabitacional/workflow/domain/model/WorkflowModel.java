package com.esteirahabitacional.workflow.domain.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record WorkflowModel(
        UUID id, UUID organizationId, int version, String name,
        List<WorkflowStageDefinition> stages, Instant createdAt) {
    public WorkflowModel {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(organizationId, "organizationId is required");
        if (version < 1) {
            throw new IllegalArgumentException("version must be positive");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        stages = List.copyOf(Objects.requireNonNull(stages, "stages are required"));
        if (stages.isEmpty()) {
            throw new IllegalArgumentException("at least one stage is required");
        }
        validateStages(stages);
        Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public static WorkflowModel initialMvp(UUID id, UUID organizationId, Instant createdAt) {
        return new WorkflowModel(id, organizationId, 1, "Fluxo habitacional MVP v1", List.of(
                stage("INITIAL_REVIEW", "Entrada e análise inicial", 1),
                stage("BUYER_DOCUMENTS", "Documentação do comprador", 2),
                stage("PRE_APPROVAL", "Pré-aprovação", 3),
                stage("PROPERTY_DOCUMENTS", "Documentação do imóvel", 4),
                stage("BANK_ANALYSIS", "Análise bancária", 5),
                stage("CONTRACT_SIGNATURE", "Contrato e assinatura", 6)), createdAt);
    }

    public WorkflowStageDefinition stage(String code) {
        return stages.stream().filter(item -> item.code().equals(code)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown workflow stage: " + code));
    }

    private static WorkflowStageDefinition stage(String code, String name, int position) {
        return new WorkflowStageDefinition(code, name, position, Set.of());
    }

    private static void validateStages(List<WorkflowStageDefinition> stages) {
        Set<String> codes = new HashSet<>();
        Set<Integer> positions = new HashSet<>();
        for (WorkflowStageDefinition stage : stages) {
            if (!codes.add(stage.code()) || !positions.add(stage.position())) {
                throw new IllegalArgumentException("Stage codes and positions must be unique");
            }
        }
        for (int index = 0; index < stages.size(); index++) {
            if (stages.get(index).position() != index + 1) {
                throw new IllegalArgumentException("Stages must be ordered without gaps");
            }
        }
    }
}
