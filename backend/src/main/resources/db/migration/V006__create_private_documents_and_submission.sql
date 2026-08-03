CREATE TABLE document_types (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(160) NOT NULL,
    allowed_extensions VARCHAR(160) NOT NULL,
    allowed_content_types VARCHAR(320) NOT NULL,
    maximum_bytes BIGINT NOT NULL CHECK (maximum_bytes > 0),
    validity_required BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (organization_id, code),
    UNIQUE (id, organization_id)
);

CREATE TABLE document_checklist_templates (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    version INTEGER NOT NULL CHECK (version > 0),
    name VARCHAR(160) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (organization_id, version),
    UNIQUE (id, organization_id)
);

CREATE UNIQUE INDEX uq_document_active_checklist_template
    ON document_checklist_templates (organization_id) WHERE active;

CREATE TABLE document_checklist_template_items (
    template_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    document_type_id UUID NOT NULL,
    position INTEGER NOT NULL CHECK (position > 0),
    required BOOLEAN NOT NULL,
    PRIMARY KEY (template_id, document_type_id),
    UNIQUE (template_id, position),
    FOREIGN KEY (template_id, organization_id)
        REFERENCES document_checklist_templates(id, organization_id) ON DELETE CASCADE,
    FOREIGN KEY (document_type_id, organization_id)
        REFERENCES document_types(id, organization_id)
);

CREATE TABLE document_requests (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    process_id UUID NOT NULL,
    document_type_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    requested_by UUID NOT NULL REFERENCES identity_users(id),
    status VARCHAR(32) NOT NULL CHECK (status IN (
        'REQUESTED', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED',
        'RESUBMISSION_REQUESTED', 'CANCELLED', 'EXPIRED')),
    rejection_reason VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    requested_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (organization_id, process_id, document_type_id),
    UNIQUE (id, organization_id),
    FOREIGN KEY (process_id, organization_id)
        REFERENCES financing_processes(id, organization_id),
    FOREIGN KEY (document_type_id, organization_id)
        REFERENCES document_types(id, organization_id),
    CONSTRAINT ck_document_rejection_reason CHECK (
        status NOT IN ('REJECTED', 'RESUBMISSION_REQUESTED') OR rejection_reason IS NOT NULL)
);

CREATE INDEX idx_document_requests_process
    ON document_requests (organization_id, process_id, status);

CREATE TABLE document_upload_intents (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    request_id UUID NOT NULL,
    sender_id UUID NOT NULL REFERENCES identity_users(id),
    object_key VARCHAR(320) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    declared_content_type VARCHAR(160) NOT NULL,
    declared_size BIGINT NOT NULL CHECK (declared_size > 0),
    token_hash CHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN (
        'PENDING', 'STORED', 'COMPLETED', 'EXPIRED', 'REJECTED', 'ABORTED')),
    expires_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (id, organization_id),
    FOREIGN KEY (request_id, organization_id)
        REFERENCES document_requests(id, organization_id)
);

CREATE INDEX idx_document_upload_cleanup
    ON document_upload_intents (expires_at)
    WHERE status IN ('PENDING', 'STORED');

CREATE TABLE document_versions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    request_id UUID NOT NULL,
    upload_id UUID NOT NULL UNIQUE,
    version_number INTEGER NOT NULL CHECK (version_number > 0),
    storage_key VARCHAR(320) NOT NULL UNIQUE,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(160) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    checksum CHAR(64) NOT NULL,
    submitted_by UUID NOT NULL REFERENCES identity_users(id),
    submitted_at TIMESTAMPTZ NOT NULL,
    valid_until DATE,
    UNIQUE (request_id, version_number),
    UNIQUE (id, organization_id),
    FOREIGN KEY (request_id, organization_id)
        REFERENCES document_requests(id, organization_id),
    FOREIGN KEY (upload_id, organization_id)
        REFERENCES document_upload_intents(id, organization_id)
);

CREATE TABLE document_download_grants (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    version_id UUID NOT NULL,
    object_key VARCHAR(320) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(160) NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    FOREIGN KEY (version_id, organization_id)
        REFERENCES document_versions(id, organization_id)
);

CREATE INDEX idx_document_download_grants_expiry
    ON document_download_grants (expires_at);

CREATE TABLE document_audit (
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

CREATE INDEX idx_document_audit
    ON document_audit (organization_id, process_id, occurred_at);
