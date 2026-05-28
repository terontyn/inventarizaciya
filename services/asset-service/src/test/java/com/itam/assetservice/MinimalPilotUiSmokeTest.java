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
    void appScriptUsesExistingContractsAndSurfacesBoundedLifecycleActions() throws Exception {
        mockMvc.perform(get("/app.js"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/javascript")))
            .andExpect(content().string(containsString("/api/v1/assets")))
            .andExpect(content().string(containsString("/assign")))
            .andExpect(content().string(containsString("/transfer")))
            .andExpect(content().string(containsString("/return")))
            .andExpect(content().string(containsString("/retire")))
            .andExpect(content().string(containsString("/archive")))
            .andExpect(content().string(containsString("/unarchive")))
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
                assertThat(body, containsString("Жизненный цикл"));
                assertThat(body, containsString("Списать актив"));
                assertThat(body, containsString("Списать актив?"));
                assertThat(body, containsString("Это не удаление записи"));
                assertThat(body, containsString("Актив списан. История и audit сохранены."));
                assertThat(body, containsString("Архивировать актив"));
                assertThat(body, containsString("Архивировать актив?"));
                assertThat(body, containsString("Актив будет скрыт из рабочих списков"));
                assertThat(body, containsString("история ответственности и audit сохранятся"));
                assertThat(body, containsString("Актив архивирован. Запись, история и audit сохранены."));
                assertThat(body, containsString("Архив"));
                assertThat(body, containsString("Разархивировать актив"));
                assertThat(body, containsString("Разархивировать актив?"));
                assertThat(body, containsString("Это не восстановление в эксплуатацию"));
                assertThat(body, containsString("Актив разархивирован. Он снова доступен в рабочих списках как списанный актив."));
                assertThat(body, containsString("RETIRED"));
                assertThat(body, containsString("Информационный лист сотрудника"));
                assertThat(body, containsString("Информационный лист"));
                assertThat(body, containsString("employeeSheet"));
                assertThat(body, containsString("standalone-employee-sheet"));
                assertThat(body, containsString("data-standalone-employee-id"));
                assertThat(body, containsString("Найти сотрудника по имени"));
                assertThat(body, containsString("Поиск только по уже загруженным пользователям рабочего пространства"));
                assertThat(body, containsString("Введите имя сотрудника"));
                assertThat(body, containsString("Пользователи ещё не загружены. Подключите контур и обновите справочники."));
                assertThat(body, containsString("Сотрудник не найден в загруженных данных."));
                assertThat(body, containsString("У сотрудника нет закреплённых устройств в загруженных данных."));
                assertThat(body, containsString("Закреплённые устройства"));
                assertThat(body, containsString("Только для чтения"));
                assertThat(body, containsString("Сформировано в рабочем пространстве учёта"));
                assertThat(body, containsString("Администратор"));
                assertThat(body, containsString("Оператор учёта"));
                assertThat(body, containsString("Наблюдатель"));
                assertThat(body, containsString("Аудитор"));
                assertThat(body, containsString("Быстрый поиск"));
                assertThat(body, containsString("Поиск"));
                assertThat(body, containsString("workspace-search-area"));
                assertThat(body, containsString("Сотрудники и активы"));
                assertThat(body, containsString("Поиск по уже загруженным сотрудникам и активам рабочего пространства"));
                assertThat(body, containsString("Ищет имя сотрудника, инвентарный тег, название, серийный номер и тип устройства"));
                assertThat(body, containsString("Сотрудники"));
                assertThat(body, containsString("Активы / устройства"));
                assertThat(body, containsString("По загруженным данным рабочего пространства"));
                assertThat(body, containsString("Без поиска на сервере"));
                assertThat(body, containsString("Поиск: сотрудник, инвентарный номер, название, серийный номер"));
                assertThat(body, containsString("Можно искать сотрудника, инвентарный номер, название актива, серийный номер или тип устройства"));
                assertThat(body, containsString("Совпадение по комментарию"));
                assertThat(body, containsString("Фильтр активов"));
                assertThat(body, containsString("Все рабочие"));
                assertThat(body, containsString("Назначенные"));
                assertThat(body, containsString("Неназначенные"));
                assertThat(body, containsString("Списанные"));
                assertThat(body, containsString("Тип"));
                assertThat(body, containsString("Локация"));
                assertThat(body, containsString("Сбросить фильтры"));
                assertThat(body, containsString("Сортировка активов"));
                assertThat(body, containsString("По названию"));
                assertThat(body, containsString("По инвентарному номеру"));
                assertThat(body, containsString("По типу"));
                assertThat(body, containsString("По статусу"));
                assertThat(body, containsString("По локации"));
                assertThat(body, containsString("Сбросить сортировку"));
                assertThat(body, containsString("QR Lite"));
                assertThat(body, containsString("QR по инвентарному номеру"));
                assertThat(body, containsString("Производный QR"));
                assertThat(body, containsString("Сформирован из существующего идентификатора актива"));
                assertThat(body, containsString("Не является отдельным идентификатором"));
                assertThat(body, containsString("Не открывает публичный доступ"));
                assertThat(body, containsString("Инвентарный тег"));
                assertThat(body, containsString("Технические подсказки"));
                assertThat(body, containsString("data-overview-technical-hints"));
                assertThat(body, containsString("data-overview-technical-hint"));
                assertThat(body, containsString("ASSET_OVERVIEW_TECHNICAL_HINT_FIELDS"));
                assertThat(body, containsString("Технические атрибуты"));
                assertThat(body, containsString("Технические данные"));
                assertThat(body, containsString("Данные устройства"));
                assertThat(body, containsString("Сетевые данные"));
                assertThat(body, containsString("Удалённый доступ"));
                assertThat(body, containsString("Телефонные данные"));
                assertThat(body, containsString("Данные принтера"));
                assertThat(body, containsString("Редактировать технические данные"));
                assertThat(body, containsString("Для этого типа актива технические атрибуты v1 не настроены"));
                assertThat(body, containsString("Hostname"));
                assertThat(body, containsString("Имя компьютера"));
                assertThat(body, containsString("IP-адрес"));
                assertThat(body, containsString("MAC-адрес"));
                assertThat(body, containsString("AnyDesk ID"));
                assertThat(body, containsString("IP телефона"));
                assertThat(body, containsString("Внутренний номер"));
                assertThat(body, containsString("SIP / линия"));
                assertThat(body, containsString("Сетевое имя принтера"));
                assertThat(body, containsString("IP принтера"));
                assertThat(body, containsString("Примечание по размещению"));
                assertThat(body, containsString("Поиск по техническим данным"));
                assertThat(body, containsString("Ищет по вручную заполненным техническим данным устройств"));
                assertThat(body, containsString("Поиск по утверждённым техническим атрибутам устройств через отдельный endpoint"));
                assertThat(body, containsString("Чувствительные значения остаются замаскированы"));
                assertThat(body, containsString("Быстрый поиск остаётся поиском по уже загруженным данным рабочего пространства"));
                assertThat(body, containsString("Активы / устройства"));
                assertThat(body, containsString("Введите минимум 2 символа"));
                assertThat(body, containsString("Ничего не найдено по утверждённым техническим данным"));
                assertThat(body, containsString("Часть значения скрыта"));
                assertThat(body, containsString("/api/v1/assets/technical-attribute-search"));
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
            .andExpect(content().string(not(containsString("note snippet"))))
            .andExpect(content().string(not(containsString("note highlight"))))
            .andExpect(content().string(not(containsString("raw note"))))
            .andExpect(content().string(not(containsString("data-employee-sheet-note"))))
            .andExpect(content().string(not(containsString("data-technical-search-note"))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"anydesk_id\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"ip_address\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"mac_address\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"phone_ip\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"sip_line_id\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"printer_ip\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"printer_placement_note\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"note\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"profile\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"technical_attributes\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-hint=\"qr\""))))
            .andExpect(content().string(not(containsString("data-overview-technical-edit"))))
            .andExpect(content().string(not(containsString("overview-technical-hints-form"))))
            .andExpect(content().string(not(containsString("audit/history search"))))
            .andExpect(content().string(not(containsString("scanner"))))
            .andExpect(content().string(not(containsString("scan"))))
            .andExpect(content().string(not(containsString("camera"))))
            .andExpect(content().string(not(containsString("mobile scan"))))
            .andExpect(content().string(not(containsString("public lookup"))))
            .andExpect(content().string(not(containsString("public URL"))))
            .andExpect(content().string(not(containsString("qr_code"))))
            .andExpect(content().string(not(containsString("barcode"))))
            .andExpect(content().string(not(containsString("inventory_number"))))
            .andExpect(content().string(not(containsString("remote QR service"))))
            .andExpect(content().string(not(containsString("QR search"))))
            .andExpect(content().string(not(containsString("export"))))
            .andExpect(content().string(not(containsString("PDF"))))
            .andExpect(content().string(not(containsString("Excel"))))
            .andExpect(content().string(not(containsString("window.print"))))
            .andExpect(content().string(not(containsString("print action"))))
            .andExpect(content().string(not(containsString("/delete"))))
            .andExpect(content().string(not(containsString("bulk retire"))))
            .andExpect(content().string(not(containsString("bulk archive"))))
            .andExpect(content().string(not(containsString("bulk unarchive"))))
            .andExpect(content().string(not(containsString("correction delete"))))
            .andExpect(content().string(not(containsString("hard delete"))))
            .andExpect(content().string(not(containsString("generic status editor"))))
            .andExpect(content().string(not(containsString("recovery"))))
            .andExpect(content().string(not(containsString("Восстановить в работу"))))
            .andExpect(content().string(not(containsString("Вернуть в эксплуатацию"))))
            .andExpect(content().string(not(containsString("Сделать активным"))))
            .andExpect(content().string(not(containsString("Назначить обратно"))))
            .andExpect(content().string(not(containsString("Откатить списание"))))
            .andExpect(content().string(not(containsString("data-quick-find-retire"))))
            .andExpect(content().string(not(containsString("data-technical-search-retire"))))
            .andExpect(content().string(not(containsString("data-employee-sheet-retire"))))
            .andExpect(content().string(not(containsString("data-quick-find-archive"))))
            .andExpect(content().string(not(containsString("data-technical-search-archive"))))
            .andExpect(content().string(not(containsString("data-employee-sheet-archive"))))
            .andExpect(content().string(not(containsString("data-quick-find-unarchive"))))
            .andExpect(content().string(not(containsString("data-technical-search-unarchive"))))
            .andExpect(content().string(not(containsString("data-employee-sheet-unarchive"))))
            .andExpect(content().string(not(containsString("data-asset-filter-retire"))))
            .andExpect(content().string(not(containsString("data-asset-filter-archive"))))
            .andExpect(content().string(not(containsString("data-asset-filter-unarchive"))))
            .andExpect(content().string(not(containsString("data-asset-sort-retire"))))
            .andExpect(content().string(not(containsString("data-asset-sort-archive"))))
            .andExpect(content().string(not(containsString("data-asset-sort-unarchive"))))
            .andExpect(content().string(not(containsString("data-asset-sort-bulk"))))
            .andExpect(content().string(not(containsString("data-asset-sort-export"))))
            .andExpect(content().string(not(containsString("data-asset-sort-persist"))))
            .andExpect(content().string(not(containsString("assetSortLocalStorage"))))
            .andExpect(content().string(not(containsString("Generic PATCH only"))))
            .andExpect(content().string(not(containsString("Mutate path"))))
            .andExpect(content().string(not(containsString("Master data"))))
            .andExpect(content().string(not(containsString("data-technical-search-action"))))
            .andExpect(content().string(not(containsString("data-employee-sheet-action"))))
            .andExpect(content().string(not(containsString("saved views"))))
            .andExpect(content().string(not(containsString("persistent preferences"))))
            .andExpect(content().string(not(containsString("bulk actions"))))
            .andExpect(content().string(not(containsString("dashboard"))))
            .andExpect(content().string(not(containsString("Asset 360"))))
            .andExpect(content().string(not(containsString("Elasticsearch"))))
            .andExpect(content().string(not(containsString("global search"))))
            .andExpect(content().string(not(containsString("advanced filters"))));
    }
}
