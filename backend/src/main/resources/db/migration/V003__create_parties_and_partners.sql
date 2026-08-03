CREATE TABLE party_clients (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    cpf CHAR(11) NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    email VARCHAR(254),
    phone VARCHAR(13),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_party_clients_organization_cpf UNIQUE (organization_id, cpf),
    CONSTRAINT uk_party_clients_organization_id UNIQUE (organization_id, id),
    CONSTRAINT ck_party_clients_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_party_clients_contact CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

CREATE TABLE party_agencies (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    cnpj CHAR(14) NOT NULL,
    legal_name VARCHAR(160) NOT NULL,
    email VARCHAR(254),
    phone VARCHAR(13),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_party_agencies_organization_cnpj UNIQUE (organization_id, cnpj),
    CONSTRAINT uk_party_agencies_organization_id UNIQUE (organization_id, id),
    CONSTRAINT ck_party_agencies_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_party_agencies_contact CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

CREATE TABLE party_brokers (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    cpf CHAR(11) NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    email VARCHAR(254),
    phone VARCHAR(13),
    agency_id UUID,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_party_brokers_organization_cpf UNIQUE (organization_id, cpf),
    CONSTRAINT uk_party_brokers_organization_id UNIQUE (organization_id, id),
    CONSTRAINT fk_party_brokers_agency_tenant FOREIGN KEY (organization_id, agency_id)
        REFERENCES party_agencies (organization_id, id),
    CONSTRAINT ck_party_brokers_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_party_brokers_contact CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

CREATE INDEX idx_party_clients_organization_name
    ON party_clients (organization_id, full_name, id);

CREATE INDEX idx_party_brokers_organization_name
    ON party_brokers (organization_id, full_name, id);

CREATE INDEX idx_party_agencies_organization_name
    ON party_agencies (organization_id, legal_name, id);

CREATE TABLE party_action_audit (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    actor_user_id UUID NOT NULL,
    target_id UUID NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    action VARCHAR(80) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    result VARCHAR(20) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    technical_origin VARCHAR(40) NOT NULL,
    CONSTRAINT fk_party_audit_actor_tenant FOREIGN KEY (organization_id, actor_user_id)
        REFERENCES identity_users (organization_id, id),
    CONSTRAINT ck_party_audit_target_type CHECK (target_type IN ('CLIENT', 'BROKER', 'AGENCY'))
);

CREATE INDEX idx_party_action_audit_organization_target_time
    ON party_action_audit (organization_id, target_id, occurred_at DESC);
