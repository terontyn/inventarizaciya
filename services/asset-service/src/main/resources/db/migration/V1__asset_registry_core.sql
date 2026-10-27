CREATE TABLE assets (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    asset_tag TEXT NOT NULL,
    serial_number TEXT NOT NULL,
    asset_type TEXT NOT NULL,
    status TEXT NOT NULL,
    assigned_user_id UUID NULL,
    location_id UUID NULL,
    CONSTRAINT uq_assets_tenant_asset_tag UNIQUE (tenant_id, asset_tag)
);

CREATE INDEX idx_assets_tenant_id ON assets (tenant_id);

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
