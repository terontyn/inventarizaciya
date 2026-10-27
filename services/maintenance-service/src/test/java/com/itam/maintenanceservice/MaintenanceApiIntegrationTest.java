package com.itam.maintenanceservice;

import static com.itam.maintenanceservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.maintenanceservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.maintenanceservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.maintenanceservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itam.maintenanceservice.application.PilotRoleNames;
import com.itam.maintenanceservice.domain.MaintenanceWorkState;
import com.itam.maintenanceservice.domain.MaintenanceWorkType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class MaintenanceApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void readEndpointsReturnTenantScopedOpenWorkAndAssetMaintenanceView() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetA = UUID.randomUUID();
        UUID assetB = UUID.randomUUID();
        UUID workA = insertMaintenanceWork(
            tenantA,
            assetA,
            MaintenanceWorkType.INSPECTION,
            MaintenanceWorkState.OPEN,
            Instant.parse("2026-04-17T08:00:00Z"),
            null
        );
        insertMaintenanceWork(
            tenantB,
            assetB,
            MaintenanceWorkType.REPAIR,
            MaintenanceWorkState.OPEN,
            Instant.parse("2026-04-17T09:00:00Z"),
            null
        );

        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetLookup(tenantA, supportReviewerId, assetA, HttpStatus.OK);

        mockMvc.perform(get("/api/v1/maintenance/work/open")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(workA.toString()))
            .andExpect(jsonPath("$[0].asset_id").value(assetA.toString()))
            .andExpect(jsonPath("$[0].work_type").value("INSPECTION"))
            .andExpect(jsonPath("$[0].work_state").value("OPEN"));

        mockMvc.perform(get("/api/v1/maintenance/work/{id}", workA)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(workA.toString()))
            .andExpect(jsonPath("$.asset_id").value(assetA.toString()))
            .andExpect(jsonPath("$.work_type").value("INSPECTION"))
            .andExpect(jsonPath("$.work_state").value("OPEN"));

        mockMvc.perform(get("/api/v1/maintenance/assets/{assetId}", assetA)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetA.toString()))
            .andExpect(jsonPath("$.has_open_work").value(true))
            .andExpect(jsonPath("$.serviceable_now").value(false))
            .andExpect(jsonPath("$.work_id").value(workA.toString()))
            .andExpect(jsonPath("$.work_type").value("INSPECTION"))
            .andExpect(jsonPath("$.work_state").value("OPEN"));

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void assetOperatorCanOpenAndCompleteWorkWithAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);

        String responseBody = mockMvc.perform(post("/api/v1/maintenance/assets/{assetId}/work", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-open")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("work_type", "REPAIR"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.work_type").value("REPAIR"))
            .andExpect(jsonPath("$.work_state").value("OPEN"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID workId = UUID.fromString(objectMapper.readTree(responseBody).path("id").asText());
        MaintenanceWorkRow openRow = maintenanceWorkRow(tenantId, workId);
        assertThat(openRow.workState()).isEqualTo(MaintenanceWorkState.OPEN);
        assertThat(openRow.completedAt()).isNull();

        AuditRow openAudit = latestAuditRow();
        assertThat(openAudit.actionType()).isEqualTo("OPEN_WORK");
        assertThat(openAudit.correlationId()).isEqualTo("corr-open");
        assertThat(openAudit.changePayload()).contains("REPAIR");

        mockMvc.perform(post("/api/v1/maintenance/work/{id}/complete", workId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(workId.toString()))
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.work_state").value("COMPLETED"));

        MaintenanceWorkRow completedRow = maintenanceWorkRow(tenantId, workId);
        assertThat(completedRow.workState()).isEqualTo(MaintenanceWorkState.COMPLETED);
        assertThat(completedRow.completedAt()).isNotNull();

        AuditRow completeAudit = latestAuditRow();
        assertThat(completeAudit.actionType()).isEqualTo("COMPLETE_WORK");
        assertThat(completeAudit.correlationId()).isEqualTo("corr-complete");
        assertThat(completeAudit.changePayload()).contains("OPEN");
        assertThat(completeAudit.changePayload()).contains("COMPLETED");

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void supportReviewerCanReadButCannotOpenOrCompleteWork() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID workId = insertMaintenanceWork(
            tenantId,
            assetId,
            MaintenanceWorkType.SERVICING,
            MaintenanceWorkState.OPEN,
            Instant.now(),
            null
        );

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));

        mockMvc.perform(get("/api/v1/maintenance/work/open")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/maintenance/assets/{assetId}/work", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("work_type", "INSPECTION"))))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/maintenance/work/{id}/complete", workId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer"))
            .andExpect(status().isForbidden());

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void oneOpenWorkInvariantIsEnforcedAtApiLevel() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        insertMaintenanceWork(
            tenantId,
            assetId,
            MaintenanceWorkType.INSPECTION,
            MaintenanceWorkState.OPEN,
            Instant.now(),
            null
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, assetId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/maintenance/assets/{assetId}/work", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("work_type", "REPAIR"))))
            .andExpect(status().isConflict());

        mockUserService.verify();
        mockAssetService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void assetMaintenanceViewReturnsServiceableWhenTrackedAssetHasNoOpenWork() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetLookup(tenantId, supportReviewerId, assetId, HttpStatus.OK);

        mockMvc.perform(get("/api/v1/maintenance/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.has_open_work").value(false))
            .andExpect(jsonPath("$.serviceable_now").value(true));

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void completeRejectsAlreadyCompletedWork() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID workId = insertMaintenanceWork(
            tenantId,
            assetId,
            MaintenanceWorkType.REPAIR,
            MaintenanceWorkState.COMPLETED,
            Instant.parse("2026-04-16T10:00:00Z"),
            Instant.parse("2026-04-16T12:00:00Z")
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, assetId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/maintenance/work/{id}/complete", workId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin"))
            .andExpect(status().isConflict());

        mockUserService.verify();
        mockAssetService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void endpointsReturnServiceUnavailableWhenCrossServiceLookupFails() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        expectRoleLookupFailure(tenantId, callerId, HttpStatus.INTERNAL_SERVER_ERROR);

        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, callerId, assetId, HttpStatus.INTERNAL_SERVER_ERROR);

        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetLookup(tenantId, callerId, assetId, HttpStatus.INTERNAL_SERVER_ERROR);

        mockMvc.perform(get("/api/v1/maintenance/work/open")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(post("/api/v1/maintenance/assets/{assetId}/work", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("work_type", "INSPECTION"))))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(get("/api/v1/maintenance/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isServiceUnavailable());

        mockUserService.verify();
        mockAssetService.verify();
    }
}
