CREATE TABLE asset_technical_attributes (
    tenant_id UUID NOT NULL,
    asset_id UUID NOT NULL,
    attribute_key VARCHAR(64) NOT NULL,
    attribute_value VARCHAR(512) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (tenant_id, asset_id, attribute_key),
    CONSTRAINT fk_asset_technical_attributes_asset
        FOREIGN KEY (tenant_id, asset_id)
        REFERENCES assets (tenant_id, id)
);
