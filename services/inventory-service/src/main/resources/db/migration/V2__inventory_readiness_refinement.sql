ALTER TABLE warehouses
    ADD COLUMN branch_id UUID NOT NULL;

ALTER TABLE warehouses
    ADD COLUMN location_id UUID NULL;

CREATE INDEX idx_warehouses_tenant_branch_id ON warehouses (tenant_id, branch_id);
CREATE INDEX idx_warehouses_tenant_location_id ON warehouses (tenant_id, location_id);

ALTER TABLE inventory_records
    ADD COLUMN readiness_state VARCHAR(32) NOT NULL DEFAULT 'NOT_READY';

ALTER TABLE inventory_records
    ADD CONSTRAINT ck_inventory_records_readiness_state
        CHECK (readiness_state IN ('READY', 'NOT_READY'));

CREATE INDEX idx_inventory_records_tenant_readiness_state ON inventory_records (tenant_id, readiness_state);
