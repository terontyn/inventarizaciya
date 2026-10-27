package com.itam.ticketservice;

import static com.itam.ticketservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_ACTOR_HEADER;
import static com.itam.ticketservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_TENANT_HEADER;
import static com.itam.ticketservice.api.AuthenticatedRequestContextExtractor.AUTHENTICATED_USER_HEADER;
import static com.itam.ticketservice.api.AuthenticatedRequestContextExtractor.CORRELATION_ID_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itam.ticketservice.application.PilotRoleNames;
import com.itam.ticketservice.domain.TicketState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class TicketApiIntegrationTest extends IntegrationTestSupport {

    @Test
    void readEndpointsReturnAssetLinkedCurrentVisibility() throws Exception {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetA = UUID.randomUUID();
        UUID assetB = UUID.randomUUID();
        UUID openA1 = insertTicket(
            tenantA,
            assetA,
            TicketState.OPEN,
            Instant.parse("2026-04-19T08:00:00Z"),
            null
        );
        UUID openA2 = insertTicket(
            tenantA,
            assetA,
            TicketState.OPEN,
            Instant.parse("2026-04-19T09:00:00Z"),
            null
        );
        UUID closedA = insertTicket(
            tenantA,
            assetA,
            TicketState.CLOSED,
            Instant.parse("2026-04-19T07:00:00Z"),
            Instant.parse("2026-04-19T10:00:00Z")
        );
        insertTicket(
            tenantB,
            assetB,
            TicketState.OPEN,
            Instant.parse("2026-04-19T11:00:00Z"),
            null
        );

        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantA, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetLookup(tenantA, supportReviewerId, assetA, HttpStatus.OK);

        mockMvc.perform(get("/api/v1/tickets/open")
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(openA2.toString()))
            .andExpect(jsonPath("$[0].asset_id").value(assetA.toString()))
            .andExpect(jsonPath("$[0].ticket_state").value("OPEN"))
            .andExpect(jsonPath("$[1].id").value(openA1.toString()));

        mockMvc.perform(get("/api/v1/tickets/{id}", closedA)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(closedA.toString()))
            .andExpect(jsonPath("$.asset_id").value(assetA.toString()))
            .andExpect(jsonPath("$.ticket_state").value("CLOSED"));

        mockMvc.perform(get("/api/v1/tickets/assets/{assetId}", assetA)
                .header(AUTHENTICATED_TENANT_HEADER, tenantA)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(openA2.toString()))
            .andExpect(jsonPath("$[1].id").value(openA1.toString()));

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void assetOperatorCanOpenMultipleTicketsOnSameAssetAndCloseOnlyAddressedTicketWithAudit() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID assetOperatorId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);
        expectRoleLookup(tenantId, assetOperatorId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, assetOperatorId, assetId, HttpStatus.OK);

        String firstOpenResponse = mockMvc.perform(post("/api/v1/tickets/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-open-1"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.ticket_state").value("OPEN"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID firstTicketId = UUID.fromString(objectMapper.readTree(firstOpenResponse).path("id").asText());
        TicketRow firstOpenRow = ticketRow(tenantId, firstTicketId);
        assertThat(firstOpenRow.ticketState()).isEqualTo(TicketState.OPEN);
        assertThat(firstOpenRow.closedAt()).isNull();

        AuditRow openAudit = latestAuditRow();
        assertThat(openAudit.actionType()).isEqualTo("OPEN_TICKET");
        assertThat(openAudit.correlationId()).isEqualTo("corr-open-1");
        assertThat(openAudit.changePayload()).contains(assetId.toString());

        String secondOpenResponse = mockMvc.perform(post("/api/v1/tickets/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-open-2"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.ticket_state").value("OPEN"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID secondTicketId = UUID.fromString(objectMapper.readTree(secondOpenResponse).path("id").asText());
        TicketRow secondOpenRow = ticketRow(tenantId, secondTicketId);
        assertThat(secondOpenRow.ticketState()).isEqualTo(TicketState.OPEN);
        assertThat(secondOpenRow.closedAt()).isNull();

        mockMvc.perform(post("/api/v1/tickets/{id}/close", firstTicketId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator")
                .header(CORRELATION_ID_HEADER, "corr-close-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(firstTicketId.toString()))
            .andExpect(jsonPath("$.asset_id").value(assetId.toString()))
            .andExpect(jsonPath("$.ticket_state").value("CLOSED"));

        TicketRow firstClosedRow = ticketRow(tenantId, firstTicketId);
        TicketRow secondStillOpenRow = ticketRow(tenantId, secondTicketId);
        assertThat(firstClosedRow.ticketState()).isEqualTo(TicketState.CLOSED);
        assertThat(firstClosedRow.closedAt()).isNotNull();
        assertThat(secondStillOpenRow.ticketState()).isEqualTo(TicketState.OPEN);
        assertThat(secondStillOpenRow.closedAt()).isNull();

        mockMvc.perform(get("/api/v1/tickets/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, assetOperatorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(secondTicketId.toString()))
            .andExpect(jsonPath("$[0].ticket_state").value("OPEN"));

        AuditRow closeAudit = latestAuditRow();
        assertThat(closeAudit.actionType()).isEqualTo("CLOSE_TICKET");
        assertThat(closeAudit.correlationId()).isEqualTo("corr-close-1");
        assertThat(closeAudit.changePayload()).contains("OPEN");
        assertThat(closeAudit.changePayload()).contains("CLOSED");

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void supportReviewerCanReadButCannotOpenOrCloseTickets() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID supportReviewerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID ticketId = insertTicket(
            tenantId,
            assetId,
            TicketState.OPEN,
            Instant.parse("2026-04-19T12:00:00Z"),
            null
        );

        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectRoleLookup(tenantId, supportReviewerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));

        mockMvc.perform(get("/api/v1/tickets/open")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/tickets/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/tickets/{id}/close", ticketId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, supportReviewerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "support-reviewer"))
            .andExpect(status().isForbidden());

        mockUserService.verify();
        mockAssetService.verify();
    }

    @Test
    void closeRejectsAlreadyClosedTicket() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID ticketId = insertTicket(
            tenantId,
            assetId,
            TicketState.CLOSED,
            Instant.parse("2026-04-19T08:00:00Z"),
            Instant.parse("2026-04-19T11:00:00Z")
        );

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, assetId, HttpStatus.OK);

        mockMvc.perform(post("/api/v1/tickets/{id}/close", ticketId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin"))
            .andExpect(status().isConflict());

        mockUserService.verify();
        mockAssetService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void openRejectsUnknownAsset() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID tenantAdminId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();

        expectRoleLookup(tenantId, tenantAdminId, List.of(PilotRoleNames.TENANT_ADMIN));
        expectAssetLookup(tenantId, tenantAdminId, assetId, HttpStatus.NOT_FOUND);

        mockMvc.perform(post("/api/v1/tickets/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, tenantAdminId)
                .header(AUTHENTICATED_ACTOR_HEADER, "tenant-admin"))
            .andExpect(status().isNotFound());

        mockUserService.verify();
        mockAssetService.verify();
        assertThat(countRows("local_audit_records")).isEqualTo(0);
    }

    @Test
    void ticketEndpointsReturnServiceUnavailableWhenCrossServiceLookupFails() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID callerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        UUID ticketId = insertTicket(
            tenantId,
            assetId,
            TicketState.OPEN,
            Instant.parse("2026-04-19T14:00:00Z"),
            null
        );

        expectRoleLookupFailure(tenantId, callerId, HttpStatus.INTERNAL_SERVER_ERROR);
        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, callerId, assetId, HttpStatus.INTERNAL_SERVER_ERROR);
        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.SUPPORT_REVIEWER));
        expectAssetLookup(tenantId, callerId, assetId, HttpStatus.INTERNAL_SERVER_ERROR);
        expectRoleLookup(tenantId, callerId, List.of(PilotRoleNames.ASSET_OPERATOR));
        expectAssetLookup(tenantId, callerId, assetId, HttpStatus.INTERNAL_SERVER_ERROR);

        mockMvc.perform(get("/api/v1/tickets/open")
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(post("/api/v1/tickets/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator"))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(get("/api/v1/tickets/assets/{assetId}", assetId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId))
            .andExpect(status().isServiceUnavailable());

        mockMvc.perform(post("/api/v1/tickets/{id}/close", ticketId)
                .header(AUTHENTICATED_TENANT_HEADER, tenantId)
                .header(AUTHENTICATED_USER_HEADER, callerId)
                .header(AUTHENTICATED_ACTOR_HEADER, "asset-operator"))
            .andExpect(status().isServiceUnavailable());

        mockUserService.verify();
        mockAssetService.verify();
    }
}
