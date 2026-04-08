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
            .andExpect(content().string(containsString("data-ui-surface=\"minimal-pilot-ui\"")))
            .andExpect(content().string(containsString("data-edit-boundary=\"asset-master-record-only\"")))
            .andExpect(content().string(containsString("data-responsibility-flows=\"assign,transfer,return\"")))
            .andExpect(content().string(containsString("id=\"session-form\"")))
            .andExpect(content().string(containsString("id=\"asset-create-form\"")))
            .andExpect(content().string(containsString("id=\"admin-resource-nav\"")));
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
            .andExpect(content().string(containsString("/api/v1/users")))
            .andExpect(content().string(containsString("/api/v1/roles")))
            .andExpect(content().string(containsString("/api/v1/departments")))
            .andExpect(content().string(containsString("/api/v1/branches")))
            .andExpect(content().string(containsString("/api/v1/locations")))
            .andExpect(content().string(not(containsString("/retire"))));
    }
}
