package com.itam.maintenanceservice;

import static com.itam.maintenanceservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.maintenanceservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itam.maintenanceservice.domain.MaintenanceWorkState;
import com.itam.maintenanceservice.domain.MaintenanceWorkType;
import java.sql.Timestamp;
import java.time.Instant;
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

@SpringBootTest(classes = com.itam.maintenanceservice.boot.MaintenanceServiceApplication.class)
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
        jdbcTemplate.update("DELETE FROM maintenance_works");

        mockUserService = MockRestServiceServer.bindTo(userServiceRestTemplate)
            .ignoreExpectOrder(true)
            .build();
        mockAssetService = MockRestServiceServer.bindTo(assetServiceRestTemplate)
            .ignoreExpectOrder(true)
            .build();
    }

    protected UUID insertMaintenanceWork(
        UUID tenantId,
        UUID assetId,
        MaintenanceWorkType workType,
        MaintenanceWorkState workState,
        Instant openedAt,
        Instant completedAt
    ) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO maintenance_works (id, tenant_id, asset_id, work_type, work_state, opened_at, completed_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)",
            id,
            tenantId,
            assetId,
            workType.name(),
            workState.name(),
            Timestamp.from(openedAt),
            completedAt == null ? null : Timestamp.from(completedAt)
        );
        return id;
    }

    protected int countRows(String tableName) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
        return count == null ? 0 : count;
    }

    protected MaintenanceWorkRow maintenanceWorkRow(UUID tenantId, UUID workId) {
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, work_type, work_state, opened_at, completed_at "
                + "FROM maintenance_works WHERE tenant_id = ? AND id = ?",
            resultSet -> {
                if (!resultSet.next()) {
                    throw new AssertionError("Expected maintenance work to exist");
                }
                Timestamp completedAt = resultSet.getTimestamp("completed_at");
                return new MaintenanceWorkRow(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getObject("tenant_id", UUID.class),
                    resultSet.getObject("asset_id", UUID.class),
                    MaintenanceWorkType.valueOf(resultSet.getString("work_type")),
                    MaintenanceWorkState.valueOf(resultSet.getString("work_state")),
                    resultSet.getTimestamp("opened_at").toInstant(),
                    completedAt == null ? null : completedAt.toInstant()
                );
            },
            tenantId,
            workId
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

    protected record MaintenanceWorkRow(
        UUID id,
        UUID tenantId,
        UUID assetId,
        MaintenanceWorkType workType,
        MaintenanceWorkState workState,
        Instant openedAt,
        Instant completedAt
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
