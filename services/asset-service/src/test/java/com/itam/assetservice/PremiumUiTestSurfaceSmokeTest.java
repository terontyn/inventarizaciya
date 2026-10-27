package com.itam.assetservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Premium UI Test Surface — Smoke Test
 *
 * Verifies that the isolated test surface at /test-ui/ is served correctly
 * by Spring Boot's default static resource handler without any additional
 * WebMvcConfigurer configuration.
 *
 * Scope:
 *  - HTTP 200 for all three premium UI static assets
 *  - Correct Content-Type for each asset
 *  - Non-regression guard: root "/" still serves the production SPA shell
 *
 * Negative guardrails (enforced by this test):
 *  - No database interaction (all test-ui operations are in-memory in the browser)
 *  - No modification of production static files
 *  - No Spring Security or JWT dependency required
 *
 * This test does NOT authorize migration of /app.js to the premium layout.
 * This test surface is branch-isolated: feature/premium-ui-test-surface only.
 */
@SpringBootTest(classes = com.itam.assetservice.boot.AssetServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PremiumUiTestSurfaceSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Positive: test-ui resources are served ──────────────────────────────

    @Test
    void testUiIndexHtmlIsServedWithStatus200() throws Exception {
        mockMvc.perform(get("/test-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("ITAM")))
                .andExpect(content().string(containsString("login-screen")))
                .andExpect(content().string(containsString("quick-admin")))
                .andExpect(content().string(containsString("quick-employee")));
    }

    @Test
    void testUiStyleCssIsServedWithStatus200() throws Exception {
        mockMvc.perform(get("/test-ui/style.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.valueOf("text/css")));
    }

    @Test
    void testUiAppJsIsServedWithStatus200() throws Exception {
        mockMvc.perform(get("/test-ui/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.valueOf("application/javascript")));
    }

    // ── Negative: premium test-ui app.js must NOT call any backend endpoints ─

    @Test
    void testUiAppJsContainsNoBackendApiCalls() throws Exception {
        mockMvc.perform(get("/test-ui/app.js"))
                .andExpect(status().isOk())
                // in-memory only — no fetch() or XMLHttpRequest to backend
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("fetch("))))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("XMLHttpRequest"))))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("/api/v1/"))))
                // no localStorage persistence
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("localStorage.setItem"))))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("localStorage.getItem"))))
                // no hard delete, correction delete, bulk operations
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("DELETE"))))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("correction delete"))))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("hard delete"))));
    }

    // ── Non-regression: production root UI is completely untouched ───────────

    @Test
    void productionRootUiRemainsUnchangedAndUnaffectedByTestSurface() throws Exception {
        // Root "/" must still forward to the original production index.html
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        // Production index.html must still contain its canonical data-attributes
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString(
                        "data-ui-surface=\"operational-workspace-tree-detail\"")))
                .andExpect(content().string(containsString(
                        "data-existing-surface=\"minimal-pilot-ui\"")))
                .andExpect(content().string(containsString(
                        "data-navigation-model=\"revised-tree-and-detail\"")))
                .andExpect(content().string(containsString(
                        "id=\"workspace-tree\"")))
                .andExpect(content().string(containsString(
                        "id=\"session-form\"")))
                // Must NOT contain premium UI markers — confirms no file mix-up
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("login-screen"))))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("quick-admin"))))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(containsString("quick-employee"))));

        // Production app.js must still reference real backend API contracts
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.valueOf("application/javascript")))
                .andExpect(content().string(containsString("/api/v1/assets")))
                .andExpect(content().string(containsString("renderTreeNavigation")))
                .andExpect(content().string(containsString("TREE_GROUPS")));
    }
}
