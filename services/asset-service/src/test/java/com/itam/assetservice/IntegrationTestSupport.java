package com.itam.assetservice;

import static com.itam.assetservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
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

    @Value("${integration.user-service.base-url}")
    protected String userServiceBaseUrl;

    protected MockRestServiceServer mockUserService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM asset_responsibility_facts");
        jdbcTemplate.update("DELETE FROM local_audit_records");
        jdbcTemplate.update("DELETE FROM assets");
        mockUserService = MockRestServiceServer.createServer(userServiceRestTemplate);
    }

    protected UUID insertAsset(
        UUID tenantId,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        UUID assignedUserId,
        UUID locationId
    ) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO assets ("
                + "id, tenant_id, asset_tag, serial_number, asset_type, status, assigned_user_id, location_id"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            id,
            tenantId,
            assetTag,
            serialNumber,
            assetType,
            status,
            assignedUserId,
            locationId
        );
        return id;
    }

    protected int countRows(String tableName) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
        return count == null ? 0 : count;
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

    protected ResponsibilityFactRow latestResponsibilityFactRow() {
        return jdbcTemplate.query(
            "SELECT tenant_id, asset_id, fact_type, previous_assigned_user_id, new_assigned_user_id, "
                + "previous_location_id, new_location_id "
                + "FROM asset_responsibility_facts ORDER BY occurred_at DESC, id DESC LIMIT 1",
            resultSet -> {
                if (!resultSet.next()) {
                    throw new AssertionError("Expected at least one responsibility fact");
                }
                return new ResponsibilityFactRow(
                    resultSet.getObject("tenant_id", UUID.class),
                    resultSet.getObject("asset_id", UUID.class),
                    resultSet.getString("fact_type"),
                    resultSet.getObject("previous_assigned_user_id", UUID.class),
                    resultSet.getObject("new_assigned_user_id", UUID.class),
                    resultSet.getObject("previous_location_id", UUID.class),
                    resultSet.getObject("new_location_id", UUID.class)
                );
            }
        );
    }

    protected AssetRow assetRow(UUID tenantId, UUID assetId) {
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_tag, serial_number, asset_type, status, assigned_user_id, location_id "
                + "FROM assets WHERE tenant_id = ? AND id = ?",
            resultSet -> {
                if (!resultSet.next()) {
                    throw new AssertionError("Expected asset row to exist");
                }
                return new AssetRow(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getObject("tenant_id", UUID.class),
                    resultSet.getString("asset_tag"),
                    resultSet.getString("serial_number"),
                    resultSet.getString("asset_type"),
                    resultSet.getString("status"),
                    resultSet.getObject("assigned_user_id", UUID.class),
                    resultSet.getObject("location_id", UUID.class)
                );
            },
            tenantId,
            assetId
        );
    }

    protected void expectUserLookup(UUID tenantId, UUID userId, HttpStatus responseStatus) {
        expectReferenceLookup("/api/v1/users/" + userId, tenantId, responseStatus);
    }

    protected void expectLocationLookup(UUID tenantId, UUID locationId, HttpStatus responseStatus) {
        expectReferenceLookup("/api/v1/locations/" + locationId, tenantId, responseStatus);
    }

    private void expectReferenceLookup(String path, UUID tenantId, HttpStatus responseStatus) {
        mockUserService.expect(requestTo(userServiceBaseUrl + path))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andRespond(responseStatus.is2xxSuccessful()
                ? withSuccess("{}", MediaType.APPLICATION_JSON)
                : withStatus(responseStatus));
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

    protected record ResponsibilityFactRow(
        UUID tenantId,
        UUID assetId,
        String factType,
        UUID previousAssignedUserId,
        UUID newAssignedUserId,
        UUID previousLocationId,
        UUID newLocationId
    ) {
    }

    protected record AssetRow(
        UUID id,
        UUID tenantId,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        UUID assignedUserId,
        UUID locationId
    ) {
    }
}
