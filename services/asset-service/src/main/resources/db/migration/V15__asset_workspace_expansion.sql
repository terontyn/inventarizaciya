-- Forward-only expansion. V8-V14 are reserved for a separate legacy installation lineage.
-- No demo rows and no Flyway checksum repair are performed here.
CREATE TABLE IF NOT EXISTS support_requests (
    id UUID PRIMARY KEY, tenant_id UUID NOT NULL, user_id UUID NOT NULL,
    user_name VARCHAR(255) NOT NULL, type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL, status VARCHAR(50) NOT NULL,
    resolution_comment TEXT, created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_support_requests_tenant ON support_requests(tenant_id);
ALTER TABLE support_requests ADD COLUMN IF NOT EXISTS resolution_comment TEXT;
ALTER TABLE assets ADD COLUMN IF NOT EXISTS qr_data TEXT;
ALTER TABLE assets ADD COLUMN IF NOT EXISTS parent_asset_id UUID NULL REFERENCES assets(id);
ALTER TABLE assets ADD COLUMN IF NOT EXISTS ip_address TEXT NULL;
CREATE INDEX IF NOT EXISTS idx_assets_parent_asset_id ON assets(parent_asset_id);
CREATE TABLE IF NOT EXISTS asset_types (
    id UUID PRIMARY KEY, tenant_id UUID NOT NULL, name VARCHAR(255) NOT NULL,
    attributes JSONB, CONSTRAINT uq_asset_types_tenant_name UNIQUE(tenant_id,name)
);
CREATE INDEX IF NOT EXISTS idx_asset_types_tenant_id ON asset_types(tenant_id);
CREATE TABLE IF NOT EXISTS sim_cards (
    id UUID PRIMARY KEY, tenant_id UUID NOT NULL, phone_number VARCHAR(255) NOT NULL,
    operator VARCHAR(255) NOT NULL, tariff VARCHAR(255), issued_to_name VARCHAR(255),
    location_name VARCHAR(255), status VARCHAR(64) NOT NULL,
    CONSTRAINT uq_sim_cards_tenant_phone UNIQUE(tenant_id,phone_number)
);
CREATE INDEX IF NOT EXISTS idx_sim_cards_tenant_id ON sim_cards(tenant_id);
CREATE TABLE IF NOT EXISTS cartridges (
    id UUID PRIMARY KEY, tenant_id UUID NOT NULL, model VARCHAR(255) NOT NULL,
    compatibility VARCHAR(255) NOT NULL, location_id UUID NULL, status VARCHAR(64) NOT NULL,
    printer_asset_id UUID NULL REFERENCES assets(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_cartridges_tenant_id ON cartridges(tenant_id);
CREATE INDEX IF NOT EXISTS idx_cartridges_printer_asset_id ON cartridges(printer_asset_id);
