CREATE TABLE software_records (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_software_records_tenant_id UNIQUE (tenant_id, id)
);

CREATE INDEX idx_software_records_tenant_id ON software_records (tenant_id);

CREATE TABLE software_user_allocations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    software_record_id UUID NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT fk_software_user_allocations_record_same_tenant FOREIGN KEY (tenant_id, software_record_id)
        REFERENCES software_records (tenant_id, id),
    CONSTRAINT uq_software_user_allocations_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_software_user_allocations_current_pair UNIQUE (tenant_id, software_record_id, user_id)
);

CREATE INDEX idx_software_user_allocations_tenant_record_id ON software_user_allocations (tenant_id, software_record_id);
CREATE INDEX idx_software_user_allocations_tenant_user_id ON software_user_allocations (tenant_id, user_id);

CREATE TABLE software_asset_allocations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    software_record_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    CONSTRAINT fk_software_asset_allocations_record_same_tenant FOREIGN KEY (tenant_id, software_record_id)
        REFERENCES software_records (tenant_id, id),
    CONSTRAINT uq_software_asset_allocations_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_software_asset_allocations_current_pair UNIQUE (tenant_id, software_record_id, asset_id)
);

CREATE INDEX idx_software_asset_allocations_tenant_record_id ON software_asset_allocations (tenant_id, software_record_id);
CREATE INDEX idx_software_asset_allocations_tenant_asset_id ON software_asset_allocations (tenant_id, asset_id);

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

CREATE INDEX idx_software_local_audit_tenant_entity ON local_audit_records (tenant_id, entity_type, entity_id);
