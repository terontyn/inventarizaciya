package com.itam.ticketservice;

import static com.itam.ticketservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.ticketservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itam.ticketservice.domain.TicketState;
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

@SpringBootTest(classes = com.itam.ticketservice.boot.TicketServiceApplication.class)
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
        jdbcTemplate.update("DELETE FROM tickets");

        mockUserService = MockRestServiceServer.bindTo(userServiceRestTemplate)
            .ignoreExpectOrder(true)
            .build();
        mockAssetService = MockRestServiceServer.bindTo(assetServiceRestTemplate)
            .ignoreExpectOrder(true)
            .build();
    }

    protected UUID insertTicket(
        UUID tenantId,
        UUID assetId,
        TicketState ticketState,
        Instant openedAt,
        Instant closedAt
    ) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO tickets (id, tenant_id, asset_id, ticket_state, opened_at, closed_at) VALUES (?, ?, ?, ?, ?, ?)",
            id,
            tenantId,
            assetId,
            ticketState.name(),
            Timestamp.from(openedAt),
            closedAt == null ? null : Timestamp.from(closedAt)
        );
        return id;
    }

    protected int countRows(String tableName) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
        return count == null ? 0 : count;
    }

    protected TicketRow ticketRow(UUID tenantId, UUID ticketId) {
        return jdbcTemplate.query(
            "SELECT id, tenant_id, asset_id, ticket_state, opened_at, closed_at FROM tickets WHERE tenant_id = ? AND id = ?",
            resultSet -> {
                if (!resultSet.next()) {
                    throw new AssertionError("Expected ticket to exist");
                }
                Timestamp closedAt = resultSet.getTimestamp("closed_at");
                return new TicketRow(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getObject("tenant_id", UUID.class),
                    resultSet.getObject("asset_id", UUID.class),
                    TicketState.valueOf(resultSet.getString("ticket_state")),
                    resultSet.getTimestamp("opened_at").toInstant(),
                    closedAt == null ? null : closedAt.toInstant()
                );
            },
            tenantId,
            ticketId
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

    protected record TicketRow(
        UUID id,
        UUID tenantId,
        UUID assetId,
        TicketState ticketState,
        Instant openedAt,
        Instant closedAt
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
