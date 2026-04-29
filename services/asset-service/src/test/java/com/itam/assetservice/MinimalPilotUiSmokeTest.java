package com.itam.assetservice;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = com.itam.assetservice.boot.AssetServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MinimalPilotUiSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rootServesMinimalPilotUiShell() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("index.html"));

        mockMvc.perform(get("/index.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("data-ui-surface=\"operational-workspace-ux-v2\"")))
            .andExpect(content().string(containsString("data-existing-surface=\"minimal-pilot-ui\"")))
            .andExpect(content().string(containsString("data-edit-boundary=\"asset-master-record-only\"")))
            .andExpect(content().string(containsString("data-responsibility-flows=\"assign,transfer,return\"")))
            .andExpect(content().string(containsString("id=\"session-form\"")))
            .andExpect(content().string(containsString("id=\"workspace-screen\"")))
            .andExpect(content().string(containsString("id=\"assets-screen\"")))
            .andExpect(content().string(containsString("id=\"review-screen\"")))
            .andExpect(content().string(containsString("id=\"asset-create-form\"")))
            .andExpect(content().string(containsString("id=\"workspace-overview\"")))
            .andExpect(content().string(containsString("id=\"asset-journey-banner\"")))
            .andExpect(content().string(containsString("id=\"review-section-nav\"")))
            .andExpect(content().string(containsString("id=\"review-detail-panel\"")))
            .andExpect(content().string(containsString("id=\"admin-resource-nav\"")))
            .andExpect(content().string(containsString("id=\"setup-overview\"")));
    }

    @Test
    void appScriptUsesExistingContractsAndDoesNotSurfaceRetireUi() throws Exception {
        mockMvc.perform(get("/app.js"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/javascript")))
            .andExpect(content().string(containsString("/api/v1/assets")))
            .andExpect(content().string(containsString("/assign")))
            .andExpect(content().string(containsString("/transfer")))
            .andExpect(content().string(containsString("/return")))
            .andExpect(content().string(containsString("/history")))
            .andExpect(content().string(containsString("/audit")))
            .andExpect(content().string(containsString("PRIMARY_SCREENS")))
            .andExpect(content().string(containsString("ASSET_SECTIONS")))
            .andExpect(content().string(containsString("REVIEW_SECTIONS")))
            .andExpect(content().string(containsString("data-asset-section")))
            .andExpect(content().string(containsString("data-review-section")))
            .andExpect(content().string(containsString("renderAuditDetails")))
            .andExpect(content().string(containsString("/api/v1/users")))
            .andExpect(content().string(containsString("/api/v1/roles")))
            .andExpect(content().string(containsString("/api/v1/departments")))
            .andExpect(content().string(containsString("/api/v1/branches")))
            .andExpect(content().string(containsString("/api/v1/locations")))
            .andExpect(content().string(containsString("/api/v1/users/${userId}/roles")))
            .andExpect(content().string(not(containsString("/internal/api/v1/users/"))))
            .andExpect(content().string(not(containsString("/retire"))))
            .andExpect(content().string(not(containsString("/api/v1/inventory/"))))
            .andExpect(content().string(not(containsString("/api/v1/maintenance/"))))
            .andExpect(content().string(not(containsString("/api/v1/software/"))))
            .andExpect(content().string(not(containsString("/api/v1/tickets/"))))
            .andExpect(content().string(not(containsString("/api/v1/reporting/"))))
            .andExpect(content().string(not(containsString("Generic PATCH only"))))
            .andExpect(content().string(not(containsString("Mutate path"))))
            .andExpect(content().string(not(containsString("Master data"))))
            .andExpect(content().string(not(containsString("History / Audit"))))
            .andExpect(content().string(not(containsString("saved views"))))
            .andExpect(content().string(not(containsString("bulk actions"))))
            .andExpect(content().string(not(containsString("dashboard"))))
            .andExpect(content().string(not(containsString("search"))))
            .andExpect(content().string(not(containsString("Asset 360"))))
            .andExpect(content().string(not(containsString("Elasticsearch"))));
    }
}
