package com.itam.softwareservice.infrastructure;

import com.itam.softwareservice.domain.SoftwareAssetAllocation;
import com.itam.softwareservice.domain.SoftwareAssetAllocationRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSoftwareAssetAllocationRepository implements SoftwareAssetAllocationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcSoftwareAssetAllocationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SoftwareAssetAllocation> findBySoftwareRecordId(UUID tenantId, UUID recordId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, software_record_id, asset_id "
                + "FROM software_asset_allocations WHERE tenant_id = :tenantId AND software_record_id = :recordId "
                + "ORDER BY asset_id ASC",
            Map.of(
                "tenantId", scopedTenantId,
                "recordId", recordId
            ),
            (resultSet, rowNum) -> new SoftwareAssetAllocation(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getObject("software_record_id", UUID.class),
                resultSet.getObject("asset_id", UUID.class)
            )
        );
    }

    @Override
    public List<SoftwareAssetAllocation> findByAssetId(UUID tenantId, UUID assetId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, software_record_id, asset_id "
                + "FROM software_asset_allocations WHERE tenant_id = :tenantId AND asset_id = :assetId "
                + "ORDER BY software_record_id ASC",
            Map.of(
                "tenantId", scopedTenantId,
                "assetId", assetId
            ),
            (resultSet, rowNum) -> new SoftwareAssetAllocation(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getObject("software_record_id", UUID.class),
                resultSet.getObject("asset_id", UUID.class)
            )
        );
    }

    @Override
    public Optional<SoftwareAssetAllocation> findBySoftwareRecordIdAndAssetId(UUID tenantId, UUID recordId, UUID assetId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, software_record_id, asset_id "
                + "FROM software_asset_allocations "
                + "WHERE tenant_id = :tenantId AND software_record_id = :recordId AND asset_id = :assetId",
            Map.of(
                "tenantId", scopedTenantId,
                "recordId", recordId,
                "assetId", assetId
            ),
            (resultSet, rowNum) -> new SoftwareAssetAllocation(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getObject("software_record_id", UUID.class),
                resultSet.getObject("asset_id", UUID.class)
            )
        ).stream().findFirst();
    }

    @Override
    public SoftwareAssetAllocation create(UUID tenantId, UUID recordId, UUID assetId) {
        UUID id = UUID.randomUUID();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        parameters.put("tenantId", SqlTenantScope.requireTenantId(tenantId));
        parameters.put("recordId", recordId);
        parameters.put("assetId", assetId);
        jdbcTemplate.update(
            "INSERT INTO software_asset_allocations (id, tenant_id, software_record_id, asset_id) "
                + "VALUES (:id, :tenantId, :recordId, :assetId)",
            parameters
        );
        return new SoftwareAssetAllocation(id, tenantId, recordId, assetId);
    }

    @Override
    public void deleteById(UUID tenantId, UUID allocationId) {
        jdbcTemplate.update(
            "DELETE FROM software_asset_allocations WHERE tenant_id = :tenantId AND id = :id",
            Map.of(
                "tenantId", SqlTenantScope.requireTenantId(tenantId),
                "id", allocationId
            )
        );
    }
}
