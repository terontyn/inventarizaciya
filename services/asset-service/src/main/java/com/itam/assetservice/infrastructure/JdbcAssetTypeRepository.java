package com.itam.assetservice.infrastructure;

import com.itam.assetservice.domain.AssetType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAssetTypeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<AssetType> ROW_MAPPER = (rs, rowNum) -> new AssetType(
        rs.getObject("id", UUID.class),
        rs.getObject("tenant_id", UUID.class),
        rs.getString("name"),
        rs.getString("attributes")
    );

    public JdbcAssetTypeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AssetType> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, name, attributes FROM asset_types WHERE tenant_id = :tenantId ORDER BY name",
            Map.of("tenantId", scopedTenantId),
            ROW_MAPPER
        );
    }

    public AssetType save(AssetType assetType) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(assetType.tenantId());
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", assetType.id())
            .addValue("tenantId", scopedTenantId)
            .addValue("name", assetType.name())
            .addValue("attributes", assetType.attributes());
        jdbcTemplate.update(
            "INSERT INTO asset_types (id, tenant_id, name, attributes) VALUES (:id, :tenantId, :name, :attributes::jsonb) " +
            "ON CONFLICT (tenant_id, name) DO UPDATE SET attributes = :attributes::jsonb",
            params
        );
        return assetType;
    }
}
