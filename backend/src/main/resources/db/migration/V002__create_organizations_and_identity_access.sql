CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE identity_users (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    email VARCHAR(254) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    role VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    access_changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_identity_users_organization_email UNIQUE (organization_id, email),
    CONSTRAINT uk_identity_users_organization_id UNIQUE (organization_id, id),
    CONSTRAINT ck_identity_users_role CHECK (
        role IN ('MANAGER', 'ANALYST', 'BROKER', 'CLIENT', 'SELLER', 'PLATFORM_ADMIN')
    ),
    CONSTRAINT ck_identity_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'REVOKED'))
);

CREATE INDEX idx_identity_users_organization_status
    ON identity_users (organization_id, status);

CREATE TABLE access_action_audit (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    actor_user_id UUID,
    target_user_id UUID NOT NULL,
    action VARCHAR(80) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    result VARCHAR(20) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    technical_origin VARCHAR(40) NOT NULL,
    CONSTRAINT fk_access_audit_actor_tenant FOREIGN KEY (organization_id, actor_user_id)
        REFERENCES identity_users (organization_id, id),
    CONSTRAINT fk_access_audit_target_tenant FOREIGN KEY (organization_id, target_user_id)
        REFERENCES identity_users (organization_id, id)
);

CREATE INDEX idx_access_action_audit_organization_target_time
    ON access_action_audit (organization_id, target_user_id, occurred_at DESC);

CREATE TABLE platform_administration_audit (
    id UUID PRIMARY KEY,
    actor_organization_id UUID NOT NULL,
    actor_user_id UUID NOT NULL,
    target_organization_id UUID NOT NULL REFERENCES organizations (id),
    action VARCHAR(80) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    result VARCHAR(20) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    CONSTRAINT fk_platform_audit_actor_tenant
        FOREIGN KEY (actor_organization_id, actor_user_id)
        REFERENCES identity_users (organization_id, id)
);

CREATE INDEX idx_platform_administration_audit_actor_time
    ON platform_administration_audit (actor_organization_id, actor_user_id, occurred_at DESC);
