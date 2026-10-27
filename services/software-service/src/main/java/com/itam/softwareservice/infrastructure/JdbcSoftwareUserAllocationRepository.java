package com.itam.softwareservice.infrastructure;

import com.itam.softwareservice.domain.SoftwareUserAllocation;
import com.itam.softwareservice.domain.SoftwareUserAllocationRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSoftwareUserAllocationRepository implements SoftwareUserAllocationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcSoftwareUserAllocationRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SoftwareUserAllocation> findBySoftwareRecordId(UUID tenantId, UUID recordId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, software_record_id, user_id "
                + "FROM software_user_allocations WHERE tenant_id = :tenantId AND software_record_id = :recordId "
                + "ORDER BY user_id ASC",
            Map.of(
                "tenantId", scopedTenantId,
                "recordId", recordId
            ),
            (resultSet, rowNum) -> new SoftwareUserAllocation(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getObject("software_record_id", UUID.class),
                resultSet.getObject("user_id", UUID.class)
            )
        );
    }

    @Override
    public List<SoftwareUserAllocation> findByUserId(UUID tenantId, UUID userId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, software_record_id, user_id "
                + "FROM software_user_allocations WHERE tenant_id = :tenantId AND user_id = :userId "
                + "ORDER BY software_record_id ASC",
            Map.of(
                "tenantId", scopedTenantId,
                "userId", userId
            ),
            (resultSet, rowNum) -> new SoftwareUserAllocation(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getObject("software_record_id", UUID.class),
                resultSet.getObject("user_id", UUID.class)
            )
        );
    }

    @Override
    public Optional<SoftwareUserAllocation> findBySoftwareRecordIdAndUserId(UUID tenantId, UUID recordId, UUID userId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, software_record_id, user_id "
                + "FROM software_user_allocations "
                + "WHERE tenant_id = :tenantId AND software_record_id = :recordId AND user_id = :userId",
            Map.of(
                "tenantId", scopedTenantId,
                "recordId", recordId,
                "userId", userId
            ),
            (resultSet, rowNum) -> new SoftwareUserAllocation(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("tenant_id", UUID.class),
                resultSet.getObject("software_record_id", UUID.class),
                resultSet.getObject("user_id", UUID.class)
            )
        ).stream().findFirst();
    }

    @Override
    public SoftwareUserAllocation create(UUID tenantId, UUID recordId, UUID userId) {
        UUID id = UUID.randomUUID();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        parameters.put("tenantId", SqlTenantScope.requireTenantId(tenantId));
        parameters.put("recordId", recordId);
        parameters.put("userId", userId);
        jdbcTemplate.update(
            "INSERT INTO software_user_allocations (id, tenant_id, software_record_id, user_id) "
                + "VALUES (:id, :tenantId, :recordId, :userId)",
            parameters
        );
        return new SoftwareUserAllocation(id, tenantId, recordId, userId);
    }

    @Override
    public void deleteById(UUID tenantId, UUID allocationId) {
        jdbcTemplate.update(
            "DELETE FROM software_user_allocations WHERE tenant_id = :tenantId AND id = :id",
            Map.of(
                "tenantId", SqlTenantScope.requireTenantId(tenantId),
                "id", allocationId
            )
        );
    }
}
