CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE departments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_departments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uq_departments_tenant_id UNIQUE (tenant_id, id)
);

CREATE INDEX idx_departments_tenant_id ON departments (tenant_id);

CREATE TABLE branches (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_branches_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT uq_branches_tenant_id UNIQUE (tenant_id, id)
);

CREATE INDEX idx_branches_tenant_id ON branches (tenant_id);

CREATE TABLE locations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_locations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_locations_branch_same_tenant FOREIGN KEY (tenant_id, branch_id)
        REFERENCES branches (tenant_id, id),
    CONSTRAINT uq_locations_tenant_id UNIQUE (tenant_id, id)
);

CREATE INDEX idx_locations_tenant_id ON locations (tenant_id);
CREATE INDEX idx_locations_tenant_branch_id ON locations (tenant_id, branch_id);

CREATE TABLE local_audit_records (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    actor VARCHAR(255) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id UUID NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    change_payload TEXT NOT NULL
);

CREATE INDEX idx_local_audit_records_tenant_occurred_at
    ON local_audit_records (tenant_id, occurred_at);
