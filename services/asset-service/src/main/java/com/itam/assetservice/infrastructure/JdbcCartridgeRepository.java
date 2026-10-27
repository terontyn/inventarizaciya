package com.itam.assetservice.infrastructure;

import com.itam.assetservice.domain.Cartridge;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCartridgeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final RowMapper<Cartridge> ROW_MAPPER = (rs, rowNum) -> new Cartridge(
        rs.getObject("id", UUID.class),
        rs.getObject("tenant_id", UUID.class),
        rs.getString("model"),
        rs.getString("compatibility"),
        rs.getObject("location_id", UUID.class),
        rs.getString("status"),
        rs.getObject("printer_asset_id", UUID.class)
    );

    public JdbcCartridgeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Cartridge> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, model, compatibility, location_id, status, printer_asset_id FROM cartridges WHERE tenant_id = :tenantId ORDER BY model",
            Map.of("tenantId", scopedTenantId),
            ROW_MAPPER
        );
    }

    public Optional<Cartridge> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<Cartridge> results = jdbcTemplate.query(
            "SELECT id, tenant_id, model, compatibility, location_id, status, printer_asset_id FROM cartridges WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            ROW_MAPPER
        );
        return results.stream().findFirst();
    }

    public Cartridge save(Cartridge cartridge) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(cartridge.tenantId());
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", cartridge.id())
            .addValue("tenantId", scopedTenantId)
            .addValue("model", cartridge.model())
            .addValue("compatibility", cartridge.compatibility())
            .addValue("locationId", cartridge.locationId())
            .addValue("status", cartridge.status())
            .addValue("printerAssetId", cartridge.printerAssetId());
        jdbcTemplate.update(
            "INSERT INTO cartridges (id, tenant_id, model, compatibility, location_id, status, printer_asset_id) " +
            "VALUES (:id, :tenantId, :model, :compatibility, :locationId, :status, :printerAssetId) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "model = :model, compatibility = :compatibility, location_id = :locationId, status = :status, printer_asset_id = :printerAssetId",
            params
        );
        return cartridge;
    }

    public void delete(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        jdbcTemplate.update(
            "DELETE FROM cartridges WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id)
        );
    }
}
