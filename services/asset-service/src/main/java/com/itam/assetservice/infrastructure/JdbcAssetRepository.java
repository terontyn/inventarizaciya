package com.itam.assetservice.infrastructure;

import com.itam.assetservice.domain.Asset;
import com.itam.assetservice.domain.AssetRepository;
import com.itam.assetservice.domain.AssetStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAssetRepository implements AssetRepository {

    private static final RowMapper<Asset> ASSET_ROW_MAPPER = (resultSet, rowNum) -> new Asset(
        resultSet.getObject("id", UUID.class),
        resultSet.getObject("tenant_id", UUID.class),
        resultSet.getString("asset_tag"),
        resultSet.getString("serial_number"),
        resultSet.getString("asset_type"),
        resultSet.getString("status"),
        resultSet.getObject("assigned_user_id", UUID.class),
        resultSet.getObject("location_id", UUID.class),
        resultSet.getString("display_name"),
        resultSet.getString("note")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcAssetRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Asset create(
        UUID tenantId,
        String assetTag,
        String serialNumber,
        String assetType,
        UUID assignedUserId,
        UUID locationId,
        String displayName,
        String note
    ) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        UUID id = UUID.randomUUID();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("tenantId", scopedTenantId)
            .addValue("assetTag", assetTag)
            .addValue("serialNumber", serialNumber)
            .addValue("assetType", assetType)
            .addValue("assignedUserId", assignedUserId)
            .addValue("locationId", locationId)
            .addValue("displayName", displayName)
            .addValue("note", note);
        jdbcTemplate.update(
            "INSERT INTO assets ("
                + "id, tenant_id, asset_tag, serial_number, asset_type, status, assigned_user_id, "
                + "location_id, display_name, note"
                + ") VALUES ("
                + ":id, :tenantId, :assetTag, :serialNumber, :assetType, '" + AssetStatus.ACTIVE
                + "', :assignedUserId, :locationId, :displayName, :note"
                + ")",
            parameters
        );
        return new Asset(
            id,
            scopedTenantId,
            assetTag,
            serialNumber,
            assetType,
            AssetStatus.ACTIVE,
            assignedUserId,
            locationId,
            displayName,
            note
        );
    }

    @Override
    public List<Asset> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_tag, serial_number, asset_type, status, assigned_user_id, "
                + "location_id, display_name, note "
                + "FROM assets WHERE tenant_id = :tenantId ORDER BY asset_tag, id",
            Map.of("tenantId", scopedTenantId),
            ASSET_ROW_MAPPER
        );
    }

    @Override
    public Optional<Asset> findById(UUID tenantId, UUID id) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        List<Asset> assets = jdbcTemplate.query(
            "SELECT id, tenant_id, asset_tag, serial_number, asset_type, status, assigned_user_id, "
                + "location_id, display_name, note "
                + "FROM assets WHERE tenant_id = :tenantId AND id = :id",
            Map.of("tenantId", scopedTenantId, "id", id),
            ASSET_ROW_MAPPER
        );
        return assets.stream().findFirst();
    }

    @Override
    public Optional<Asset> update(
        UUID tenantId,
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        String displayName,
        String note
    ) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("tenantId", scopedTenantId)
            .addValue("id", id)
            .addValue("assetTag", assetTag)
            .addValue("serialNumber", serialNumber)
            .addValue("assetType", assetType)
            .addValue("displayName", displayName)
            .addValue("note", note);
        int updated = jdbcTemplate.update(
            "UPDATE assets SET asset_tag = :assetTag, serial_number = :serialNumber, asset_type = :assetType, "
                + "display_name = :displayName, note = :note "
                + "WHERE tenant_id = :tenantId AND id = :id",
            parameters
        );
        return updated == 0 ? Optional.empty() : findById(scopedTenantId, id);
    }

    @Override
    public Optional<Asset> updateCurrentState(
        UUID tenantId,
        UUID id,
        String status,
        UUID assignedUserId,
        UUID locationId
    ) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("tenantId", scopedTenantId)
            .addValue("id", id)
            .addValue("status", status)
            .addValue("assignedUserId", assignedUserId)
            .addValue("locationId", locationId);
        int updated = jdbcTemplate.update(
            "UPDATE assets SET status = :status, assigned_user_id = :assignedUserId, location_id = :locationId "
                + "WHERE tenant_id = :tenantId AND id = :id",
            parameters
        );
        return updated == 0 ? Optional.empty() : findById(scopedTenantId, id);
    }
}
