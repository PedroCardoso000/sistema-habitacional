ALTER TABLE financing_processes
    DROP CONSTRAINT financing_processes_status_check;
ALTER TABLE financing_processes
    ADD CONSTRAINT financing_processes_status_check CHECK (status IN ('DRAFT', 'ACTIVE'));

CREATE TABLE workflow_models (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    version INTEGER NOT NULL CHECK (version > 0),
    name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (organization_id, version),
    UNIQUE (id, organization_id)
);

CREATE UNIQUE INDEX uq_workflow_active_model
    ON workflow_models (organization_id) WHERE active;

CREATE TABLE workflow_stage_definitions (
    model_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    stage_code VARCHAR(64) NOT NULL,
    stage_name VARCHAR(160) NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    required_exit_criteria TEXT[] NOT NULL DEFAULT '{}',
    PRIMARY KEY (model_id, stage_code),
    UNIQUE (model_id, position),
    FOREIGN KEY (model_id, organization_id)
        REFERENCES workflow_models(id, organization_id) ON DELETE CASCADE
);

CREATE TABLE workflow_journeys (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    process_id UUID NOT NULL,
    workflow_model_id UUID NOT NULL,
    workflow_version INTEGER NOT NULL CHECK (workflow_version > 0),
    next_action_description VARCHAR(500),
    next_action_responsible_id UUID REFERENCES identity_users(id),
    next_action_due_at TIMESTAMPTZ,
    next_action_defined_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    initialized_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (organization_id, process_id),
    UNIQUE (id, organization_id),
    FOREIGN KEY (process_id, organization_id)
        REFERENCES financing_processes(id, organization_id),
    FOREIGN KEY (workflow_model_id, organization_id)
        REFERENCES workflow_models(id, organization_id)
);

CREATE INDEX idx_workflow_missing_next_action
    ON workflow_journeys (organization_id, updated_at)
    WHERE next_action_description IS NULL;

CREATE TABLE workflow_process_stages (
    journey_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    stage_code VARCHAR(64) NOT NULL,
    stage_name VARCHAR(160) NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    status VARCHAR(16) NOT NULL CHECK (status IN ('PENDING', 'CURRENT', 'COMPLETED', 'BLOCKED')),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    PRIMARY KEY (journey_id, stage_code),
    UNIQUE (journey_id, position),
    FOREIGN KEY (journey_id, organization_id)
        REFERENCES workflow_journeys(id, organization_id) ON DELETE CASCADE
);

CREATE TABLE workflow_stage_transitions (
    journey_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    sequence INTEGER NOT NULL CHECK (sequence > 0),
    transition_type VARCHAR(32) NOT NULL,
    from_stage_code VARCHAR(64),
    to_stage_code VARCHAR(64) NOT NULL,
    justification VARCHAR(1000),
    actor_id UUID NOT NULL REFERENCES identity_users(id),
    occurred_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (journey_id, sequence),
    FOREIGN KEY (journey_id, organization_id)
        REFERENCES workflow_journeys(id, organization_id) ON DELETE CASCADE
);

CREATE TABLE workflow_audit (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    process_id UUID NOT NULL,
    actor_id UUID NOT NULL REFERENCES identity_users(id),
    action VARCHAR(64) NOT NULL,
    result VARCHAR(16) NOT NULL,
    correlation_id VARCHAR(128),
    occurred_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (process_id, organization_id)
        REFERENCES financing_processes(id, organization_id)
);

CREATE INDEX idx_workflow_audit
    ON workflow_audit (organization_id, process_id, occurred_at);
