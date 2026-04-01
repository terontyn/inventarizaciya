ALTER TABLE assets
    ADD CONSTRAINT uq_assets_tenant_id_id UNIQUE (tenant_id, id);

CREATE TABLE asset_responsibility_facts (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    fact_type VARCHAR(32) NOT NULL,
    previous_assigned_user_id UUID NULL,
    new_assigned_user_id UUID NULL,
    previous_location_id UUID NULL,
    new_location_id UUID NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_asset_responsibility_facts_asset
        FOREIGN KEY (tenant_id, asset_id)
        REFERENCES assets (tenant_id, id)
);

CREATE INDEX idx_asset_responsibility_facts_tenant_asset_occurred_at
    ON asset_responsibility_facts (tenant_id, asset_id, occurred_at);
