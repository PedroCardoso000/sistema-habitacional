CREATE TABLE financing_process_number_counters (
    organization_id UUID PRIMARY KEY REFERENCES organizations(id),
    next_value BIGINT NOT NULL CHECK (next_value > 0)
);

CREATE TABLE financing_processes (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    process_number VARCHAR(32) NOT NULL,
    origin VARCHAR(32) NOT NULL CHECK (origin IN ('BROKER', 'DIRECT_CLIENT')),
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' CHECK (status = 'DRAFT'),
    author_user_id UUID NOT NULL REFERENCES identity_users(id),
    broker_id UUID,
    responsible_user_id UUID NOT NULL REFERENCES identity_users(id),
    main_client_id UUID,
    priority VARCHAR(16) NOT NULL CHECK (priority IN ('NORMAL', 'HIGH', 'URGENT')),
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_financing_process_tenant_id UNIQUE (id, organization_id),
    CONSTRAINT uq_financing_process_number_per_org UNIQUE (organization_id, process_number),
    CONSTRAINT ck_financing_process_origin_broker CHECK (
        (origin = 'BROKER' AND broker_id IS NOT NULL) OR
        (origin = 'DIRECT_CLIENT' AND broker_id IS NULL)
    )
);

CREATE INDEX idx_financing_process_list
    ON financing_processes (organization_id, created_at DESC);

CREATE TABLE financing_process_participants (
    organization_id UUID NOT NULL,
    process_id UUID NOT NULL,
    participant_type VARCHAR(16) NOT NULL CHECK (participant_type IN ('CLIENT', 'BROKER')),
    participant_id UUID NOT NULL,
    PRIMARY KEY (organization_id, process_id, participant_type, participant_id),
    FOREIGN KEY (process_id, organization_id)
        REFERENCES financing_processes(id, organization_id) ON DELETE CASCADE
);

CREATE TABLE financing_process_property_history (
    organization_id UUID NOT NULL,
    process_id UUID NOT NULL,
    sequence INTEGER NOT NULL CHECK (sequence > 0),
    address_line VARCHAR(255) NOT NULL,
    city VARCHAR(120) NOT NULL,
    state VARCHAR(80) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    associated_by UUID NOT NULL REFERENCES identity_users(id),
    associated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (organization_id, process_id, sequence),
    FOREIGN KEY (process_id, organization_id)
        REFERENCES financing_processes(id, organization_id) ON DELETE CASCADE
);

CREATE TABLE financing_process_audit (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    process_id UUID NOT NULL,
    actor_id UUID NOT NULL REFERENCES identity_users(id),
    action VARCHAR(64) NOT NULL,
    correlation_id VARCHAR(128),
    occurred_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (process_id, organization_id)
        REFERENCES financing_processes(id, organization_id)
);

CREATE INDEX idx_financing_process_audit
    ON financing_process_audit (organization_id, process_id, occurred_at);
