CREATE TABLE maintenance_works (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    work_type VARCHAR(32) NOT NULL,
    work_state VARCHAR(32) NOT NULL,
    opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT uq_maintenance_works_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT ck_maintenance_works_type CHECK (work_type IN ('INSPECTION', 'REPAIR', 'SERVICING')),
    CONSTRAINT ck_maintenance_works_state CHECK (work_state IN ('OPEN', 'COMPLETED')),
    CONSTRAINT ck_maintenance_works_completion_consistency CHECK (
        (work_state = 'OPEN' AND completed_at IS NULL)
        OR
        (work_state = 'COMPLETED' AND completed_at IS NOT NULL)
    )
);

CREATE INDEX idx_maintenance_works_tenant_asset_id ON maintenance_works (tenant_id, asset_id);
CREATE INDEX idx_maintenance_works_tenant_work_state_opened_at ON maintenance_works (tenant_id, work_state, opened_at);

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

CREATE INDEX idx_maintenance_local_audit_tenant_entity ON local_audit_records (tenant_id, entity_type, entity_id);
