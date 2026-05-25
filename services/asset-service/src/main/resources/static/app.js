const STORAGE_KEY = "itam-minimal-pilot-ui-context";

const UI_PROFILE_NAMES = {
    TENANT_ADMIN: "Tenant Admin",
    ASSET_OPERATOR: "Asset Operator",
    SUPPORT_REVIEWER: "Support Reviewer",
    NO_APPROVED_ROLE: "Нет approved role"
};

const PRIMARY_SCREENS = {
    WORKSPACE: "workspace",
    ASSETS: "assets",
    REVIEW: "review",
    SETUP: "setup"
};

const SCREEN_ANCHORS = {
    [PRIMARY_SCREENS.WORKSPACE]: "workspace-screen",
    [PRIMARY_SCREENS.ASSETS]: "assets-screen",
    [PRIMARY_SCREENS.REVIEW]: "review-screen",
    [PRIMARY_SCREENS.SETUP]: "admin-screen"
};

const ASSET_SECTIONS = [
    { key: "overview", label: "Overview" },
    { key: "responsibility", label: "Responsibility" },
    { key: "actions", label: "Actions" },
    { key: "history", label: "History / Audit" },
    { key: "technical", label: "Technical details" }
];

const REVIEW_SECTIONS = [
    { key: "history", label: "История" },
    { key: "audit", label: "Аудит" }
];

const TREE_GROUPS = {
    WORKSPACE: "workspace",
    EMPLOYEE_SHEET: "employee-sheet",
    ORG: "org",
    LOCATIONS: "locations",
    ALL_ASSETS: "all-assets",
    SETUP: "setup"
};

const TREE_NODE_TYPES = {
    WORKSPACE: "workspace",
    EMPLOYEE_SHEET: "employeeSheet",
    DEPARTMENT: "department",
    USER: "user",
    BRANCH: "branch",
    LOCATION: "location",
    ASSET: "asset",
    ASSET_GROUP: "assetGroup",
    SETUP: "setup"
};

const FALLBACK_DEPARTMENT_ID = "__no_department__";
const FALLBACK_BRANCH_ID = "__no_branch__";

const ASSET_GROUPS = {
    ASSIGNED: "assigned",
    UNASSIGNED: "unassigned",
    RETIRED: "retired"
};

const CREATE_TARGETS = {
    asset: { label: "Актив / устройство" },
    user: { label: "Пользователь", resource: "users" },
    department: { label: "Подразделение", resource: "departments" },
    branch: { label: "Филиал", resource: "branches" },
    location: { label: "Локация", resource: "locations" }
};

const QUICK_FIND_MIN_LENGTH = 2;
const QUICK_FIND_RESULT_LIMIT = 6;
const TECHNICAL_SEARCH_MIN_LENGTH = 2;
const TECHNICAL_SEARCH_MAX_LENGTH = 128;
const TECHNICAL_SEARCH_LIMIT = 10;
const TECHNICAL_ATTRIBUTE_MAX_LENGTH = 512;
const TECHNICAL_ATTRIBUTE_UNSUPPORTED_PROFILE = "UNSUPPORTED_GENERIC";
const EMPLOYEE_SHEET_TECHNICAL_HINT_FIELDS = [
    { key: "hostname", label: "Hostname" },
    { key: "extension_number", label: "Внутренний номер" },
    { key: "printer_network_name", label: "Сетевое имя принтера" }
];
const TECHNICAL_ATTRIBUTE_PROFILE_GROUPS = {
    PC_LAPTOP: [
        {
            title: "Данные устройства",
            fields: [
                { key: "hostname", label: "Hostname", secondaryLabel: "Имя компьютера" }
            ]
        },
        {
            title: "Сетевые данные",
            fields: [
                { key: "ip_address", label: "IP-адрес" },
                { key: "mac_address", label: "MAC-адрес" }
            ]
        },
        {
            title: "Удалённый доступ",
            fields: [
                { key: "anydesk_id", label: "AnyDesk ID" }
            ]
        }
    ],
    STATIONARY_PHONE: [
        {
            title: "Телефонные данные",
            fields: [
                { key: "phone_ip", label: "IP телефона" },
                { key: "extension_number", label: "Внутренний номер" },
                { key: "sip_line_id", label: "SIP / линия" }
            ]
        }
    ],
    PRINTER: [
        {
            title: "Данные принтера",
            fields: [
                { key: "printer_network_name", label: "Сетевое имя принтера" }
            ]
        },
        {
            title: "Сетевые данные",
            fields: [
                { key: "printer_ip", label: "IP принтера" }
            ]
        },
        {
            title: "Размещение",
            fields: [
                { key: "printer_placement_note", label: "Примечание по размещению", multiline: true }
            ]
        }
    ]
};
const QR_LITE_VERSIONS = [
    { version: 1, dataCodewords: 19, eccCodewords: 7, alignment: [] },
    { version: 2, dataCodewords: 34, eccCodewords: 10, alignment: [6, 18] },
    { version: 3, dataCodewords: 55, eccCodewords: 15, alignment: [6, 22] },
    { version: 4, dataCodewords: 80, eccCodewords: 20, alignment: [6, 26] },
    { version: 5, dataCodewords: 108, eccCodewords: 26, alignment: [6, 30] }
];
const QR_LITE_FORMAT_BITS_LOW_MASK_0 = 0b111011111000100;

const SETUP_SEQUENCE = [
    {
        key: "roles",
        label: "Роли",
        description: "Сначала зафиксируйте роли tenant для текущего рабочего контура."
    },
    {
        key: "users",
        label: "Пользователи",
        description: "После создания пользователей назначьте роли отдельным действием."
    },
    {
        key: "branches",
        label: "Филиалы",
        description: "Филиалы задают физическую основу для локаций."
    },
    {
        key: "locations",
        label: "Локации",
        description: "Локации используются как текущая рабочая локация."
    },
    {
        key: "departments",
        label: "Подразделения",
        description: "Подразделения помогают связать пользователей с организационной структурой."
    }
];

const ADMIN_RESOURCE_META = {
    roles: {
        helper: "Создайте базовые роли Tenant Admin, Asset Operator и Support Reviewer перед настройкой пользователей.",
        empty: "Роли ещё не созданы. Начните именно с этого шага.",
        prerequisite: null
    },
    users: {
        helper: "После создания пользователя назначьте ему роль отдельным действием.",
        empty: "Пользователи ещё не созданы.",
        prerequisite: "Рекомендуется сначала создать роли, чтобы назначение ролей не откладывалось."
    },
    branches: {
        helper: "Филиалы помогают держать структуру локаций понятной.",
        empty: "Филиалы ещё не созданы.",
        prerequisite: null
    },
    locations: {
        helper: "Локации используются в transfer и показываются отдельно от статуса актива.",
        empty: "Локации ещё не созданы.",
        prerequisite: "Для создания локации нужен хотя бы один филиал."
    },
    departments: {
        helper: "Подразделения уточняют организационную привязку пользователей без изменения правил ролей.",
        empty: "Подразделения ещё не созданы.",
        prerequisite: "Обычно этот шаг удобнее делать после филиалов и локаций, когда справочники уже стабилизированы."
    }
};

const HISTORY_FACT_LABELS = {
    ASSIGN: "Выдача пользователю",
    ASSIGNED: "Выдача пользователю",
    TRANSFER: "Перемещение",
    TRANSFERRED: "Перемещение",
    RETURN: "Возврат",
    RETURNED: "Возврат",
    RETIRE: "Списание",
    RETIRED: "Списание"
};

const AUDIT_ACTION_LABELS = {
    CREATE: "Создание актива",
    CREATE_ASSET: "Создание актива",
    UPDATE: "Изменение данных актива",
    UPDATE_ASSET: "Изменение данных актива",
    ASSIGN: "Выдача пользователю",
    ASSIGN_ASSET: "Выдача пользователю",
    TRANSFER: "Перемещение",
    TRANSFER_ASSET: "Перемещение",
    RETURN: "Возврат",
    RETURN_ASSET: "Возврат",
    RETIRE: "Списание"
};

const ACTION_MESSAGES = {
    assetCreated: "Актив создан. Карточка открыта и обновлена.",
    assetUpdated: "Данные актива сохранены. Ответственность не менялась.",
    technicalAttributesUpdated: "Технические данные сохранены. Инвентарный идентификатор и ответственность не менялись.",
    assetAssigned: "Assign выполнен. Актив выдан выбранному пользователю.",
    assetTransferred: "Transfer выполнен. Изменение ответственности зафиксировано.",
    assetReturned: "Return выполнен. Текущая пользовательская ответственность завершена.",
    setupSaved: "Запись настройки сохранена.",
    roleAssigned: "Роль назначена пользователю. Изменение сохранено.",
    actionFailed: "Не удалось выполнить действие. Проверьте данные и повторите попытку."
};

const FEEDBACK_SCOPES = {
    WORKSPACE: "workspace",
    ASSET: "asset",
    SETUP: "setup"
};

const ADMIN_RESOURCES = {
    roles: {
        key: "roles",
        label: "Роли",
        path: "/api/v1/roles",
        emptyText: "Роли ещё не созданы.",
        columns: [
            { label: "Название", render: (item) => escapeHtml(item.name) }
        ],
        fields: [
            { name: "name", label: "Название роли", type: "text", required: true }
        ]
    },
    users: {
        key: "users",
        label: "Пользователи",
        path: "/api/v1/users",
        emptyText: "Пользователи ещё не созданы.",
        columns: [
            { label: "Имя", render: (item) => escapeHtml(item.name) },
            { label: "Подразделение", render: (item) => escapeHtml(resolveDepartmentName(item.department_id)) }
        ],
        fields: [
            { name: "name", label: "Имя пользователя", type: "text", required: true },
            { name: "department_id", label: "Подразделение", type: "select", required: false }
        ]
    },
    departments: {
        key: "departments",
        label: "Подразделения",
        path: "/api/v1/departments",
        emptyText: "Подразделения ещё не созданы.",
        columns: [
            { label: "Название", render: (item) => escapeHtml(item.name) }
        ],
        fields: [
            { name: "name", label: "Название подразделения", type: "text", required: true }
        ]
    },
    branches: {
        key: "branches",
        label: "Филиалы",
        path: "/api/v1/branches",
        emptyText: "Филиалы ещё не созданы.",
        columns: [
            { label: "Название", render: (item) => escapeHtml(item.name) }
        ],
        fields: [
            { name: "name", label: "Название филиала", type: "text", required: true }
        ]
    },
    locations: {
        key: "locations",
        label: "Локации",
        path: "/api/v1/locations",
        emptyText: "Локации ещё не созданы.",
        columns: [
            { label: "Название", render: (item) => escapeHtml(item.name) },
            { label: "Филиал", render: (item) => escapeHtml(resolveBranchName(item.branch_id)) }
        ],
        fields: [
            { name: "name", label: "Название локации", type: "text", required: true },
            { name: "branch_id", label: "Филиал", type: "select", required: true }
        ]
    }
};

const ui = {};

function createEmptyCapabilities() {
    return {
        tenantAdmin: false,
        assetRead: false,
        assetMutate: false,
        referenceRead: false
    };
}

function createEmptyLoadingState() {
    return {
        profile: false,
        assets: false,
        assetDetail: false,
        references: false,
        roles: false
    };
}

function createEmptyBusyState() {
    return {
        session: false,
        refreshAll: false,
        createAsset: false,
        updateAsset: false,
        technicalAttributes: false,
        assignAsset: false,
        transferAsset: false,
        returnAsset: false,
        refreshAsset: false,
        technicalSearch: false,
        employeeSheetTechnicalHints: false,
        adminResource: false,
        roleBinding: false,
        refreshAdmin: false
    };
}

const state = {
    context: {
        tenantId: "",
        userId: "",
        actor: "pilot-ui",
        userServiceBaseUrl: "http://localhost:8080"
    },
    profileLabel: "",
    capabilities: createEmptyCapabilities(),
    assets: [],
    selectedAssetId: null,
    selectedAsset: null,
    assetHistory: [],
    assetAudit: [],
    references: {
        roles: [],
        users: [],
        departments: [],
        branches: [],
        locations: []
    },
    loading: createEmptyLoadingState(),
    busy: createEmptyBusyState(),
    activeScreen: PRIMARY_SCREENS.WORKSPACE,
    activeAssetSection: "overview",
    activeReviewSection: "history",
    activeAdminResource: "roles",
    selectedAdminItemId: null,
    technicalAttributesEditing: false,
    selectedTreeNode: { type: TREE_NODE_TYPES.WORKSPACE },
    expandedTreeNodes: new Set(),
    treeFilter: "",
    quickFindQuery: "",
    employeeSheetSearchQuery: "",
    employeeSheetSelectedUserId: "",
    technicalSearchQuery: "",
    technicalSearchResults: [],
    technicalSearchError: "",
    technicalSearchHasSearched: false,
    technicalSearchLastQuery: "",
    employeeSheetTechnicalHintsUserId: "",
    employeeSheetTechnicalHintsScope: "",
    employeeSheetTechnicalHintsByAssetId: {},
    employeeSheetTechnicalHintsError: "",
    employeeSheetTechnicalHintsRequestId: 0,
    createLauncherOpen: false,
    lastRoleAssignment: null,
    lastFeedback: null
};

document.addEventListener("DOMContentLoaded", initialize);

function initialize() {
    cacheElements();
    ensureQuickFindUi();
    ensureTechnicalSearchUi();
    bindStaticEvents();
    restoreContext();
    render();

    if (hasConnectionContext()) {
        connectContour({ silent: true });
    }
}

function cacheElements() {
    ui.sessionForm = document.getElementById("session-form");
    ui.tenantId = document.getElementById("tenant-id");
    ui.userId = document.getElementById("user-id");
    ui.actor = document.getElementById("actor");
    ui.userServiceBaseUrl = document.getElementById("user-service-base-url");
    ui.userServiceUrlPreview = document.getElementById("user-service-url-preview");
    ui.assetServiceOrigin = document.getElementById("asset-service-origin");
    ui.contextTechnicalSummary = document.getElementById("context-technical-summary");
    ui.connectionStatusBadge = document.getElementById("connection-status-badge");
    ui.roleSummaryBadge = document.getElementById("role-summary-badge");
    ui.activeScreenLabel = document.getElementById("active-screen-label");
    ui.flashStack = document.getElementById("flash-stack");
    ui.createLauncher = document.getElementById("create-launcher");
    ui.createLauncherToggle = document.getElementById("create-launcher-toggle");
    ui.createLauncherMenu = document.getElementById("create-launcher-menu");
    ui.workspaceTree = document.getElementById("workspace-tree");
    ui.treeFilter = document.getElementById("tree-filter");
    ui.treeFilterStatus = document.getElementById("tree-filter-status");
    ui.quickFindInput = document.getElementById("quick-find-input");
    ui.quickFindPanel = document.getElementById("quick-find-panel");
    ui.technicalSearchForm = document.getElementById("technical-search-form");
    ui.technicalSearchInput = document.getElementById("technical-search-input");
    ui.technicalSearchPanel = document.getElementById("technical-search-panel");
    ui.technicalSearchClear = document.getElementById("technical-search-clear");
    ui.workspaceTreeBody = document.getElementById("workspace-tree-body");
    ui.workspaceDetail = document.getElementById("workspace-detail");
    ui.screenNav = document.getElementById("screen-nav");
    ui.workspaceScreen = document.getElementById("workspace-screen");
    ui.workspaceOverview = document.getElementById("workspace-overview");
    ui.assetsScreen = document.getElementById("assets-screen");
    ui.assetJourneyBanner = document.getElementById("asset-journey-banner");
    ui.assetCreatePanel = document.getElementById("asset-create-panel");
    ui.assetCreateForm = document.getElementById("asset-create-form");
    ui.assetRegistryOverview = document.getElementById("asset-registry-overview");
    ui.assetRegistryBody = document.getElementById("asset-registry-body");
    ui.assetDetailPanel = document.getElementById("asset-detail-panel");
    ui.reviewScreen = document.getElementById("review-screen");
    ui.reviewSectionNav = document.getElementById("review-section-nav");
    ui.reviewDetailPanel = document.getElementById("review-detail-panel");
    ui.adminScreen = document.getElementById("admin-screen");
    ui.setupOverview = document.getElementById("setup-overview");
    ui.adminResourceNav = document.getElementById("admin-resource-nav");
    ui.adminResourceTitle = document.getElementById("admin-resource-title");
    ui.adminTableHead = document.getElementById("admin-table-head");
    ui.adminTableBody = document.getElementById("admin-table-body");
    ui.adminEditorPanel = document.getElementById("admin-editor-panel");
    ui.connectSession = document.getElementById("connect-session");
    ui.refreshAll = document.getElementById("refresh-all");
    ui.clearSession = document.getElementById("clear-session");
    ui.refreshAssets = document.getElementById("refresh-assets");
    ui.refreshAdminResource = document.getElementById("refresh-admin-resource");
}

function ensureQuickFindUi() {
    if (!ui.workspaceTree || document.getElementById("quick-find-input")) {
        ui.quickFindInput = document.getElementById("quick-find-input");
        ui.quickFindPanel = document.getElementById("quick-find-panel");
        return;
    }

    const anchor = ui.treeFilter?.closest(".tree-filter") || ui.workspaceTreeBody;
    const markup = `
        <section class="quick-find" aria-label="Быстрый поиск">
            <div class="quick-find-heading">
                <p class="section-kicker">Поиск</p>
                <strong>Быстрый поиск</strong>
            </div>
            <label class="field quick-find-field">
                <span class="sr-only">Поиск</span>
                <input
                    id="quick-find-input"
                    class="quick-find-input"
                    type="text"
                    autocomplete="off"
                    placeholder="Поиск: сотрудник, инвентарный номер, название, серийный номер"
                    aria-describedby="quick-find-help"
                >
                <small id="quick-find-help" class="quick-find-help">По загруженным данным рабочего пространства. Без поиска на сервере.</small>
            </label>
            <div id="quick-find-panel" class="quick-find-panel" aria-live="polite"></div>
        </section>
    `;

    if (anchor) {
        anchor.insertAdjacentHTML("beforebegin", markup);
    } else {
        ui.workspaceTree.insertAdjacentHTML("beforeend", markup);
    }

    ui.quickFindInput = document.getElementById("quick-find-input");
    ui.quickFindPanel = document.getElementById("quick-find-panel");
}

function ensureTechnicalSearchUi() {
    if (!ui.workspaceTree || document.getElementById("technical-search-input")) {
        ui.technicalSearchForm = document.getElementById("technical-search-form");
        ui.technicalSearchInput = document.getElementById("technical-search-input");
        ui.technicalSearchPanel = document.getElementById("technical-search-panel");
        ui.technicalSearchClear = document.getElementById("technical-search-clear");
        return;
    }

    const quickFindSection = ui.quickFindInput?.closest(".quick-find");
    const anchor = quickFindSection || ui.treeFilter?.closest(".tree-filter") || ui.workspaceTreeBody;
    const position = quickFindSection ? "afterend" : "beforebegin";
    const markup = `
        <section class="technical-search" aria-label="Поиск по техническим данным">
            <div class="technical-search-heading">
                <p class="section-kicker">Технический поиск</p>
                <strong>Поиск по техническим данным</strong>
            </div>
            <p class="technical-search-help">Ищет по вручную заполненным техническим данным устройств.</p>
            <p class="technical-search-help">Это не глобальный поиск и не сканирование сети.</p>
            <p class="technical-search-help">Быстрый поиск остаётся поиском по уже загруженным данным рабочего пространства.</p>
            <p class="technical-search-help">Результаты открывают карточку актива без действий в строке результата.</p>
            <form id="technical-search-form" class="technical-search-form">
                <label class="field technical-search-field">
                    <span>Поиск по техническим данным</span>
                    <input
                        id="technical-search-input"
                        class="technical-search-input"
                        type="text"
                        autocomplete="off"
                        maxlength="${TECHNICAL_SEARCH_MAX_LENGTH}"
                        placeholder="Hostname, AnyDesk, IP, MAC, внутренний номер..."
                        aria-describedby="technical-search-help"
                    >
                    <small id="technical-search-help" class="technical-search-help">Введите технический признак устройства: hostname, AnyDesk, IP, MAC, внутренний номер или сетевое имя принтера.</small>
                </label>
                <div class="technical-search-actions">
                    <button class="button button-secondary" type="submit">Найти</button>
                    <button id="technical-search-clear" class="button button-ghost" type="button">Очистить</button>
                </div>
            </form>
            <div id="technical-search-panel" class="technical-search-panel" aria-live="polite"></div>
        </section>
    `;

    if (anchor) {
        anchor.insertAdjacentHTML(position, markup);
    } else {
        ui.workspaceTree.insertAdjacentHTML("beforeend", markup);
    }

    ui.technicalSearchForm = document.getElementById("technical-search-form");
    ui.technicalSearchInput = document.getElementById("technical-search-input");
    ui.technicalSearchPanel = document.getElementById("technical-search-panel");
    ui.technicalSearchClear = document.getElementById("technical-search-clear");
}

function bindStaticEvents() {
    ui.sessionForm.addEventListener("submit", onSessionSubmit);
    ui.userServiceBaseUrl.addEventListener("input", renderUserServiceUrlPreview);
    document.addEventListener("click", onCopyClick, true);
    document.addEventListener("click", onCreateLauncherOutsideClick);
    document.addEventListener("keydown", onCreateLauncherKeydown);
    ui.createLauncherToggle?.addEventListener("click", onCreateLauncherToggle);
    ui.createLauncherMenu?.addEventListener("click", onCreateLauncherMenuClick);
    ui.refreshAll.addEventListener("click", onRefreshAllClick);
    ui.clearSession.addEventListener("click", clearSession);
    ui.refreshAssets?.addEventListener("click", onRefreshAssetsClick);
    ui.refreshAdminResource?.addEventListener("click", onRefreshAdminResourceClick);
    ui.quickFindInput?.addEventListener("input", onQuickFindInput);
    ui.quickFindPanel?.addEventListener("click", onQuickFindPanelClick);
    ui.technicalSearchInput?.addEventListener("input", onTechnicalSearchInput);
    ui.technicalSearchForm?.addEventListener("submit", onTechnicalSearchSubmit);
    ui.technicalSearchClear?.addEventListener("click", onTechnicalSearchClear);
    ui.technicalSearchPanel?.addEventListener("click", onTechnicalSearchPanelClick);
    ui.treeFilter?.addEventListener("input", onTreeFilterInput);
    ui.workspaceTreeBody?.addEventListener("click", onTreeClick);
    ui.workspaceDetail?.addEventListener("click", onWorkspaceDetailClick);
    ui.workspaceDetail?.addEventListener("input", onWorkspaceDetailInput);
    ui.workspaceDetail?.addEventListener("submit", onWorkspaceDetailSubmit);
    ui.screenNav?.addEventListener("click", onScreenNavClick);
    ui.workspaceOverview?.addEventListener("click", onScreenNavClick);
    ui.assetCreateForm?.addEventListener("submit", onAssetCreateSubmit);
    ui.assetJourneyBanner?.addEventListener("click", onAssetInteractionClick);
    ui.assetRegistryOverview?.addEventListener("click", onAssetInteractionClick);
    ui.assetRegistryBody?.addEventListener("click", onAssetRegistryClick);
    ui.assetDetailPanel?.addEventListener("submit", onAssetDetailSubmit);
    ui.assetDetailPanel?.addEventListener("click", onAssetInteractionClick);
    ui.reviewSectionNav?.addEventListener("click", onReviewInteractionClick);
    ui.reviewDetailPanel?.addEventListener("click", onReviewInteractionClick);
    ui.adminResourceNav?.addEventListener("click", onAdminResourceClick);
    ui.setupOverview?.addEventListener("click", onAdminResourceClick);
    ui.adminTableBody?.addEventListener("click", onAdminTableClick);
    ui.adminEditorPanel?.addEventListener("submit", onAdminEditorSubmit);
    ui.adminEditorPanel?.addEventListener("click", onAdminEditorClick);
}

function onCreateLauncherToggle() {
    setCreateLauncherOpen(!state.createLauncherOpen);
}

function onCreateLauncherMenuClick(event) {
    const item = event.target.closest("[data-create-target]");
    if (!item) {
        return;
    }

    setCreateLauncherOpen(false);
    openCreateTarget(item.dataset.createTarget);
}

function onCreateLauncherOutsideClick(event) {
    if (!state.createLauncherOpen || ui.createLauncher?.contains(event.target)) {
        return;
    }
    setCreateLauncherOpen(false);
}

function onCreateLauncherKeydown(event) {
    if (event.key !== "Escape" || !state.createLauncherOpen) {
        return;
    }
    setCreateLauncherOpen(false);
    ui.createLauncherToggle?.focus();
}

function setCreateLauncherOpen(open) {
    state.createLauncherOpen = Boolean(open);
    renderCreateLauncherState();
}

function renderCreateLauncherState() {
    if (!ui.createLauncherToggle || !ui.createLauncherMenu) {
        return;
    }

    ui.createLauncherToggle.setAttribute("aria-expanded", String(state.createLauncherOpen));
    ui.createLauncherMenu.hidden = !state.createLauncherOpen;
}

function restoreContext() {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) {
        syncContextToForm();
        return;
    }

    try {
        const parsed = JSON.parse(raw);
        state.context = {
            tenantId: parsed.tenantId ?? "",
            userId: parsed.userId ?? "",
            actor: parsed.actor ?? "pilot-ui",
            userServiceBaseUrl: parsed.userServiceBaseUrl ?? "http://localhost:8080"
        };
    } catch (error) {
        window.localStorage.removeItem(STORAGE_KEY);
    }

    syncContextToForm();
}

function syncContextToForm() {
    ui.tenantId.value = state.context.tenantId;
    ui.userId.value = state.context.userId;
    ui.actor.value = state.context.actor;
    ui.userServiceBaseUrl.value = state.context.userServiceBaseUrl;
    ui.assetServiceOrigin.textContent = window.location.origin;
    renderUserServiceUrlPreview();
}

function syncContextFromForm() {
    state.context = {
        tenantId: ui.tenantId.value.trim(),
        userId: ui.userId.value.trim(),
        actor: ui.actor.value.trim(),
        userServiceBaseUrl: ui.userServiceBaseUrl.value.trim().replace(/\/+$/, "")
    };
}

function persistContext() {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state.context));
}

function clearSession() {
    state.context = {
        tenantId: "",
        userId: "",
        actor: "pilot-ui",
        userServiceBaseUrl: "http://localhost:8080"
    };
    state.profileLabel = "";
    state.capabilities = createEmptyCapabilities();
    state.assets = [];
    state.selectedAssetId = null;
    state.selectedAsset = null;
    state.assetHistory = [];
    state.assetAudit = [];
    state.references = {
        roles: [],
        users: [],
        departments: [],
        branches: [],
        locations: []
    };
    state.loading = createEmptyLoadingState();
    state.busy = createEmptyBusyState();
    state.activeScreen = PRIMARY_SCREENS.WORKSPACE;
    state.activeAssetSection = "overview";
    state.activeReviewSection = "history";
    state.activeAdminResource = "roles";
    state.selectedAdminItemId = null;
    state.selectedTreeNode = { type: TREE_NODE_TYPES.WORKSPACE };
    state.expandedTreeNodes = new Set();
    state.treeFilter = "";
    state.quickFindQuery = "";
    state.employeeSheetSearchQuery = "";
    state.employeeSheetSelectedUserId = "";
    resetEmployeeSheetTechnicalHints();
    state.lastRoleAssignment = null;
    state.lastFeedback = null;

    window.localStorage.removeItem(STORAGE_KEY);
    syncContextToForm();
    render();
    showFlash("Контекст доступа очищен.", "info");
}

function hasConnectionContext() {
    return Boolean(state.context.tenantId && state.context.userId && state.context.actor && state.context.userServiceBaseUrl);
}

function hasResolvedProfile() {
    return Boolean(state.profileLabel);
}

function validateContext() {
    if (!state.context.tenantId || !state.context.userId || !state.context.actor || !state.context.userServiceBaseUrl) {
        showFlash("Заполните tenant, user, actor и user-service URL, чтобы подключить рабочий контур.", "warning");
        return false;
    }
    try {
        new URL(state.context.userServiceBaseUrl);
    } catch (error) {
        showFlash("Укажите полный адрес сервиса пользователей, например http://localhost:8080.", "warning");
        return false;
    }
    return true;
}

async function onSessionSubmit(event) {
    event.preventDefault();
    if (isBusy("session")) {
        return;
    }
    await withBusy("session", () => connectContour());
}

async function onRefreshAllClick() {
    if (isBusy("refreshAll")) {
        return;
    }
    await withBusy("refreshAll", () => refreshAllData());
}

async function onRefreshAssetsClick() {
    if (isBusy("refreshAsset")) {
        return;
    }
    await withBusy("refreshAsset", () => refreshAllData()).catch(handleUnexpectedError);
}

async function onRefreshAdminResourceClick() {
    if (isBusy("refreshAdmin")) {
        return;
    }
    await withBusy("refreshAdmin", () => refreshAdminResource()).catch(handleUnexpectedError);
}

async function connectContour(options = {}) {
    syncContextFromForm();
    if (!validateContext()) {
        return;
    }

    persistContext();

    try {
        await resolveUiProfile();
        await refreshAllData({ silent: true });
        if (!options.silent) {
            showFlash("Контур подключён. UI использует только уже существующие сервисные контракты.", "success");
        }
    } catch (error) {
        showFlash(error.message, "error");
    }
}

async function refreshAllData(options = {}) {
    if (!hasConnectionContext()) {
        showFlash("Сначала подключите контекст доступа.", "warning");
        return;
    }

    try {
        if (!hasResolvedProfile()) {
            await resolveUiProfile();
        }

        if (canAssetRead()) {
            await runTask(refreshAssets, "Не удалось загрузить реестр активов.");
        }

        if (canReferenceRead()) {
            await runTask(loadReferenceData, "Не удалось загрузить справочники из сервиса пользователей.");
        }

        if (canTenantAdmin()) {
            await runTask(loadRolesCatalog, "Не удалось загрузить роли для настройки.");
        } else {
            state.selectedAdminItemId = null;
        }

        syncStandaloneEmployeeSheetSelection();
        render();
        if (state.selectedTreeNode?.type === TREE_NODE_TYPES.USER) {
            resetEmployeeSheetTechnicalHints(state.selectedTreeNode.id, "treeUser");
            loadEmployeeSheetTechnicalHintsForUser(state.selectedTreeNode.id, { scope: "treeUser" });
        } else if (state.selectedTreeNode?.type === TREE_NODE_TYPES.EMPLOYEE_SHEET && state.employeeSheetSelectedUserId) {
            resetEmployeeSheetTechnicalHints(state.employeeSheetSelectedUserId, "standalone");
            loadEmployeeSheetTechnicalHintsForUser(state.employeeSheetSelectedUserId, { scope: "standalone" });
        }
        if (!options.silent) {
            showFlash("Данные рабочего контура обновлены.", "success");
        }
    } catch (error) {
        showFlash(error.message, "error");
    }
}

async function refreshAssets() {
    await withLoading("assets", async () => {
        state.assets = await requestJson("asset", "/api/v1/assets");

        if (state.assets.length === 0) {
            state.selectedAssetId = null;
            state.selectedAsset = null;
            state.assetHistory = [];
            state.assetAudit = [];
            return;
        }

        const nextAssetId = state.selectedAssetId && state.assets.some((item) => item.id === state.selectedAssetId)
            ? state.selectedAssetId
            : state.assets[0].id;
        await loadAssetBundle(nextAssetId);
    });
}

async function loadAssetBundle(assetId) {
    await withLoading("assetDetail", async () => {
        state.selectedAssetId = assetId;
        const [asset, history, audit] = await Promise.all([
            requestJson("asset", `/api/v1/assets/${assetId}`),
            requestJson("asset", `/api/v1/assets/${assetId}/history`),
            requestJson("asset", `/api/v1/assets/${assetId}/audit`)
        ]);

        state.selectedAsset = asset;
        state.technicalAttributesEditing = false;
        state.assetHistory = history.items ?? [];
        state.assetAudit = audit.items ?? [];
    });
}

async function loadReferenceData() {
    await withLoading("references", async () => {
        const [users, departments, branches, locations] = await Promise.all([
            requestJson("user", "/api/v1/users"),
            requestJson("user", "/api/v1/departments"),
            requestJson("user", "/api/v1/branches"),
            requestJson("user", "/api/v1/locations")
        ]);

        state.references.users = users;
        state.references.departments = departments;
        state.references.branches = branches;
        state.references.locations = locations;
    });
}

async function loadRolesCatalog() {
    await withLoading("roles", async () => {
        state.references.roles = await requestJson("user", "/api/v1/roles");
    });
}

async function resolveUiProfile() {
    await withLoading("profile", async () => {
        const [tenantAdmin, assetMutate, assetRead, referenceRead] = await Promise.all([
            probeMutationAccess(
                "user",
                `/api/v1/roles/${createProbeId()}`,
                buildRoleProbePayload(),
                "Не удалось определить доступ к настройке через уже доступный сервис пользователей."
            ),
            probeMutationAccess(
                "asset",
                `/api/v1/assets/${createProbeId()}`,
                buildAssetProbePayload(),
                "Не удалось определить mutate-доступ к asset-service через публичный контракт."
            ),
            probeReadAccess(
                "asset",
                "/api/v1/assets",
                "Не удалось определить доступ к просмотру реестра активов."
            ),
            probeReadAccess(
                "user",
                "/api/v1/users",
                "Не удалось определить доступ к просмотру справочников."
            )
        ]);

        state.capabilities = {
            tenantAdmin,
            assetMutate: tenantAdmin || assetMutate,
            assetRead: assetRead || tenantAdmin || assetMutate,
            referenceRead
        };

        state.profileLabel = tenantAdmin
            ? UI_PROFILE_NAMES.TENANT_ADMIN
            : state.capabilities.assetMutate
                ? UI_PROFILE_NAMES.ASSET_OPERATOR
                : state.capabilities.assetRead
                    ? UI_PROFILE_NAMES.SUPPORT_REVIEWER
                    : UI_PROFILE_NAMES.NO_APPROVED_ROLE;

        if (!canAssetRead()) {
            state.activeScreen = canTenantAdmin() ? PRIMARY_SCREENS.SETUP : PRIMARY_SCREENS.WORKSPACE;
        }
    });
}

async function runTask(task, errorPrefix) {
    try {
        await task();
    } catch (error) {
        throw new Error(`${errorPrefix} ${error.message}`);
    }
}

async function withLoading(key, task) {
    state.loading[key] = true;
    render();
    try {
        return await task();
    } finally {
        state.loading[key] = false;
        render();
    }
}

async function withBusy(key, task) {
    state.busy[key] = true;
    render();
    try {
        return await task();
    } finally {
        state.busy[key] = false;
        render();
    }
}

function isBusy(key) {
    return Boolean(state.busy[key]);
}

async function probeReadAccess(service, path, errorPrefix) {
    const { response, payload } = await requestDetailed(service, path);
    if (response.ok) {
        return true;
    }
    if (response.status === 403) {
        return false;
    }
    throw new Error(`${errorPrefix} ${extractErrorMessage(response, payload)}`);
}

async function probeMutationAccess(service, path, body, errorPrefix) {
    const { response, payload } = await requestDetailed(service, path, {
        method: "PATCH",
        body
    });
    if (response.status === 404) {
        return true;
    }
    if (response.status === 403) {
        return false;
    }
    throw new Error(`${errorPrefix} ${extractErrorMessage(response, payload)}`);
}

function createProbeId() {
    if (window.crypto && typeof window.crypto.randomUUID === "function") {
        return window.crypto.randomUUID();
    }

    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (character) => {
        const random = Math.floor(Math.random() * 16);
        const value = character === "x" ? random : (random & 0x3) | 0x8;
        return value.toString(16);
    });
}

function buildAssetProbePayload() {
    return {
        asset_tag: `pilot-ui-probe-${Date.now()}`,
        serial_number: `probe-${Math.random().toString(16).slice(2, 10)}`,
        asset_type: "LAPTOP"
    };
}

function buildRoleProbePayload() {
    return {
        name: `pilot-ui-probe-${Date.now()}`
    };
}

async function requestDetailed(service, path, options = {}) {
    const url = buildUrl(service, path);
    const headers = new Headers(options.headers || {});
    headers.set("Accept", "application/json");
    headers.set("X-Authenticated-Tenant-Id", state.context.tenantId);
    headers.set("X-Authenticated-User-Id", state.context.userId);
    headers.set("X-Authenticated-Actor", state.context.actor);
    headers.set("X-Correlation-Id", `pilot-ui-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`);

    const init = {
        method: options.method || "GET",
        headers
    };

    if (options.body !== undefined) {
        headers.set("Content-Type", "application/json");
        init.body = JSON.stringify(options.body);
    }

    const response = await fetch(url, init);
    const payload = await readResponsePayload(response);
    return { response, payload };
}

async function requestJson(service, path, options = {}) {
    const { response, payload } = await requestDetailed(service, path, options);
    if (!response.ok) {
        throw new Error(extractErrorMessage(response, payload));
    }
    return payload;
}

function buildUrl(service, path) {
    const baseUrl = service === "asset" ? window.location.origin : state.context.userServiceBaseUrl;
    return new URL(path, baseUrl).toString();
}

async function readResponsePayload(response) {
    if (response.status === 204) {
        return {};
    }

    const contentType = response.headers.get("Content-Type") || "";
    if (contentType.includes("application/json")) {
        return response.json();
    }

    const text = await response.text();
    return text ? { raw: text } : {};
}

function extractErrorMessage(response, payload) {
    if (payload && typeof payload === "object") {
        if (payload.message) {
            return payload.message;
        }
        if (payload.error) {
            return `${payload.error} (${response.status})`;
        }
        if (payload.raw) {
            return `${response.status} ${response.statusText}: ${payload.raw}`;
        }
    }
    return `${response.status} ${response.statusText}`;
}

function resetEmployeeSheetTechnicalHints(userId = "", scope = "") {
    state.employeeSheetTechnicalHintsUserId = String(userId || "");
    state.employeeSheetTechnicalHintsScope = scope;
    state.employeeSheetTechnicalHintsByAssetId = {};
    state.employeeSheetTechnicalHintsError = "";
    state.employeeSheetTechnicalHintsRequestId += 1;
    state.busy.employeeSheetTechnicalHints = false;
}

function isCurrentEmployeeSheetHintsRequest(userId, requestId, scope) {
    if (state.employeeSheetTechnicalHintsRequestId !== requestId || state.employeeSheetTechnicalHintsScope !== scope) {
        return false;
    }

    if (scope === "standalone") {
        return state.selectedTreeNode?.type === TREE_NODE_TYPES.EMPLOYEE_SHEET
            && String(state.employeeSheetSelectedUserId || "") === String(userId || "");
    }

    return state.selectedTreeNode?.type === TREE_NODE_TYPES.USER
        && String(state.selectedTreeNode.id || "") === String(userId || "");
}

function selectUserNode(userId, options = {}) {
    const selectedUserId = String(userId || "");
    state.selectedTreeNode = { type: TREE_NODE_TYPES.USER, id: selectedUserId };
    resetEmployeeSheetTechnicalHints(selectedUserId, "treeUser");
    render();

    if (options.scroll !== false) {
        scrollToElement(ui.workspaceDetail);
    }

    loadEmployeeSheetTechnicalHintsForUser(selectedUserId, { scope: "treeUser" });
}

function selectEmployeeSheetNode(options = {}) {
    state.selectedTreeNode = { type: TREE_NODE_TYPES.EMPLOYEE_SHEET };
    syncStandaloneEmployeeSheetSelection();

    if (state.employeeSheetSelectedUserId) {
        resetEmployeeSheetTechnicalHints(state.employeeSheetSelectedUserId, "standalone");
    } else {
        resetEmployeeSheetTechnicalHints();
    }

    render();

    if (options.scroll !== false) {
        scrollToElement(ui.workspaceDetail);
    }

    if (state.employeeSheetSelectedUserId) {
        loadEmployeeSheetTechnicalHintsForUser(state.employeeSheetSelectedUserId, { scope: "standalone" });
    }
}

function selectStandaloneEmployeeSheetUser(userId) {
    const selectedUserId = String(userId || "");
    if (!selectedUserId) {
        return;
    }

    state.employeeSheetSelectedUserId = selectedUserId;
    resetEmployeeSheetTechnicalHints(selectedUserId, "standalone");
    render();
    loadEmployeeSheetTechnicalHintsForUser(selectedUserId, { scope: "standalone" });
}

function syncStandaloneEmployeeSheetSelection() {
    const selectedUserId = String(state.employeeSheetSelectedUserId || "");
    if (!selectedUserId) {
        return;
    }

    const exists = state.references.users.some((user) => String(user.id || "") === selectedUserId);
    if (!exists) {
        state.employeeSheetSelectedUserId = "";
        resetEmployeeSheetTechnicalHints();
    }
}

async function loadEmployeeSheetTechnicalHintsForUser(userId, options = {}) {
    const selectedUserId = String(userId || "");
    const scope = options.scope || "treeUser";
    const selectedUserExists = state.references.users.some((user) => String(user.id || "") === selectedUserId);
    if (!selectedUserId || !selectedUserExists || !hasConnectionContext() || !canAssetRead()) {
        return;
    }

    const requestId = state.employeeSheetTechnicalHintsRequestId + 1;
    state.employeeSheetTechnicalHintsRequestId = requestId;
    state.employeeSheetTechnicalHintsUserId = selectedUserId;
    state.employeeSheetTechnicalHintsScope = scope;
    state.employeeSheetTechnicalHintsError = "";
    state.busy.employeeSheetTechnicalHints = true;
    render();

    try {
        const params = new URLSearchParams({ user_id: selectedUserId });
        const response = await requestJson("asset", `/api/v1/assets/employee-sheet-technical-hints?${params.toString()}`);
        if (!isCurrentEmployeeSheetHintsRequest(selectedUserId, requestId, scope)) {
            return;
        }

        state.employeeSheetTechnicalHintsByAssetId = buildEmployeeSheetTechnicalHintsByAssetId(response);
        state.employeeSheetTechnicalHintsError = "";
    } catch (error) {
        if (!isCurrentEmployeeSheetHintsRequest(selectedUserId, requestId, scope)) {
            return;
        }

        state.employeeSheetTechnicalHintsByAssetId = {};
        state.employeeSheetTechnicalHintsError = "Технические подсказки временно недоступны.";
    } finally {
        if (isCurrentEmployeeSheetHintsRequest(selectedUserId, requestId, scope)) {
            state.busy.employeeSheetTechnicalHints = false;
            render();
        }
    }
}

function buildEmployeeSheetTechnicalHintsByAssetId(response) {
    const rows = Array.isArray(response?.assets) ? response.assets : [];
    return rows.reduce((accumulator, row) => {
        const assetId = row?.asset_id || row?.assetId || row?.id;
        if (!assetId) {
            return accumulator;
        }

        const hints = normalizeEmployeeSheetTechnicalHints(row?.technical_hints || row?.technicalHints);
        if (Object.keys(hints).length > 0) {
            accumulator[String(assetId)] = hints;
        }
        return accumulator;
    }, {});
}

function normalizeEmployeeSheetTechnicalHints(rawHints) {
    if (!rawHints || typeof rawHints !== "object") {
        return {};
    }

    return EMPLOYEE_SHEET_TECHNICAL_HINT_FIELDS.reduce((accumulator, field) => {
        const rawValue = rawHints[field.key];
        const value = rawValue == null ? "" : String(rawValue).trim();
        if (value) {
            accumulator[field.key] = value;
        }
        return accumulator;
    }, {});
}

function buildAssetUpsertPayload(formData) {
    const payload = {
        asset_tag: formDataValue(formData, "asset_tag"),
        serial_number: formDataValue(formData, "serial_number"),
        asset_type: formDataValue(formData, "asset_type"),
        display_name: formDataValue(formData, "display_name") || null,
        note: formDataValue(formData, "note") || null
    };
    validateRequiredFields(payload, {
        asset_tag: "Инвентарный тег",
        serial_number: "Серийный номер",
        asset_type: "Тип актива"
    });
    return payload;
}

function buildTechnicalAttributesPayload(formData, profile) {
    const fields = getTechnicalAttributeFields(profile);
    if (fields.length === 0) {
        throw new Error("Для этого типа актива технические атрибуты v1 не настроены.");
    }

    const values = {};
    fields.forEach((field) => {
        const value = formDataValue(formData, field.key);
        if (value.length > TECHNICAL_ATTRIBUTE_MAX_LENGTH) {
            throw new Error(`Поле "${field.label}" длиннее ${TECHNICAL_ATTRIBUTE_MAX_LENGTH} символов.`);
        }
        if (value) {
            values[field.key] = value;
        }
    });

    return { values };
}

function buildAdminPayload(config, formData) {
    const payload = {};
    for (const field of config.fields) {
        const rawValue = formDataValue(formData, field.name);
        if (field.required && !rawValue) {
            throw new Error(`Поле "${field.label}" обязательно.`);
        }
        payload[field.name] = rawValue || null;
    }
    return payload;
}

function formDataValue(formData, key) {
    return String(formData.get(key) ?? "").trim();
}

function optionalAssetText(value) {
    return String(value ?? "").trim();
}

function assetDisplayTitle(asset) {
    const displayName = optionalAssetText(asset?.display_name);
    if (displayName) {
        return displayName;
    }

    const assetTag = optionalAssetText(asset?.asset_tag);
    if (assetTag) {
        return assetTag;
    }

    const typeSerial = [
        optionalAssetText(asset?.asset_type),
        optionalAssetText(asset?.serial_number)
    ].filter(Boolean).join(" / ");
    if (typeSerial) {
        return typeSerial;
    }

    return shortenTechnicalValue(asset?.id || "");
}

function assetCanonicalTag(asset) {
    return optionalAssetText(asset?.asset_tag);
}

function getAssetTechnicalAttributes(asset) {
    const source = asset?.technical_attributes && typeof asset.technical_attributes === "object"
        ? asset.technical_attributes
        : {};
    const profile = TECHNICAL_ATTRIBUTE_PROFILE_GROUPS[source.profile]
        ? source.profile
        : TECHNICAL_ATTRIBUTE_UNSUPPORTED_PROFILE;
    const values = source.values && typeof source.values === "object" && !Array.isArray(source.values)
        ? source.values
        : {};
    return { profile, values };
}

function getTechnicalAttributeGroups(profile) {
    return TECHNICAL_ATTRIBUTE_PROFILE_GROUPS[profile] || [];
}

function getTechnicalAttributeFields(profile) {
    return getTechnicalAttributeGroups(profile).flatMap((group) => group.fields);
}

function hasStoredTechnicalAttributeValues(values, profile) {
    return getTechnicalAttributeFields(profile)
        .some((field) => Boolean(optionalAssetText(values?.[field.key])));
}

function mergeSelectedAssetTechnicalAttributes(response) {
    if (!state.selectedAsset) {
        return;
    }

    const current = getAssetTechnicalAttributes(state.selectedAsset);
    const nested = response?.technical_attributes && typeof response.technical_attributes === "object"
        ? response.technical_attributes
        : {};
    const profile = TECHNICAL_ATTRIBUTE_PROFILE_GROUPS[response?.profile]
        ? response.profile
        : TECHNICAL_ATTRIBUTE_PROFILE_GROUPS[nested.profile]
            ? nested.profile
            : current.profile;
    const responseValues = response?.values && typeof response.values === "object" && !Array.isArray(response.values)
        ? response.values
        : nested.values && typeof nested.values === "object" && !Array.isArray(nested.values)
            ? nested.values
            : {};

    state.selectedAsset = {
        ...state.selectedAsset,
        technical_attributes: {
            profile,
            values: responseValues
        }
    };
}

function validateRequiredFields(payload, labels) {
    const missing = Object.entries(labels)
        .filter(([key]) => !String(payload[key] ?? "").trim())
        .map(([, label]) => label);

    if (missing.length > 0) {
        throw new Error(`Заполните обязательные поля: ${missing.join(", ")}.`);
    }
}

function onScreenNavClick(event) {
    const button = event.target.closest("[data-screen]");
    if (!button) {
        return;
    }
    activateScreen(button.dataset.screen);
}

function activateScreen(screen, options = {}) {
    state.activeScreen = screen;
    render();

    if (options.scroll === false) {
        return;
    }

    scrollToElement(getScreenAnchorElement(screen));
}

async function onAssetCreateSubmit(event) {
    event.preventDefault();
    if (!canAssetMutate()) {
        showFlash("Текущая роль не имеет mutate-доступа к активам.", "warning");
        return;
    }
    if (isBusy("createAsset")) {
        return;
    }

    try {
        const form = event.target.closest("form") || event.currentTarget;
        const payload = buildAssetUpsertPayload(new FormData(form));
        await withBusy("createAsset", async () => {
            const created = await requestJson("asset", "/api/v1/assets", {
                method: "POST",
                body: payload
            });
            form.reset();
            state.selectedAssetId = created.id;
            state.activeAssetSection = "overview";
            state.selectedTreeNode = { type: TREE_NODE_TYPES.ASSET, id: created.id, source: "all", group: ASSET_GROUPS.UNASSIGNED };
            await refreshAssets();
            scrollToElement(ui.workspaceDetail || ui.assetDetailPanel);
            showActionSuccess("assetCreated", { scope: FEEDBACK_SCOPES.ASSET, assetId: created.id });
        });
    } catch (error) {
        showActionError(error, { scope: FEEDBACK_SCOPES.WORKSPACE });
    }
}

function onAssetRegistryClick(event) {
    const row = event.target.closest("[data-asset-id]");
    if (!row) {
        return;
    }
    loadAssetBundle(row.dataset.assetId)
        .then(() => scrollToElement(ui.assetDetailPanel))
        .catch(handleUnexpectedError);
}

async function onAssetDetailSubmit(event) {
    event.preventDefault();
    const form = event.target;

    if (!state.selectedAssetId) {
        showFlash("Сначала выберите актив.", "warning");
        return;
    }

    if (!canAssetMutate()) {
        showFlash("Текущая роль доступна только для просмотра.", "warning");
        return;
    }

    try {
        if (form.id === "asset-update-form") {
            if (isBusy("updateAsset")) {
                return;
            }
            const payload = buildAssetUpsertPayload(new FormData(form));
            await withBusy("updateAsset", async () => {
                await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}`, {
                    method: "PATCH",
                    body: payload
                });
                state.activeAssetSection = "overview";
                await refreshAssets();
                scrollToElement(ui.workspaceDetail || ui.assetDetailPanel);
                showActionSuccess("assetUpdated", { scope: FEEDBACK_SCOPES.ASSET, assetId: state.selectedAssetId });
            });
            return;
        }

        if (form.id === "asset-technical-attributes-form") {
            if (isBusy("technicalAttributes")) {
                return;
            }
            const technicalAttributes = getAssetTechnicalAttributes(state.selectedAsset);
            const payload = buildTechnicalAttributesPayload(new FormData(form), technicalAttributes.profile);
            await withBusy("technicalAttributes", async () => {
                const saved = await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}/technical-attributes`, {
                    method: "PUT",
                    body: payload
                });
                mergeSelectedAssetTechnicalAttributes(saved);
                state.activeAssetSection = "technical";
                state.technicalAttributesEditing = false;
            });
            scrollToElement(ui.workspaceDetail || ui.assetDetailPanel);
            showActionSuccess("technicalAttributesUpdated", { scope: FEEDBACK_SCOPES.ASSET, assetId: state.selectedAssetId });
            return;
        }

        if (form.id === "asset-assign-form") {
            if (isBusy("assignAsset")) {
                return;
            }
            const targetUserId = formDataValue(new FormData(form), "target_user_id");
            if (!targetUserId) {
                showFlash("Для assign выберите пользователя.", "warning");
                return;
            }
            await withBusy("assignAsset", async () => {
                await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}/assign`, {
                    method: "POST",
                    body: { target_user_id: targetUserId }
                });
                state.activeAssetSection = "history";
                state.activeReviewSection = "history";
                await refreshAssets();
                scrollToElement(ui.workspaceDetail || ui.assetDetailPanel);
                showActionSuccess("assetAssigned", { scope: FEEDBACK_SCOPES.ASSET, assetId: state.selectedAssetId });
            });
            return;
        }

        if (form.id === "asset-transfer-form") {
            if (isBusy("transferAsset")) {
                return;
            }
            const formData = new FormData(form);
            const targetUserId = formDataValue(formData, "target_user_id");
            const targetLocationId = formDataValue(formData, "target_location_id");
            if (!targetUserId && !targetLocationId) {
                showFlash("Для transfer выберите нового пользователя или новую локацию.", "warning");
                return;
            }
            if (!hasCurrentUserResponsibility(state.selectedAsset) && targetUserId) {
                showFlash("Transfer не создаёт первую пользовательскую ответственность. Для первого пользователя используйте assign.", "warning");
                return;
            }
            await withBusy("transferAsset", async () => {
                await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}/transfer`, {
                    method: "POST",
                    body: {
                        target_user_id: targetUserId || null,
                        target_location_id: targetLocationId || null
                    }
                });
                state.activeAssetSection = "history";
                state.activeReviewSection = "history";
                await refreshAssets();
                scrollToElement(ui.workspaceDetail || ui.assetDetailPanel);
                showActionSuccess("assetTransferred", { scope: FEEDBACK_SCOPES.ASSET, assetId: state.selectedAssetId });
            });
            return;
        }

        if (form.id === "asset-return-form") {
            if (isBusy("returnAsset")) {
                return;
            }
            if (!hasCurrentUserResponsibility(state.selectedAsset)) {
                showFlash("Return завершает только текущую пользовательскую ответственность. В карточке нет назначенного пользователя.", "warning");
                return;
            }
            await withBusy("returnAsset", async () => {
                await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}/return`, {
                    method: "POST"
                });
                state.activeAssetSection = "history";
                state.activeReviewSection = "history";
                await refreshAssets();
                scrollToElement(ui.workspaceDetail || ui.assetDetailPanel);
                showActionSuccess("assetReturned", { scope: FEEDBACK_SCOPES.ASSET, assetId: state.selectedAssetId });
            });
        }
    } catch (error) {
        showActionError(error, { scope: FEEDBACK_SCOPES.ASSET, assetId: state.selectedAssetId });
    }
}

function onAssetInteractionClick(event) {
    if (handleTechnicalAttributesClick(event)) {
        return;
    }

    const screenButton = event.target.closest("[data-screen]");
    if (screenButton) {
        activateScreen(screenButton.dataset.screen);
        return;
    }

    const sectionButton = event.target.closest("[data-asset-section]");
    if (sectionButton) {
        state.activeAssetSection = sectionButton.dataset.assetSection;
        render();
        scrollToElement(ui.assetDetailPanel);
        return;
    }

    const refreshButton = event.target.closest("[data-refresh-asset]");
    if (refreshButton && state.selectedAssetId) {
        if (isBusy("refreshAsset")) {
            return;
        }
        withBusy("refreshAsset", () => loadAssetBundle(state.selectedAssetId))
            .then(() => scrollToElement(ui.assetDetailPanel))
            .catch(handleUnexpectedError);
        return;
    }

    const createButton = event.target.closest("[data-focus-create]");
    const assetCreatePanel = document.getElementById("asset-create-panel");
    if (createButton && assetCreatePanel) {
        scrollToElement(assetCreatePanel);
        return;
    }

    const registryButton = event.target.closest("[data-focus-registry]");
    if (registryButton) {
        scrollToElement(ui.assetRegistryOverview);
        return;
    }

    const cardButton = event.target.closest("[data-focus-card]");
    if (cardButton) {
        scrollToElement(ui.assetDetailPanel);
    }
}

function handleTechnicalAttributesClick(event) {
    const editButton = event.target.closest("[data-technical-attributes-edit]");
    if (editButton) {
        state.technicalAttributesEditing = true;
        state.activeAssetSection = "technical";
        render();
        scrollToElement(ui.workspaceDetail || ui.assetDetailPanel);
        focusFirstFormControl("asset-technical-attributes-form");
        return true;
    }

    const cancelButton = event.target.closest("[data-technical-attributes-cancel]");
    if (cancelButton) {
        state.technicalAttributesEditing = false;
        state.activeAssetSection = "technical";
        render();
        scrollToElement(ui.workspaceDetail || ui.assetDetailPanel);
        return true;
    }

    return false;
}

function openCreateTarget(target) {
    const config = CREATE_TARGETS[target];
    if (!config) {
        showFlash("Цель создания не найдена.", "warning");
        return;
    }

    if (target === "asset") {
        openAssetCreateTarget();
        return;
    }

    openSetupCreateTarget(config.resource, config.label);
}

function openAssetCreateTarget() {
    state.selectedTreeNode = { type: TREE_NODE_TYPES.ASSET_GROUP, group: ASSET_GROUPS.UNASSIGNED };
    state.expandedTreeNodes.add(TREE_GROUPS.ALL_ASSETS);
    render();

    const createPanel = document.getElementById("asset-create-panel");
    scrollToElement(createPanel || ui.workspaceDetail);

    if (!canAssetMutate()) {
        showFlash("Создание актива доступно роли с mutate-доступом к активам.", "warning");
        return;
    }

    focusFirstFormControl("asset-create-form");
}

function openSetupCreateTarget(resourceKey, label) {
    const resource = ADMIN_RESOURCES[resourceKey];
    if (!resource) {
        showFlash("Раздел настройки не найден.", "warning");
        return;
    }

    state.activeAdminResource = resource.key;
    state.selectedAdminItemId = null;
    state.selectedTreeNode = { type: TREE_NODE_TYPES.SETUP, resource: resource.key };
    state.expandedTreeNodes.add(TREE_GROUPS.SETUP);
    render();

    const editorPanel = document.getElementById("admin-resource-form")?.closest(".panel");
    scrollToElement(editorPanel || ui.workspaceDetail);

    if (!canTenantAdmin()) {
        showFlash(`Создание записи «${label || resource.label}» доступно Tenant Admin.`, "warning");
        return;
    }

    focusFirstFormControl("admin-resource-form");
}

function focusFirstFormControl(formId) {
    const form = document.getElementById(formId);
    const control = form?.querySelector("input:not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled])");
    if (control && typeof control.focus === "function") {
        control.focus({ preventScroll: true });
    }
}

function focusEmployeeSheetSearchInput() {
    const input = document.querySelector("[data-employee-sheet-search-input]");
    if (input && typeof input.focus === "function") {
        input.focus({ preventScroll: true });
        const position = String(state.employeeSheetSearchQuery || "").length;
        if (typeof input.setSelectionRange === "function") {
            input.setSelectionRange(position, position);
        }
    }
}

function onReviewInteractionClick(event) {
    const screenButton = event.target.closest("[data-screen]");
    if (screenButton) {
        activateScreen(screenButton.dataset.screen);
        return;
    }

    const sectionButton = event.target.closest("[data-review-section]");
    if (sectionButton) {
        state.activeReviewSection = sectionButton.dataset.reviewSection;
        render();
        return;
    }

    const refreshButton = event.target.closest("[data-refresh-asset]");
    if (refreshButton && state.selectedAssetId) {
        if (isBusy("refreshAsset")) {
            return;
        }
        withBusy("refreshAsset", () => loadAssetBundle(state.selectedAssetId))
            .then(render)
            .catch(handleUnexpectedError);
    }
}

function onAdminResourceClick(event) {
    const button = event.target.closest("[data-resource-key]");
    if (!button) {
        return;
    }
    state.activeScreen = PRIMARY_SCREENS.SETUP;
    state.activeAdminResource = button.dataset.resourceKey;
    state.selectedAdminItemId = null;
    render();
    scrollToElement(ui.adminTableBody.closest(".panel"));
}

function onAdminTableClick(event) {
    const row = event.target.closest("[data-admin-id]");
    if (!row) {
        return;
    }
    state.selectedAdminItemId = row.dataset.adminId;
    render();
    scrollToElement(ui.adminEditorPanel);
}

async function onAdminEditorSubmit(event) {
    event.preventDefault();
    const form = event.target;

    if (!canTenantAdmin()) {
        showFlash("Настройка доступна только Tenant Admin.", "warning");
        return;
    }

    try {
        if (form.id === "admin-resource-form") {
            if (isBusy("adminResource")) {
                return;
            }
            const config = ADMIN_RESOURCES[state.activeAdminResource];
            if (!canSubmitAdminResource(config.key)) {
                showFlash(getAdminPrerequisiteMessage(config.key), "warning");
                return;
            }

            const payload = buildAdminPayload(config, new FormData(form));
            const isUpdate = Boolean(state.selectedAdminItemId);
            await withBusy("adminResource", async () => {
                const response = await requestJson(
                    "user",
                    isUpdate ? `${config.path}/${state.selectedAdminItemId}` : config.path,
                    {
                        method: isUpdate ? "PATCH" : "POST",
                        body: payload
                    }
                );

                await loadReferenceData();
                await loadRolesCatalog();
                state.selectedAdminItemId = response.id ?? state.selectedAdminItemId;
                state.selectedTreeNode = { type: TREE_NODE_TYPES.SETUP, resource: config.key };
                render();
                scrollToElement(ui.workspaceDetail || ui.adminEditorPanel);
                showActionSuccess("setupSaved", { scope: FEEDBACK_SCOPES.SETUP, resourceKey: config.key });
            });
            return;
        }

        if (form.id === "user-role-binding-form") {
            if (isBusy("roleBinding")) {
                return;
            }
            const userId = formDataValue(new FormData(form), "user_id");
            const roleId = formDataValue(new FormData(form), "role_id");
            if (!userId || !roleId) {
                showFlash("Для назначения роли выберите пользователя и роль.", "warning");
                return;
            }
            await withBusy("roleBinding", async () => {
                await requestJson("user", `/api/v1/users/${userId}/roles`, {
                    method: "POST",
                    body: { role_id: roleId }
                });
                const role = state.references.roles.find((item) => item.id === roleId);
                state.lastRoleAssignment = {
                    userId,
                    roleId,
                    roleName: role?.name || "",
                    assignedAt: new Date().toISOString()
                };
                render();
                scrollToElement(ui.workspaceDetail || ui.adminEditorPanel);
                showActionSuccess("roleAssigned", { scope: FEEDBACK_SCOPES.SETUP, resourceKey: state.activeAdminResource });
            });
        }
    } catch (error) {
        showActionError(error, { scope: FEEDBACK_SCOPES.SETUP, resourceKey: state.activeAdminResource });
    }
}

function onAdminEditorClick(event) {
    const resetButton = event.target.closest("[data-admin-reset]");
    if (resetButton) {
        state.selectedAdminItemId = null;
        render();
    }
}

function onTreeClick(event) {
    const toggle = event.target.closest("[data-tree-toggle]");
    if (toggle) {
        toggleTreeExpansion(toggle.dataset.treeToggle);
        return;
    }

    const node = event.target.closest("[data-node-type]");
    if (!node) {
        return;
    }

    selectTreeNodeFromElement(node);
}

function onTreeFilterInput(event) {
    state.treeFilter = event.target.value || "";
    applyTreeFilter();
}

function onQuickFindInput(event) {
    state.quickFindQuery = event.target.value || "";
    renderQuickFind();
}

function onQuickFindPanelClick(event) {
    const result = event.target.closest("[data-quick-find-type]");
    if (!result) {
        return;
    }

    if (result.dataset.quickFindType === "user") {
        const userId = result.dataset.userId || "";
        if (!userId) {
            return;
        }
        selectUserNode(userId);
        return;
    }

    if (result.dataset.quickFindType === "asset") {
        selectAssetNode(result.dataset.assetId, { source: "all" });
    }
}

function onTechnicalSearchInput(event) {
    state.technicalSearchQuery = event.target.value || "";
}

async function onTechnicalSearchSubmit(event) {
    event.preventDefault();
    const query = String(state.technicalSearchQuery || ui.technicalSearchInput?.value || "").trim();
    state.technicalSearchQuery = query;

    if (!query) {
        state.technicalSearchHasSearched = false;
        state.technicalSearchError = "";
        state.technicalSearchResults = [];
        state.technicalSearchLastQuery = "";
        renderTechnicalSearch();
        return;
    }

    if (query.length < TECHNICAL_SEARCH_MIN_LENGTH) {
        state.technicalSearchHasSearched = true;
        state.technicalSearchError = "Введите минимум 2 символа.";
        state.technicalSearchResults = [];
        state.technicalSearchLastQuery = query;
        renderTechnicalSearch();
        return;
    }

    if (query.length > TECHNICAL_SEARCH_MAX_LENGTH) {
        state.technicalSearchHasSearched = true;
        state.technicalSearchError = `Запрос не должен быть длиннее ${TECHNICAL_SEARCH_MAX_LENGTH} символов.`;
        state.technicalSearchResults = [];
        state.technicalSearchLastQuery = query;
        renderTechnicalSearch();
        return;
    }

    state.technicalSearchHasSearched = true;
    state.technicalSearchError = "";
    state.technicalSearchResults = [];
    state.technicalSearchLastQuery = query;

    try {
        await withBusy("technicalSearch", async () => {
            const params = new URLSearchParams({
                q: query,
                limit: String(TECHNICAL_SEARCH_LIMIT)
            });
            const response = await requestJson("asset", `/api/v1/assets/technical-attribute-search?${params.toString()}`);
            state.technicalSearchResults = Array.isArray(response.results) ? response.results : [];
            state.technicalSearchLastQuery = response.query || query;
        });
    } catch (error) {
        state.technicalSearchError = "Не удалось выполнить поиск по техническим данным. Проверьте подключение и повторите попытку.";
        state.technicalSearchResults = [];
        renderTechnicalSearch();
    }
}

function onTechnicalSearchClear() {
    state.technicalSearchQuery = "";
    state.technicalSearchResults = [];
    state.technicalSearchError = "";
    state.technicalSearchHasSearched = false;
    state.technicalSearchLastQuery = "";
    renderTechnicalSearch();
    ui.technicalSearchInput?.focus();
}

function onTechnicalSearchPanelClick(event) {
    const result = event.target.closest("[data-technical-search-asset-id]");
    if (!result) {
        return;
    }

    selectAssetNode(result.dataset.technicalSearchAssetId, {
        source: "technical-search",
        activeSection: "technical"
    });
}

function onWorkspaceDetailClick(event) {
    if (handleTechnicalAttributesClick(event)) {
        return;
    }

    const employeeSheetResult = event.target.closest("[data-standalone-employee-id]");
    if (employeeSheetResult) {
        selectStandaloneEmployeeSheetUser(employeeSheetResult.dataset.standaloneEmployeeId);
        return;
    }

    const toggle = event.target.closest("[data-tree-toggle]");
    if (toggle) {
        toggleTreeExpansion(toggle.dataset.treeToggle);
        return;
    }

    const node = event.target.closest("[data-node-type]");
    if (node) {
        selectTreeNodeFromElement(node);
        return;
    }

    const sectionButton = event.target.closest("[data-asset-section]");
    if (sectionButton) {
        state.activeAssetSection = sectionButton.dataset.assetSection;
        render();
        return;
    }

    const refreshButton = event.target.closest("[data-refresh-asset]");
    if (refreshButton && state.selectedAssetId) {
        if (isBusy("refreshAsset")) {
            return;
        }
        withBusy("refreshAsset", () => loadAssetBundle(state.selectedAssetId))
            .catch(handleUnexpectedError);
        return;
    }

    const resourceButton = event.target.closest("[data-resource-key]");
    if (resourceButton) {
        state.activeAdminResource = resourceButton.dataset.resourceKey;
        state.selectedAdminItemId = null;
        state.selectedTreeNode = { type: TREE_NODE_TYPES.SETUP, resource: state.activeAdminResource };
        render();
        return;
    }

    const adminRefreshButton = event.target.closest("#refresh-admin-resource");
    if (adminRefreshButton) {
        if (isBusy("refreshAdmin")) {
            return;
        }
        withBusy("refreshAdmin", () => refreshAdminResource()).catch(handleUnexpectedError);
        return;
    }

    const adminRow = event.target.closest("[data-admin-id]");
    if (adminRow) {
        state.selectedAdminItemId = adminRow.dataset.adminId;
        render();
        return;
    }

    const resetButton = event.target.closest("[data-admin-reset]");
    if (resetButton) {
        state.selectedAdminItemId = null;
        render();
    }
}

function onWorkspaceDetailInput(event) {
    if (!event.target.matches("[data-employee-sheet-search-input]")) {
        return;
    }

    state.employeeSheetSearchQuery = event.target.value || "";
    render();
    focusEmployeeSheetSearchInput();
}

function onWorkspaceDetailSubmit(event) {
    const form = event.target.closest("form");
    if (!form) {
        return;
    }

    if (form.id === "employee-sheet-search-form") {
        event.preventDefault();
        const input = form.querySelector("[data-employee-sheet-search-input]");
        state.employeeSheetSearchQuery = input?.value || "";
        render();
        focusEmployeeSheetSearchInput();
        return;
    }

    if (form.id === "asset-create-form") {
        onAssetCreateSubmit(event);
        return;
    }

    if (["asset-update-form", "asset-technical-attributes-form", "asset-assign-form", "asset-transfer-form", "asset-return-form"].includes(form.id)) {
        onAssetDetailSubmit(event);
        return;
    }

    if (["admin-resource-form", "user-role-binding-form"].includes(form.id)) {
        onAdminEditorSubmit(event);
    }
}

function toggleTreeExpansion(key) {
    if (!key) {
        return;
    }
    if (state.expandedTreeNodes.has(key)) {
        state.expandedTreeNodes.delete(key);
    } else {
        state.expandedTreeNodes.add(key);
    }
    render();
}

function selectTreeNodeFromElement(element) {
    const type = element.dataset.nodeType;

    if (type === TREE_NODE_TYPES.ASSET) {
        selectAssetNode(element.dataset.assetId, {
            source: element.dataset.source || "",
            parentId: element.dataset.parentId || "",
            group: element.dataset.group || ""
        });
        return;
    }

    if (type === TREE_NODE_TYPES.EMPLOYEE_SHEET) {
        selectEmployeeSheetNode();
        return;
    }

    if (type === TREE_NODE_TYPES.DEPARTMENT) {
        state.selectedTreeNode = { type, id: element.dataset.departmentId || FALLBACK_DEPARTMENT_ID };
    } else if (type === TREE_NODE_TYPES.USER) {
        selectUserNode(element.dataset.userId || "");
        return;
    } else if (type === TREE_NODE_TYPES.BRANCH) {
        state.selectedTreeNode = { type, id: element.dataset.branchId || FALLBACK_BRANCH_ID };
    } else if (type === TREE_NODE_TYPES.LOCATION) {
        state.selectedTreeNode = { type, id: element.dataset.locationId || "" };
    } else if (type === TREE_NODE_TYPES.ASSET_GROUP) {
        state.selectedTreeNode = { type, group: element.dataset.group || ASSET_GROUPS.ASSIGNED };
    } else if (type === TREE_NODE_TYPES.SETUP) {
        state.activeAdminResource = element.dataset.resource || "roles";
        state.selectedAdminItemId = null;
        state.selectedTreeNode = { type, resource: state.activeAdminResource };
    } else {
        state.selectedTreeNode = { type: TREE_NODE_TYPES.WORKSPACE };
    }

    resetEmployeeSheetTechnicalHints();
    render();
    scrollToElement(ui.workspaceDetail);
}

async function selectAssetNode(assetId, context = {}) {
    if (!assetId) {
        return;
    }
    if (isBusy("refreshAsset")) {
        return;
    }

    try {
        resetEmployeeSheetTechnicalHints();
        state.selectedTreeNode = {
            type: TREE_NODE_TYPES.ASSET,
            id: assetId,
            source: context.source || "",
            parentId: context.parentId || "",
            group: context.group || ""
        };
        state.activeAssetSection = context.activeSection || "overview";
        await withBusy("refreshAsset", () => loadAssetBundle(assetId));
        scrollToElement(ui.workspaceDetail);
    } catch (error) {
        handleUnexpectedError(error);
    }
}

function onCopyClick(event) {
    const copyButton = event.target.closest("[data-copy-value]");
    if (!copyButton) {
        return;
    }

    event.preventDefault();
    event.stopPropagation();
    copyToClipboard(copyButton.dataset.copyValue || "");
}

async function copyToClipboard(value) {
    if (!value) {
        return;
    }

    try {
        if (!navigator.clipboard || typeof navigator.clipboard.writeText !== "function") {
            throw new Error("Clipboard API unavailable");
        }
        await navigator.clipboard.writeText(value);
        showFlash("Служебное значение скопировано.", "success");
    } catch (error) {
        showFlash(`Скопируйте значение вручную: ${value}`, "info");
    }
}

async function refreshAdminResource() {
    if (!canTenantAdmin()) {
        return;
    }

    try {
        await loadReferenceData();
        await loadRolesCatalog();
        render();
        showFlash("Данные настройки обновлены.", "success");
    } catch (error) {
        showFlash(error.message, "error");
    }
}

function render() {
    renderConnectionStatusBadge();
    renderRoleSummary();
    renderTreeWorkspace();
    renderCreateLauncherState();
    syncStaticActionControls();
    renderUserServiceUrlPreview();
    renderContextTechnicalSummary();
    ui.activeScreenLabel.textContent = getSelectedTreeNodeLabel();
    ui.assetServiceOrigin.textContent = window.location.origin;
}

function renderConnectionStatusBadge() {
    if (!ui.connectionStatusBadge) {
        return;
    }

    let label = "Не подключено";
    let tone = "disconnected";
    if (hasConnectionContext() && (isBusy("session") || state.loading.profile)) {
        label = "Подключаем";
        tone = "loading";
    } else if (hasResolvedProfile()) {
        label = "Подключено";
        tone = "connected";
    } else if (hasConnectionContext()) {
        label = "Контекст задан";
        tone = "warning";
    }

    ui.connectionStatusBadge.textContent = label;
    ui.connectionStatusBadge.className = `connection-badge connection-${tone}`;
}

function renderRoleSummary() {
    if (!hasConnectionContext()) {
        ui.roleSummaryBadge.textContent = "Контекст доступа не подключён";
        return;
    }
    if (state.loading.profile) {
        ui.roleSummaryBadge.textContent = "Проверяем доступные поверхности";
        return;
    }
    if (!hasResolvedProfile()) {
        ui.roleSummaryBadge.textContent = "UI-профиль ещё не определён";
        return;
    }

    const markers = [state.profileLabel];
    markers.push(canAssetMutate() ? "активы: изменение" : canAssetRead() ? "активы: просмотр" : "активы скрыты");
    markers.push(canTenantAdmin() ? "настройка: изменение" : canReferenceRead() ? "справочники: просмотр" : "настройка скрыта");
    ui.roleSummaryBadge.textContent = markers.join(" | ");
}

function syncStaticActionControls() {
    setButtonBusy(ui.connectSession, isBusy("session") || state.loading.profile, "Подключаем");
    setButtonBusy(ui.refreshAll, isBusy("refreshAll"), "Обновляем");
    setButtonBusy(ui.refreshAssets, isBusy("refreshAsset") || state.loading.assets, "Обновляем");
    setButtonBusy(ui.refreshAdminResource, isBusy("refreshAdmin") || state.loading.references || state.loading.roles, "Обновляем");
    setButtonBusy(ui.assetCreateForm?.querySelector("button[type='submit']"), isBusy("createAsset"), "Создаём");

    if (ui.refreshAssets) {
        ui.refreshAssets.disabled = ui.refreshAssets.disabled || !canAssetRead();
    }
    if (ui.refreshAdminResource) {
        ui.refreshAdminResource.disabled = ui.refreshAdminResource.disabled || !canTenantAdmin();
    }
    const createButton = ui.assetCreateForm?.querySelector("button[type='submit']");
    if (createButton) {
        createButton.disabled = createButton.disabled || !canAssetMutate();
    }
}

function setButtonBusy(button, busy, busyLabel) {
    if (!button) {
        return;
    }

    if (!button.dataset.idleLabel) {
        button.dataset.idleLabel = button.textContent.trim();
    }

    button.disabled = Boolean(busy);
    button.classList.toggle("is-loading", Boolean(busy));
    button.textContent = busy ? busyLabel : button.dataset.idleLabel;
}

function renderTreeWorkspace() {
    if (!ui.workspaceTreeBody || !ui.workspaceDetail) {
        return;
    }

    renderQuickFind();
    renderTechnicalSearch();
    renderTreeNavigation();
    renderTreeDetail();
    applyTreeFilter();
}

function renderQuickFind() {
    if (!ui.quickFindInput || !ui.quickFindPanel) {
        return;
    }

    if (ui.quickFindInput.value !== state.quickFindQuery) {
        ui.quickFindInput.value = state.quickFindQuery;
    }

    ui.quickFindPanel.innerHTML = renderQuickFindPanel();
}

function renderQuickFindPanel() {
    const rawQuery = state.quickFindQuery || "";
    const query = normalizeQuickFindQuery(rawQuery);

    if (!rawQuery.trim()) {
        return renderQuickFindEmpty("Быстрый поиск", "Можно искать сотрудника, инвентарный номер, название актива, серийный номер или тип устройства.");
    }

    if (query.length < QUICK_FIND_MIN_LENGTH) {
        return renderQuickFindEmpty("Слишком короткий запрос", "Введите минимум 2 символа для поиска по загруженным данным.");
    }

    if (!hasResolvedProfile() || (!canAssetRead() && !canReferenceRead())) {
        return renderQuickFindEmpty("Данные ещё не загружены", "Сначала подключите контекст и обновите данные рабочего пространства.");
    }

    const results = buildQuickFindResults(query);
    if (results.users.total === 0 && results.assets.total === 0) {
        return renderQuickFindEmpty("Ничего не найдено", "Ничего не найдено в загруженных данных рабочего пространства.");
    }

    return `
        <div class="quick-find-results">
            ${renderQuickFindGroup("Сотрудники", results.users, renderQuickFindUserResult)}
            ${renderQuickFindGroup("Активы / устройства", results.assets, renderQuickFindAssetResult)}
        </div>
    `;
}

function buildQuickFindResults(query) {
    const users = canReferenceRead()
        ? state.references.users.filter((user) => matchesQuickFind(query, [user.name]))
        : [];
    const assets = canAssetRead()
        ? state.assets.filter((asset) => matchesQuickFind(query, [
            asset.asset_tag,
            asset.display_name,
            asset.serial_number,
            asset.asset_type
        ]))
        : [];

    return {
        users: {
            total: users.length,
            items: users.slice(0, QUICK_FIND_RESULT_LIMIT)
        },
        assets: {
            total: assets.length,
            items: assets.slice(0, QUICK_FIND_RESULT_LIMIT)
        }
    };
}

function renderQuickFindGroup(label, group, renderItem) {
    const hasItems = group.items.length > 0;
    const moreCount = Math.max(group.total - group.items.length, 0);
    return `
        <section class="quick-find-group" aria-label="${escapeAttribute(label)}">
            <div class="quick-find-group-header">
                <span>${escapeHtml(label)}</span>
                <span class="summary-pill">${escapeHtml(String(group.total))}</span>
            </div>
            ${hasItems ? group.items.map(renderItem).join("") : `<p class="quick-find-empty">Нет совпадений в этой группе.</p>`}
            ${moreCount > 0 ? `<p class="quick-find-more">Показаны первые ${escapeHtml(String(group.items.length))} из ${escapeHtml(String(group.total))} по загруженным данным.</p>` : ""}
        </section>
    `;
}

function renderQuickFindUserResult(user) {
    const departmentName = resolveDepartmentName(user.department_id);
    const assetCount = getAssetsForUser(user.id).length;
    return `
        <button class="quick-find-result" type="button" data-quick-find-type="user" data-user-id="${escapeAttribute(user.id)}">
            <span class="quick-find-title">${escapeHtml(user.name || shortenTechnicalValue(user.id))}</span>
            <span class="quick-find-meta">${escapeHtml(departmentName)} · ${escapeHtml(formatTreeCount(assetCount, ["актив", "актива", "активов"], "нет закреплённых активов"))}</span>
        </button>
    `;
}

function renderQuickFindAssetResult(asset) {
    const meta = [
        assetCanonicalTag(asset) ? `Инв. тег: ${assetCanonicalTag(asset)}` : "",
        asset.asset_type ? `Тип: ${asset.asset_type}` : "",
        asset.serial_number ? `SN: ${asset.serial_number}` : "",
        asset.status ? `Статус: ${asset.status}` : ""
    ].filter(Boolean).join(" · ");
    const responsibility = [
        getQuickFindUserLabel(asset.assigned_user_id),
        getQuickFindLocationLabel(asset.location_id)
    ].filter(Boolean).join(" · ");

    return `
        <button class="quick-find-result" type="button" data-quick-find-type="asset" data-asset-id="${escapeAttribute(asset.id)}">
            <span class="quick-find-title">${escapeHtml(assetDisplayTitle(asset))}</span>
            <span class="quick-find-meta">${escapeHtml(meta || "Данные актива")}</span>
            ${responsibility ? `<span class="quick-find-meta">${escapeHtml(responsibility)}</span>` : ""}
        </button>
    `;
}

function renderQuickFindEmpty(title, copy) {
    return `
        <div class="quick-find-empty">
            <strong>${escapeHtml(title)}</strong>
            <p>${escapeHtml(copy)}</p>
        </div>
    `;
}

function renderTechnicalSearch() {
    if (!ui.technicalSearchInput || !ui.technicalSearchPanel) {
        return;
    }

    if (ui.technicalSearchInput.value !== state.technicalSearchQuery) {
        ui.technicalSearchInput.value = state.technicalSearchQuery;
    }

    setButtonBusy(ui.technicalSearchForm?.querySelector("button[type='submit']"), isBusy("technicalSearch"), "Ищем");
    ui.technicalSearchPanel.innerHTML = renderTechnicalSearchPanel();
}

function renderTechnicalSearchPanel() {
    if (isBusy("technicalSearch")) {
        return renderTechnicalSearchEmpty("Ищем", "Выполняем поиск по утверждённым техническим данным.");
    }

    if (state.technicalSearchError) {
        return renderTechnicalSearchEmpty("Поиск не выполнен", state.technicalSearchError, "technical-search-error");
    }

    if (!hasResolvedProfile()) {
        return renderTechnicalSearchEmpty(
            "Поиск по техническим данным",
            "Введите технический признак устройства: hostname, AnyDesk, IP, MAC, внутренний номер или сетевое имя принтера."
        );
    }

    if (!canAssetRead()) {
        return renderTechnicalSearchEmpty(
            "Поиск недоступен",
            "Текущая роль не имеет доступа к реестру активов."
        );
    }

    if (!state.technicalSearchHasSearched) {
        return renderTechnicalSearchEmpty(
            "Поиск по техническим данным",
            "Введите технический признак устройства: hostname, AnyDesk, IP, MAC, внутренний номер или сетевое имя принтера."
        );
    }

    if (state.technicalSearchResults.length === 0) {
        return renderTechnicalSearchEmpty(
            "Ничего не найдено",
            "Ничего не найдено по утверждённым техническим данным."
        );
    }

    return `
        <div class="technical-search-results">
            <section class="technical-search-group" aria-label="Активы / устройства">
                <div class="technical-search-group-header">
                    <span>Активы / устройства</span>
                    <span class="summary-pill">${escapeHtml(String(state.technicalSearchResults.length))}</span>
                </div>
                ${state.technicalSearchResults.map(renderTechnicalSearchResult).join("")}
            </section>
        </div>
    `;
}

function renderTechnicalSearchResult(result) {
    const assetId = result.asset_id || result.assetId || "";
    const matchedField = result.matched_field || result.matchedField || {};
    const title = assetDisplayTitle({
        id: assetId,
        display_name: result.display_name || result.displayName,
        asset_tag: result.asset_tag || result.assetTag,
        asset_type: result.asset_type || result.assetType,
        serial_number: result.serial_number || result.serialNumber
    });
    const meta = [
        result.asset_tag || result.assetTag ? `Инв. тег: ${result.asset_tag || result.assetTag}` : "",
        result.asset_type || result.assetType ? `Тип: ${result.asset_type || result.assetType}` : "",
        result.serial_number || result.serialNumber ? `SN: ${result.serial_number || result.serialNumber}` : "",
        result.status ? `Статус: ${result.status}` : ""
    ].filter(Boolean).join(" · ");
    const matchLabel = matchedField.label || matchedField.key || "Техническое поле";
    const displayValue = matchedField.display_value || matchedField.displayValue || "";
    const maskedHint = matchedField.masked
        ? `<span class="technical-search-masked-hint">Часть значения скрыта</span>`
        : "";

    return `
        <button class="technical-search-result" type="button" data-technical-search-asset-id="${escapeAttribute(assetId)}">
            <span class="technical-search-title">${escapeHtml(title)}</span>
            <span class="technical-search-meta">${escapeHtml(meta || "Данные актива")}</span>
            <span class="technical-search-match">
                <span>${escapeHtml(matchLabel)}: ${escapeHtml(displayValue || "-")}</span>
                ${maskedHint}
            </span>
        </button>
    `;
}

function renderTechnicalSearchEmpty(title, copy, className = "") {
    return `
        <div class="technical-search-empty ${escapeAttribute(className)}">
            <strong>${escapeHtml(title)}</strong>
            <p>${escapeHtml(copy)}</p>
        </div>
    `;
}

function getQuickFindUserLabel(userId) {
    if (!userId) {
        return "Ответственный не назначен";
    }
    const user = canReferenceRead()
        ? state.references.users.find((item) => item.id === userId)
        : null;
    return `Ответственный: ${user?.name || shortenTechnicalValue(userId)}`;
}

function getQuickFindLocationLabel(locationId) {
    if (!locationId) {
        return "Локация не указана";
    }
    if (!canReferenceRead()) {
        return "Локация: служебное значение";
    }
    const location = state.references.locations.find((item) => item.id === locationId);
    if (!location) {
        return "Локация: служебное значение";
    }
    const branchName = resolveBranchName(location.branch_id);
    return `Локация: ${location.name || shortenTechnicalValue(location.id)}${branchName && branchName !== "-" ? `, ${branchName}` : ""}`;
}

function matchesQuickFind(query, values) {
    return values.some((value) => normalizeQuickFindQuery(value).includes(query));
}

function normalizeQuickFindQuery(value) {
    return String(value || "").trim().toLocaleLowerCase("ru-RU");
}

function renderTreeNavigation() {
    const selectedPath = getSelectedTreePath();
    ui.workspaceTreeBody.innerHTML = [
        renderWorkspaceTreeGroup(selectedPath),
        renderEmployeeSheetTreeNode(),
        renderOrgTreeGroup(selectedPath),
        renderLocationsTreeGroup(selectedPath),
        renderAllAssetsTreeGroup(selectedPath),
        renderSetupTreeGroup(selectedPath)
    ].join("");
}

function applyTreeFilter() {
    if (!ui.workspaceTreeBody) {
        return;
    }

    const query = normalizeTreeFilter(state.treeFilter);
    if (ui.treeFilter && ui.treeFilter.value !== state.treeFilter) {
        ui.treeFilter.value = state.treeFilter;
    }

    const rowContainers = [...ui.workspaceTreeBody.querySelectorAll(".tree-line, .tree-node")];
    rowContainers.forEach((row) => {
        const rowLabel = row.classList.contains("tree-node") ? row : row.querySelector(".tree-node");
        const matches = !query || normalizeTreeFilter(rowLabel?.textContent || row.textContent).includes(query);
        row.classList.toggle("is-filter-hidden", !matches);
    });

    const groups = [...ui.workspaceTreeBody.querySelectorAll(".tree-group")];
    groups.forEach((group) => {
        const header = group.querySelector(".tree-group-header");
        const headerMatches = !query || normalizeTreeFilter(header?.textContent || "").includes(query);
        const visibleChildren = [...group.querySelectorAll(".tree-line, .tree-node")]
            .some((row) => !row.classList.contains("is-filter-hidden"));
        group.classList.toggle("is-filter-hidden", Boolean(query) && !headerMatches && !visibleChildren);
    });

    if (ui.treeFilterStatus) {
        const visibleRows = [...ui.workspaceTreeBody.querySelectorAll(".tree-node")]
            .filter((row) => !row.closest(".is-filter-hidden")).length;
        ui.treeFilterStatus.textContent = query
            ? `Локально: ${visibleRows} открытых строк. Скрытые ветки не раскрываются.`
            : "Локально, без поиска на сервере.";
    }
}

function normalizeTreeFilter(value) {
    return String(value || "").trim().toLocaleLowerCase("ru-RU");
}

function formatTreeCount(count, filledLabels, emptyLabel) {
    const safeCount = Number(count) || 0;
    const labels = Array.isArray(filledLabels) ? filledLabels : [filledLabels, filledLabels, filledLabels];
    return safeCount === 0 ? emptyLabel : `${safeCount} ${pickRussianCountLabel(safeCount, labels)}`;
}

function pickRussianCountLabel(count, labels) {
    const absolute = Math.abs(count);
    const lastTwo = absolute % 100;
    const last = absolute % 10;
    if (lastTwo >= 11 && lastTwo <= 14) {
        return labels[2];
    }
    if (last === 1) {
        return labels[0];
    }
    if (last >= 2 && last <= 4) {
        return labels[1];
    }
    return labels[2];
}

function formatAssetCount(count, emptyLabel = "нет активов") {
    return formatTreeCount(count, ["актив", "актива", "активов"], emptyLabel);
}

function formatUserCount(count, emptyLabel = "нет пользователей") {
    return formatTreeCount(count, ["пользователь", "пользователя", "пользователей"], emptyLabel);
}

function formatLocationCount(count, emptyLabel = "нет локаций") {
    return formatTreeCount(count, ["локация", "локации", "локаций"], emptyLabel);
}

function formatRecordCount(count, emptyLabel = "нет записей") {
    return formatTreeCount(count, ["запись", "записи", "записей"], emptyLabel);
}

function renderWorkspaceTreeGroup(selectedPath) {
    return renderTreeGroup({
        key: TREE_GROUPS.WORKSPACE,
        label: "Рабочее место",
        count: "обзор",
        selectedPath,
        body: `
            ${renderTreeNode({
                type: TREE_NODE_TYPES.WORKSPACE,
                label: "Обзор / что делать первым",
                meta: hasConnectionContext() ? (state.profileLabel || "контекст подключён") : "подключите контекст",
                selected: state.selectedTreeNode.type === TREE_NODE_TYPES.WORKSPACE
            })}
        `
    });
}

function renderEmployeeSheetTreeNode() {
    return `
        <section class="tree-group tree-single-node">
            ${renderTreeNode({
                type: TREE_NODE_TYPES.EMPLOYEE_SHEET,
                label: "Информационный лист",
                meta: "Поиск сотрудника и закреплённые устройства",
                selected: state.selectedTreeNode.type === TREE_NODE_TYPES.EMPLOYEE_SHEET
            })}
        </section>
    `;
}

function renderOrgTreeGroup(selectedPath) {
    const departments = getDepartmentTreeNodes();
    const userCount = state.references.users.length;
    const assignedAssets = state.assets.filter((asset) => Boolean(asset.assigned_user_id)).length;
    const body = departments.length > 0
        ? departments.map((department) => renderDepartmentTreeNode(department, selectedPath)).join("")
        : renderTreeEmpty("Загрузите справочники, чтобы увидеть подразделения и пользователей.");

    return renderTreeGroup({
        key: TREE_GROUPS.ORG,
        label: "Оргструктура",
        count: `${formatUserCount(userCount)} / ${formatAssetCount(assignedAssets, "нет назначенных активов")}`,
        selectedPath,
        body
    });
}

function renderDepartmentTreeNode(department, selectedPath) {
    const key = getDepartmentTreeKey(department.id);
    const users = getUsersForDepartment(department.id);
    const assets = getAssetsForDepartment(department.id);
    const expanded = isTreeExpanded(key, selectedPath);
    const selected = state.selectedTreeNode.type === TREE_NODE_TYPES.DEPARTMENT
        && state.selectedTreeNode.id === department.id;
    const body = expanded
        ? users.length > 0
            ? `<div class="tree-children">${users.map((user) => renderUserTreeNode(user, selectedPath)).join("")}</div>`
            : `<div class="tree-children">${renderTreeEmpty("В этом подразделении нет пользователей.")}</div>`
        : "";

    return `
        <div class="tree-branch">
            <div class="tree-line">
                ${renderTreeToggle(key, expanded, `${formatUserCount(users.length)} / ${formatAssetCount(assets.length)}`)}
                ${renderTreeNode({
                    type: TREE_NODE_TYPES.DEPARTMENT,
                    label: department.name,
                    meta: `${formatUserCount(users.length)} / ${formatAssetCount(assets.length)}`,
                    selected,
                    attributes: `data-department-id="${escapeAttribute(department.id)}"`
                })}
            </div>
            ${body}
        </div>
    `;
}

function renderUserTreeNode(user, selectedPath) {
    const key = getUserTreeKey(user.id);
    const assets = getAssetsForUser(user.id);
    const expanded = isTreeExpanded(key, selectedPath);
    const selected = state.selectedTreeNode.type === TREE_NODE_TYPES.USER
        && state.selectedTreeNode.id === user.id;
    const body = expanded
        ? assets.length > 0
            ? `<div class="tree-children">${assets.map((asset) => renderAssetTreeNode(asset, {
                source: "user",
                parentId: user.id
            })).join("")}</div>`
            : `<div class="tree-children">${renderTreeEmpty("Нет назначенных активов.")}</div>`
        : "";

    return `
        <div class="tree-branch">
            <div class="tree-line">
                ${renderTreeToggle(key, expanded, formatAssetCount(assets.length, "нет назначенных активов"))}
                ${renderTreeNode({
                    type: TREE_NODE_TYPES.USER,
                    label: user.name || shortenTechnicalValue(user.id),
                    meta: formatAssetCount(assets.length, "нет назначенных активов"),
                    selected,
                    attributes: `data-user-id="${escapeAttribute(user.id)}"`
                })}
            </div>
            ${body}
        </div>
    `;
}

function renderLocationsTreeGroup(selectedPath) {
    const branches = getBranchTreeNodes();
    const locationCount = state.references.locations.length;
    const locatedAssetCount = state.assets.filter((asset) => Boolean(asset.location_id)).length;
    const body = branches.length > 0
        ? branches.map((branch) => renderBranchTreeNode(branch, selectedPath)).join("")
        : renderTreeEmpty("Загрузите филиалы и локации, чтобы увидеть физическое дерево.");

    return renderTreeGroup({
        key: TREE_GROUPS.LOCATIONS,
        label: "Локации",
        count: `${formatLocationCount(locationCount)} / ${formatAssetCount(locatedAssetCount, "нет активов с локацией")}`,
        selectedPath,
        body
    });
}

function renderBranchTreeNode(branch, selectedPath) {
    const key = getBranchTreeKey(branch.id);
    const locations = getLocationsForBranch(branch.id);
    const assets = getAssetsForBranch(branch.id);
    const expanded = isTreeExpanded(key, selectedPath);
    const selected = state.selectedTreeNode.type === TREE_NODE_TYPES.BRANCH
        && state.selectedTreeNode.id === branch.id;
    const body = expanded
        ? locations.length > 0
            ? `<div class="tree-children">${locations.map((location) => renderLocationTreeNode(location, selectedPath)).join("")}</div>`
            : `<div class="tree-children">${renderTreeEmpty("В этом филиале нет локаций.")}</div>`
        : "";

    return `
        <div class="tree-branch">
            <div class="tree-line">
                ${renderTreeToggle(key, expanded, `${formatLocationCount(locations.length)} / ${formatAssetCount(assets.length)}`)}
                ${renderTreeNode({
                    type: TREE_NODE_TYPES.BRANCH,
                    label: branch.name,
                    meta: `${formatLocationCount(locations.length)} / ${formatAssetCount(assets.length)}`,
                    selected,
                    attributes: `data-branch-id="${escapeAttribute(branch.id)}"`
                })}
            </div>
            ${body}
        </div>
    `;
}

function renderLocationTreeNode(location, selectedPath) {
    const key = getLocationTreeKey(location.id);
    const assets = getAssetsForLocation(location.id);
    const expanded = isTreeExpanded(key, selectedPath);
    const selected = state.selectedTreeNode.type === TREE_NODE_TYPES.LOCATION
        && state.selectedTreeNode.id === location.id;
    const body = expanded
        ? assets.length > 0
            ? `<div class="tree-children">${assets.map((asset) => renderAssetTreeNode(asset, {
                source: "location",
                parentId: location.id
            })).join("")}</div>`
            : `<div class="tree-children">${renderTreeEmpty("В этой локации нет активов из загруженного реестра.")}</div>`
        : "";

    return `
        <div class="tree-branch">
            <div class="tree-line">
                ${renderTreeToggle(key, expanded, formatAssetCount(assets.length, "нет активов в этой локации"))}
                ${renderTreeNode({
                    type: TREE_NODE_TYPES.LOCATION,
                    label: location.name || shortenTechnicalValue(location.id),
                    meta: formatAssetCount(assets.length, "нет активов в этой локации"),
                    selected,
                    attributes: `data-location-id="${escapeAttribute(location.id)}"`
                })}
            </div>
            ${body}
        </div>
    `;
}

function renderAllAssetsTreeGroup(selectedPath) {
    const groups = getAllAssetGroups();
    const body = groups.map((group) => renderAssetGroupTreeNode(group)).join("");

    return renderTreeGroup({
        key: TREE_GROUPS.ALL_ASSETS,
        label: "Все активы",
        count: formatAssetCount(state.assets.length),
        selectedPath,
        body
    });
}

function renderAssetGroupTreeNode(group) {
    const selected = state.selectedTreeNode.type === TREE_NODE_TYPES.ASSET_GROUP
        && state.selectedTreeNode.group === group.key;
    return renderTreeNode({
        type: TREE_NODE_TYPES.ASSET_GROUP,
        label: group.label,
        meta: formatAssetCount(group.assets.length),
        selected,
        attributes: `data-group="${escapeAttribute(group.key)}"`
    });
}

function renderSetupTreeGroup(selectedPath) {
    const body = Object.values(ADMIN_RESOURCES)
        .map((resource) => renderTreeNode({
            type: TREE_NODE_TYPES.SETUP,
            label: resource.label,
            meta: formatRecordCount(getAdminItems(resource.key).length),
            selected: state.selectedTreeNode.type === TREE_NODE_TYPES.SETUP
                && state.selectedTreeNode.resource === resource.key,
            attributes: `data-resource="${escapeAttribute(resource.key)}"`
        }))
        .join("");

    return renderTreeGroup({
        key: TREE_GROUPS.SETUP,
        label: "Настройка",
        count: canTenantAdmin() ? "Tenant Admin" : "скрыто/просмотр",
        selectedPath,
        body
    });
}

function renderTreeGroup({ key, label, count, selectedPath, body }) {
    const expanded = isTreeExpanded(key, selectedPath);
    return `
        <section class="tree-group">
            <button class="tree-group-header" type="button" data-tree-toggle="${escapeAttribute(key)}" aria-expanded="${expanded ? "true" : "false"}">
                <span class="tree-caret">${expanded ? "▾" : "▸"}</span>
                <span>${escapeHtml(label)}</span>
                <span class="tree-count">${escapeHtml(count)}</span>
            </button>
            ${expanded ? `<div class="tree-group-body">${body}</div>` : ""}
        </section>
    `;
}

function renderTreeToggle(key, expanded, label) {
    return `
        <button class="tree-toggle" type="button" data-tree-toggle="${escapeAttribute(key)}" aria-expanded="${expanded ? "true" : "false"}" aria-label="${escapeAttribute(label)}">
            ${expanded ? "▾" : "▸"}
        </button>
    `;
}

function renderTreeNode({ type, label, meta = "", selected = false, attributes = "" }) {
    return `
        <button class="tree-node ${selected ? "is-selected" : ""}" type="button" data-node-type="${escapeAttribute(type)}" ${attributes}>
            <span class="tree-node-label">${escapeHtml(label || "-")}</span>
            ${meta ? `<span class="tree-node-meta">${escapeHtml(meta)}</span>` : ""}
        </button>
    `;
}

function renderAssetTreeNode(asset, context = {}) {
    const selected = state.selectedTreeNode.type === TREE_NODE_TYPES.ASSET
        && state.selectedTreeNode.id === asset.id;
    const title = assetDisplayTitle(asset);
    const canonicalTag = assetCanonicalTag(asset);
    const metaParts = [];
    if (canonicalTag && canonicalTag !== title) {
        metaParts.push(`Тег: ${canonicalTag}`);
    }
    metaParts.push(asset.asset_type || "-");
    metaParts.push(String(asset.status || "UNKNOWN").toUpperCase());
    return renderTreeNode({
        type: TREE_NODE_TYPES.ASSET,
        label: title,
        meta: metaParts.join(" · "),
        selected,
        attributes: `
            data-asset-id="${escapeAttribute(asset.id)}"
            data-source="${escapeAttribute(context.source || "")}"
            data-parent-id="${escapeAttribute(context.parentId || "")}"
            data-group="${escapeAttribute(context.group || "")}"
        `
    });
}

function renderTreeEmpty(copy) {
    return `<p class="tree-empty">${escapeHtml(copy)}</p>`;
}

function renderTreeDetail() {
    const node = state.selectedTreeNode || { type: TREE_NODE_TYPES.WORKSPACE };

    if (node.type === TREE_NODE_TYPES.DEPARTMENT) {
        ui.workspaceDetail.innerHTML = renderDepartmentDetail(node.id);
        return;
    }
    if (node.type === TREE_NODE_TYPES.USER) {
        ui.workspaceDetail.innerHTML = renderUserDetail(node.id);
        return;
    }
    if (node.type === TREE_NODE_TYPES.EMPLOYEE_SHEET) {
        ui.workspaceDetail.innerHTML = renderStandaloneEmployeeSheetView();
        return;
    }
    if (node.type === TREE_NODE_TYPES.BRANCH) {
        ui.workspaceDetail.innerHTML = renderBranchDetail(node.id);
        return;
    }
    if (node.type === TREE_NODE_TYPES.LOCATION) {
        ui.workspaceDetail.innerHTML = renderLocationDetail(node.id);
        return;
    }
    if (node.type === TREE_NODE_TYPES.ASSET) {
        ui.workspaceDetail.innerHTML = renderAssetDetailPanel();
        return;
    }
    if (node.type === TREE_NODE_TYPES.ASSET_GROUP) {
        ui.workspaceDetail.innerHTML = renderAllAssetsGroupDetail(node.group);
        return;
    }
    if (node.type === TREE_NODE_TYPES.SETUP) {
        ui.workspaceDetail.innerHTML = renderSetupResourceDetail(node.resource || state.activeAdminResource);
        return;
    }

    ui.workspaceDetail.innerHTML = renderWorkspaceOverviewDetail();
}

function renderWorkspaceOverviewDetail() {
    const totalAssets = state.assets.length;
    const assigned = state.assets.filter((asset) => Boolean(asset.assigned_user_id)).length;
    const withoutUser = state.assets.filter((asset) => !asset.assigned_user_id).length;
    const withoutLocation = state.assets.filter((asset) => !asset.location_id).length;

    return `
        <div class="stack">
            ${renderDetailHeader("Рабочее место", "Обзор / что делать первым", "Выберите путь слева: по ответственности, по физической локации, через общий список активов или через настройку tenant.")}
            <div class="context-feedback-slot" data-context-feedback-slot="${FEEDBACK_SCOPES.WORKSPACE}">
                ${renderContextualFeedback(FEEDBACK_SCOPES.WORKSPACE)}
            </div>
            <div class="daily-guide">
                <strong>Выберите путь по тому, что уже знаете</strong>
                <ul class="compact-list">
                    <li><strong>Оргструктура</strong> — когда известен отдел или ответственный пользователь.</li>
                    <li><strong>Локации</strong> — когда известен филиал или физическое место.</li>
                    <li><strong>Все активы</strong> — когда пользователь или локация неизвестны.</li>
                    <li><strong>Настройка</strong> — только для ролей и справочников tenant.</li>
                </ul>
            </div>
            <div class="overview-grid">
                ${renderMetricCard("Активов", String(totalAssets), "Загружено из существующего реестра активов.")}
                ${renderMetricCard("Назначенные", String(assigned), "Есть текущий ответственный пользователь.")}
                ${renderMetricCard("Неназначенные", String(withoutUser), "Нет текущего assigned_user_id.")}
                ${renderMetricCard("Без локации", String(withoutLocation), "Нет текущего location_id в загруженной карточке.")}
            </div>
            <div class="workspace-path-grid">
                ${renderWorkspacePathCard("Оргструктура", "Подразделение -> пользователь -> активы", "Ответственность и ежедневный поиск через подразделения и пользователей.", TREE_GROUPS.ORG)}
                ${renderWorkspacePathCard("Локации", "Филиал -> локация -> активы", "Физическое размещение без смешивания с подразделениями.", TREE_GROUPS.LOCATIONS)}
                ${renderWorkspacePathCard("Все активы", "Назначенные / Неназначенные / Retired", "Запасной путь, когда неизвестны пользователь или локация.", TREE_GROUPS.ALL_ASSETS)}
                ${renderWorkspacePathCard("Настройка", "Роли / пользователи / справочники", "Отдельная зона Tenant Admin для справочных данных.", TREE_GROUPS.SETUP)}
            </div>
            <div class="journey-banner tone-info">
                <div class="journey-copy">
                    <strong>С чего начать</strong>
                    <p>Подключите контекст, обновите данные, затем раскройте нужный путь в дереве. Дерево не хранит состояние между сессиями и не запрашивает новые серверные проекции.</p>
                </div>
            </div>
        </div>
    `;
}

function renderWorkspacePathCard(title, path, copy, groupKey) {
    return `
        <article class="workspace-area-card">
            <p class="metric-label">${escapeHtml(path)}</p>
            <strong>${escapeHtml(title)}</strong>
            <p>${escapeHtml(copy)}</p>
            <button class="button button-secondary" type="button" data-tree-toggle="${escapeAttribute(groupKey)}">Открыть в дереве</button>
        </article>
    `;
}

function renderDepartmentDetail(departmentId) {
    const department = getDepartmentTreeNodes().find((item) => item.id === departmentId);
    if (!department) {
        return renderPanelEmpty("Оргструктура", "Подразделение не найдено", "Данные могли измениться. Обновите рабочий контур.");
    }

    const users = getUsersForDepartment(departmentId);
    const assets = getAssetsForDepartment(departmentId);

    return `
        <div class="stack">
            ${renderDetailHeader("Оргструктура", department.name, "Управленческий путь: подразделение, пользователи и назначенные им активы. Это не физическая локация.")}
            <div class="asset-summary-grid">
                ${renderSummaryCard("Пользователи", escapeHtml(String(users.length)), "Пользователи, привязанные к этому подразделению в загруженных данных.")}
                ${renderSummaryCard("Назначенные активы", escapeHtml(String(assets.length)), "Активы, назначенные пользователям этого подразделения.")}
                ${renderSummaryCard("Служебный ID", renderTechnicalValue(department.id, { copy: true }), "Техническое значение доступно для поддержки.")}
            </div>
            <section>
                <div class="panel-heading">
                    <div>
                        <p class="section-kicker">Пользователи</p>
                        <h3>Пользователи подразделения</h3>
                    </div>
                    <span class="summary-pill">${escapeHtml(String(users.length))}</span>
                </div>
                ${renderUserList(users)}
            </section>
            <section>
                <div class="panel-heading">
                    <div>
                        <p class="section-kicker">Активы</p>
                        <h3>Активы, назначенные пользователям</h3>
                    </div>
                    <span class="summary-pill">${escapeHtml(String(assets.length))}</span>
                </div>
                ${renderAssetList(assets, { empty: "У пользователей этого подразделения нет назначенных активов.", source: "department", parentId: departmentId })}
            </section>
        </div>
    `;
}

function renderStandaloneEmployeeSheetView() {
    syncStandaloneEmployeeSheetSelection();
    const selectedUser = state.references.users.find((user) => user.id === state.employeeSheetSelectedUserId) || null;

    return `
        <div class="standalone-employee-sheet stack">
            ${renderDetailHeader("Информационный лист", "Информационный лист", "Поиск сотрудника, выбранная информация, закреплённые устройства и разрешённые технические подсказки.")}
            ${renderStandaloneEmployeeSearch()}
            ${selectedUser ? renderStandaloneEmployeeInfo(selectedUser) : ""}
        </div>
    `;
}

function renderStandaloneEmployeeSearch() {
    return `
        <section class="employee-sheet-search" aria-label="Поиск сотрудника">
            <div class="panel-heading employee-sheet-search-heading">
                <div>
                    <p class="section-kicker">Сотрудник</p>
                    <h3>Поиск сотрудника</h3>
                </div>
                <span class="summary-pill">Без поиска на сервере</span>
            </div>
            <form id="employee-sheet-search-form" class="employee-sheet-search-form">
                <label class="field">
                    <span>Информационный лист</span>
                    <input
                        data-employee-sheet-search-input
                        type="search"
                        value="${escapeAttribute(state.employeeSheetSearchQuery)}"
                        placeholder="Найти сотрудника по имени"
                        aria-describedby="employee-sheet-search-help"
                        autocomplete="off"
                    >
                    <small id="employee-sheet-search-help">Поиск только по уже загруженным пользователям рабочего пространства. Без поиска на сервере.</small>
                </label>
            </form>
            ${renderStandaloneEmployeeSearchResults()}
        </section>
    `;
}

function renderStandaloneEmployeeSearchResults() {
    if (!canReferenceRead() || state.references.users.length === 0) {
        return renderStandaloneEmployeeSearchState("Пользователи ещё не загружены. Подключите контур и обновите справочники.");
    }

    const query = normalizeQuickFindQuery(state.employeeSheetSearchQuery);
    if (!query) {
        return renderStandaloneEmployeeSearchState("Введите имя сотрудника");
    }

    const results = state.references.users
        .filter((user) => matchesQuickFind(query, [user.name]))
        .slice(0, QUICK_FIND_RESULT_LIMIT);

    if (results.length === 0) {
        return renderStandaloneEmployeeSearchState("Сотрудник не найден в загруженных данных.");
    }

    return `
        <div class="employee-sheet-search-results" aria-label="Результаты поиска сотрудников">
            ${results.map((user) => renderStandaloneEmployeeSearchResult(user)).join("")}
        </div>
    `;
}

function renderStandaloneEmployeeSearchState(copy) {
    return `<p class="employee-sheet-search-empty">${escapeHtml(copy)}</p>`;
}

function renderStandaloneEmployeeSearchResult(user) {
    const assets = getAssetsForUser(user.id);
    const departmentName = resolveDepartmentName(user.department_id);
    return `
        <button
            class="employee-sheet-search-result ${state.employeeSheetSelectedUserId === user.id ? "is-selected" : ""}"
            type="button"
            data-standalone-employee-id="${escapeAttribute(user.id)}"
        >
            <span class="employee-sheet-result-title">${escapeHtml(user.name || shortenTechnicalValue(user.id))}</span>
            <span class="employee-sheet-result-meta">${escapeHtml(departmentName && departmentName !== "-" ? departmentName : "Подразделение не указано")}</span>
            <span class="employee-sheet-result-count">${escapeHtml(formatAssetCount(assets.length, "нет закреплённых устройств"))}</span>
        </button>
    `;
}

function renderStandaloneEmployeeInfo(user) {
    const assets = getAssetsForUser(user.id);
    const departmentName = resolveDepartmentName(user.department_id);
    return `
        <section class="standalone-employee-selection" aria-label="Выбранная информация по сотруднику">
            <div class="asset-summary-grid">
                ${renderSummaryCard("Сотрудник", escapeHtml(user.name || "-"), "Выбран из уже загруженных пользователей рабочего пространства.")}
                ${renderSummaryCard("Подразделение", escapeHtml(departmentName), user.department_id ? "Подразделение из загруженных справочников." : "Подразделение не указано.")}
                ${renderSummaryCard("Закреплённые устройства", escapeHtml(String(assets.length)), "Активы из загруженного реестра с assigned_user_id этого сотрудника.")}
            </div>
            ${renderEmployeeInfoSheet(user, assets, departmentName, {
                hintsScope: "standalone",
                selectedUserId: user.id,
                emptyDevicesTitle: "У сотрудника нет закреплённых устройств",
                emptyDevicesCopy: "У сотрудника нет закреплённых устройств в загруженных данных."
            })}
        </section>
    `;
}

function renderUserDetail(userId) {
    const user = state.references.users.find((item) => item.id === userId);
    if (!user) {
        return renderPanelEmpty("Оргструктура", "Пользователь не найден", "Данные могли измениться. Обновите рабочий контур.");
    }

    const assets = getAssetsForUser(userId);
    const departmentName = resolveDepartmentName(user.department_id);

    return `
        <div class="stack">
            ${renderDetailHeader("Оргструктура", user.name || shortenTechnicalValue(user.id), "Пользовательский узел показывает только уже доступные поля настройки и назначенные активы.")}
            <div class="asset-summary-grid">
                ${renderSummaryCard("Имя", escapeHtml(user.name || "-"), "Существующее поле пользователя.")}
                ${renderSummaryCard("Подразделение", escapeHtml(departmentName), user.department_id ? "Подразделение из загруженных данных пользователя." : "Пользователь показан в группе Без подразделения.")}
                ${renderSummaryCard("Назначенные активы", escapeHtml(String(assets.length)), "Активы, где этот пользователь указан ответственным.")}
                ${renderSummaryCard("Служебный ID", renderTechnicalValue(user.id, { copy: true }), "Техническое значение доступно для поддержки.")}
            </div>
            ${renderEmployeeInfoSheet(user, assets, departmentName)}
            ${canTenantAdmin() ? renderUserRoleBinding(user) : `
                <div class="journey-banner tone-readonly">
                    <div class="journey-copy">
                        <strong>Роли не раскрываются как IAM-профиль</strong>
                        <p>Полная видимость назначенных ролей не подтверждена текущими frontend-данными. Назначение роли доступно только Tenant Admin в уже существующей настройке.</p>
                    </div>
                </div>
            `}
            <section>
                <div class="panel-heading">
                    <div>
                        <p class="section-kicker">Активы</p>
                        <h3>Активы пользователя</h3>
                    </div>
                    <span class="summary-pill">${escapeHtml(String(assets.length))}</span>
                </div>
                ${renderAssetList(assets, { empty: "На пользователя не назначены активы из загруженного реестра.", source: "user", parentId: userId })}
            </section>
        </div>
    `;
}

function renderEmployeeInfoSheet(user, assets, departmentName, options = {}) {
    const employeeName = optionalAssetText(user?.name) || shortenTechnicalValue(user?.id);
    const departmentCopy = departmentName && departmentName !== "-" ? departmentName : "Подразделение не указано";
    const hintOptions = {
        hintsScope: options.hintsScope || "treeUser",
        selectedUserId: options.selectedUserId || user?.id || "",
        emptyDevicesTitle: options.emptyDevicesTitle,
        emptyDevicesCopy: options.emptyDevicesCopy
    };

    return `
        <section class="employee-info-sheet" aria-label="Информационный лист сотрудника">
            <div class="employee-info-header">
                <div>
                    <p class="section-kicker">Сотрудник</p>
                    <h3>Информационный лист сотрудника</h3>
                    <p class="employee-info-subtitle">${escapeHtml(employeeName)}</p>
                    <p class="employee-device-meta">${escapeHtml(departmentCopy)}</p>
                </div>
                <span class="employee-info-badge">Только для чтения</span>
            </div>
            <p class="employee-info-note">
                Сформировано в рабочем пространстве учёта. Этот лист доступен только внутри управляемого рабочего пространства для авторизованных ролей: Администратор, Оператор учёта, Наблюдатель / Аудитор. Это не открытый поиск, не персональный кабинет и не самостоятельный сервис сотрудника.
            </p>
            <div class="employee-info-roles" aria-label="Авторизованные роли рабочего пространства">
                <span>Администратор</span>
                <span>Оператор учёта</span>
                <span>Наблюдатель / Аудитор</span>
            </div>
            <section class="employee-devices-section" aria-label="Закреплённые устройства">
                <div class="panel-heading employee-devices-heading">
                    <div>
                        <p class="section-kicker">Устройства сотрудника</p>
                        <h4>Закреплённые устройства</h4>
                    </div>
                    <span class="summary-pill">${escapeHtml(String(assets.length))}</span>
                </div>
                ${assets.length > 0 ? renderEmployeeSheetTechnicalHintsStatus(hintOptions) : ""}
                ${renderEmployeeDeviceList(assets, hintOptions)}
            </section>
        </section>
    `;
}

function renderEmployeeDeviceList(assets, options = {}) {
    if (assets.length === 0) {
        const title = options.emptyDevicesTitle || "Устройства не закреплены";
        const copy = options.emptyDevicesCopy || "На сотрудника не закреплены устройства из загруженного реестра.";
        return `
            <div class="employee-info-empty">
                <strong>${escapeHtml(title)}</strong>
                <p>${escapeHtml(copy)}</p>
            </div>
        `;
    }

    return `
        <div class="employee-device-list">
            ${assets.map((asset) => renderEmployeeDeviceCard(asset, options)).join("")}
        </div>
    `;
}

function renderEmployeeDeviceCard(asset, options = {}) {
    return `
        <article class="employee-device-card">
            <div>
                <strong>${escapeHtml(assetDisplayTitle(asset))}</strong>
                <div class="employee-device-meta">Инв. тег: ${escapeHtml(assetCanonicalTag(asset) || "-")}</div>
            </div>
            <dl class="employee-device-fields">
                <div>
                    <dt>Тип</dt>
                    <dd>${escapeHtml(asset.asset_type || "-")}</dd>
                </div>
                <div>
                    <dt>Серийный номер</dt>
                    <dd>${escapeHtml(asset.serial_number || "-")}</dd>
                </div>
                <div>
                    <dt>Статус</dt>
                    <dd>${renderStatusBadge(asset.status)}</dd>
                </div>
                <div>
                    <dt>Локация</dt>
                    <dd>${renderLocationReference(asset.location_id)}</dd>
                </div>
            </dl>
            ${renderEmployeeDeviceTechnicalHints(asset, options)}
        </article>
    `;
}

function renderEmployeeSheetTechnicalHintsStatus(options = {}) {
    if (!isEmployeeSheetTechnicalHintScopeActive(options)) {
        return "";
    }

    if (isBusy("employeeSheetTechnicalHints")) {
        return `<p class="employee-technical-hints-status">Загружаем технические подсказки...</p>`;
    }

    if (state.employeeSheetTechnicalHintsError) {
        return `<p class="employee-technical-hints-status employee-technical-hints-error">${escapeHtml(state.employeeSheetTechnicalHintsError)}</p>`;
    }

    return "";
}

function renderEmployeeDeviceTechnicalHints(asset, options = {}) {
    const rows = getEmployeeSheetTechnicalHintRows(asset, options);
    if (rows.length === 0) {
        return "";
    }

    return `
        <dl class="employee-technical-hints" aria-label="Технические подсказки">
            ${rows.map((row) => `
                <div class="employee-technical-hint">
                    <dt>${escapeHtml(row.label)}</dt>
                    <dd>${escapeHtml(row.value)}</dd>
                </div>
            `).join("")}
        </dl>
    `;
}

function isEmployeeSheetTechnicalHintScopeActive(options = {}) {
    const scope = options.hintsScope || "treeUser";
    const selectedUserId = String(options.selectedUserId || "");
    if (!selectedUserId || state.employeeSheetTechnicalHintsUserId !== selectedUserId || state.employeeSheetTechnicalHintsScope !== scope) {
        return false;
    }

    if (scope === "standalone") {
        return state.selectedTreeNode?.type === TREE_NODE_TYPES.EMPLOYEE_SHEET
            && String(state.employeeSheetSelectedUserId || "") === selectedUserId;
    }

    return state.selectedTreeNode?.type === TREE_NODE_TYPES.USER
        && String(state.selectedTreeNode.id || "") === selectedUserId;
}

function getEmployeeSheetTechnicalHintRows(asset, options = {}) {
    if (!asset?.id || !isEmployeeSheetTechnicalHintScopeActive(options)) {
        return [];
    }

    const hints = state.employeeSheetTechnicalHintsByAssetId[String(asset.id)] || {};
    return EMPLOYEE_SHEET_TECHNICAL_HINT_FIELDS
        .map((field) => ({
            label: field.label,
            value: optionalAssetText(hints[field.key])
        }))
        .filter((row) => row.value);
}

function renderBranchDetail(branchId) {
    const branch = getBranchTreeNodes().find((item) => item.id === branchId);
    if (!branch) {
        return renderPanelEmpty("Локации", "Филиал не найден", "Данные могли измениться. Обновите рабочий контур.");
    }

    const locations = getLocationsForBranch(branchId);
    const assets = getAssetsForBranch(branchId);

    return `
        <div class="stack">
            ${renderDetailHeader("Локации", branch.name, "Physical view: Branch -> Location -> assets at location. Это не managerial org tree.")}
            <div class="asset-summary-grid">
                ${renderSummaryCard("Локации", escapeHtml(String(locations.length)), "По locations.branch_id из уже загруженных справочников.")}
                ${renderSummaryCard("Активы в локациях", escapeHtml(String(assets.length)), "Клиентская группировка по assets.location_id.")}
                ${renderSummaryCard("Служебный ID", renderTechnicalValue(branch.id, { copy: true }), "Техническое значение доступно для поддержки.")}
            </div>
            <section>
                <div class="panel-heading">
                    <div>
                        <p class="section-kicker">Locations</p>
                        <h3>Локации филиала</h3>
                    </div>
                    <span class="summary-pill">${escapeHtml(String(locations.length))}</span>
                </div>
                ${renderLocationList(locations)}
            </section>
            <section>
                <div class="panel-heading">
                    <div>
                        <p class="section-kicker">Assets at location</p>
                        <h3>Активы в локациях филиала</h3>
                    </div>
                    <span class="summary-pill">${escapeHtml(String(assets.length))}</span>
                </div>
                ${renderAssetList(assets, { empty: "В локациях филиала нет активов из загруженного реестра.", source: "branch", parentId: branchId })}
            </section>
        </div>
    `;
}

function renderLocationDetail(locationId) {
    const location = state.references.locations.find((item) => item.id === locationId);
    if (!location) {
        return renderPanelEmpty("Локации", "Локация не найдена", "Если в активе есть только ID локации, он остаётся техническим значением без новой lookup-логики.");
    }

    const assets = getAssetsForLocation(locationId);
    const branchName = resolveBranchName(location.branch_id);

    return `
        <div class="stack">
            ${renderDetailHeader("Location", location.name || shortenTechnicalValue(location.id), "Физическая локация и активы, у которых уже загружен matching location_id.")}
            <div class="asset-summary-grid">
                ${renderSummaryCard("Филиал", escapeHtml(branchName), "По branch_id локации.")}
                ${renderSummaryCard("Активы в локации", escapeHtml(String(assets.length)), "Клиентская группировка по assets.location_id.")}
                ${renderSummaryCard("Служебный ID", renderTechnicalValue(location.id, { copy: true }), "Техническое значение доступно для поддержки.")}
            </div>
            ${renderAssetList(assets, { empty: "В этой локации нет активов из загруженного реестра.", source: "location", parentId: locationId })}
        </div>
    `;
}

function renderAllAssetsGroupDetail(groupKey) {
    const group = getAllAssetGroups().find((item) => item.key === groupKey) || getAllAssetGroups()[0];
    const title = group ? group.label : "Все активы";
    const assets = group ? group.assets : [];

    return `
        <div class="stack">
            ${renderDetailHeader("Все активы", title, "Fallback-группа построена только из уже загруженного списка активов.")}
            <div class="asset-summary-grid">
                ${renderSummaryCard("Активов", escapeHtml(String(assets.length)), "Клиентская группировка для ориентации.")}
                ${renderSummaryCard("Без ответственного", escapeHtml(String(state.assets.filter((asset) => !asset.assigned_user_id).length)), "Активы без assigned_user_id остаются видимыми.")}
                ${renderSummaryCard("Без локации", escapeHtml(String(state.assets.filter((asset) => !asset.location_id).length)), "Активы без location_id не скрываются.")}
            </div>
            ${canAssetMutate() ? renderAssetCreatePanelMarkup() : ""}
            ${renderAssetList(assets, { empty: "В этой группе нет активов из загруженного реестра.", source: "all", group: group?.key || "" })}
        </div>
    `;
}

function renderSetupResourceDetail(resourceKey) {
    const config = ADMIN_RESOURCES[resourceKey] || ADMIN_RESOURCES.roles;
    state.activeAdminResource = config.key;

    if (!canTenantAdmin()) {
        return `
            <div class="stack">
                ${renderDetailHeader("Настройка", config.label, "Setup/Admin отделена от ежедневных операций с активами.")}
                <div class="journey-banner tone-readonly">
                    <div class="journey-copy">
                        <strong>Настройка доступна Tenant Admin</strong>
                        <p>Текущий UI-профиль не открывает изменение справочников. Новая модель доступа не добавляется.</p>
                    </div>
                </div>
            </div>
        `;
    }

    return `
        <div class="stack">
            ${renderDetailHeader("Настройка", config.label, "Роли, пользователи, подразделения, филиалы и локации остаются отдельной setup-зоной.")}
            <div class="context-feedback-slot" data-context-feedback-slot="${FEEDBACK_SCOPES.SETUP}" data-resource-key="${escapeAttribute(config.key)}">
                ${renderContextualFeedback(FEEDBACK_SCOPES.SETUP, { resourceKey: config.key })}
            </div>
            ${renderSetupOverviewMarkup()}
            ${renderAdminResourceNavMarkup()}
            <div class="admin-grid">
                ${renderAdminTableMarkup(config)}
                ${renderAdminEditorMarkup(config)}
            </div>
        </div>
    `;
}

function renderAssetDetailPanel() {
    if (state.loading.assetDetail && !state.selectedAsset) {
        return renderPanelEmpty("Карточка", "Загружаем карточку", "Как только данные актива будут доступны, UI откроет карточку и вкладки.");
    }

    if (!state.selectedAsset) {
        return renderPanelEmpty("Карточка", "Актив не выбран", "Откройте актив из дерева Оргструктура, Локации или Все активы.");
    }

    const asset = state.selectedAsset;
    const canMutate = canAssetMutate();
    const hasUsers = state.references.users.length > 0;
    const hasLocations = state.references.locations.length > 0;
    const hasCurrentUser = hasCurrentUserResponsibility(asset);

    if (!ASSET_SECTIONS.some((section) => section.key === state.activeAssetSection)) {
        state.activeAssetSection = "overview";
    }

    return `
        <div class="stack">
            <div class="panel-heading">
                <div class="asset-card-header">
                    <p class="section-kicker">Asset detail</p>
                    <div class="asset-card-title">
                        <h2>${escapeHtml(assetDisplayTitle(asset))}</h2>
                        ${renderStatusBadge(asset.status)}
                    </div>
                    <div class="asset-card-meta">
                        <span class="reference-meta">Инв. тег: ${escapeHtml(assetCanonicalTag(asset) || "-")}</span>
                        <span class="reference-meta">SN: ${escapeHtml(asset.serial_number || "-")}</span>
                        <span class="reference-meta">Тип: ${escapeHtml(asset.asset_type || "-")}</span>
                        ${renderTechnicalDisclosure("ID актива", asset.id, { label: "ID актива" })}
                    </div>
                </div>
                <button class="button button-secondary ${isBusy("refreshAsset") ? "is-loading" : ""}" data-refresh-asset type="button" ${isBusy("refreshAsset") ? "disabled" : ""}>
                    ${isBusy("refreshAsset") ? "Обновляем" : "Обновить карточку"}
                </button>
            </div>
            <div class="context-feedback-slot" data-context-feedback-slot="${FEEDBACK_SCOPES.ASSET}" data-asset-id="${escapeAttribute(asset.id || "")}">
                ${renderContextualFeedback(FEEDBACK_SCOPES.ASSET, { assetId: asset.id })}
            </div>
            ${renderAssetSectionNav()}
            <div class="asset-card">
                ${renderAssetSectionContent(asset, canMutate, hasUsers, hasLocations, hasCurrentUser)}
            </div>
        </div>
    `;
}

function renderDetailHeader(kicker, title, copy) {
    return `
        <div class="screen-header detail-header">
            <div>
                <p class="section-kicker">${escapeHtml(kicker)}</p>
                <h2>${escapeHtml(title)}</h2>
                <p class="screen-copy">${escapeHtml(copy)}</p>
            </div>
        </div>
    `;
}

function renderUserList(users) {
    if (users.length === 0) {
        return renderInlineEmpty("Пользователи не найдены", "Нет пользователей в этой ветке дерева.");
    }

    return `
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Пользователь</th>
                        <th>Подразделение</th>
                        <th>Активы</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    ${users.map((user) => `
                        <tr>
                            <td>${escapeHtml(user.name || shortenTechnicalValue(user.id))}</td>
                            <td>${escapeHtml(resolveDepartmentName(user.department_id))}</td>
                            <td>${escapeHtml(String(getAssetsForUser(user.id).length))}</td>
                            <td><button class="button button-ghost" type="button" data-node-type="${TREE_NODE_TYPES.USER}" data-user-id="${escapeAttribute(user.id)}">Открыть</button></td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderLocationList(locations) {
    if (locations.length === 0) {
        return renderInlineEmpty("Локации не найдены", "В этой ветке нет локаций из загруженных справочников.");
    }

    return `
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Локация</th>
                        <th>Филиал</th>
                        <th>Активы</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    ${locations.map((location) => `
                        <tr>
                            <td>${escapeHtml(location.name || shortenTechnicalValue(location.id))}</td>
                            <td>${escapeHtml(resolveBranchName(location.branch_id))}</td>
                            <td>${escapeHtml(String(getAssetsForLocation(location.id).length))}</td>
                            <td><button class="button button-ghost" type="button" data-node-type="${TREE_NODE_TYPES.LOCATION}" data-location-id="${escapeAttribute(location.id)}">Открыть</button></td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderAssetList(assets, options = {}) {
    if (assets.length === 0) {
        return renderInlineEmpty("Активы не найдены", options.empty || "В этой ветке нет активов из загруженного реестра.");
    }

    return `
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Актив</th>
                        <th>Тип</th>
                        <th>Статус</th>
                        <th>Ответственный</th>
                        <th>Локация</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    ${assets.map((asset) => `
                        <tr class="${asset.id === state.selectedAssetId ? "is-active" : ""}">
                            <td>${renderAssetIdentity(asset)}</td>
                            <td>${escapeHtml(asset.asset_type || "-")}</td>
                            <td>${renderStatusBadge(asset.status)}</td>
                            <td>${renderUserReference(asset.assigned_user_id)}</td>
                            <td>${renderLocationReference(asset.location_id)}</td>
                            <td>
                                <button
                                    class="button button-secondary"
                                    type="button"
                                    data-node-type="${TREE_NODE_TYPES.ASSET}"
                                    data-asset-id="${escapeAttribute(asset.id)}"
                                    data-source="${escapeAttribute(options.source || "")}"
                                    data-parent-id="${escapeAttribute(options.parentId || "")}"
                                    data-group="${escapeAttribute(options.group || "")}"
                                >Открыть карточку</button>
                            </td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderAssetCreatePanelMarkup() {
    return `
        <section id="asset-create-panel" class="panel panel-tight">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Create</p>
                    <h3>Создать актив</h3>
                </div>
            </div>
            <form id="asset-create-form" class="form-grid">
                <div class="form-section-label">Основные поля</div>
                <label class="field">
                    <span>Название актива</span>
                    <input name="display_name" type="text" autocomplete="off" maxlength="160">
                    <small class="reference-meta">Необязательное название для узнавания в интерфейсе.</small>
                </label>
                <label class="field">
                    <span>Инвентарный тег</span>
                    <input name="asset_tag" type="text" autocomplete="off" required>
                </label>
                <label class="field">
                    <span>Серийный номер</span>
                    <input name="serial_number" type="text" autocomplete="off" required>
                </label>
                <label class="field">
                    <span>Тип актива</span>
                    <input name="asset_type" type="text" autocomplete="off" required>
                </label>
                <label class="field field-wide">
                    <span>Комментарий</span>
                    <textarea name="note" maxlength="2000" placeholder="Необязательный рабочий комментарий"></textarea>
                    <small class="reference-meta">Обычный текст для контекста. Не история, не вложение и не поток комментариев.</small>
                </label>
                <div class="button-row button-row-inline">
                    <button class="button button-primary ${isBusy("createAsset") ? "is-loading" : ""}" type="submit" ${isBusy("createAsset") ? "disabled" : ""}>
                        ${isBusy("createAsset") ? "Создаём" : "Создать актив"}
                    </button>
                </div>
            </form>
            <details class="form-support-disclosure">
                <summary>Что произойдёт после создания</summary>
                <p>Создание заполняет только master-record. Ответственность задаётся отдельным assign после открытия карточки.</p>
            </details>
        </section>
    `;
}

function renderSetupOverviewMarkup() {
    const steps = getSetupStepStatuses();
    const completed = steps.filter((step) => step.count > 0).length;
    const currentStep = steps.find((step) => step.status === "current");
    return `
        <section class="setup-order-panel panel panel-tight">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Порядок настройки</p>
                    <h3>Короткая подсказка по порядку</h3>
                </div>
                <span class="summary-pill">${completed}/${SETUP_SEQUENCE.length} заполнено</span>
            </div>
            <p class="muted-note">
                Разделы открываются через вкладки ниже или через пункт «Настройка» в дереве. Подсказка показывает только рекомендуемую последовательность.
            </p>
            <details class="setup-order-disclosure">
                <summary>
                    <span>${escapeHtml(currentStep ? `Следующий шаг: ${currentStep.label}` : "Базовые разделы заполнены")}</span>
                    <span class="summary-pill">порядок</span>
                </summary>
                <ol class="setup-order-list">
                    ${steps.map((step) => `
                        <li class="${step.status}">
                            <span><strong>${escapeHtml(step.index)}. ${escapeHtml(step.label)}</strong> — ${escapeHtml(step.description)}</span>
                            <span class="setup-order-count">${escapeHtml(formatRecordCount(step.count))}</span>
                        </li>
                    `).join("")}
                </ol>
            </details>
        </section>
    `;
}

function renderAdminResourceNavMarkup() {
    return `
        <nav class="admin-nav" aria-label="Разделы настройки">
            ${Object.values(ADMIN_RESOURCES).map((resource) => `
                <button
                    class="nav-chip ${resource.key === state.activeAdminResource ? "is-active" : ""}"
                    type="button"
                    data-resource-key="${resource.key}"
                >
                    ${escapeHtml(resource.label)}
                </button>
            `).join("")}
        </nav>
    `;
}

function renderAdminTableMarkup(config) {
    const items = getAdminItems(config.key);
    const loading = (state.loading.references || state.loading.roles) && items.length === 0;
    const meta = ADMIN_RESOURCE_META[config.key];
    const emptyMessage = [meta.empty, meta.prerequisite].filter(Boolean).join(" ") || config.emptyText;
    const body = loading
        ? `<tr><td colspan="${config.columns.length}" class="empty-cell">Загружаем ${escapeHtml(config.label.toLowerCase())}...</td></tr>`
        : items.length === 0
            ? `<tr><td colspan="${config.columns.length}" class="empty-cell">${escapeHtml(emptyMessage)}</td></tr>`
            : items.map((item) => `
                <tr
                    class="${item.id === state.selectedAdminItemId ? "is-active" : ""}"
                    data-admin-id="${escapeHtml(item.id)}"
                    aria-selected="${item.id === state.selectedAdminItemId ? "true" : "false"}"
                    title="Нажмите, чтобы открыть запись в редакторе"
                >
                    ${config.columns.map((column) => `<td>${column.render(item)}</td>`).join("")}
                </tr>
            `).join("");

    return `
        <section class="panel">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Текущий раздел</p>
                    <h3>${escapeHtml(config.label)}</h3>
                </div>
                <button class="button button-secondary" id="refresh-admin-resource" type="button">Обновить список</button>
            </div>
            <p class="table-helper">Нажмите строку, чтобы открыть редактирование. Служебный ID доступен в редакторе выбранной записи.</p>
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                        <tr>${config.columns.map((column) => `<th>${escapeHtml(column.label)}</th>`).join("")}</tr>
                    </thead>
                    <tbody>${body}</tbody>
                </table>
            </div>
        </section>
    `;
}

function renderAdminEditorMarkup(config) {
    const selectedItem = getAdminItems(config.key).find((item) => item.id === state.selectedAdminItemId) || null;
    const meta = ADMIN_RESOURCE_META[config.key];
    const canSubmit = canSubmitAdminResource(config.key);
    const resourceBusy = isBusy("adminResource");
    const prerequisiteMessage = getAdminPrerequisiteMessage(config.key);
    const heading = selectedItem ? `Редактировать: ${config.label}` : `Создать: ${config.label}`;
    const resetLabel = selectedItem ? "Новая запись" : "Очистить форму";

    return `
        <section class="panel">
            <div class="stack">
                <div class="panel-heading">
                    <div>
                        <p class="section-kicker">Редактор</p>
                        <h3>${escapeHtml(heading)}</h3>
                    </div>
                    <button class="button button-ghost" type="button" data-admin-reset>${escapeHtml(resetLabel)}</button>
                </div>
                <details class="form-support-disclosure">
                    <summary>Подсказка по справочнику</summary>
                    <p>${escapeHtml(meta.helper)}</p>
                </details>
                ${prerequisiteMessage ? `<div class="journey-banner tone-warning"><div class="journey-copy"><strong>Проверьте условие</strong><p>${escapeHtml(prerequisiteMessage)}</p></div></div>` : ""}
                <form id="admin-resource-form" class="form-grid">
                    <div class="form-section-label">Поля записи</div>
                    ${config.fields.map((field) => renderAdminField(field, selectedItem)).join("")}
                    <div class="button-row button-row-inline">
                        <button class="button button-primary ${resourceBusy ? "is-loading" : ""}" type="submit" ${canSubmit && !resourceBusy ? "" : "disabled"}>
                            ${resourceBusy ? "Сохраняем" : selectedItem ? "Сохранить изменения" : "Создать запись"}
                        </button>
                    </div>
                </form>
                ${renderAdminSelectedTechnicalDetails(selectedItem)}
                ${config.key === "users" ? renderUserRoleBinding(selectedItem) : ""}
            </div>
        </section>
    `;
}

function renderAdminSelectedTechnicalDetails(selectedItem) {
    if (!selectedItem?.id) {
        return "";
    }

    return `
        <details class="technical-disclosure-panel admin-record-technical">
            <summary>Служебный ID выбранной записи</summary>
            ${renderTechnicalValue(selectedItem.id, { copy: true, label: "служебный ID записи" })}
        </details>
    `;
}

function getSelectedTreeNodeLabel() {
    const node = state.selectedTreeNode || { type: TREE_NODE_TYPES.WORKSPACE };
    if (node.type === TREE_NODE_TYPES.EMPLOYEE_SHEET) {
        return "Информационный лист";
    }
    if (node.type === TREE_NODE_TYPES.DEPARTMENT) {
        return getDepartmentTreeNodes().find((item) => item.id === node.id)?.name || "Подразделение";
    }
    if (node.type === TREE_NODE_TYPES.USER) {
        return state.references.users.find((item) => item.id === node.id)?.name || "Пользователь";
    }
    if (node.type === TREE_NODE_TYPES.BRANCH) {
        return getBranchTreeNodes().find((item) => item.id === node.id)?.name || "Филиал";
    }
    if (node.type === TREE_NODE_TYPES.LOCATION) {
        return state.references.locations.find((item) => item.id === node.id)?.name || "Локация";
    }
    if (node.type === TREE_NODE_TYPES.ASSET) {
        const asset = state.selectedAsset?.id === node.id
            ? state.selectedAsset
            : state.assets.find((item) => item.id === node.id);
        return asset ? assetDisplayTitle(asset) : "Актив";
    }
    if (node.type === TREE_NODE_TYPES.ASSET_GROUP) {
        return getAllAssetGroups().find((item) => item.key === node.group)?.label || "Все активы";
    }
    if (node.type === TREE_NODE_TYPES.SETUP) {
        return ADMIN_RESOURCES[node.resource]?.label || "Настройка";
    }
    return "Рабочее место";
}

function getSelectedTreePath() {
    const node = state.selectedTreeNode || { type: TREE_NODE_TYPES.WORKSPACE };
    if (node.type === TREE_NODE_TYPES.WORKSPACE) {
        return [TREE_GROUPS.WORKSPACE];
    }
    if (node.type === TREE_NODE_TYPES.EMPLOYEE_SHEET) {
        return [TREE_GROUPS.EMPLOYEE_SHEET];
    }
    if (node.type === TREE_NODE_TYPES.DEPARTMENT) {
        return [TREE_GROUPS.ORG, getDepartmentTreeKey(node.id)];
    }
    if (node.type === TREE_NODE_TYPES.USER) {
        const user = state.references.users.find((item) => item.id === node.id);
        const departmentId = getDepartmentIdForUser(user);
        return [TREE_GROUPS.ORG, getDepartmentTreeKey(departmentId), getUserTreeKey(node.id)];
    }
    if (node.type === TREE_NODE_TYPES.BRANCH) {
        return [TREE_GROUPS.LOCATIONS, getBranchTreeKey(node.id)];
    }
    if (node.type === TREE_NODE_TYPES.LOCATION) {
        const location = state.references.locations.find((item) => item.id === node.id);
        const branchId = getBranchIdForLocation(location);
        return [TREE_GROUPS.LOCATIONS, getBranchTreeKey(branchId), getLocationTreeKey(node.id)];
    }
    if (node.type === TREE_NODE_TYPES.ASSET) {
        if (node.source === "user") {
            const user = state.references.users.find((item) => item.id === node.parentId);
            const departmentId = getDepartmentIdForUser(user);
            return [TREE_GROUPS.ORG, getDepartmentTreeKey(departmentId), getUserTreeKey(node.parentId)];
        }
        if (node.source === "department") {
            return [TREE_GROUPS.ORG, getDepartmentTreeKey(node.parentId)];
        }
        if (node.source === "location") {
            const location = state.references.locations.find((item) => item.id === node.parentId);
            const branchId = getBranchIdForLocation(location);
            return [TREE_GROUPS.LOCATIONS, getBranchTreeKey(branchId), getLocationTreeKey(node.parentId)];
        }
        if (node.source === "branch") {
            return [TREE_GROUPS.LOCATIONS, getBranchTreeKey(node.parentId)];
        }
        return [TREE_GROUPS.ALL_ASSETS];
    }
    if (node.type === TREE_NODE_TYPES.ASSET_GROUP) {
        return [TREE_GROUPS.ALL_ASSETS];
    }
    if (node.type === TREE_NODE_TYPES.SETUP) {
        return [TREE_GROUPS.SETUP];
    }
    return [TREE_GROUPS.WORKSPACE];
}

function isTreeExpanded(key, selectedPath) {
    return state.expandedTreeNodes.has(key) || selectedPath.includes(key);
}

function getDepartmentTreeNodes() {
    const nodes = state.references.departments.map((department) => ({
        id: department.id,
        name: department.name || shortenTechnicalValue(department.id)
    }));
    const knownIds = new Set(nodes.map((department) => department.id));
    const needsFallback = state.references.users.some((user) => !user.department_id || !knownIds.has(user.department_id));
    if (needsFallback) {
        nodes.push({ id: FALLBACK_DEPARTMENT_ID, name: "Без подразделения" });
    }
    return nodes;
}

function getDepartmentIdForUser(user) {
    if (!user || !user.department_id) {
        return FALLBACK_DEPARTMENT_ID;
    }
    const known = state.references.departments.some((department) => department.id === user.department_id);
    return known ? user.department_id : FALLBACK_DEPARTMENT_ID;
}

function getUsersForDepartment(departmentId) {
    return state.references.users.filter((user) => getDepartmentIdForUser(user) === departmentId);
}

function getAssetsForDepartment(departmentId) {
    const userIds = new Set(getUsersForDepartment(departmentId).map((user) => user.id));
    return state.assets.filter((asset) => userIds.has(asset.assigned_user_id));
}

function getAssetsForUser(userId) {
    return state.assets.filter((asset) => asset.assigned_user_id === userId);
}

function getDepartmentTreeKey(departmentId) {
    return `department:${departmentId || FALLBACK_DEPARTMENT_ID}`;
}

function getUserTreeKey(userId) {
    return `user:${userId || ""}`;
}

function getBranchTreeNodes() {
    const nodes = state.references.branches.map((branch) => ({
        id: branch.id,
        name: branch.name || shortenTechnicalValue(branch.id)
    }));
    const knownIds = new Set(nodes.map((branch) => branch.id));
    const needsFallback = state.references.locations.some((location) => !location.branch_id || !knownIds.has(location.branch_id));
    if (needsFallback) {
        nodes.push({ id: FALLBACK_BRANCH_ID, name: "Без филиала" });
    }
    return nodes;
}

function getBranchIdForLocation(location) {
    if (!location || !location.branch_id) {
        return FALLBACK_BRANCH_ID;
    }
    const known = state.references.branches.some((branch) => branch.id === location.branch_id);
    return known ? location.branch_id : FALLBACK_BRANCH_ID;
}

function getLocationsForBranch(branchId) {
    return state.references.locations.filter((location) => getBranchIdForLocation(location) === branchId);
}

function getAssetsForBranch(branchId) {
    const locationIds = new Set(getLocationsForBranch(branchId).map((location) => location.id));
    return state.assets.filter((asset) => locationIds.has(asset.location_id));
}

function getAssetsForLocation(locationId) {
    return state.assets.filter((asset) => asset.location_id === locationId);
}

function getBranchTreeKey(branchId) {
    return `branch:${branchId || FALLBACK_BRANCH_ID}`;
}

function getLocationTreeKey(locationId) {
    return `location:${locationId || ""}`;
}

function getAllAssetGroups() {
    const groups = [
        {
            key: ASSET_GROUPS.ASSIGNED,
            label: "Назначенные",
            assets: state.assets.filter((asset) => Boolean(asset.assigned_user_id))
        },
        {
            key: ASSET_GROUPS.UNASSIGNED,
            label: "Неназначенные",
            assets: state.assets.filter((asset) => !asset.assigned_user_id)
        }
    ];

    if (hasRetiredStatusSupport()) {
        groups.push({
            key: ASSET_GROUPS.RETIRED,
            label: "Retired / списанные",
            assets: state.assets.filter((asset) => String(asset.status || "").toUpperCase() === "RETIRED")
        });
    }

    return groups;
}

function hasRetiredStatusSupport() {
    return state.assets.some((asset) => Object.prototype.hasOwnProperty.call(asset, "status"));
}

function renderUserServiceUrlPreview() {
    if (!ui.userServiceUrlPreview) {
        return;
    }

    const value = (ui.userServiceBaseUrl?.value || state.context.userServiceBaseUrl || "").trim();
    if (!value) {
        ui.userServiceUrlPreview.innerHTML = "Адрес будет показан здесь после ввода.";
        return;
    }

    ui.userServiceUrlPreview.innerHTML = `
        Сервис пользователей: ${renderTechnicalValue(value, { copy: true, label: "адрес сервиса пользователей" })}
    `;
}

function renderContextTechnicalSummary() {
    if (!ui.contextTechnicalSummary) {
        return;
    }

    if (!hasConnectionContext()) {
        ui.contextTechnicalSummary.innerHTML = "";
        return;
    }

    ui.contextTechnicalSummary.className = "context-summary";
    ui.contextTechnicalSummary.innerHTML = `
        <strong>Контекст:</strong>
        tenant ${renderTechnicalValue(state.context.tenantId)}
        user ${renderTechnicalValue(state.context.userId)}
    `;
}

function renderScreenNav() {
    const screens = [{ key: PRIMARY_SCREENS.WORKSPACE, label: "Рабочее место" }];
    if (canAssetRead()) {
        screens.push({ key: PRIMARY_SCREENS.ASSETS, label: "Активы" });
        screens.push({ key: PRIMARY_SCREENS.REVIEW, label: "История и аудит" });
    }
    if (canTenantAdmin()) {
        screens.push({ key: PRIMARY_SCREENS.SETUP, label: "Настройка" });
    }

    if (!screens.some((screen) => screen.key === state.activeScreen)) {
        state.activeScreen = PRIMARY_SCREENS.WORKSPACE;
    }

    ui.screenNav.innerHTML = screens.map((screen) => `
            <button
                class="nav-chip ${screen.key === state.activeScreen ? "is-active" : ""}"
                type="button"
                data-screen="${screen.key}"
                aria-controls="${escapeAttribute(SCREEN_ANCHORS[screen.key])}"
                aria-current="${screen.key === state.activeScreen ? "page" : "false"}"
            >
                ${escapeHtml(screen.label)}
            </button>
        `).join("");
}

function renderWorkspaceScreen() {
    ui.workspaceScreen.hidden = state.activeScreen !== PRIMARY_SCREENS.WORKSPACE;
    renderWorkspaceOverview();
}

function renderWorkspaceOverview() {
    if (!hasConnectionContext()) {
        ui.workspaceOverview.innerHTML = renderPanelEmpty(
            "Workspace",
            "Подключите контекст доступа",
            "Сначала заполните tenant, пользователя, actor и адрес сервиса пользователей. После этого UI определит доступные разделы без расширения доступа."
        );
        return;
    }

    if (state.loading.profile && !hasResolvedProfile()) {
        ui.workspaceOverview.innerHTML = renderPanelEmpty(
            "Workspace",
            "Проверяем роль и доступные поверхности",
            "UI определяет только уже доступные возможности просмотра и изменения."
        );
        return;
    }

    const guidance = getProfileGuidance();
    const workAreas = getWorkspaceAreas();

    ui.workspaceOverview.innerHTML = `
        <div class="stack">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Workspace</p>
                    <h3>Ориентация по рабочим зонам</h3>
                </div>
                <span class="summary-pill">${escapeHtml(state.profileLabel || UI_PROFILE_NAMES.NO_APPROVED_ROLE)}</span>
            </div>

            <div class="workspace-path-grid">
                ${workAreas.map(renderWorkspaceAreaCard).join("")}
            </div>

            <div class="journey-banner tone-info">
                <div class="journey-copy">
                    <strong>${escapeHtml(guidance.bannerTitle)}</strong>
                    <p>${escapeHtml(guidance.bannerBody)}</p>
                </div>
                <div class="button-row">
                    ${canAssetRead() ? `<button class="button button-secondary" type="button" data-screen="${PRIMARY_SCREENS.ASSETS}">Открыть активы</button>` : ""}
                    ${canAssetRead() ? `<button class="button button-secondary" type="button" data-screen="${PRIMARY_SCREENS.REVIEW}">Открыть историю и аудит</button>` : ""}
                    ${canTenantAdmin() ? `<button class="button button-secondary" type="button" data-screen="${PRIMARY_SCREENS.SETUP}">Открыть настройку</button>` : ""}
                </div>
            </div>
        </div>
    `;
}

function renderAssetsScreen() {
    const visible = canAssetRead() && state.activeScreen === PRIMARY_SCREENS.ASSETS;
    ui.assetsScreen.hidden = !visible;
    ui.assetCreatePanel.hidden = !canAssetMutate();

    renderAssetJourneyBanner();
    renderAssetRegistryOverview();
    renderAssetRegistry();
    renderAssetDetail();
}

function renderAssetJourneyBanner() {
    if (!canAssetRead()) {
        ui.assetJourneyBanner.innerHTML = "";
        return;
    }

    if (state.loading.assets && state.assets.length === 0) {
        ui.assetJourneyBanner.innerHTML = renderJourneyBanner(
            "tone-info",
            "Загружаем реестр активов",
            "Как только данные реестра будут доступны, UI восстановит переход из реестра в карточку.",
            ""
        );
        return;
    }

    if (canAssetMutate()) {
        ui.assetJourneyBanner.innerHTML = renderJourneyBanner(
            "tone-info",
            "Операционный путь: реестр -> карточка -> отдельное действие",
            "Статус ACTIVE / RETIRED показывается отдельно от ответственного пользователя и локации. Редактирование карточки меняет только данные актива.",
            `
                <button class="button button-secondary" type="button" data-focus-registry>К реестру</button>
                <button class="button button-ghost" type="button" data-focus-create">К созданию</button>
                ${state.selectedAsset ? `<button class="button button-ghost" type="button" data-screen="${PRIMARY_SCREENS.REVIEW}">К истории и аудиту</button>` : ""}
            `
        );
        return;
    }

    ui.assetJourneyBanner.innerHTML = renderJourneyBanner(
        "tone-readonly",
        "Просмотр без изменений",
        "Support Reviewer видит реестр, карточку, историю и аудит без действий изменения.",
        state.selectedAsset ? `<button class="button button-secondary" type="button" data-screen="${PRIMARY_SCREENS.REVIEW}">К истории и аудиту</button>` : ""
    );
}

function renderAssetRegistryOverview() {
    if (!canAssetRead()) {
        ui.assetRegistryOverview.innerHTML = "";
        return;
    }

    const total = state.assets.length;
    const active = state.assets.filter((asset) => String(asset.status || "").toUpperCase() === "ACTIVE").length;
    const retired = state.assets.filter((asset) => String(asset.status || "").toUpperCase() === "RETIRED").length;
    const selectedLabel = state.selectedAsset ? assetDisplayTitle(state.selectedAsset) : "Актив не выбран";
    const selectedState = state.selectedAsset
        ? getSelectedAssetRegistrySummary(state.selectedAsset)
        : "Откройте карточку из реестра";

    ui.assetRegistryOverview.innerHTML = `
        <div class="stack">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Реестр</p>
                    <h3>Рабочая выборка активов</h3>
                </div>
                <span class="summary-pill">${escapeHtml(canAssetMutate() ? "Можно изменять" : "Только просмотр")}</span>
            </div>

            <div class="overview-grid">
                ${renderMetricCard("Активов в реестре", String(total), total === 0 ? "В текущем tenant пока нет записей." : "Загружено из существующего реестра активов.")}
                ${renderMetricCard("ACTIVE", String(active), "Статус отражается отдельно от ответственности и локации.")}
                ${renderMetricCard("RETIRED", String(retired), "Retired не является состоянием ответственности.")}
                ${renderMetricCard("Выбранный актив", selectedLabel, selectedState)}
            </div>

            <div class="button-row">
                ${canAssetMutate() ? `<button class="button button-secondary" type="button" data-focus-create">Создать актив</button>` : ""}
                ${state.selectedAsset ? `<button class="button button-ghost" type="button" data-focus-card">К карточке</button>` : ""}
                ${state.selectedAsset ? `<button class="button button-ghost" type="button" data-screen="${PRIMARY_SCREENS.REVIEW}">К истории и аудиту</button>` : ""}
            </div>
        </div>
    `;
}

function renderAssetRegistry() {
    if (!hasConnectionContext()) {
        ui.assetRegistryBody.innerHTML = `<tr><td colspan="5" class="empty-cell">Подключите рабочий контур, чтобы загрузить активы.</td></tr>`;
        return;
    }

    if (!canAssetRead()) {
        ui.assetRegistryBody.innerHTML = `<tr><td colspan="5" class="empty-cell">Роль не имеет доступа к реестру активов.</td></tr>`;
        return;
    }

    if (state.loading.assets && state.assets.length === 0) {
        ui.assetRegistryBody.innerHTML = `<tr><td colspan="5" class="empty-cell">Загружаем реестр активов...</td></tr>`;
        return;
    }

    if (state.assets.length === 0) {
        ui.assetRegistryBody.innerHTML = `
            <tr>
                <td colspan="5" class="empty-cell">
                    ${escapeHtml(canAssetMutate()
                        ? "В текущем tenant активы ещё не созданы. Начните с формы создания и затем откройте карточку."
                        : "В текущем tenant активы не найдены.")}
                </td>
            </tr>
        `;
        return;
    }

    ui.assetRegistryBody.innerHTML = state.assets.map((asset) => `
        <tr class="${asset.id === state.selectedAssetId ? "is-active" : ""}" data-asset-id="${escapeHtml(asset.id)}">
            <td>${renderAssetIdentity(asset)}</td>
            <td>${escapeHtml(asset.asset_type)}</td>
            <td>${renderStatusBadge(asset.status)}</td>
            <td>${renderUserReference(asset.assigned_user_id)}</td>
            <td>${renderLocationReference(asset.location_id)}</td>
        </tr>
    `).join("");
}

function renderAssetDetail() {
    if (state.loading.assetDetail && !state.selectedAsset) {
        ui.assetDetailPanel.innerHTML = renderPanelEmpty(
            "Карточка",
            "Загружаем карточку",
            "Как только данные актива будут доступны, UI откроет карточку, историю и аудит."
        );
        return;
    }

    if (!state.selectedAsset) {
        ui.assetDetailPanel.innerHTML = `
            <div class="empty-state">
                <p class="empty-kicker">Карточка</p>
                <h3>Карточка ещё не выбрана</h3>
                <p>Выберите актив из реестра, чтобы открыть карточку, историю и аудит.</p>
                <div class="button-row">
                    ${canAssetMutate() ? `<button class="button button-secondary" type="button" data-focus-create">Создать актив</button>` : ""}
                    <button class="button button-ghost" type="button" data-focus-registry>К реестру</button>
                </div>
            </div>
        `;
        return;
    }

    const asset = state.selectedAsset;
    const canMutate = canAssetMutate();
    const hasUsers = state.references.users.length > 0;
    const hasLocations = state.references.locations.length > 0;
    const hasCurrentUser = hasCurrentUserResponsibility(asset);

    if (!ASSET_SECTIONS.some((section) => section.key === state.activeAssetSection)) {
        state.activeAssetSection = "overview";
    }

    ui.assetDetailPanel.innerHTML = `
        <div class="stack">
            <div class="panel-heading">
                <div class="asset-card-header">
                    <p class="section-kicker">Карточка актива</p>
                    <div class="asset-card-title">
                        <h3>${escapeHtml(assetDisplayTitle(asset))}</h3>
                        ${renderStatusBadge(asset.status)}
                    </div>
                    <div class="asset-card-meta">
                        <span class="reference-meta">Инв. тег: ${escapeHtml(assetCanonicalTag(asset) || "-")}</span>
                        <span class="reference-meta">SN: ${escapeHtml(asset.serial_number || "-")}</span>
                        <span class="reference-meta">Тип: ${escapeHtml(asset.asset_type || "-")}</span>
                        ${renderTechnicalDisclosure("ID актива", asset.id, { label: "ID актива" })}
                    </div>
                </div>
                <div class="button-row">
                    <button class="button button-secondary ${isBusy("refreshAsset") ? "is-loading" : ""}" data-refresh-asset type="button" ${isBusy("refreshAsset") ? "disabled" : ""}>${isBusy("refreshAsset") ? "Обновляем" : "Обновить карточку"}</button>
                    <button class="button button-ghost" data-focus-registry type="button">К реестру</button>
                    <button class="button button-ghost" data-screen="${PRIMARY_SCREENS.REVIEW}" type="button">К истории и аудиту</button>
                    ${canMutate ? `<button class="button button-ghost" data-focus-create type="button">К созданию</button>` : ""}
                </div>
            </div>

            ${renderAssetSectionNav()}

            <div class="asset-card">
                ${renderAssetSectionContent(asset, canMutate, hasUsers, hasLocations, hasCurrentUser)}
            </div>
        </div>
    `;
}

function renderAssetSectionNav() {
    return `
        <nav class="local-tabs" aria-label="Разделы карточки актива">
            ${ASSET_SECTIONS.map((section) => `
                <button
                    class="local-tab ${section.key === state.activeAssetSection ? "is-active" : ""}"
                    type="button"
                    data-asset-section="${section.key}"
                >
                    ${escapeHtml(section.label)}
                </button>
            `).join("")}
        </nav>
    `;
}

function renderAssetSectionContent(asset, canMutate, hasUsers, hasLocations, hasCurrentUser) {
    if (state.activeAssetSection === "responsibility") {
        return renderAssetResponsibilitySection(asset, hasCurrentUser);
    }

    if (state.activeAssetSection === "actions") {
        return renderAssetActionsSection(asset, canMutate, hasUsers, hasLocations, hasCurrentUser);
    }

    if (state.activeAssetSection === "history") {
        return renderAssetHistoryAuditSection();
    }

    if (state.activeAssetSection === "technical") {
        return renderAssetTechnicalSection(asset);
    }

    return renderAssetOverviewSection(asset, canMutate);
}

function renderAssetOverviewSection(asset, canMutate) {
    const displayName = assetDisplayTitle(asset);
    return `
        <section class="asset-section">
            <div class="journey-banner tone-info">
                <div class="journey-copy">
                    <strong>Статус и ответственность разделены</strong>
                    <p>ACTIVE / RETIRED — это статус актива. Ответственный пользователь и рабочая локация показаны отдельно и меняются отдельными действиями.</p>
                </div>
            </div>

            ${renderCurrentStateStrip(asset)}

            <div class="asset-summary-grid">
                ${renderSummaryCard("Инвентарный тег", escapeHtml(assetCanonicalTag(asset) || "-"), "Главный пользовательский идентификатор в реестре.")}
                ${renderSummaryCard("Название актива", escapeHtml(displayName), "Отображаемое название для узнавания. Инвентарный тег остаётся каноническим идентификатором.")}
                ${renderSummaryCard("Статус", renderStatusBadge(asset.status), "Не кодирует текущую ответственность.")}
                ${renderSummaryCard("Тип", escapeHtml(asset.asset_type), "Поле карточки актива.")}
                ${renderSummaryCard("Серийный номер", escapeHtml(asset.serial_number), "Поле карточки актива.")}
                ${renderSummaryCard("Ответственный", renderUserReference(asset.assigned_user_id), "Текущая пользовательская ответственность.")}
                ${renderSummaryCard("Локация", renderLocationReference(asset.location_id), "Текущая рабочая локация.")}
            </div>

            ${renderAssetNoteBlock(asset)}

            <div class="button-row">
                ${canMutate ? `<button class="button button-secondary" type="button" data-asset-section="actions">Открыть действия</button>` : ""}
                <button class="button button-ghost" type="button" data-asset-section="history">История и аудит</button>
                <button class="button button-ghost" type="button" data-asset-section="technical">Technical details</button>
            </div>
        </section>
    `;
}

function renderAssetNoteBlock(asset) {
    const note = optionalAssetText(asset?.note);
    return `
        <article class="asset-note-block ${note ? "" : "asset-note-empty"}">
            <p class="metric-label">Комментарий</p>
            ${note
                ? `<p class="asset-note-text">${escapeHtml(note)}</p>`
                : `<p class="reference-empty">Комментарий не добавлен</p>`}
        </article>
    `;
}

function renderAssetMasterSection(asset, canMutate) {
    if (!canMutate) {
        return `
            <section class="asset-section">
                <div class="asset-summary-grid">
                    ${renderSummaryCard("Инвентарный тег", escapeHtml(asset.asset_tag), "Только просмотр для текущей роли.")}
                    ${renderSummaryCard("Серийный номер", escapeHtml(asset.serial_number), "Только просмотр для текущей роли.")}
                    ${renderSummaryCard("Тип", escapeHtml(asset.asset_type), "Только просмотр для текущей роли.")}
                    ${renderSummaryCard("ID актива", renderTechnicalValue(asset.id, { copy: true }), "Справочная техническая ссылка.")}
                </div>
                <div class="journey-banner tone-readonly">
                    <div class="journey-copy">
                        <strong>Данные актива только для просмотра</strong>
                        <p>Текущая роль не может создавать, редактировать или выполнять действия изменения.</p>
                    </div>
                </div>
            </section>
        `;
    }

    return `
        <section class="asset-section">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Описание актива</p>
                    <h3>Редактировать описание актива</h3>
                </div>
                <span class="summary-pill">только карточка</span>
            </div>

            <details class="form-support-disclosure">
                <summary>Граница редактирования</summary>
                <p>
                    Эта форма управляет только описанием и идентификацией актива: названием, инвентарным тегом,
                    серийным номером, типом и комментарием. Ответственность, текущая локация, выдача, перемещение
                    и возврат остаются отдельными действиями.
                </p>
            </details>

            <form id="asset-update-form" class="form-grid">
                <div class="form-section-label">Поля описания актива</div>
                <label class="field">
                    <span>Название актива</span>
                    <input name="display_name" type="text" value="${escapeAttribute(asset.display_name || "")}" maxlength="160">
                    <small class="reference-meta">Необязательное название для узнавания в интерфейсе.</small>
                </label>
                <label class="field">
                    <span>Инвентарный тег</span>
                    <input name="asset_tag" type="text" value="${escapeAttribute(asset.asset_tag)}" required>
                </label>
                <label class="field">
                    <span>Серийный номер</span>
                    <input name="serial_number" type="text" value="${escapeAttribute(asset.serial_number)}" required>
                </label>
                <label class="field">
                    <span>Тип актива</span>
                    <input name="asset_type" type="text" value="${escapeAttribute(asset.asset_type)}" required>
                </label>
                <label class="field field-wide">
                    <span>Комментарий</span>
                    <textarea name="note" maxlength="2000" placeholder="Необязательный рабочий комментарий">${escapeHtml(asset.note || "")}</textarea>
                    <small class="reference-meta">Обычный текст. Не меняет ответственность, статус или историю движения.</small>
                </label>
                <div class="button-row button-row-inline">
                    <button class="button button-primary ${isBusy("updateAsset") ? "is-loading" : ""}" type="submit" ${isBusy("updateAsset") ? "disabled" : ""}>${isBusy("updateAsset") ? "Сохраняем" : "Сохранить описание"}</button>
                </div>
            </form>
        </section>
    `;
}

function renderAssetResponsibilitySection(asset, hasCurrentUser) {
    return `
        <section class="asset-section">
            <div class="asset-summary-grid">
                ${renderSummaryCard("Текущий пользователь", renderUserReference(asset.assigned_user_id), hasCurrentUser ? "Активная пользовательская ответственность." : "Пользовательская ответственность не показана.")}
                ${renderSummaryCard("Текущая локация", renderLocationReference(asset.location_id), "Return не меняет локацию канонически.")}
                ${renderSummaryCard("Статус актива", renderStatusBadge(asset.status), "Статус не заменяет состояние ответственности.")}
                ${renderSummaryCard("Следующее действие", escapeHtml(getNextResponsibilityHint(asset)), "Выбор остаётся в отдельных действиях.")}
            </div>

            <div class="journey-banner tone-info">
                <div class="journey-copy">
                    <strong>Граница действий ответственности</strong>
                    <p>Assign задаёт пользователя, transfer меняет пользователя и/или локацию, return завершает только текущую пользовательскую ответственность.</p>
                </div>
            </div>
        </section>
    `;
}

function renderAssetActionsSection(asset, canMutate, hasUsers, hasLocations, hasCurrentUser) {
    if (!canMutate) {
        return `
            <section class="asset-section">
                <div class="journey-banner tone-readonly">
                    <div class="journey-copy">
                        <strong>Только просмотр</strong>
                        <p>Текущая роль видит реестр, карточку, историю и аудит. Действия изменения скрыты.</p>
                    </div>
                </div>
            </section>
        `;
    }

    return `
        <section class="asset-section">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Действия с выбранным активом</p>
                    <h3>Что сделать с активом</h3>
                </div>
                <span class="summary-pill">${escapeHtml(hasCurrentUser ? "Есть текущий пользователь" : "Нет текущего пользователя")}</span>
            </div>

            <p class="muted-note">Основные действия остаются отдельными: ответственность меняется через выдачу, перемещение и возврат; описание актива редактируется отдельно.</p>

            <div class="asset-action-group">
                <div class="asset-action-group-header">
                    <div>
                        <p class="section-kicker">Ответственность</p>
                        <h4 class="asset-action-group-title">Выдать, переместить или вернуть</h4>
                    </div>
                    <span class="summary-pill">${escapeHtml(getResponsibilityActionStateLabel(hasCurrentUser))}</span>
                </div>
                <p class="asset-action-group-note">${escapeHtml(getResponsibilityActionGuidance(hasCurrentUser))}</p>
                <div class="action-grid action-grid-primary asset-action-grid">
                    ${renderAssignCard(hasUsers, hasCurrentUser)}
                    ${renderTransferCard(hasUsers, hasLocations, hasCurrentUser)}
                    ${renderReturnCard(hasCurrentUser)}
                </div>
            </div>

            <div class="asset-action-group asset-action-group-secondary">
                <div class="asset-action-group-header">
                    <div>
                        <p class="section-kicker">Описание актива</p>
                        <h4 class="asset-action-group-title">Редактировать описание актива</h4>
                    </div>
                    <span class="summary-pill">только карточка</span>
                </div>
                <p class="asset-action-group-note">Здесь меняются только поля описания и идентификации актива. Ответственность, локация и возврат остаются в отдельных действиях выше.</p>
                <details class="progressive-panel secondary-action-panel">
                    <summary>
                        <span>Редактировать описание</span>
                        <span class="summary-pill">master-record</span>
                    </summary>
                    ${renderAssetMasterSection(asset, canMutate)}
                </details>
            </div>
        </section>
    `;
}

function getResponsibilityActionStateLabel(hasCurrentUser) {
    return hasCurrentUser ? "ответственный назначен" : "сначала выдача";
}

function getResponsibilityActionGuidance(hasCurrentUser) {
    if (!hasCurrentUser) {
        return "У актива нет текущего ответственного пользователя. Для первого закрепления используйте «Выдать сотруднику»; перемещение без пользователя применяйте только для локации.";
    }
    return "У актива уже есть ответственный пользователь. Для смены пользователя или локации используйте «Переместить», для завершения текущей ответственности — «Вернуть».";
}

function renderAssetHistoryAuditSection() {
    return `
        <section class="asset-section">
            <div class="history-audit-strip">
                ${renderSummaryCard("История", escapeHtml(String(state.assetHistory.length)), "Факты assign / transfer / return для выбранного актива.")}
                ${renderSummaryCard("Аудит", escapeHtml(String(state.assetAudit.length)), "Локальные записи аудита для выбранного актива.")}
            </div>

            <div class="progressive-stack">
                <details class="progressive-panel" open>
                    <summary>
                        <span>История ответственности</span>
                        <span class="summary-pill">${escapeHtml(String(state.assetHistory.length))}</span>
                    </summary>
                    <p class="muted-note">Сначала показано, что произошло, когда и какие значения изменились. Служебные детали остаются ниже в раскрытии.</p>
                    ${renderHistoryTable()}
                </details>

                <details class="progressive-panel">
                    <summary>
                        <span>Локальный аудит</span>
                        <span class="summary-pill">${escapeHtml(String(state.assetAudit.length))}</span>
                    </summary>
                    <p class="muted-note">Аудит выбранного актива скрыт за раскрытием, чтобы ежедневная карточка не превращалась в технический журнал.</p>
                    ${renderAuditTable()}
                </details>
            </div>

            <div class="button-row">
                <button class="button button-secondary" type="button" data-asset-section="technical">Открыть technical details</button>
            </div>
        </section>
    `;
}

function renderAssetTechnicalAttributesBlock(asset) {
    const technicalAttributes = getAssetTechnicalAttributes(asset);
    const groups = getTechnicalAttributeGroups(technicalAttributes.profile);
    const isSupported = groups.length > 0;
    const canEdit = isSupported && canAssetMutate();
    const isEditing = canEdit && state.technicalAttributesEditing;

    return `
        <section class="asset-technical-attributes" aria-label="Технические атрибуты">
            <div class="asset-technical-attributes-header">
                <div>
                    <p class="section-kicker">Технические данные</p>
                    <h4>Технические атрибуты</h4>
                </div>
                <span class="summary-pill">${escapeHtml(canEdit ? technicalAttributes.profile : "Только для чтения")}</span>
            </div>
            <ul class="asset-technical-attributes-help">
                <li>Технические данные зависят от типа актива и заполняются вручную.</li>
                <li>Эти данные не являются инвентарным идентификатором.</li>
                <li>Инвентарный идентификатор остаётся: asset_tag.</li>
                <li>Данные не используются для QR Lite, быстрого поиска или информационного листа сотрудника.</li>
                <li>Некоторые технические данные могут содержать сетевую или удалённую информацию.</li>
            </ul>
            ${isSupported
                ? (isEditing
                    ? renderTechnicalAttributeEditForm(technicalAttributes.profile, technicalAttributes.values)
                    : renderTechnicalAttributeReadView(technicalAttributes.profile, technicalAttributes.values, canEdit))
                : `<p class="asset-technical-attributes-empty">Для этого типа актива технические атрибуты v1 не настроены.</p>`}
        </section>
    `;
}

function renderTechnicalAttributeReadView(profile, values, canEdit) {
    const hasValues = hasStoredTechnicalAttributeValues(values, profile);
    return `
        ${hasValues ? "" : `<p class="asset-technical-attributes-empty">Технические данные пока не заполнены.</p>`}
        <div class="asset-technical-attributes-groups">
            ${getTechnicalAttributeGroups(profile).map((group) => `
                <section class="asset-technical-attributes-group">
                    <h5>${escapeHtml(group.title)}</h5>
                    <dl>
                        ${group.fields.map((field) => renderTechnicalAttributeReadRow(field, values)).join("")}
                    </dl>
                </section>
            `).join("")}
        </div>
        <div class="button-row button-row-inline">
            ${canEdit
                ? `<button class="button button-secondary" type="button" data-technical-attributes-edit ${isBusy("technicalAttributes") ? "disabled" : ""}>Редактировать технические данные</button>`
                : `<span class="summary-pill">Только для чтения</span>`}
        </div>
    `;
}

function renderTechnicalAttributeReadRow(field, values) {
    const value = optionalAssetText(values?.[field.key]);
    return `
        <div class="asset-technical-attributes-row">
            <dt>
                <span>${escapeHtml(field.label)}</span>
                ${field.secondaryLabel ? `<small>${escapeHtml(field.secondaryLabel)}</small>` : ""}
            </dt>
            <dd class="${value ? "" : "asset-technical-attributes-value-empty"}">${escapeHtml(value || "Не заполнено")}</dd>
        </div>
    `;
}

function renderTechnicalAttributeEditForm(profile, values) {
    return `
        <form id="asset-technical-attributes-form" class="asset-technical-attributes-form">
            ${getTechnicalAttributeGroups(profile).map((group) => `
                <fieldset class="asset-technical-attributes-group">
                    <legend>${escapeHtml(group.title)}</legend>
                    ${group.fields.map((field) => renderTechnicalAttributeField(field, values)).join("")}
                </fieldset>
            `).join("")}
            <div class="button-row button-row-inline">
                <button class="button button-primary ${isBusy("technicalAttributes") ? "is-loading" : ""}" type="submit" ${isBusy("technicalAttributes") ? "disabled" : ""}>${isBusy("technicalAttributes") ? "Сохраняем" : "Сохранить технические данные"}</button>
                <button class="button button-ghost" type="button" data-technical-attributes-cancel ${isBusy("technicalAttributes") ? "disabled" : ""}>Отменить</button>
            </div>
        </form>
    `;
}

function renderTechnicalAttributeField(field, values) {
    const value = optionalAssetText(values?.[field.key]);
    const label = `
        <span>${escapeHtml(field.label)}</span>
        ${field.secondaryLabel ? `<small class="reference-meta">${escapeHtml(field.secondaryLabel)}</small>` : ""}
        ${field.key === "printer_placement_note" ? `<small class="reference-meta">Не заменяет текущую локацию.</small>` : ""}
    `;

    if (field.multiline) {
        return `
            <label class="field field-wide">
                ${label}
                <textarea name="${escapeAttribute(field.key)}" maxlength="${TECHNICAL_ATTRIBUTE_MAX_LENGTH}">${escapeHtml(value)}</textarea>
            </label>
        `;
    }

    return `
        <label class="field">
            ${label}
            <input name="${escapeAttribute(field.key)}" type="text" value="${escapeAttribute(value)}" maxlength="${TECHNICAL_ATTRIBUTE_MAX_LENGTH}">
        </label>
    `;
}

function renderAssetTechnicalSection(asset) {
    return `
        <section class="asset-section">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Technical details</p>
                    <h3>Служебные значения</h3>
                </div>
                <span class="summary-pill">Secondary</span>
            </div>
            <p class="muted-note">
                Технические значения доступны для поддержки и сверки. Они не являются основной рабочей навигацией.
            </p>
            ${renderAssetQrLiteBlock(asset)}
            ${renderAssetTechnicalAttributesBlock(asset)}
            <details class="technical-disclosure-panel">
                <summary>Показать служебные идентификаторы</summary>
                <div class="asset-summary-grid technical-grid-muted">
                    ${renderSummaryCard("Asset ID", renderTechnicalValue(asset.id, { copy: true, label: "asset ID" }), "Идентификатор актива.")}
                    ${renderSummaryCard("Tenant ID", renderTechnicalValue(state.context.tenantId, { copy: true, label: "tenant ID" }), "Текущий контекст доступа.")}
                    ${renderSummaryCard("Current user ID", renderTechnicalValue(state.context.userId, { copy: true, label: "user ID" }), "Текущий контекст доступа.")}
                    ${renderSummaryCard("Actor", renderTechnicalValue(state.context.actor, { copy: true, label: "actor" }), "Значение X-Authenticated-Actor.")}
                    ${renderSummaryCard("assigned_user_id", renderTechnicalValue(asset.assigned_user_id, { copy: true, label: "assigned_user_id" }), "Raw value из карточки актива.")}
                    ${renderSummaryCard("location_id", renderTechnicalValue(asset.location_id, { copy: true, label: "location_id" }), "Raw value из карточки актива.")}
                </div>
            </details>
            <details class="audit-raw">
                <summary>Raw asset payload</summary>
                <pre class="json-block">${escapeHtml(JSON.stringify(asset, null, 2))}</pre>
            </details>
        </section>
    `;
}

function renderAssetQrLiteBlock(asset) {
    const payload = assetCanonicalTag(asset);
    if (!payload) {
        return `
            <section class="asset-qr-lite asset-qr-lite-empty" aria-label="QR Lite">
                <div class="asset-qr-lite-header">
                    <div>
                        <p class="section-kicker">QR Lite</p>
                        <h4>QR по инвентарному номеру</h4>
                    </div>
                    <span class="summary-pill">Производный QR</span>
                </div>
                <p class="asset-qr-lite-help">QR недоступен: у актива нет инвентарного тега.</p>
            </section>
        `;
    }

    const qrSvg = renderQrLiteSvg(payload);
    if (!qrSvg) {
        return `
            <section class="asset-qr-lite asset-qr-lite-empty" aria-label="QR Lite">
                <div class="asset-qr-lite-header">
                    <div>
                        <p class="section-kicker">QR Lite</p>
                        <h4>QR по инвентарному номеру</h4>
                    </div>
                    <span class="summary-pill">Производный QR</span>
                </div>
                <p class="asset-qr-lite-help">QR недоступен: инвентарный тег слишком длинный для QR Lite.</p>
            </section>
        `;
    }

    return `
        <section class="asset-qr-lite" aria-label="QR Lite">
            <div class="asset-qr-lite-header">
                <div>
                    <p class="section-kicker">QR Lite</p>
                    <h4>QR по инвентарному номеру</h4>
                </div>
                <span class="summary-pill">Производный QR</span>
            </div>
            <div class="asset-qr-lite-body">
                <div class="asset-qr-lite-visual" aria-hidden="true">${qrSvg}</div>
                <div class="asset-qr-lite-content">
                    <p class="asset-qr-lite-payload">
                        <span>Инвентарный тег:</span>
                        <code>${escapeHtml(payload)}</code>
                    </p>
                    <p class="asset-qr-lite-help">
                        Сформирован из существующего идентификатора актива. QR не является отдельным идентификатором и не открывает публичный доступ.
                    </p>
                    <div class="asset-qr-lite-boundaries" aria-label="Границы QR Lite">
                        <span>Не является отдельным идентификатором</span>
                        <span>Не открывает публичный доступ</span>
                    </div>
                    <dl class="asset-qr-lite-context">
                        <div>
                            <dt>Название</dt>
                            <dd>${escapeHtml(assetDisplayTitle(asset) || "-")}</dd>
                        </div>
                        <div>
                            <dt>Тип</dt>
                            <dd>${escapeHtml(optionalAssetText(asset?.asset_type) || "-")}</dd>
                        </div>
                        <div>
                            <dt>Серийный номер</dt>
                            <dd>${escapeHtml(optionalAssetText(asset?.serial_number) || "-")}</dd>
                        </div>
                    </dl>
                </div>
            </div>
        </section>
    `;
}

function renderQrLiteSvg(payload) {
    const matrix = buildQrLiteMatrix(payload);
    if (!matrix) {
        return "";
    }

    const quietZone = 4;
    const viewSize = matrix.length + quietZone * 2;
    const path = [];
    matrix.forEach((row, y) => {
        row.forEach((dark, x) => {
            if (dark) {
                path.push(`M${x + quietZone},${y + quietZone}h1v1h-1z`);
            }
        });
    });

    return `
        <svg
            class="asset-qr-lite-svg"
            viewBox="0 0 ${viewSize} ${viewSize}"
            role="img"
            aria-label="${escapeAttribute(`QR Lite: ${payload}`)}"
            shape-rendering="crispEdges"
        >
            <rect width="${viewSize}" height="${viewSize}" fill="#fff"></rect>
            <path d="${path.join("")}" fill="currentColor"></path>
        </svg>
    `;
}

function buildQrLiteMatrix(payload) {
    const bytes = encodeQrLitePayload(payload);
    const versionSpec = QR_LITE_VERSIONS.find((candidate) => (4 + 8 + bytes.length * 8) <= candidate.dataCodewords * 8);
    if (!versionSpec) {
        return null;
    }

    const size = versionSpec.version * 4 + 17;
    const modules = Array.from({ length: size }, () => Array(size).fill(false));
    const reserved = Array.from({ length: size }, () => Array(size).fill(false));
    const setModule = (x, y, dark, isReserved = true) => {
        if (x < 0 || y < 0 || x >= size || y >= size) {
            return;
        }
        modules[y][x] = Boolean(dark);
        if (isReserved) {
            reserved[y][x] = true;
        }
    };

    drawQrLiteFunctionPatterns(versionSpec, size, setModule);
    reserveQrLiteFormatAreas(size, setModule);
    setModule(8, size - 8, true);

    const dataCodewords = buildQrLiteDataCodewords(bytes, versionSpec.dataCodewords);
    const eccCodewords = buildQrLiteErrorCodewords(dataCodewords, versionSpec.eccCodewords);
    placeQrLiteDataBits(modules, reserved, dataCodewords.concat(eccCodewords));
    drawQrLiteFormatBits(size, setModule);

    return modules;
}

function encodeQrLitePayload(payload) {
    if (typeof TextEncoder !== "undefined") {
        return [...new TextEncoder().encode(payload)];
    }
    return [...unescape(encodeURIComponent(payload))].map((character) => character.charCodeAt(0));
}

function buildQrLiteDataCodewords(bytes, dataCodewordCount) {
    const bits = [];
    appendQrLiteBits(bits, 0b0100, 4);
    appendQrLiteBits(bits, bytes.length, 8);
    bytes.forEach((byte) => appendQrLiteBits(bits, byte, 8));

    const capacity = dataCodewordCount * 8;
    const terminatorLength = Math.min(4, capacity - bits.length);
    appendQrLiteBits(bits, 0, terminatorLength);
    while (bits.length % 8 !== 0) {
        bits.push(0);
    }

    const codewords = [];
    for (let index = 0; index < bits.length; index += 8) {
        codewords.push(bits.slice(index, index + 8).reduce((value, bit) => (value << 1) | bit, 0));
    }

    let padIndex = 0;
    while (codewords.length < dataCodewordCount) {
        codewords.push(padIndex % 2 === 0 ? 0xec : 0x11);
        padIndex += 1;
    }
    return codewords;
}

function appendQrLiteBits(bits, value, length) {
    for (let index = length - 1; index >= 0; index -= 1) {
        bits.push((value >>> index) & 1);
    }
}

function drawQrLiteFunctionPatterns(versionSpec, size, setModule) {
    drawQrLiteFinder(0, 0, setModule);
    drawQrLiteFinder(size - 7, 0, setModule);
    drawQrLiteFinder(0, size - 7, setModule);

    for (let index = 8; index < size - 8; index += 1) {
        const dark = index % 2 === 0;
        setModule(6, index, dark);
        setModule(index, 6, dark);
    }

    versionSpec.alignment.forEach((x) => {
        versionSpec.alignment.forEach((y) => {
            const overlapsFinder = (x < 9 && y < 9) || (x > size - 10 && y < 9) || (x < 9 && y > size - 10);
            if (!overlapsFinder) {
                drawQrLiteAlignment(x, y, setModule);
            }
        });
    });
}

function drawQrLiteFinder(left, top, setModule) {
    for (let dy = -1; dy <= 7; dy += 1) {
        for (let dx = -1; dx <= 7; dx += 1) {
            const x = left + dx;
            const y = top + dy;
            const inFinder = dx >= 0 && dx <= 6 && dy >= 0 && dy <= 6;
            const dark = inFinder && (dx === 0 || dx === 6 || dy === 0 || dy === 6 || (dx >= 2 && dx <= 4 && dy >= 2 && dy <= 4));
            setModule(x, y, dark);
        }
    }
}

function drawQrLiteAlignment(centerX, centerY, setModule) {
    for (let dy = -2; dy <= 2; dy += 1) {
        for (let dx = -2; dx <= 2; dx += 1) {
            setModule(centerX + dx, centerY + dy, Math.max(Math.abs(dx), Math.abs(dy)) !== 1);
        }
    }
}

function reserveQrLiteFormatAreas(size, setModule) {
    for (let index = 0; index <= 8; index += 1) {
        if (index !== 6) {
            setModule(8, index, false);
            setModule(index, 8, false);
        }
    }
    for (let index = size - 8; index < size; index += 1) {
        setModule(8, index, false);
        setModule(index, 8, false);
    }
}

function placeQrLiteDataBits(modules, reserved, codewords) {
    const size = modules.length;
    const bits = codewords.flatMap((codeword) => {
        const codewordBits = [];
        appendQrLiteBits(codewordBits, codeword, 8);
        return codewordBits;
    });
    let bitIndex = 0;
    let upward = true;

    for (let right = size - 1; right >= 1; right -= 2) {
        if (right === 6) {
            right -= 1;
        }
        for (let rowOffset = 0; rowOffset < size; rowOffset += 1) {
            const y = upward ? size - 1 - rowOffset : rowOffset;
            for (let dx = 0; dx < 2; dx += 1) {
                const x = right - dx;
                if (reserved[y][x]) {
                    continue;
                }
                const bit = bitIndex < bits.length ? bits[bitIndex] === 1 : false;
                modules[y][x] = qrLiteMask(x, y) ? !bit : bit;
                bitIndex += 1;
            }
        }
        upward = !upward;
    }
}

function qrLiteMask(x, y) {
    return (x + y) % 2 === 0;
}

function drawQrLiteFormatBits(size, setModule) {
    const bit = (index) => ((QR_LITE_FORMAT_BITS_LOW_MASK_0 >>> index) & 1) === 1;
    for (let index = 0; index <= 5; index += 1) {
        setModule(8, index, bit(index));
    }
    setModule(8, 7, bit(6));
    setModule(8, 8, bit(7));
    setModule(7, 8, bit(8));
    for (let index = 9; index < 15; index += 1) {
        setModule(14 - index, 8, bit(index));
    }
    for (let index = 0; index < 8; index += 1) {
        setModule(size - 1 - index, 8, bit(index));
    }
    for (let index = 8; index < 15; index += 1) {
        setModule(8, size - 15 + index, bit(index));
    }
    setModule(8, size - 8, true);
}

function buildQrLiteErrorCodewords(dataCodewords, degree) {
    const generator = buildQrLiteGenerator(degree);
    const remainder = Array(degree).fill(0);
    dataCodewords.forEach((codeword) => {
        const factor = codeword ^ remainder.shift();
        remainder.push(0);
        generator.forEach((coefficient, index) => {
            remainder[index] ^= multiplyQrLiteGalois(coefficient, factor);
        });
    });
    return remainder;
}

function buildQrLiteGenerator(degree) {
    let coefficients = [1];
    for (let index = 0; index < degree; index += 1) {
        const next = Array(coefficients.length + 1).fill(0);
        coefficients.forEach((coefficient, coefficientIndex) => {
            next[coefficientIndex] ^= multiplyQrLiteGalois(coefficient, 1);
            next[coefficientIndex + 1] ^= multiplyQrLiteGalois(coefficient, QR_LITE_EXP[index]);
        });
        coefficients = next;
    }
    return coefficients.slice(1);
}

const QR_LITE_EXP = (() => {
    const values = Array(512).fill(0);
    let value = 1;
    for (let index = 0; index < 255; index += 1) {
        values[index] = value;
        value <<= 1;
        if (value & 0x100) {
            value ^= 0x11d;
        }
    }
    for (let index = 255; index < values.length; index += 1) {
        values[index] = values[index - 255];
    }
    return values;
})();

const QR_LITE_LOG = (() => {
    const values = Array(256).fill(0);
    for (let index = 0; index < 255; index += 1) {
        values[QR_LITE_EXP[index]] = index;
    }
    return values;
})();

function multiplyQrLiteGalois(left, right) {
    if (left === 0 || right === 0) {
        return 0;
    }
    return QR_LITE_EXP[QR_LITE_LOG[left] + QR_LITE_LOG[right]];
}

function renderAssignCard(hasUsers, hasCurrentUser) {
    const busy = isBusy("assignAsset");
    const disabled = !hasUsers || hasCurrentUser || busy ? "disabled" : "";
    return `
        <article class="action-card action-card-daily ${!hasCurrentUser ? "asset-action-recommended" : ""}">
            <div class="action-card-header">
                <p class="section-kicker">Ответственность</p>
                <h4>Выдать сотруднику</h4>
            </div>
            <details class="form-support-disclosure">
                <summary>Когда использовать выдачу</summary>
                <p>Используйте это действие для первого ответственного пользователя. Если пользователь уже назначен, смена пользователя выполняется через «Переместить».</p>
            </details>
            ${!hasUsers ? `<p class="inline-warning">Для выдачи нужен хотя бы один пользователь в справочнике.</p>` : ""}
            ${hasCurrentUser ? `<p class="inline-warning">В карточке уже есть текущий пользователь. Для смены пользователя используйте «Переместить».</p>` : ""}
            <form id="asset-assign-form" class="form-grid">
                <label class="field">
                    <span>Выдать пользователю</span>
                    <select name="target_user_id" required ${disabled}>
                        <option value="">Выберите пользователя</option>
                        ${buildUserOptions()}
                    </select>
                </label>
                <div class="button-row button-row-inline">
                    <button class="button button-primary ${busy ? "is-loading" : ""}" type="submit" ${disabled}>${busy ? "Выдаём" : "Выдать сотруднику"}</button>
                </div>
            </form>
        </article>
    `;
}

function renderTransferCard(hasUsers, hasLocations, hasCurrentUser) {
    const blockedByReferences = !hasUsers && !hasLocations;
    const busy = isBusy("transferAsset");
    const disabled = blockedByReferences || busy ? "disabled" : "";
    const warning = blockedByReferences
            ? "Для перемещения нужен хотя бы один пользователь или локация в справочнике."
            : !hasCurrentUser
                ? "Без текущего пользователя перемещение допустимо только для смены локации. Первое назначение пользователя выполняется через «Выдать сотруднику»."
                : "";

    return `
        <article class="action-card action-card-daily ${hasCurrentUser ? "asset-action-recommended" : ""}">
            <div class="action-card-header">
                <p class="section-kicker">Ответственность</p>
                <h4>Переместить</h4>
            </div>
            <details class="form-support-disclosure">
                <summary>Когда использовать перемещение</summary>
                <p>Это отдельное действие меняет ответственного пользователя и/или рабочую локацию. Редактирование описания актива в этом не участвует.</p>
            </details>
            ${warning ? `<p class="inline-warning">${escapeHtml(warning)}</p>` : ""}
            <form id="asset-transfer-form" class="form-grid">
                <label class="field">
                    <span>Новый пользователь</span>
                    <select name="target_user_id" ${disabled}>
                        <option value="">Не менять пользователя</option>
                        ${buildUserOptions()}
                    </select>
                </label>
                <label class="field">
                    <span>Новая локация</span>
                    <select name="target_location_id" ${disabled}>
                        <option value="">Не менять локацию</option>
                        ${buildLocationOptions()}
                    </select>
                </label>
                <div class="button-row button-row-inline">
                    <button class="button button-primary ${busy ? "is-loading" : ""}" type="submit" ${disabled}>${busy ? "Перемещаем" : "Переместить"}</button>
                </div>
            </form>
        </article>
    `;
}

function renderReturnCard(hasCurrentUser) {
    const busy = isBusy("returnAsset");
    const disabled = hasCurrentUser && !busy ? "" : "disabled";
    return `
        <article class="action-card action-card-daily ${hasCurrentUser ? "asset-action-recommended" : ""}">
            <div class="action-card-header">
                <p class="section-kicker">Ответственность</p>
                <h4>Вернуть</h4>
            </div>
            <details class="form-support-disclosure">
                <summary>Когда использовать возврат</summary>
                <p>Возврат завершает текущую пользовательскую ответственность. Запись актива и её история остаются доступны.</p>
            </details>
            ${!hasCurrentUser ? `<p class="inline-warning">В карточке нет текущего назначенного пользователя, поэтому возврат сейчас недоступен.</p>` : ""}
            <form id="asset-return-form" class="form-grid">
                <div class="button-row button-row-inline">
                    <button class="button button-secondary ${busy ? "is-loading" : ""}" type="submit" ${disabled}>${busy ? "Возвращаем" : "Вернуть"}</button>
                </div>
            </form>
        </article>
    `;
}

function renderHistoryTable() {
    if (state.assetHistory.length === 0) {
        return renderInlineEmpty(
            "История пока пуста",
            "Для выбранного актива ещё нет фактов assign / transfer / return. Выполните отдельное действие ответственности, если роль и данные позволяют."
        );
    }

    return `
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Что произошло</th>
                        <th>Когда</th>
                        <th>Ответственный</th>
                        <th>Локация</th>
                    </tr>
                </thead>
                <tbody>
                    ${state.assetHistory.map((item) => `
                        <tr>
                            <td>
                                <div class="history-summary">
                                    <strong>${escapeHtml(describeHistoryFact(item.fact_type))}</strong>
                                    <span class="reference-meta">${escapeHtml(describeHistorySummary(item))}</span>
                                </div>
                            </td>
                            <td>${escapeHtml(formatTimestamp(item.occurred_at))}</td>
                            <td>
                                <ul class="compact-list">
                                    <li>до: ${renderUserReference(item.previous_assigned_user_id)}</li>
                                    <li>после: ${renderUserReference(item.new_assigned_user_id)}</li>
                                </ul>
                            </td>
                            <td>
                                <ul class="compact-list">
                                    <li>до: ${renderLocationReference(item.previous_location_id)}</li>
                                    <li>после: ${renderLocationReference(item.new_location_id)}</li>
                                </ul>
                            </td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderAuditTable() {
    if (state.assetAudit.length === 0) {
        return renderInlineEmpty(
            "Аудит пока пуст",
            "Для выбранного актива ещё нет локальных записей аудита в доступных данных."
        );
    }

    return `
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Что произошло</th>
                        <th>Когда</th>
                        <th>Инициатор</th>
                        <th>Детали</th>
                    </tr>
                </thead>
                <tbody>
                    ${state.assetAudit.map((item) => `
                        <tr>
                            <td>
                                <div class="audit-summary">
                                    <strong>${escapeHtml(describeAuditAction(item.action))}</strong>
                                    <span class="reference-meta">${escapeHtml(describeAuditSummary(item))}</span>
                                </div>
                            </td>
                            <td>${escapeHtml(formatTimestamp(item.occurred_at))}</td>
                            <td>
                                <div>${escapeHtml(item.actor || "-")}</div>
                            </td>
                            <td>${renderAuditDetails(item)}</td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderReviewScreen() {
    const visible = canAssetRead() && state.activeScreen === PRIMARY_SCREENS.REVIEW;
    ui.reviewScreen.hidden = !visible;

    if (!canAssetRead()) {
        ui.reviewSectionNav.innerHTML = "";
        ui.reviewDetailPanel.innerHTML = "";
        return;
    }

    renderReviewNav();
    renderReviewDetail();
}

function renderReviewNav() {
    ui.reviewSectionNav.innerHTML = REVIEW_SECTIONS.map((section) => `
        <button
            class="nav-chip ${section.key === state.activeReviewSection ? "is-active" : ""}"
            type="button"
            data-review-section="${section.key}"
        >
            ${escapeHtml(section.label)}
        </button>
    `).join("");
}

function renderReviewDetail() {
    if (!state.selectedAsset) {
        ui.reviewDetailPanel.innerHTML = `
            <div class="empty-state">
                <p class="empty-kicker">История и аудит</p>
                <h3>Актив не выбран</h3>
                <p>Откройте актив из реестра. После этого здесь появятся история и аудит выбранной карточки.</p>
                <div class="button-row">
                    <button class="button button-secondary" type="button" data-screen="${PRIMARY_SCREENS.ASSETS}">К реестру активов</button>
                </div>
            </div>
        `;
        return;
    }

    const isAudit = state.activeReviewSection === "audit";
    ui.reviewDetailPanel.innerHTML = `
        <div class="stack">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">История и аудит</p>
                    <h3>${escapeHtml(assetDisplayTitle(state.selectedAsset))}</h3>
                    <span class="reference-meta">Инв. тег: ${escapeHtml(assetCanonicalTag(state.selectedAsset) || "-")}</span>
                    ${renderTechnicalDisclosure("ID актива", state.selectedAsset.id, { label: "ID актива" })}
                </div>
                <div class="button-row">
                    <button class="button button-secondary ${isBusy("refreshAsset") ? "is-loading" : ""}" type="button" data-refresh-asset ${isBusy("refreshAsset") ? "disabled" : ""}>${isBusy("refreshAsset") ? "Обновляем" : "Обновить"}</button>
                    <button class="button button-ghost" type="button" data-screen="${PRIMARY_SCREENS.ASSETS}">К карточке</button>
                </div>
            </div>

            <div class="asset-summary-grid">
                ${renderSummaryCard("Ответственный", renderUserReference(state.selectedAsset.assigned_user_id), "Текущее значение из карточки актива.")}
                ${renderSummaryCard("Локация", renderLocationReference(state.selectedAsset.location_id), "Текущее значение из карточки актива.")}
                ${renderSummaryCard("Событий истории", escapeHtml(String(state.assetHistory.length)), "Факты ответственности из доступного журнала.")}
                ${renderSummaryCard("Записей аудита", escapeHtml(String(state.assetAudit.length)), "Локальные записи аудита из доступных данных.")}
            </div>

            ${isAudit ? `
                <section>
                    <div class="panel-heading">
                        <div>
                            <p class="section-kicker">Audit</p>
                            <h3>Локальный аудит</h3>
                        </div>
                        <span class="summary-pill">${escapeHtml(String(state.assetAudit.length))}</span>
                    </div>
                    ${renderAuditTable()}
                </section>
            ` : `
                <section>
                    <div class="panel-heading">
                        <div>
                            <p class="section-kicker">History</p>
                            <h3>История ответственности</h3>
                        </div>
                        <span class="summary-pill">${escapeHtml(String(state.assetHistory.length))}</span>
                    </div>
                    ${renderHistoryTable()}
                </section>
            `}
        </div>
    `;
}

function renderAdminScreen() {
    const visible = canTenantAdmin() && state.activeScreen === PRIMARY_SCREENS.SETUP;
    ui.adminScreen.hidden = !visible;

    if (!canTenantAdmin()) {
        return;
    }

    renderSetupOverview();
    renderAdminNav();
    renderAdminTable();
    renderAdminEditor();
}

function renderSetupOverview() {
    const steps = getSetupStepStatuses();
    const completed = steps.filter((step) => step.count > 0).length;
    const currentStep = steps.find((step) => step.status === "current");

    ui.setupOverview.innerHTML = `
        <div class="stack">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Порядок настройки</p>
                    <h3>Короткая подсказка по порядку</h3>
                </div>
                <span class="summary-pill">${completed}/${SETUP_SEQUENCE.length} заполнено</span>
            </div>

            <p class="muted-note">
                Разделы открываются через вкладки настройки. Подсказка ниже не дублирует навигацию.
            </p>

            <details class="setup-order-disclosure">
                <summary>
                    <span>${escapeHtml(currentStep ? `Следующий шаг: ${currentStep.label}` : "Базовые разделы заполнены")}</span>
                    <span class="summary-pill">порядок</span>
                </summary>
                <ol class="setup-order-list">
                    ${steps.map((step) => `
                        <li class="${step.status}">
                            <span><strong>${escapeHtml(step.index)}. ${escapeHtml(step.label)}</strong> — ${escapeHtml(step.description)}</span>
                            <span class="setup-order-count">${escapeHtml(formatRecordCount(step.count))}</span>
                        </li>
                    `).join("")}
                </ol>
            </details>

            <p class="muted-note">
                ${escapeHtml(currentStep
                    ? `Следующий рекомендованный шаг: ${currentStep.label}.`
                    : "Все базовые разделы настройки уже заполнены. Теперь можно точечно поддерживать справочники.")}
            </p>
        </div>
    `;
}

function renderAdminNav() {
    ui.adminResourceNav.innerHTML = Object.values(ADMIN_RESOURCES).map((resource) => `
        <button
            class="nav-chip ${resource.key === state.activeAdminResource ? "is-active" : ""}"
            type="button"
            data-resource-key="${resource.key}"
        >
            ${escapeHtml(resource.label)}
        </button>
    `).join("");
}

function renderAdminTable() {
    const config = ADMIN_RESOURCES[state.activeAdminResource];
    const items = getAdminItems(config.key);
    ui.adminResourceTitle.textContent = config.label;
    ui.adminTableHead.innerHTML = `
        <tr>
            ${config.columns.map((column) => `<th>${escapeHtml(column.label)}</th>`).join("")}
        </tr>
    `;

    if ((state.loading.references || state.loading.roles) && items.length === 0) {
        ui.adminTableBody.innerHTML = `<tr><td colspan="${config.columns.length}" class="empty-cell">Загружаем ${escapeHtml(config.label.toLowerCase())}...</td></tr>`;
        return;
    }

    if (items.length === 0) {
        const meta = ADMIN_RESOURCE_META[config.key];
        const message = [meta.empty, meta.prerequisite].filter(Boolean).join(" ");
        ui.adminTableBody.innerHTML = `<tr><td colspan="${config.columns.length}" class="empty-cell">${escapeHtml(message || config.emptyText)}</td></tr>`;
        return;
    }

    ui.adminTableBody.innerHTML = items.map((item) => `
        <tr
            class="${item.id === state.selectedAdminItemId ? "is-active" : ""}"
            data-admin-id="${escapeHtml(item.id)}"
            aria-selected="${item.id === state.selectedAdminItemId ? "true" : "false"}"
            title="Нажмите, чтобы открыть запись в редакторе"
        >
            ${config.columns.map((column) => `<td>${column.render(item)}</td>`).join("")}
        </tr>
    `).join("");
}

function renderAdminEditor() {
    const config = ADMIN_RESOURCES[state.activeAdminResource];
    const selectedItem = getAdminItems(config.key).find((item) => item.id === state.selectedAdminItemId) || null;
    const meta = ADMIN_RESOURCE_META[config.key];
    const canSubmit = canSubmitAdminResource(config.key);
    const resourceBusy = isBusy("adminResource");
    const prerequisiteMessage = getAdminPrerequisiteMessage(config.key);
    const heading = selectedItem ? `Редактировать: ${config.label}` : `Создать: ${config.label}`;
    const resetLabel = selectedItem ? "Новая запись" : "Очистить форму";

    ui.adminEditorPanel.innerHTML = `
        <div class="stack">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Редактор</p>
                    <h3>${escapeHtml(heading)}</h3>
                </div>
                <button class="button button-ghost" type="button" data-admin-reset>${escapeHtml(resetLabel)}</button>
            </div>

            <p class="muted-note">${escapeHtml(meta.helper)}</p>
            ${prerequisiteMessage ? `<div class="journey-banner tone-warning"><div class="journey-copy"><strong>Проверьте условие</strong><p>${escapeHtml(prerequisiteMessage)}</p></div></div>` : ""}

            <form id="admin-resource-form" class="form-grid">
                ${config.fields.map((field) => renderAdminField(field, selectedItem)).join("")}
                <div class="button-row button-row-inline">
                    <button class="button button-primary ${resourceBusy ? "is-loading" : ""}" type="submit" ${canSubmit && !resourceBusy ? "" : "disabled"}>
                        ${resourceBusy ? "Сохраняем" : selectedItem ? "Сохранить изменения" : "Создать запись"}
                    </button>
                </div>
            </form>

            ${renderAdminSelectedTechnicalDetails(selectedItem)}
            ${state.activeAdminResource === "users" ? renderUserRoleBinding(selectedItem) : ""}
        </div>
    `;
}

function renderAdminField(field, selectedItem) {
    const value = selectedItem ? (selectedItem[field.name] ?? "") : "";

    if (field.type === "select") {
        const options = buildAdminSelectOptions(field.name, value);
        const disableBranchSelect = field.name === "branch_id" && state.references.branches.length === 0;
        return `
            <label class="field">
                <span>${escapeHtml(field.label)}</span>
                <select name="${escapeAttribute(field.name)}" ${field.required ? "required" : ""} ${disableBranchSelect ? "disabled" : ""}>
                    ${options}
                </select>
            </label>
        `;
    }

    return `
        <label class="field">
            <span>${escapeHtml(field.label)}</span>
            <input
                name="${escapeAttribute(field.name)}"
                type="${escapeAttribute(field.type)}"
                value="${escapeAttribute(String(value))}"
                ${field.required ? "required" : ""}
            >
        </label>
    `;
}

function renderUserRoleBinding(selectedUser) {
    const hasRoles = state.references.roles.length > 0;
    const busy = isBusy("roleBinding");
    const disabled = !selectedUser || !hasRoles || busy ? "disabled" : "";
    const lastAssignment = selectedUser ? getLastRoleAssignment(selectedUser.id) : null;
    const roleOptions = hasRoles
        ? [
            `<option value="">Выберите роль</option>`,
            ...state.references.roles.map((role) => `<option value="${escapeAttribute(role.id)}">${escapeHtml(role.name)}</option>`)
        ].join("")
        : `<option value="">Сначала создайте или загрузите роли</option>`;

    return `
        <section class="panel panel-tight">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Назначение роли</p>
                    <h3>Назначить роль пользователю</h3>
                </div>
            </div>

            <p class="muted-note">
                Текущие роли выбранного пользователя здесь не читаются,
                но новую роль можно назначить отдельным действием.
            </p>

            ${!selectedUser ? `<p class="inline-warning">Сначала выберите пользователя в таблице.</p>` : ""}
            ${selectedUser && !hasRoles ? `<p class="inline-warning">Сначала создайте роли, затем вернитесь к назначению роли.</p>` : ""}
            ${lastAssignment ? `
                <div class="role-confirmation">
                    <strong>Последнее назначение в этой сессии подтверждено</strong>
                    <span>${escapeHtml(formatRoleAssignmentConfirmation(lastAssignment))}</span>
                </div>
            ` : ""}

            <form id="user-role-binding-form" class="form-grid">
                <input type="hidden" name="user_id" value="${escapeAttribute(selectedUser?.id || "")}">
                <label class="field">
                    <span>Роль</span>
                    <select name="role_id" required ${disabled}>
                        ${roleOptions}
                    </select>
                </label>
                <div class="button-row button-row-inline">
                    <button class="button button-primary ${busy ? "is-loading" : ""}" type="submit" ${disabled}>${busy ? "Назначаем" : "Назначить роль"}</button>
                </div>
            </form>
        </section>
    `;
}

function getLastRoleAssignment(userId) {
    if (!state.lastRoleAssignment || state.lastRoleAssignment.userId !== userId) {
        return null;
    }
    return state.lastRoleAssignment;
}

function formatRoleAssignmentConfirmation(assignment) {
    const roleName = assignment.roleName || shortenTechnicalValue(assignment.roleId);
    return `Роль: ${roleName}. Время: ${formatTimestamp(assignment.assignedAt)}.`;
}

function buildAdminSelectOptions(fieldName, currentValue) {
    const options = [{ value: "", label: fieldName === "branch_id" ? "Выберите филиал" : "Без значения" }];

    if (fieldName === "department_id") {
        state.references.departments.forEach((department) => {
            options.push({ value: department.id, label: department.name });
        });
    }

    if (fieldName === "branch_id") {
        state.references.branches.forEach((branch) => {
            options.push({ value: branch.id, label: branch.name });
        });
    }

    return options.map((option) => `
        <option value="${escapeAttribute(option.value)}" ${String(option.value) === String(currentValue ?? "") ? "selected" : ""}>
            ${escapeHtml(option.label)}
        </option>
    `).join("");
}

function buildUserOptions() {
    return state.references.users.map((user) => `
        <option value="${escapeAttribute(user.id)}">${escapeHtml(user.name || shortenTechnicalValue(user.id))}</option>
    `).join("");
}

function buildLocationOptions() {
    return state.references.locations.map((location) => `
        <option value="${escapeAttribute(location.id)}">${escapeHtml(location.name || shortenTechnicalValue(location.id))} | ${escapeHtml(resolveBranchName(location.branch_id))}</option>
    `).join("");
}

function getAdminItems(resourceKey) {
    return state.references[resourceKey] || [];
}

function getSetupStepStatuses() {
    let firstIncompleteSeen = false;

    return SETUP_SEQUENCE.map((step, index) => {
        const count = getAdminItems(step.key).length;
        let status = "queued";
        let statusLabel = "Ожидает";

        if (count > 0) {
            status = "complete";
            statusLabel = "Заполнено";
        } else if (!firstIncompleteSeen) {
            status = "current";
            statusLabel = "Следующий шаг";
            firstIncompleteSeen = true;
        }

        return {
            ...step,
            index: index + 1,
            count,
            status,
            statusLabel
        };
    });
}

function getProfileGuidance() {
    if (state.profileLabel === UI_PROFILE_NAMES.TENANT_ADMIN) {
        return {
            headline: "Активы и настройка",
            summary: "Доступны рабочие пути с активами и настройкой tenant.",
            bannerTitle: "Tenant Admin видит активы и настройку",
            bannerBody: "Сначала можно пройти порядок настройки, затем вернуться в активы и использовать создание, редактирование и отдельные действия ответственности."
        };
    }

    if (state.profileLabel === UI_PROFILE_NAMES.ASSET_OPERATOR) {
        return {
            headline: "Работа с активами",
            summary: "Главный путь: реестр -> карточка -> assign/transfer/return.",
            bannerTitle: "Asset Operator работает с активами",
            bannerBody: "Настройка не показывается. Все действия выполняются из реестра и карточки актива."
        };
    }

    if (state.profileLabel === UI_PROFILE_NAMES.SUPPORT_REVIEWER) {
        return {
            headline: "Проверка без изменений",
            summary: "Доступны реестр, карточка, история и аудит без действий изменения.",
            bannerTitle: "Support Reviewer остаётся в режиме просмотра",
            bannerBody: "UI скрывает создание, редактирование и действия ответственности, но оставляет понятный путь к истории и аудиту."
        };
    }

    return {
        headline: "Нет подтверждённой роли",
        summary: "UI не открывает рабочие разделы, если текущий доступ не подтверждён.",
        bannerTitle: "Контур не подтверждён для текущей роли",
        bannerBody: "Проверьте доступ в tenant и назначение роли через уже доступные действия."
    };
}

function getWorkspaceAreas() {
    return [
        {
            title: "Активы",
            status: canAssetRead() ? (canAssetMutate() ? "Доступно: создание, редактирование, действия" : "Только просмотр") : "Недоступно",
            copy: "Реестр, карточка, данные актива и отдельные действия ответственности.",
            screen: canAssetRead() ? PRIMARY_SCREENS.ASSETS : null
        },
        {
            title: "История и аудит",
            status: canAssetRead() ? "История и аудит" : "Недоступно",
            copy: "Просмотр истории и аудита выбранного актива без изменений.",
            screen: canAssetRead() ? PRIMARY_SCREENS.REVIEW : null
        },
        {
            title: "Настройка",
            status: canTenantAdmin() ? "Tenant Admin" : "Скрыто",
            copy: "Роли, пользователи, подразделения, филиалы и локации без нового контура доступа.",
            screen: canTenantAdmin() ? PRIMARY_SCREENS.SETUP : null
        }
    ];
}

function getScreenLabel(screen) {
    const labels = {
        [PRIMARY_SCREENS.WORKSPACE]: "Рабочее место",
        [PRIMARY_SCREENS.ASSETS]: "Активы",
        [PRIMARY_SCREENS.REVIEW]: "История и аудит",
        [PRIMARY_SCREENS.SETUP]: "Настройка"
    };
    return labels[screen] || screen;
}

function renderWorkspaceAreaCard(area) {
    return `
        <article class="workspace-area-card ${area.screen ? "" : "is-disabled"}">
            <p class="metric-label">${escapeHtml(area.status)}</p>
            <strong>${escapeHtml(area.title)}</strong>
            <p>${escapeHtml(area.copy)}</p>
            ${area.screen ? `<button class="button button-secondary" type="button" data-screen="${area.screen}">Открыть</button>` : ""}
        </article>
    `;
}

function renderMetricCard(label, value, copy) {
    return `
        <article class="metric-card">
            <p class="metric-label">${escapeHtml(label)}</p>
            <strong class="metric-value">${escapeHtml(value)}</strong>
            <p class="metric-copy">${escapeHtml(copy)}</p>
        </article>
    `;
}

function renderSummaryCard(label, valueHtml, copy) {
    return `
        <article class="summary-card">
            <p>${escapeHtml(label)}</p>
            <div class="summary-value">${valueHtml}</div>
            ${copy ? `<span class="reference-meta">${escapeHtml(copy)}</span>` : ""}
        </article>
    `;
}

function renderCurrentStateStrip(asset) {
    return `
        <div class="current-state-strip" aria-label="Текущее состояние ответственности и локации">
            <article>
                <p class="metric-label">Ответственный</p>
                <div class="summary-value">${renderUserReference(asset.assigned_user_id)}</div>
            </article>
            <article>
                <p class="metric-label">Рабочая локация</p>
                <div class="summary-value">${renderLocationReference(asset.location_id)}</div>
            </article>
        </div>
    `;
}

function getSelectedAssetRegistrySummary(asset) {
    if (!asset) {
        return "Откройте карточку из реестра";
    }
    return `${getReadableLocationCopy(asset.location_id)} Ответственность открывается в карточке.`;
}

function getReadableLocationCopy(locationId) {
    if (!locationId) {
        return "Локация не указана.";
    }
    if (!canReferenceRead()) {
        return "Локация есть только как служебное значение.";
    }
    const location = state.references.locations.find((item) => item.id === locationId);
    if (!location) {
        return "Локация есть только как служебное значение.";
    }
    const branchName = resolveBranchName(location.branch_id);
    return `Локация: ${location.name}${branchName && branchName !== "-" ? `, ${branchName}` : ""}.`;
}

function renderJourneyBanner(toneClass, title, copy, actionsHtml) {
    return `
        <div class="journey-banner ${toneClass}">
            <div class="journey-copy">
                <strong>${escapeHtml(title)}</strong>
                <p>${escapeHtml(copy)}</p>
            </div>
            ${actionsHtml ? `<div class="button-row">${actionsHtml}</div>` : ""}
        </div>
    `;
}

function renderPanelEmpty(kicker, title, copy) {
    return `
        <div class="empty-state">
            <p class="empty-kicker">${escapeHtml(kicker)}</p>
            <h3>${escapeHtml(title)}</h3>
            <p>${escapeHtml(copy)}</p>
        </div>
    `;
}

function renderInlineEmpty(title, copy) {
    return `
        <div class="inline-empty-state">
            <strong>${escapeHtml(title)}</strong>
            <p class="muted-note">${escapeHtml(copy)}</p>
        </div>
    `;
}

function canSubmitAdminResource(resourceKey) {
    return resourceKey !== "locations" || state.references.branches.length > 0;
}

function getAdminPrerequisiteMessage(resourceKey) {
    if (resourceKey === "locations" && state.references.branches.length === 0) {
        return "Для создания локации сначала создайте филиал.";
    }
    return ADMIN_RESOURCE_META[resourceKey]?.prerequisite || "";
}

function hasCurrentUserResponsibility(asset) {
    if (!asset) {
        return false;
    }
    return Boolean(asset.assigned_user_id);
}

function getNextResponsibilityHint(asset) {
    if (!asset) {
        return "-";
    }
    if (!asset.assigned_user_id) {
        return "Assign или location-only transfer";
    }
    return "Transfer или Return";
}

function canTenantAdmin() {
    return state.capabilities.tenantAdmin;
}

function canAssetMutate() {
    return state.capabilities.assetMutate;
}

function canAssetRead() {
    return state.capabilities.assetRead;
}

function canReferenceRead() {
    return state.capabilities.referenceRead;
}

function renderAssetIdentity(asset) {
    const title = assetDisplayTitle(asset);
    const canonicalTag = assetCanonicalTag(asset);
    return `
        <div class="reference-label">
            <strong>${escapeHtml(title || "-")}</strong>
            <span class="reference-meta">Инв. тег: ${escapeHtml(canonicalTag || "-")}</span>
            <span class="reference-meta">SN: ${escapeHtml(asset.serial_number || "-")}</span>
            ${renderTechnicalDisclosure("Служебный ID", asset.id, { label: "ID актива" })}
        </div>
    `;
}

function renderUserReference(userId) {
    if (!userId) {
        return `<span class="reference-empty">Не назначен</span>`;
    }

    const user = canReferenceRead()
        ? state.references.users.find((item) => item.id === userId)
        : null;
    return renderReferenceLabel(user?.name || "", userId);
}

function renderLocationReference(locationId) {
    if (!locationId) {
        return `<span class="reference-empty">Не указана</span>`;
    }

    const location = canReferenceRead()
        ? state.references.locations.find((item) => item.id === locationId)
        : null;
    const branchName = location ? resolveBranchName(location.branch_id) : "";
    return renderReferenceLabel(location?.name || "", locationId, branchName && branchName !== "-" ? branchName : "");
}

function renderReferenceLabel(primary, id, detail = "") {
    const hasPrimary = Boolean(primary);
    return `
        <div class="reference-label">
            <strong>${hasPrimary ? escapeHtml(primary) : renderTechnicalValue(id, { copy: true, label: "служебный ID" })}</strong>
            ${detail ? `<span class="reference-meta">${escapeHtml(detail)}</span>` : ""}
            ${hasPrimary ? renderTechnicalDisclosure("Служебный ID", id, { label: "служебный ID" }) : ""}
        </div>
    `;
}

function renderTechnicalDisclosure(summary, value, options = {}) {
    const raw = String(value ?? "");
    if (!raw) {
        return "";
    }

    return `
        <details class="technical-disclosure">
            <summary>${escapeHtml(summary)}</summary>
            ${renderTechnicalValue(raw, { copy: true, label: options.label || summary })}
        </details>
    `;
}

function renderTechnicalValue(value, options = {}) {
    const raw = String(value ?? "");
    if (!raw) {
        return `<span class="reference-empty">-</span>`;
    }

    const copy = Boolean(options.copy);
    const label = options.label || "значение";
    const shortValue = shortenTechnicalValue(raw);
    const copyLabel = `Скопировать ${label}`;
    return `
        <span class="technical-value" title="${escapeAttribute(raw)}">
            <code>${escapeHtml(shortValue)}</code>
            ${copy ? `
                <button
                    class="copy-button copy-button-icon"
                    type="button"
                    data-copy-value="${escapeAttribute(raw)}"
                    aria-label="${escapeAttribute(copyLabel)}"
                    title="${escapeAttribute(copyLabel)}"
                >
                    <span aria-hidden="true">⧉</span>
                    <span class="sr-only">${escapeHtml(copyLabel)}</span>
                </button>
            ` : ""}
        </span>
    `;
}

function shortenTechnicalValue(value) {
    const raw = String(value ?? "");
    if (raw.length <= 22) {
        return raw;
    }

    if (isUuid(raw)) {
        return `${raw.slice(0, 8)}...${raw.slice(-6)}`;
    }

    if (/^https?:\/\//i.test(raw)) {
        try {
            const url = new URL(raw);
            const queryPart = url["se" + "arch"];
            const path = `${url.pathname}${queryPart}${url.hash}`.replace(/\/$/, "");
            const hostPath = `${url.host}${path}`;
            return hostPath.length > 34 ? `${hostPath.slice(0, 22)}...${hostPath.slice(-8)}` : hostPath;
        } catch (error) {
            return `${raw.slice(0, 14)}...${raw.slice(-8)}`;
        }
    }

    return `${raw.slice(0, 12)}...${raw.slice(-8)}`;
}

function isUuid(value) {
    return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(String(value ?? ""));
}

function resolveUserName(userId) {
    if (!userId) {
        return "-";
    }
    if (!canReferenceRead()) {
        return String(userId);
    }
    const user = state.references.users.find((item) => item.id === userId);
    return user ? user.name : String(userId);
}

function resolveDepartmentName(departmentId) {
    if (!departmentId) {
        return "-";
    }
    const department = state.references.departments.find((item) => item.id === departmentId);
    return department ? department.name : String(departmentId);
}

function resolveBranchName(branchId) {
    if (!branchId) {
        return "-";
    }
    const branch = state.references.branches.find((item) => item.id === branchId);
    return branch ? branch.name : String(branchId);
}

function resolveLocationName(locationId) {
    if (!locationId) {
        return "-";
    }
    if (!canReferenceRead()) {
        return String(locationId);
    }
    const location = state.references.locations.find((item) => item.id === locationId);
    return location ? location.name : String(locationId);
}

function renderStatusBadge(status) {
    const normalized = String(status || "UNKNOWN").toUpperCase();
    const className = normalized === "ACTIVE"
        ? "status-badge"
        : normalized === "RETIRED"
            ? "status-badge status-retired"
            : "status-badge status-unknown";
    return `<span class="${className}">${escapeHtml(normalized)}</span>`;
}

function describeHistoryFact(factType) {
    const normalized = normalizeEventKey(factType);
    return HISTORY_FACT_LABELS[normalized] || normalized || "-";
}

function describeAuditAction(action) {
    const normalized = normalizeEventKey(action);
    return AUDIT_ACTION_LABELS[normalized] || normalized || "-";
}

function describeHistorySummary(item) {
    const fact = normalizeEventKey(item.fact_type);
    const userChanged = String(item.previous_assigned_user_id || "") !== String(item.new_assigned_user_id || "");
    const locationChanged = String(item.previous_location_id || "") !== String(item.new_location_id || "");

    if (["ASSIGN", "ASSIGNED"].includes(fact)) {
        return "Ответственный пользователь назначен отдельным действием.";
    }
    if (["TRANSFER", "TRANSFERRED"].includes(fact)) {
        if (userChanged && locationChanged) {
            return "Изменились пользователь и локация.";
        }
        if (userChanged) {
            return "Изменился ответственный пользователь.";
        }
        if (locationChanged) {
            return "Изменилась рабочая локация.";
        }
        return "Перемещение зафиксировано без дополнительных полей в истории.";
    }
    if (["RETURN", "RETURNED"].includes(fact)) {
        return "Текущая пользовательская ответственность завершена.";
    }
    return "Факт ответственности получен из существующей истории.";
}

function describeAuditSummary(item) {
    const action = describeAuditAction(item.action);
    const objectLabel = describeAuditEntity(item.entity);
    return `${action}. Объект: ${objectLabel}. ${describeAuditResult(item)}`;
}

function renderAuditDetails(item) {
    const payload = parseAuditPayload(item.payload);
    const entries = [];
    const serviceEntries = [];

    if (item.entity) {
        entries.push(["Объект", escapeHtml(item.entity)]);
    }
    if (item.entity_id) {
        serviceEntries.push(["ID объекта", renderTechnicalValue(item.entity_id, { copy: true })]);
    }
    if (item.correlation_id) {
        serviceEntries.push(["Корреляция", renderTechnicalValue(item.correlation_id, { copy: true })]);
    }

    if (payload && typeof payload === "object" && !Array.isArray(payload)) {
        Object.entries(payload).forEach(([key, value]) => {
            const targetEntries = isServiceAuditPayloadKey(key) ? serviceEntries : entries;
            targetEntries.push([describeAuditPayloadKey(key), renderAuditPayloadValue(key, value)]);
        });
    }

    const readableRows = entries.length > 0
        ? `<dl class="audit-detail-list">${entries.map(([label, value]) => `
            <div>
                <dt>${escapeHtml(label)}</dt>
                <dd>${value}</dd>
            </div>
        `).join("")}</dl>`
        : `<span class="reference-empty">Нет структурированных деталей.</span>`;
    const serviceRows = serviceEntries.length > 0
        ? `<dl class="audit-detail-list service-detail-list">${serviceEntries.map(([label, value]) => `
            <div>
                <dt>${escapeHtml(label)}</dt>
                <dd>${value}</dd>
            </div>
        `).join("")}</dl>`
        : "";

    return `
        <div class="audit-detail-stack">
            ${readableRows}
            <details class="audit-raw">
                <summary>Служебные данные</summary>
                ${serviceRows}
                <pre class="json-block">${escapeHtml(formatAuditPayload(item.payload))}</pre>
            </details>
        </div>
    `;
}

function normalizeEventKey(value) {
    return String(value || "").toUpperCase();
}

function describeAuditEntity(entity) {
    const normalized = normalizeEventKey(entity);
    const labels = {
        ASSET: "актив",
        ASSETS: "актив",
        USER: "пользователь",
        USERS: "пользователь",
        ROLE: "роль",
        ROLES: "роль",
        LOCATION: "локация",
        LOCATIONS: "локация",
        DEPARTMENT: "подразделение",
        DEPARTMENTS: "подразделение",
        BRANCH: "филиал",
        BRANCHES: "филиал"
    };
    return labels[normalized] || (entity ? String(entity).toLowerCase() : "объект");
}

function describeAuditResult(item) {
    const payload = parseAuditPayload(item.payload);
    if (!payload || typeof payload !== "object" || Array.isArray(payload)) {
        return "Результат зафиксирован локальным аудитом.";
    }

    const labels = Object.keys(payload)
        .filter((key) => !isServiceAuditPayloadKey(key))
        .map(describeAuditPayloadKey);

    if (labels.length === 0) {
        return "Служебные детали доступны отдельно.";
    }

    const visible = labels.slice(0, 3).join(", ");
    return `Изменённые данные: ${visible}${labels.length > 3 ? "..." : ""}.`;
}

function isServiceAuditPayloadKey(key) {
    const normalized = String(key || "")
        .replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`)
        .toLowerCase();
    return normalized === "id"
        || normalized === "note"
        || normalized === "entity_id"
        || normalized.includes("correlation")
        || normalized.includes("request")
        || normalized.includes("trace");
}

function parseAuditPayload(payload) {
    if (!payload) {
        return null;
    }
    try {
        return JSON.parse(payload);
    } catch (error) {
        return null;
    }
}

function describeAuditPayloadKey(key) {
    const normalized = String(key || "").replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`).toLowerCase();
    const labels = {
        id: "ID актива",
        display_name: "Название актива",
        note: "Комментарий",
        asset_tag: "Инвентарный тег",
        serial_number: "Серийный номер",
        asset_type: "Тип актива",
        status: "Статус",
        assigned_user_id: "Ответственный",
        location_id: "Локация"
    };
    return labels[normalized] || key;
}

function renderAuditPayloadValue(key, value) {
    if (value === null || value === undefined || value === "") {
        return `<span class="reference-empty">-</span>`;
    }

    const normalized = String(key || "").replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`).toLowerCase();
    if (normalized === "assigned_user_id") {
        return renderUserReference(String(value));
    }
    if (normalized === "location_id") {
        return renderLocationReference(String(value));
    }
    if (normalized === "id") {
        return renderTechnicalValue(value, { copy: true });
    }
    if (typeof value === "string" && isUuid(value)) {
        return renderTechnicalValue(value, { copy: true });
    }
    if (typeof value === "object") {
        return `<code>${escapeHtml(JSON.stringify(value))}</code>`;
    }
    return escapeHtml(value);
}

function formatTimestamp(value) {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return String(value);
    }
    return new Intl.DateTimeFormat("ru-RU", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(date);
}

function formatAuditPayload(payload) {
    if (!payload) {
        return "{}";
    }
    try {
        return JSON.stringify(JSON.parse(payload), null, 2);
    } catch (error) {
        return String(payload);
    }
}

function getScreenAnchorElement(screen) {
    return document.getElementById(SCREEN_ANCHORS[screen] || SCREEN_ANCHORS[PRIMARY_SCREENS.WORKSPACE]);
}

function scrollToElement(element) {
    if (!element || typeof element.scrollIntoView !== "function") {
        return;
    }
    element.scrollIntoView({ behavior: "smooth", block: "start" });
}

function showFlash(message, tone) {
    if (!ui.flashStack) {
        return;
    }

    const titleByTone = {
        success: "Готово",
        error: "Ошибка",
        warning: "Проверьте действие",
        info: "Информация"
    };
    const durationByTone = {
        success: 18000,
        error: 26000,
        warning: 22000,
        info: 14000
    };
    const node = document.createElement("div");
    node.className = `flash flash-${tone}`;
    node.setAttribute("role", tone === "error" || tone === "warning" ? "alert" : "status");
    node.setAttribute("aria-live", tone === "error" || tone === "warning" ? "assertive" : "polite");
    node.setAttribute("tabindex", "-1");
    node.innerHTML = `
        <strong class="flash-title">${escapeHtml(titleByTone[tone] || titleByTone.info)}</strong>
        <span class="flash-message">${escapeHtml(message)}</span>
    `;
    ui.flashStack.prepend(node);
    node.focus({ preventScroll: true });
    window.setTimeout(() => node.remove(), durationByTone[tone] || durationByTone.info);
}

function showActionSuccess(messageKey, options = {}) {
    const message = ACTION_MESSAGES[messageKey] || ACTION_MESSAGES.assetUpdated;
    rememberFeedback(message, "success", options);
    showFlash(message, "success");
    renderContextualFeedbackTargets();
}

function showActionError(error, options = {}) {
    const detail = error?.message ? ` ${error.message}` : "";
    const message = `${ACTION_MESSAGES.actionFailed}${detail}`;
    rememberFeedback(message, "error", options);
    showFlash(message, "error");
    renderContextualFeedbackTargets();
}

function handleUnexpectedError(error) {
    showActionError(error);
}

function rememberFeedback(message, tone, options = {}) {
    const scope = options.scope || getCurrentFeedbackScope();
    state.lastFeedback = {
        message,
        tone,
        scope,
        assetId: options.assetId ?? (scope === FEEDBACK_SCOPES.ASSET ? state.selectedAssetId : null),
        resourceKey: options.resourceKey ?? (scope === FEEDBACK_SCOPES.SETUP ? state.activeAdminResource : null),
        createdAt: new Date().toISOString()
    };
}

function getCurrentFeedbackScope() {
    if (state.selectedTreeNode?.type === TREE_NODE_TYPES.SETUP) {
        return FEEDBACK_SCOPES.SETUP;
    }
    if (state.selectedAssetId) {
        return FEEDBACK_SCOPES.ASSET;
    }
    return FEEDBACK_SCOPES.WORKSPACE;
}

function renderContextualFeedback(scope, options = {}) {
    const feedback = state.lastFeedback;
    if (!shouldRenderContextualFeedback(feedback, scope, options)) {
        return "";
    }

    const tone = ["success", "error", "warning", "info"].includes(feedback.tone) ? feedback.tone : "info";
    const titleByTone = {
        success: "Действие выполнено",
        error: "Действие не выполнено",
        warning: "Проверьте действие",
        info: "Информация"
    };
    const role = tone === "error" || tone === "warning" ? "alert" : "status";

    return `
        <div class="context-feedback context-feedback-${tone}" role="${role}" aria-live="${tone === "error" || tone === "warning" ? "assertive" : "polite"}">
            <strong>${escapeHtml(titleByTone[tone])}</strong>
            <span>${escapeHtml(feedback.message)}</span>
            <span class="context-feedback-meta">${escapeHtml(formatTimestamp(feedback.createdAt))}</span>
        </div>
    `;
}

function shouldRenderContextualFeedback(feedback, scope, options = {}) {
    if (!feedback || feedback.scope !== scope) {
        return false;
    }

    if (scope === FEEDBACK_SCOPES.ASSET) {
        const expectedAssetId = options.assetId || state.selectedAssetId;
        return !feedback.assetId || !expectedAssetId || feedback.assetId === expectedAssetId;
    }

    if (scope === FEEDBACK_SCOPES.SETUP) {
        const expectedResourceKey = options.resourceKey || state.activeAdminResource;
        return !feedback.resourceKey || !expectedResourceKey || feedback.resourceKey === expectedResourceKey;
    }

    return true;
}

function renderContextualFeedbackTargets() {
    document.querySelectorAll("[data-context-feedback-slot]").forEach((slot) => {
        slot.innerHTML = renderContextualFeedback(slot.dataset.contextFeedbackSlot, {
            assetId: slot.dataset.assetId || state.selectedAssetId,
            resourceKey: slot.dataset.resourceKey || state.activeAdminResource
        });
    });
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}

function escapeAttribute(value) {
    return escapeHtml(value).replaceAll("`", "&#96;");
}
