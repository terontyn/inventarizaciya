package com.itam.assetservice;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

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
            .andExpect(content().string(containsString("data-ui-surface=\"operational-workspace-tree-detail\"")))
            .andExpect(content().string(containsString("data-existing-surface=\"minimal-pilot-ui\"")))
            .andExpect(content().string(containsString("data-navigation-model=\"revised-tree-and-detail\"")))
            .andExpect(content().string(containsString("data-tree-derivation=\"client-side-only\"")))
            .andExpect(content().string(containsString("data-edit-boundary=\"asset-master-record-only\"")))
            .andExpect(content().string(containsString("data-responsibility-flows=\"assign,transfer,return\"")))
            .andExpect(content().string(containsString("id=\"session-form\"")))
            .andExpect(content().string(containsString("data-tree-detail-shell=\"true\"")))
            .andExpect(content().string(containsString("id=\"workspace-tree\"")))
            .andExpect(content().string(containsString("id=\"tree-filter\"")))
            .andExpect(content().string(containsString("id=\"workspace-tree-body\"")))
            .andExpect(content().string(containsString("id=\"workspace-detail\"")))
            .andExpect(result -> {
                String body = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
                assertThat(body, containsString("Создать"));
                assertThat(body, containsString("Актив / устройство"));
                assertThat(body, containsString("Пользователь"));
                assertThat(body, containsString("Подразделение"));
                assertThat(body, containsString("Филиал"));
                assertThat(body, containsString("Локация"));
            });
    }

    @Test
    void appScriptUsesExistingContractsAndDoesNotSurfaceRetireEndpoint() throws Exception {
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
            .andExpect(content().string(containsString("History / Audit")))
            .andExpect(content().string(containsString("Technical details")))
            .andExpect(content().string(containsString("TREE_GROUPS")))
            .andExpect(content().string(containsString("TREE_NODE_TYPES")))
            .andExpect(content().string(containsString("REVIEW_SECTIONS")))
            .andExpect(content().string(containsString("data-asset-section")))
            .andExpect(content().string(containsString("data-node-type")))
            .andExpect(content().string(containsString("data-context-feedback-slot")))
            .andExpect(content().string(containsString("applyTreeFilter")))
            .andExpect(content().string(containsString("form-support-disclosure")))
            .andExpect(content().string(containsString("renderTreeNavigation")))
            .andExpect(content().string(containsString("renderAssetDetailPanel")))
            .andExpect(content().string(containsString("assetDisplayTitle")))
            .andExpect(content().string(containsString("display_name")))
            .andExpect(content().string(containsString("name=\"note\"")))
            .andExpect(content().string(containsString("renderContextualFeedback")))
            .andExpect(content().string(containsString("openCreateTarget")))
            .andExpect(result -> {
                String body = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
                assertThat(body, containsString("Ответственность"));
                assertThat(body, containsString("Описание актива"));
                assertThat(body, containsString("Редактировать описание"));
                assertThat(body, containsString("Выдать сотруднику"));
                assertThat(body, containsString("Переместить"));
                assertThat(body, containsString("Вернуть"));
                assertThat(body, containsString("Информационный лист сотрудника"));
                assertThat(body, containsString("Закреплённые устройства"));
                assertThat(body, containsString("Только для чтения"));
                assertThat(body, containsString("Сформировано в рабочем пространстве учёта"));
                assertThat(body, containsString("Администратор"));
                assertThat(body, containsString("Оператор учёта"));
                assertThat(body, containsString("Наблюдатель"));
                assertThat(body, containsString("Аудитор"));
                assertThat(body, containsString("Быстрый поиск"));
                assertThat(body, containsString("Поиск"));
                assertThat(body, containsString("Сотрудники"));
                assertThat(body, containsString("Активы / устройства"));
                assertThat(body, containsString("По загруженным данным рабочего пространства"));
                assertThat(body, containsString("Без поиска на сервере"));
                assertThat(body, containsString("Поиск: сотрудник, инвентарный номер, название, серийный номер"));
                assertThat(body, containsString("Можно искать сотрудника, инвентарный номер, название актива, серийный номер или тип устройства"));
                assertThat(body, not(containsString("Личный кабинет")));
                assertThat(body, not(containsString("Портал сотрудника")));
                assertThat(body, not(containsString("Самообслуживание")));
                assertThat(body, not(containsString("самообслуживание")));
                assertThat(body, not(containsString("публичный поиск")));
                assertThat(body, not(containsString("поиск по ФИО")));
            })
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
            .andExpect(content().string(not(containsString("/search"))))
            .andExpect(content().string(not(containsString("public search"))))
            .andExpect(content().string(not(containsString("full-name lookup"))))
            .andExpect(content().string(not(containsString("employee self-service"))))
            .andExpect(content().string(not(containsString("note search"))))
            .andExpect(content().string(not(containsString("audit/history search"))))
            .andExpect(content().string(not(containsString("QR"))))
            .andExpect(content().string(not(containsString("export"))))
            .andExpect(content().string(not(containsString("PDF"))))
            .andExpect(content().string(not(containsString("Excel"))))
            .andExpect(content().string(not(containsString("print"))))
            .andExpect(content().string(not(containsString("/archive"))))
            .andExpect(content().string(not(containsString("/delete"))))
            .andExpect(content().string(not(containsString("Generic PATCH only"))))
            .andExpect(content().string(not(containsString("Mutate path"))))
            .andExpect(content().string(not(containsString("Master data"))))
            .andExpect(content().string(not(containsString("saved views"))))
            .andExpect(content().string(not(containsString("bulk actions"))))
            .andExpect(content().string(not(containsString("dashboard"))))
            .andExpect(content().string(not(containsString("search"))))
            .andExpect(content().string(not(containsString("Asset 360"))))
            .andExpect(content().string(not(containsString("Elasticsearch"))))
            .andExpect(content().string(not(containsString("global search"))))
            .andExpect(content().string(not(containsString("advanced filters"))));
    }
}
