CREATE TABLE warehouses (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    CONSTRAINT uq_warehouses_tenant_id UNIQUE (tenant_id, id)
);

CREATE INDEX idx_warehouses_tenant_id ON warehouses (tenant_id);

CREATE TABLE storage_places (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    warehouse_id UUID NOT NULL,
    CONSTRAINT fk_storage_places_warehouse_same_tenant FOREIGN KEY (tenant_id, warehouse_id)
        REFERENCES warehouses (tenant_id, id),
    CONSTRAINT uq_storage_places_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_storage_places_tenant_warehouse_id UNIQUE (tenant_id, warehouse_id, id)
);

CREATE INDEX idx_storage_places_tenant_id ON storage_places (tenant_id);
CREATE INDEX idx_storage_places_tenant_warehouse_id ON storage_places (tenant_id, warehouse_id);

CREATE TABLE inventory_records (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    warehouse_id UUID NOT NULL,
    storage_place_id UUID NULL,
    CONSTRAINT fk_inventory_records_warehouse_same_tenant FOREIGN KEY (tenant_id, warehouse_id)
        REFERENCES warehouses (tenant_id, id),
    CONSTRAINT fk_inventory_records_storage_place_same_tenant_warehouse FOREIGN KEY (tenant_id, warehouse_id, storage_place_id)
        REFERENCES storage_places (tenant_id, warehouse_id, id),
    CONSTRAINT uq_inventory_records_tenant_asset UNIQUE (tenant_id, asset_id),
    CONSTRAINT uq_inventory_records_tenant_id UNIQUE (tenant_id, id)
);

CREATE INDEX idx_inventory_records_tenant_asset_id ON inventory_records (tenant_id, asset_id);
CREATE INDEX idx_inventory_records_tenant_warehouse_id ON inventory_records (tenant_id, warehouse_id);

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

CREATE INDEX idx_inventory_local_audit_tenant_entity ON local_audit_records (tenant_id, entity_type, entity_id);
