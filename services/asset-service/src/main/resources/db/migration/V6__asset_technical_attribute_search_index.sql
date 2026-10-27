CREATE INDEX idx_asset_technical_attributes_search_scope
    ON asset_technical_attributes (tenant_id, attribute_key);
