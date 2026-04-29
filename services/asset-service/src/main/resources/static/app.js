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

const ASSET_SECTIONS = [
    { key: "overview", label: "Обзор" },
    { key: "master", label: "Данные актива" },
    { key: "responsibility", label: "Ответственность" },
    { key: "actions", label: "Действия" },
    { key: "history", label: "История и аудит" }
];

const REVIEW_SECTIONS = [
    { key: "history", label: "История" },
    { key: "audit", label: "Аудит" }
];

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
    ASSIGN: "Assign",
    ASSIGNED: "Assign",
    TRANSFER: "Transfer",
    TRANSFERRED: "Transfer",
    RETURN: "Return",
    RETURNED: "Return",
    RETIRE: "Retire",
    RETIRED: "Retire"
};

const AUDIT_ACTION_LABELS = {
    CREATE: "Создание актива",
    CREATE_ASSET: "Создание актива",
    UPDATE: "Обновление данных актива",
    UPDATE_ASSET: "Обновление данных актива",
    ASSIGN: "Assign",
    ASSIGN_ASSET: "Assign",
    TRANSFER: "Transfer",
    TRANSFER_ASSET: "Transfer",
    RETURN: "Return",
    RETURN_ASSET: "Return",
    RETIRE: "Retire"
};

const ADMIN_RESOURCES = {
    roles: {
        key: "roles",
        label: "Роли",
        path: "/api/v1/roles",
        emptyText: "Роли ещё не созданы.",
        columns: [
            { label: "Название", render: (item) => escapeHtml(item.name) },
            { label: "Служебный ID", render: (item) => `<span class="reference-meta"><code>${escapeHtml(item.id)}</code></span>` }
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
            { label: "Подразделение", render: (item) => escapeHtml(resolveDepartmentName(item.department_id)) },
            { label: "Служебный ID", render: (item) => `<span class="reference-meta"><code>${escapeHtml(item.id)}</code></span>` }
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
            { label: "Название", render: (item) => escapeHtml(item.name) },
            { label: "Служебный ID", render: (item) => `<span class="reference-meta"><code>${escapeHtml(item.id)}</code></span>` }
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
            { label: "Название", render: (item) => escapeHtml(item.name) },
            { label: "Служебный ID", render: (item) => `<span class="reference-meta"><code>${escapeHtml(item.id)}</code></span>` }
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
            { label: "Филиал", render: (item) => escapeHtml(resolveBranchName(item.branch_id)) },
            { label: "Служебный ID", render: (item) => `<span class="reference-meta"><code>${escapeHtml(item.id)}</code></span>` }
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
    activeScreen: PRIMARY_SCREENS.WORKSPACE,
    activeAssetSection: "overview",
    activeReviewSection: "history",
    activeAdminResource: "roles",
    selectedAdminItemId: null
};

document.addEventListener("DOMContentLoaded", initialize);

function initialize() {
    cacheElements();
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
    ui.assetServiceOrigin = document.getElementById("asset-service-origin");
    ui.roleSummaryBadge = document.getElementById("role-summary-badge");
    ui.activeScreenLabel = document.getElementById("active-screen-label");
    ui.flashStack = document.getElementById("flash-stack");
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
    ui.refreshAll = document.getElementById("refresh-all");
    ui.clearSession = document.getElementById("clear-session");
    ui.refreshAssets = document.getElementById("refresh-assets");
    ui.refreshAdminResource = document.getElementById("refresh-admin-resource");
}

function bindStaticEvents() {
    ui.sessionForm.addEventListener("submit", onSessionSubmit);
    ui.refreshAll.addEventListener("click", () => refreshAllData());
    ui.clearSession.addEventListener("click", clearSession);
    ui.refreshAssets.addEventListener("click", () => refreshAssets().catch(handleUnexpectedError));
    ui.refreshAdminResource.addEventListener("click", () => refreshAdminResource().catch(handleUnexpectedError));
    ui.screenNav.addEventListener("click", onScreenNavClick);
    ui.workspaceOverview.addEventListener("click", onScreenNavClick);
    ui.assetCreateForm.addEventListener("submit", onAssetCreateSubmit);
    ui.assetJourneyBanner.addEventListener("click", onAssetInteractionClick);
    ui.assetRegistryOverview.addEventListener("click", onAssetInteractionClick);
    ui.assetRegistryBody.addEventListener("click", onAssetRegistryClick);
    ui.assetDetailPanel.addEventListener("submit", onAssetDetailSubmit);
    ui.assetDetailPanel.addEventListener("click", onAssetInteractionClick);
    ui.reviewSectionNav.addEventListener("click", onReviewInteractionClick);
    ui.reviewDetailPanel.addEventListener("click", onReviewInteractionClick);
    ui.adminResourceNav.addEventListener("click", onAdminResourceClick);
    ui.setupOverview.addEventListener("click", onAdminResourceClick);
    ui.adminTableBody.addEventListener("click", onAdminTableClick);
    ui.adminEditorPanel.addEventListener("submit", onAdminEditorSubmit);
    ui.adminEditorPanel.addEventListener("click", onAdminEditorClick);
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
    state.activeScreen = PRIMARY_SCREENS.WORKSPACE;
    state.activeAssetSection = "overview";
    state.activeReviewSection = "history";
    state.activeAdminResource = "roles";
    state.selectedAdminItemId = null;

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
    return true;
}

async function onSessionSubmit(event) {
    event.preventDefault();
    await connectContour();
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

        render();
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

function buildAssetUpsertPayload(formData) {
    return {
        asset_tag: formDataValue(formData, "asset_tag"),
        serial_number: formDataValue(formData, "serial_number"),
        asset_type: formDataValue(formData, "asset_type")
    };
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

function onScreenNavClick(event) {
    const button = event.target.closest("[data-screen]");
    if (!button) {
        return;
    }
    state.activeScreen = button.dataset.screen;
    render();
}

async function onAssetCreateSubmit(event) {
    event.preventDefault();
    if (!canAssetMutate()) {
        showFlash("Текущая роль не имеет mutate-доступа к активам.", "warning");
        return;
    }

    const payload = buildAssetUpsertPayload(new FormData(event.currentTarget));

    try {
        const created = await requestJson("asset", "/api/v1/assets", {
            method: "POST",
            body: payload
        });
        event.currentTarget.reset();
        state.selectedAssetId = created.id;
        state.activeAssetSection = "overview";
        await refreshAssets();
        scrollToElement(ui.assetDetailPanel);
        showFlash(`Актив ${created.asset_tag} создан и открыт в карточке.`, "success");
    } catch (error) {
        showFlash(error.message, "error");
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
            const payload = buildAssetUpsertPayload(new FormData(form));
            await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}`, {
                method: "PATCH",
                body: payload
            });
            state.activeAssetSection = "master";
            await refreshAssets();
            scrollToElement(ui.assetDetailPanel);
            showFlash("Данные карточки обновлены без изменения ответственности.", "success");
            return;
        }

        if (form.id === "asset-assign-form") {
            const targetUserId = formDataValue(new FormData(form), "target_user_id");
            if (!targetUserId) {
                showFlash("Для assign выберите пользователя.", "warning");
                return;
            }
            await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}/assign`, {
                method: "POST",
                body: { target_user_id: targetUserId }
            });
            state.activeAssetSection = "history";
            state.activeReviewSection = "history";
            await refreshAssets();
            scrollToElement(ui.assetDetailPanel);
            showFlash("Assign выполнен отдельным действием.", "success");
            return;
        }

        if (form.id === "asset-transfer-form") {
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
            scrollToElement(ui.assetDetailPanel);
            showFlash("Transfer выполнен отдельным действием.", "success");
            return;
        }

        if (form.id === "asset-return-form") {
            if (!hasCurrentUserResponsibility(state.selectedAsset)) {
                showFlash("Return завершает только текущую пользовательскую ответственность. В карточке нет назначенного пользователя.", "warning");
                return;
            }
            await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}/return`, {
                method: "POST"
            });
            state.activeAssetSection = "history";
            state.activeReviewSection = "history";
            await refreshAssets();
            scrollToElement(ui.assetDetailPanel);
            showFlash("Return выполнен отдельным действием.", "success");
        }
    } catch (error) {
        showFlash(error.message, "error");
    }
}

function onAssetInteractionClick(event) {
    const screenButton = event.target.closest("[data-screen]");
    if (screenButton) {
        state.activeScreen = screenButton.dataset.screen;
        render();
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
        loadAssetBundle(state.selectedAssetId)
            .then(() => scrollToElement(ui.assetDetailPanel))
            .catch(handleUnexpectedError);
        return;
    }

    const createButton = event.target.closest("[data-focus-create]");
    if (createButton && !ui.assetCreatePanel.hidden) {
        scrollToElement(ui.assetCreatePanel);
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

function onReviewInteractionClick(event) {
    const screenButton = event.target.closest("[data-screen]");
    if (screenButton) {
        state.activeScreen = screenButton.dataset.screen;
        render();
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
        loadAssetBundle(state.selectedAssetId)
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
            const config = ADMIN_RESOURCES[state.activeAdminResource];
            if (!canSubmitAdminResource(config.key)) {
                showFlash(getAdminPrerequisiteMessage(config.key), "warning");
                return;
            }

            const payload = buildAdminPayload(config, new FormData(form));
            const isUpdate = Boolean(state.selectedAdminItemId);
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
            render();
            scrollToElement(ui.adminEditorPanel);
            showFlash(`${config.label} ${isUpdate ? "обновлены" : "созданы"}.`, "success");
            return;
        }

        if (form.id === "user-role-binding-form") {
            const userId = formDataValue(new FormData(form), "user_id");
            const roleId = formDataValue(new FormData(form), "role_id");
            if (!userId || !roleId) {
                showFlash("Для назначения роли выберите пользователя и роль.", "warning");
                return;
            }
            await requestJson("user", `/api/v1/users/${userId}/roles`, {
                method: "POST",
                body: { role_id: roleId }
            });
            render();
            scrollToElement(ui.adminEditorPanel);
            showFlash("Роль назначена через уже доступное действие.", "success");
        }
    } catch (error) {
        showFlash(error.message, "error");
    }
}

function onAdminEditorClick(event) {
    const resetButton = event.target.closest("[data-admin-reset]");
    if (resetButton) {
        state.selectedAdminItemId = null;
        render();
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
    renderRoleSummary();
    renderScreenNav();
    renderWorkspaceScreen();
    renderAssetsScreen();
    renderReviewScreen();
    renderAdminScreen();
    ui.activeScreenLabel.textContent = getScreenLabel(state.activeScreen);
    ui.assetServiceOrigin.textContent = window.location.origin;
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
    const selectedLabel = state.selectedAsset ? state.selectedAsset.asset_tag : "Актив не выбран";
    const selectedState = state.selectedAsset
        ? "Ответственность и локация доступны в карточке."
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
                <div>
                    <p class="section-kicker">Карточка актива</p>
                    <h3>${escapeHtml(asset.asset_tag)}</h3>
                    <p class="reference-meta">ID актива: ${escapeHtml(asset.id)}</p>
                </div>
                <div class="button-row">
                    <button class="button button-secondary" data-refresh-asset type="button">Обновить карточку</button>
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
    if (state.activeAssetSection === "master") {
        return renderAssetMasterSection(asset, canMutate);
    }

    if (state.activeAssetSection === "responsibility") {
        return renderAssetResponsibilitySection(asset, hasCurrentUser);
    }

    if (state.activeAssetSection === "actions") {
        return renderAssetActionsSection(canMutate, hasUsers, hasLocations, hasCurrentUser);
    }

    if (state.activeAssetSection === "history") {
        return renderAssetHistoryAuditSection();
    }

    return renderAssetOverviewSection(asset, canMutate);
}

function renderAssetOverviewSection(asset, canMutate) {
    return `
        <section class="asset-section">
            <div class="journey-banner tone-info">
                <div class="journey-copy">
                    <strong>Статус и ответственность разделены</strong>
                    <p>ACTIVE / RETIRED — это статус актива. Ответственный пользователь и рабочая локация показаны отдельно и меняются отдельными действиями.</p>
                </div>
            </div>

            <div class="asset-summary-grid">
                ${renderSummaryCard("Инвентарный тег", escapeHtml(asset.asset_tag), "Главный пользовательский идентификатор в реестре.")}
                ${renderSummaryCard("Статус", renderStatusBadge(asset.status), "Не кодирует текущую ответственность.")}
                ${renderSummaryCard("Тип", escapeHtml(asset.asset_type), "Поле карточки актива.")}
                ${renderSummaryCard("Серийный номер", escapeHtml(asset.serial_number), "Поле карточки актива.")}
                ${renderSummaryCard("Ответственный", renderUserReference(asset.assigned_user_id), "Текущая пользовательская ответственность.")}
                ${renderSummaryCard("Локация", renderLocationReference(asset.location_id), "Текущая рабочая локация.")}
            </div>

            <div class="button-row">
                ${canMutate ? `<button class="button button-secondary" type="button" data-asset-section="master">Редактировать данные актива</button>` : ""}
                ${canMutate ? `<button class="button button-secondary" type="button" data-asset-section="actions">Открыть действия</button>` : ""}
                <button class="button button-ghost" type="button" data-asset-section="history">История и аудит</button>
            </div>
        </section>
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
                    ${renderSummaryCard("ID актива", `<code>${escapeHtml(asset.id)}</code>`, "Справочная техническая ссылка.")}
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
                    <p class="section-kicker">Данные актива</p>
                    <h3>Редактировать данные актива</h3>
                </div>
                <span class="summary-pill">Только данные актива</span>
            </div>

            <p class="muted-note">
                Эта форма управляет только инвентарным тегом, серийным номером и типом. Она не отправляет
                <code>assigned_user_id</code> и <code>location_id</code>.
            </p>

            <form id="asset-update-form" class="form-grid">
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
                <div class="button-row button-row-inline">
                    <button class="button button-primary" type="submit">Сохранить данные актива</button>
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

function renderAssetActionsSection(canMutate, hasUsers, hasLocations, hasCurrentUser) {
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
                    <p class="section-kicker">Отдельные действия</p>
                    <h3>Assign / Transfer / Return</h3>
                </div>
                <span class="summary-pill">${escapeHtml(hasCurrentUser ? "Есть текущий пользователь" : "Нет текущего пользователя")}</span>
            </div>

            <div class="action-grid">
                ${renderAssignCard(hasUsers, hasCurrentUser)}
                ${renderTransferCard(hasUsers, hasLocations, hasCurrentUser)}
                ${renderReturnCard(hasCurrentUser)}
            </div>
        </section>
    `;
}

function renderAssetHistoryAuditSection() {
    return `
        <section class="asset-section">
            <div class="detail-grid">
                <section>
                    <div class="panel-heading">
                        <div>
                            <p class="section-kicker">History</p>
                            <h3>История ответственности</h3>
                        </div>
                        <span class="summary-pill">${escapeHtml(String(state.assetHistory.length))}</span>
                    </div>
                    <p class="muted-note">Журнал только для просмотра показывает доступные значения до и после без дополнительной интерпретации.</p>
                    ${renderHistoryTable()}
                </section>

                <section>
                    <div class="panel-heading">
                        <div>
                            <p class="section-kicker">Audit</p>
                            <h3>Локальный аудит</h3>
                        </div>
                        <span class="summary-pill">${escapeHtml(String(state.assetAudit.length))}</span>
                    </div>
                    <p class="muted-note">Аудит показывает actor, time, result и детали изменения только из уже доступных полей.</p>
                    ${renderAuditTable()}
                </section>
            </div>

            <div class="button-row">
                <button class="button button-secondary" type="button" data-screen="${PRIMARY_SCREENS.REVIEW}">Открыть историю и аудит</button>
            </div>
        </section>
    `;
}

function renderAssignCard(hasUsers, hasCurrentUser) {
    const disabled = !hasUsers || hasCurrentUser ? "disabled" : "";
    return `
        <article class="action-card">
            <div class="action-card-header">
                <p class="section-kicker">Assign</p>
                <h4>Первичное назначение</h4>
            </div>
            <p class="muted-note">Используйте assign для явного назначения ответственного пользователя. Если у актива уже есть ответственность, переходите в transfer.</p>
            ${!hasUsers ? `<p class="inline-warning">Для assign нужен хотя бы один пользователь в справочнике.</p>` : ""}
            ${hasCurrentUser ? `<p class="inline-warning">В карточке уже есть текущий пользователь. Для смены пользователя используйте transfer.</p>` : ""}
            <form id="asset-assign-form" class="form-grid">
                <label class="field">
                    <span>Назначить пользователю</span>
                    <select name="target_user_id" required ${disabled}>
                        <option value="">Выберите пользователя</option>
                        ${buildUserOptions()}
                    </select>
                </label>
                <div class="button-row button-row-inline">
                    <button class="button button-primary" type="submit" ${disabled}>Выполнить assign</button>
                </div>
            </form>
        </article>
    `;
}

function renderTransferCard(hasUsers, hasLocations, hasCurrentUser) {
    const blockedByReferences = !hasUsers && !hasLocations;
    const disabled = blockedByReferences ? "disabled" : "";
    const warning = blockedByReferences
            ? "Для transfer нужен хотя бы один пользователь или локация в справочнике."
            : !hasCurrentUser
                ? "Без текущего пользователя transfer допустим только для смены локации. Первое назначение пользователя выполняется через assign."
            : "";

    return `
        <article class="action-card">
            <div class="action-card-header">
                <p class="section-kicker">Transfer</p>
                <h4>Передача ответственности</h4>
            </div>
            <p class="muted-note">Transfer меняет пользователя и/или локацию отдельным действием. Редактирование данных актива не участвует.</p>
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
                    <button class="button button-primary" type="submit" ${disabled}>Выполнить transfer</button>
                </div>
            </form>
        </article>
    `;
}

function renderReturnCard(hasCurrentUser) {
    const disabled = hasCurrentUser ? "" : "disabled";
    return `
        <article class="action-card">
            <div class="action-card-header">
                <p class="section-kicker">Return</p>
                <h4>Возврат из ответственности</h4>
            </div>
            <p class="muted-note">Return остаётся отдельным действием и не трактуется как перемещение или редактирование данных актива.</p>
            ${!hasCurrentUser ? `<p class="inline-warning">В карточке нет текущего назначенного пользователя, поэтому return сейчас недоступен.</p>` : ""}
            <form id="asset-return-form" class="form-grid">
                <div class="button-row button-row-inline">
                    <button class="button button-secondary" type="submit" ${disabled}>Выполнить return</button>
                </div>
            </form>
        </article>
    `;
}

function renderHistoryTable() {
    if (state.assetHistory.length === 0) {
        return `<p class="muted-note">Для выбранного актива история ответственности ещё не сформирована.</p>`;
    }

    return `
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Факт</th>
                        <th>Когда</th>
                        <th>Ответственный</th>
                        <th>Локация</th>
                    </tr>
                </thead>
                <tbody>
                    ${state.assetHistory.map((item) => `
                        <tr>
                            <td>${escapeHtml(describeHistoryFact(item.fact_type))}</td>
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
        return `<p class="muted-note">Аудит по выбранному активу пока пуст.</p>`;
    }

    return `
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Действие</th>
                        <th>Когда</th>
                        <th>Инициатор</th>
                        <th>Детали</th>
                    </tr>
                </thead>
                <tbody>
                    ${state.assetAudit.map((item) => `
                        <tr>
                            <td>${escapeHtml(describeAuditAction(item.action))}</td>
                            <td>${escapeHtml(formatTimestamp(item.occurred_at))}</td>
                            <td>
                                <div>${escapeHtml(item.actor || "-")}</div>
                                <div class="reference-meta">Корреляция: ${escapeHtml(item.correlation_id || "-")}</div>
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
                    <h3>${escapeHtml(state.selectedAsset.asset_tag)}</h3>
                    <p class="reference-meta">ID актива: ${escapeHtml(state.selectedAsset.id)}</p>
                </div>
                <div class="button-row">
                    <button class="button button-secondary" type="button" data-refresh-asset>Обновить</button>
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
                    <h3>Порядок первой tenant-настройки</h3>
                </div>
                <span class="summary-pill">${completed}/${SETUP_SEQUENCE.length} заполнено</span>
            </div>

            <div class="overview-grid">
                ${steps.map((step) => `
                    <article class="setup-card ${step.status}">
                        <p class="metric-label">Шаг ${step.index}</p>
                        <strong>${escapeHtml(step.label)}</strong>
                        <p class="metric-value">${escapeHtml(String(step.count))}</p>
                        <p class="metric-copy">${escapeHtml(step.description)}</p>
                        <p class="setup-status">${escapeHtml(step.statusLabel)}</p>
                        <button class="button button-secondary" type="button" data-resource-key="${step.key}">Открыть раздел</button>
                    </article>
                `).join("")}
            </div>

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
        <tr class="${item.id === state.selectedAdminItemId ? "is-active" : ""}" data-admin-id="${escapeHtml(item.id)}">
            ${config.columns.map((column) => `<td>${column.render(item)}</td>`).join("")}
        </tr>
    `).join("");
}

function renderAdminEditor() {
    const config = ADMIN_RESOURCES[state.activeAdminResource];
    const selectedItem = getAdminItems(config.key).find((item) => item.id === state.selectedAdminItemId) || null;
    const meta = ADMIN_RESOURCE_META[config.key];
    const canSubmit = canSubmitAdminResource(config.key);
    const prerequisiteMessage = getAdminPrerequisiteMessage(config.key);
    const heading = selectedItem ? `Редактировать: ${config.label}` : `Создать: ${config.label}`;

    ui.adminEditorPanel.innerHTML = `
        <div class="stack">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Редактор</p>
                    <h3>${escapeHtml(heading)}</h3>
                </div>
                <button class="button button-ghost" type="button" data-admin-reset>Новая запись</button>
            </div>

            <p class="muted-note">${escapeHtml(meta.helper)}</p>
            ${prerequisiteMessage ? `<div class="journey-banner tone-warning"><div class="journey-copy"><strong>Проверьте условие</strong><p>${escapeHtml(prerequisiteMessage)}</p></div></div>` : ""}

            <form id="admin-resource-form" class="form-grid">
                ${config.fields.map((field) => renderAdminField(field, selectedItem)).join("")}
                <div class="button-row button-row-inline">
                    <button class="button button-primary" type="submit" ${canSubmit ? "" : "disabled"}>
                        ${selectedItem ? "Сохранить изменения" : "Создать запись"}
                    </button>
                </div>
            </form>

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
    const disabled = !selectedUser || !hasRoles ? "disabled" : "";
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

            <form id="user-role-binding-form" class="form-grid">
                <input type="hidden" name="user_id" value="${escapeAttribute(selectedUser?.id || "")}">
                <label class="field">
                    <span>Роль</span>
                    <select name="role_id" required ${disabled}>
                        ${roleOptions}
                    </select>
                </label>
                <div class="button-row button-row-inline">
                    <button class="button button-primary" type="submit" ${disabled}>Назначить роль</button>
                </div>
            </form>
        </section>
    `;
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
        <option value="${escapeAttribute(user.id)}">${escapeHtml(user.name || user.id)}</option>
    `).join("");
}

function buildLocationOptions() {
    return state.references.locations.map((location) => `
        <option value="${escapeAttribute(location.id)}">${escapeHtml(location.name)} | ${escapeHtml(resolveBranchName(location.branch_id))}</option>
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
    return `
        <span class="reference-label">
            <strong>${escapeHtml(asset.asset_tag || "-")}</strong>
            <span class="reference-meta">SN: ${escapeHtml(asset.serial_number || "-")}</span>
            <span class="reference-meta">ID: ${escapeHtml(asset.id || "-")}</span>
        </span>
    `;
}

function renderUserReference(userId) {
    if (!userId) {
        return `<span class="reference-empty">Не назначен</span>`;
    }

    const user = canReferenceRead()
        ? state.references.users.find((item) => item.id === userId)
        : null;
    return renderReferenceLabel(user?.name || "Пользователь", userId);
}

function renderLocationReference(locationId) {
    if (!locationId) {
        return `<span class="reference-empty">Не указана</span>`;
    }

    const location = canReferenceRead()
        ? state.references.locations.find((item) => item.id === locationId)
        : null;
    const branchName = location ? resolveBranchName(location.branch_id) : "";
    return renderReferenceLabel(location?.name || "Локация", locationId, branchName && branchName !== "-" ? branchName : "");
}

function renderReferenceLabel(primary, id, detail = "") {
    return `
        <span class="reference-label">
            <strong>${escapeHtml(primary)}</strong>
            ${detail ? `<span class="reference-meta">${escapeHtml(detail)}</span>` : ""}
            <span class="reference-meta">ID: ${escapeHtml(id)}</span>
        </span>
    `;
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
    const normalized = String(factType || "").toUpperCase();
    return HISTORY_FACT_LABELS[normalized] || normalized || "-";
}

function describeAuditAction(action) {
    const normalized = String(action || "").toUpperCase();
    return AUDIT_ACTION_LABELS[normalized] || normalized || "-";
}

function renderAuditDetails(item) {
    const payload = parseAuditPayload(item.payload);
    const entries = [];

    if (item.entity) {
        entries.push(["Объект", escapeHtml(item.entity)]);
    }
    if (item.entity_id) {
        entries.push(["ID объекта", `<code>${escapeHtml(item.entity_id)}</code>`]);
    }

    if (payload && typeof payload === "object" && !Array.isArray(payload)) {
        Object.entries(payload).forEach(([key, value]) => {
            entries.push([describeAuditPayloadKey(key), renderAuditPayloadValue(key, value)]);
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

    return `
        <div class="audit-detail-stack">
            ${readableRows}
            <details class="audit-raw">
                <summary>Служебные данные</summary>
                <pre class="json-block">${escapeHtml(formatAuditPayload(item.payload))}</pre>
            </details>
        </div>
    `;
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
        return `<code>${escapeHtml(value)}</code>`;
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

function scrollToElement(element) {
    if (!element || typeof element.scrollIntoView !== "function") {
        return;
    }
    element.scrollIntoView({ behavior: "smooth", block: "start" });
}

function showFlash(message, tone) {
    const node = document.createElement("div");
    node.className = `flash flash-${tone}`;
    node.textContent = message;
    ui.flashStack.prepend(node);
    window.setTimeout(() => node.remove(), 7000);
}

function handleUnexpectedError(error) {
    showFlash(error.message, "error");
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
