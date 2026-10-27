package com.itam.inventoryservice;

import static com.itam.inventoryservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.inventoryservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itam.inventoryservice.domain.ReadinessState;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = com.itam.inventoryservice.boot.InventoryServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected RestTemplate userServiceRestTemplate;

    @Autowired
    protected RestTemplate assetServiceRestTemplate;

    @Value("${integration.user-service.base-url}")
    protected String userServiceBaseUrl;

    @Value("${integration.asset-service.base-url}")
    protected String assetServiceBaseUrl;

    protected MockRestServiceServer mockUserService;
    protected MockRestServiceServer mockAssetService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM local_audit_records");
        jdbcTemplate.update("DELETE FROM inventory_records");
        jdbcTemplate.update("DELETE FROM storage_places");
        jdbcTemplate.update("DELETE FROM warehouses");

        mockUserService = MockRestServiceServer.bindTo(userServiceRestTemplate)
            .ignoreExpectOrder(true)
            .build();
        mockAssetService = MockRestServiceServer.bindTo(assetServiceRestTemplate)
            .ignoreExpectOrder(true)
            .build();
    }

    protected UUID insertWarehouse(UUID tenantId, UUID branchId) {
        return insertWarehouse(tenantId, branchId, null);
    }

    protected UUID insertWarehouse(UUID tenantId, UUID branchId, UUID locationId) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO warehouses (id, tenant_id, branch_id, location_id) VALUES (?, ?, ?, ?)",
            id,
            tenantId,
            branchId,
            locationId
        );
        return id;
    }

    protected UUID insertStoragePlace(UUID tenantId, UUID warehouseId) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO storage_places (id, tenant_id, warehouse_id) VALUES (?, ?, ?)",
            id,
            tenantId,
            warehouseId
        );
        return id;
    }

    protected UUID insertInventoryRecord(UUID tenantId, UUID assetId, UUID warehouseId, UUID storagePlaceId) {
        return insertInventoryRecord(tenantId, assetId, warehouseId, storagePlaceId, ReadinessState.NOT_READY);
    }

    protected UUID insertInventoryRecord(
        UUID tenantId,
        UUID assetId,
        UUID warehouseId,
        UUID storagePlaceId,
        ReadinessState readinessState
    ) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO inventory_records (id, tenant_id, asset_id, warehouse_id, storage_place_id, readiness_state) "
                + "VALUES (?, ?, ?, ?, ?, ?)",
            id,
            tenantId,
            assetId,
            warehouseId,
            storagePlaceId,
            readinessState.name()
        );
        return id;
    }

    protected int countRows(String tableName) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
        return count == null ? 0 : count;
    }

    protected InventoryRecordRow inventoryRecordRow(UUID tenantId, UUID assetId) {
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, warehouse_id, storage_place_id, readiness_state "
                + "FROM inventory_records WHERE tenant_id = ? AND asset_id = ?",
            resultSet -> {
                if (!resultSet.next()) {
                    throw new AssertionError("Expected inventory record to exist");
                }
                return new InventoryRecordRow(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getObject("tenant_id", UUID.class),
                    resultSet.getObject("asset_id", UUID.class),
                    resultSet.getObject("warehouse_id", UUID.class),
                    resultSet.getObject("storage_place_id", UUID.class),
                    ReadinessState.valueOf(resultSet.getString("readiness_state"))
                );
            },
            tenantId,
            assetId
        );
    }

    protected AuditRow latestAuditRow() {
        return jdbcTemplate.query(
            "SELECT tenant_id, actor, entity_type, action_type, correlation_id, change_payload "
                + "FROM local_audit_records ORDER BY occurred_at DESC, id DESC LIMIT 1",
            resultSet -> {
                if (!resultSet.next()) {
                    throw new AssertionError("Expected at least one audit record");
                }
                return new AuditRow(
                    resultSet.getObject("tenant_id", UUID.class),
                    resultSet.getString("actor"),
                    resultSet.getString("entity_type"),
                    resultSet.getString("action_type"),
                    resultSet.getString("correlation_id"),
                    resultSet.getString("change_payload")
                );
            }
        );
    }

    protected void expectRoleLookup(UUID tenantId, UUID authenticatedUserId, List<String> roleNames) throws Exception {
        mockUserService.expect(ExpectedCount.once(), requestTo(
                userServiceBaseUrl + "/internal/api/v1/users/" + authenticatedUserId + "/roles"
            ))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andRespond(withSuccess(
                objectMapper.writeValueAsString(new RoleResolutionResponse(authenticatedUserId, roleNames)),
                MediaType.APPLICATION_JSON
            ));
    }

    protected void expectRoleLookupFailure(UUID tenantId, UUID authenticatedUserId, HttpStatus responseStatus) {
        mockUserService.expect(ExpectedCount.once(), requestTo(
                userServiceBaseUrl + "/internal/api/v1/users/" + authenticatedUserId + "/roles"
            ))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andRespond(withStatus(responseStatus));
    }

    protected void expectAssetLookup(UUID tenantId, UUID authenticatedUserId, UUID assetId, HttpStatus responseStatus) {
        mockAssetService.expect(ExpectedCount.once(), requestTo(assetServiceBaseUrl + "/api/v1/assets/" + assetId))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andExpect(header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString()))
            .andRespond(responseStatus.is2xxSuccessful()
                ? withSuccess("{}", MediaType.APPLICATION_JSON)
                : withStatus(responseStatus));
    }

    protected void expectBranchLookup(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID branchId,
        HttpStatus responseStatus
    ) {
        expectUserServiceReferenceLookup("/api/v1/branches/" + branchId, tenantId, authenticatedUserId, responseStatus);
    }

    protected void expectLocationLookup(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID locationId,
        HttpStatus responseStatus
    ) {
        expectUserServiceReferenceLookup("/api/v1/locations/" + locationId, tenantId, authenticatedUserId, responseStatus);
    }

    private void expectUserServiceReferenceLookup(
        String path,
        UUID tenantId,
        UUID authenticatedUserId,
        HttpStatus responseStatus
    ) {
        mockUserService.expect(ExpectedCount.once(), requestTo(userServiceBaseUrl + path))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andExpect(header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString()))
            .andRespond(responseStatus.is2xxSuccessful()
                ? withSuccess("{}", MediaType.APPLICATION_JSON)
                : withStatus(responseStatus));
    }

    protected record InventoryRecordRow(
        UUID id,
        UUID tenantId,
        UUID assetId,
        UUID warehouseId,
        UUID storagePlaceId,
        ReadinessState readinessState
    ) {
    }

    protected record AuditRow(
        UUID tenantId,
        String actor,
        String entityType,
        String actionType,
        String correlationId,
        String changePayload
    ) {
    }

    private record RoleResolutionResponse(UUID userId, List<String> roleNames) {
    }
}
