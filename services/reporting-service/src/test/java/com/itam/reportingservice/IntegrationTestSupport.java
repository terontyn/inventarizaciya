package com.itam.reportingservice;

import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.reportingservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = com.itam.reportingservice.boot.ReportingServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    @Qualifier("userServiceRestTemplate")
    protected RestTemplate userServiceRestTemplate;

    @Autowired
    @Qualifier("assetServiceRestTemplate")
    protected RestTemplate assetServiceRestTemplate;

    @Autowired
    @Qualifier("inventoryServiceRestTemplate")
    protected RestTemplate inventoryServiceRestTemplate;

    @Autowired
    @Qualifier("maintenanceServiceRestTemplate")
    protected RestTemplate maintenanceServiceRestTemplate;

    @Autowired
    @Qualifier("softwareServiceRestTemplate")
    protected RestTemplate softwareServiceRestTemplate;

    @Autowired
    @Qualifier("ticketServiceRestTemplate")
    protected RestTemplate ticketServiceRestTemplate;

    @Value("${integration.user-service.base-url}")
    protected String userServiceBaseUrl;

    @Value("${integration.asset-service.base-url}")
    protected String assetServiceBaseUrl;

    @Value("${integration.inventory-service.base-url}")
    protected String inventoryServiceBaseUrl;

    @Value("${integration.maintenance-service.base-url}")
    protected String maintenanceServiceBaseUrl;

    @Value("${integration.software-service.base-url}")
    protected String softwareServiceBaseUrl;

    @Value("${integration.ticket-service.base-url}")
    protected String ticketServiceBaseUrl;

    protected MockRestServiceServer mockUserService;
    protected MockRestServiceServer mockAssetService;
    protected MockRestServiceServer mockInventoryService;
    protected MockRestServiceServer mockMaintenanceService;
    protected MockRestServiceServer mockSoftwareService;
    protected MockRestServiceServer mockTicketService;

    @BeforeEach
    void setUp() {
        mockUserService = MockRestServiceServer.bindTo(userServiceRestTemplate).ignoreExpectOrder(true).build();
        mockAssetService = MockRestServiceServer.bindTo(assetServiceRestTemplate).ignoreExpectOrder(true).build();
        mockInventoryService = MockRestServiceServer.bindTo(inventoryServiceRestTemplate).ignoreExpectOrder(true).build();
        mockMaintenanceService = MockRestServiceServer.bindTo(maintenanceServiceRestTemplate).ignoreExpectOrder(true).build();
        mockSoftwareService = MockRestServiceServer.bindTo(softwareServiceRestTemplate).ignoreExpectOrder(true).build();
        mockTicketService = MockRestServiceServer.bindTo(ticketServiceRestTemplate).ignoreExpectOrder(true).build();
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

    protected void expectAssetList(UUID tenantId, UUID authenticatedUserId, List<AssetResponse> assets) throws Exception {
        mockAssetService.expect(ExpectedCount.once(), requestTo(assetServiceBaseUrl + "/api/v1/assets"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andExpect(header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString()))
            .andRespond(withSuccess(objectMapper.writeValueAsString(assets), MediaType.APPLICATION_JSON));
    }

    protected void expectAssetListFailure(UUID tenantId, UUID authenticatedUserId, HttpStatus responseStatus) {
        mockAssetService.expect(ExpectedCount.once(), requestTo(assetServiceBaseUrl + "/api/v1/assets"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andExpect(header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString()))
            .andRespond(withStatus(responseStatus));
    }

    protected void expectAssetDetail(
        UUID tenantId,
        UUID authenticatedUserId,
        AssetResponse asset,
        HttpStatus responseStatus
    ) throws Exception {
        mockAssetService.expect(ExpectedCount.once(), requestTo(assetServiceBaseUrl + "/api/v1/assets/" + asset.id()))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andExpect(header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString()))
            .andRespond(responseStatus.is2xxSuccessful()
                ? withSuccess(objectMapper.writeValueAsString(asset), MediaType.APPLICATION_JSON)
                : withStatus(responseStatus));
    }

    protected void expectInventorySlice(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId,
        HttpStatus responseStatus,
        InventoryAssetResponse body
    ) throws Exception {
        mockInventoryService.expect(ExpectedCount.once(), requestTo(inventoryServiceBaseUrl + "/api/v1/inventory/assets/" + assetId))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andExpect(header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString()))
            .andRespond(responseStatus.is2xxSuccessful()
                ? withSuccess(objectMapper.writeValueAsString(body), MediaType.APPLICATION_JSON)
                : withStatus(responseStatus));
    }

    protected void expectMaintenanceSlice(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId,
        HttpStatus responseStatus,
        AssetMaintenanceResponse body
    ) throws Exception {
        mockMaintenanceService.expect(ExpectedCount.once(), requestTo(
                maintenanceServiceBaseUrl + "/api/v1/maintenance/assets/" + assetId
            ))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andExpect(header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString()))
            .andRespond(responseStatus.is2xxSuccessful()
                ? withSuccess(objectMapper.writeValueAsString(body), MediaType.APPLICATION_JSON)
                : withStatus(responseStatus));
    }

    protected void expectTicketSlice(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId,
        HttpStatus responseStatus,
        List<TicketResponse> body
    ) throws Exception {
        mockTicketService.expect(ExpectedCount.once(), requestTo(ticketServiceBaseUrl + "/api/v1/tickets/assets/" + assetId))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andExpect(header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString()))
            .andRespond(responseStatus.is2xxSuccessful()
                ? withSuccess(objectMapper.writeValueAsString(body), MediaType.APPLICATION_JSON)
                : withStatus(responseStatus));
    }

    protected void expectSoftwareSlice(
        UUID tenantId,
        UUID authenticatedUserId,
        UUID assetId,
        HttpStatus responseStatus,
        List<SoftwareRecordSummaryResponse> body
    ) throws Exception {
        mockSoftwareService.expect(ExpectedCount.once(), requestTo(
                softwareServiceBaseUrl + "/api/v1/software/assets/" + assetId + "/software"
            ))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(AUTHENTICATED_TENANT_HEADER, tenantId.toString()))
            .andExpect(header(AUTHENTICATED_USER_HEADER, authenticatedUserId.toString()))
            .andRespond(responseStatus.is2xxSuccessful()
                ? withSuccess(objectMapper.writeValueAsString(body), MediaType.APPLICATION_JSON)
                : withStatus(responseStatus));
    }

    protected record RoleResolutionResponse(UUID userId, List<String> roleNames) {
    }

    protected record AssetResponse(
        UUID id,
        String assetTag,
        String serialNumber,
        String assetType,
        String status,
        UUID assignedUserId,
        UUID locationId
    ) {
    }

    protected record InventoryAssetResponse(
        UUID assetId,
        Boolean inStorageNow,
        UUID warehouseId,
        UUID storagePlaceId,
        String readinessState
    ) {
    }

    protected record AssetMaintenanceResponse(
        UUID assetId,
        Boolean hasOpenWork,
        Boolean serviceableNow,
        UUID workId,
        String workType,
        String workState,
        String openedAt,
        String completedAt
    ) {
    }

    protected record TicketResponse(
        UUID id,
        UUID assetId,
        String ticketState,
        String openedAt,
        String closedAt
    ) {
    }

    protected record SoftwareRecordSummaryResponse(UUID id, String name) {
    }
}
