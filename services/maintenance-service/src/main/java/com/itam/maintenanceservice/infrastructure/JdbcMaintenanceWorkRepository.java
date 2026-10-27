package com.itam.maintenanceservice.infrastructure;

import com.itam.maintenanceservice.domain.MaintenanceWork;
import com.itam.maintenanceservice.domain.MaintenanceWorkRepository;
import com.itam.maintenanceservice.domain.MaintenanceWorkState;
import com.itam.maintenanceservice.domain.MaintenanceWorkType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMaintenanceWorkRepository implements MaintenanceWorkRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcMaintenanceWorkRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MaintenanceWork> findOpen(UUID tenantId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, work_type, work_state, opened_at, completed_at "
                + "FROM maintenance_works WHERE tenant_id = :tenantId AND work_state = 'OPEN' "
                + "ORDER BY opened_at DESC, id DESC",
            Map.of("tenantId", scopedTenantId),
            (resultSet, rowNum) -> mapWork(resultSet)
        );
    }

    @Override
    public Optional<MaintenanceWork> findById(UUID tenantId, UUID workId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, work_type, work_state, opened_at, completed_at "
                + "FROM maintenance_works WHERE tenant_id = :tenantId AND id = :id",
            Map.of(
                "tenantId", scopedTenantId,
                "id", workId
            ),
            (resultSet, rowNum) -> mapWork(resultSet)
        ).stream().findFirst();
    }

    @Override
    public Optional<MaintenanceWork> findOpenByAssetId(UUID tenantId, UUID assetId) {
        UUID scopedTenantId = SqlTenantScope.requireTenantId(tenantId);
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, work_type, work_state, opened_at, completed_at "
                + "FROM maintenance_works WHERE tenant_id = :tenantId AND asset_id = :assetId AND work_state = 'OPEN'",
            Map.of(
                "tenantId", scopedTenantId,
                "assetId", assetId
            ),
            (resultSet, rowNum) -> mapWork(resultSet)
        ).stream().findFirst();
    }

    @Override
    public MaintenanceWork createOpen(UUID tenantId, UUID assetId, MaintenanceWorkType workType, Instant openedAt) {
        UUID workId = UUID.randomUUID();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", workId);
        parameters.put("tenantId", SqlTenantScope.requireTenantId(tenantId));
        parameters.put("assetId", assetId);
        parameters.put("workType", workType.name());
        parameters.put("workState", MaintenanceWorkState.OPEN.name());
        parameters.put("openedAt", Timestamp.from(openedAt));
        parameters.put("completedAt", null);
        jdbcTemplate.update(
            "INSERT INTO maintenance_works (id, tenant_id, asset_id, work_type, work_state, opened_at, completed_at) "
                + "VALUES (:id, :tenantId, :assetId, :workType, :workState, :openedAt, :completedAt)",
            parameters
        );
        return new MaintenanceWork(workId, tenantId, assetId, workType, MaintenanceWorkState.OPEN, openedAt, null);
    }

    @Override
    public Optional<MaintenanceWork> complete(UUID tenantId, UUID workId, Instant completedAt) {
        int updated = jdbcTemplate.update(
            "UPDATE maintenance_works SET work_state = :workState, completed_at = :completedAt "
                + "WHERE tenant_id = :tenantId AND id = :id AND work_state = 'OPEN'",
            Map.of(
                "tenantId", SqlTenantScope.requireTenantId(tenantId),
                "id", workId,
                "workState", MaintenanceWorkState.COMPLETED.name(),
                "completedAt", Timestamp.from(completedAt)
            )
        );
        if (updated == 0) {
            return Optional.empty();
        }
        return findById(tenantId, workId);
    }

    private MaintenanceWork mapWork(ResultSet resultSet) throws SQLException {
        Timestamp completedAt = resultSet.getTimestamp("completed_at");
        return new MaintenanceWork(
            resultSet.getObject("id", UUID.class),
            resultSet.getObject("tenant_id", UUID.class),
            resultSet.getObject("asset_id", UUID.class),
            MaintenanceWorkType.valueOf(resultSet.getString("work_type")),
            MaintenanceWorkState.valueOf(resultSet.getString("work_state")),
            resultSet.getTimestamp("opened_at").toInstant(),
            completedAt == null ? null : completedAt.toInstant()
        );
    }
}
