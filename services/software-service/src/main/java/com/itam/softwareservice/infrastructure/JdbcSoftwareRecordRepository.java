package com.itam.softwareservice.infrastructure;

import com.itam.softwareservice.domain.SoftwareRecord;
import com.itam.softwareservice.domain.SoftwareRecordRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSoftwareRecordRepository implements SoftwareRecordRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcSoftwareRecordRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SoftwareRecord> findAll(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, name FROM software_records WHERE tenant_id = :tenantId ORDER BY name ASC, id ASC",
            Map.of("tenantId", scopedTenantId),
            (resultSet, rowNum) -> new SoftwareRecord(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getString("name")
            )
        );
    }

    @Override
    public java.util.Optional<SoftwareRecord> findById(UUID tenantId, UUID recordId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, name FROM software_records WHERE tenant_id = :tenantId AND id = :id",
            Map.of(
                "tenantId", scopedTenantId,
                "id", recordId
            ),
            (resultSet, rowNum) -> new SoftwareRecord(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getString("name")
            )
        ).stream().findFirst();
    }

    @Override
    public List<SoftwareRecord> findByIds(UUID tenantId, List<UUID> recordIds) {
        if (recordIds.isEmpty()) {
            return List.of();
        }
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, name FROM software_records WHERE tenant_id = :tenantId AND id IN (:ids) ORDER BY name ASC, id ASC",
            new MapSqlParameterSource()
                .addValue("tenantId", scopedTenantId)
                .addValue("ids", recordIds),
            (resultSet, rowNum) -> new SoftwareRecord(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getString("name")
            )
        );
    }

    @Override
    public SoftwareRecord create(UUID tenantId, String name) {
        UUID id = UUID.randomUUID();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        parameters.put("tenantId", SqlTenantScope.requireTenantId(tenantId));
        parameters.put("name", name);
        jdbcTemplate.update(
            "INSERT INTO software_records (id, tenant_id, name) VALUES (:id, :tenantId, :name)",
            parameters
        );
        return new SoftwareRecord(id, tenantId, name);
    }
}
