const STORAGE_KEY = "itam-minimal-pilot-ui-context";
const UI_PROFILE_NAMES = {
    TENANT_ADMIN: "Tenant Admin",
    ASSET_OPERATOR: "Asset Operator",
    SUPPORT_REVIEWER: "Support Reviewer",
    NO_APPROVED_ROLE: "Нет approved pilot role"
};

const ADMIN_RESOURCES = {
    roles: {
        key: "roles",
        label: "Роли",
        path: "/api/v1/roles",
        emptyText: "Роли пока не созданы.",
        columns: [
            { label: "Название", render: (item) => escapeHtml(item.name) },
            { label: "ID", render: (item) => `<code>${escapeHtml(item.id)}</code>` }
        ],
        fields: [
            { name: "name", label: "Название роли", type: "text", required: true }
        ]
    },
    users: {
        key: "users",
        label: "Пользователи",
        path: "/api/v1/users",
        emptyText: "Пользователи пока не созданы.",
        columns: [
            { label: "Имя", render: (item) => escapeHtml(item.name) },
            { label: "Подразделение", render: (item) => escapeHtml(resolveDepartmentName(item.department_id)) },
            { label: "ID", render: (item) => `<code>${escapeHtml(item.id)}</code>` }
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
        emptyText: "Подразделения пока не созданы.",
        columns: [
            { label: "Название", render: (item) => escapeHtml(item.name) },
            { label: "ID", render: (item) => `<code>${escapeHtml(item.id)}</code>` }
        ],
        fields: [
            { name: "name", label: "Название подразделения", type: "text", required: true }
        ]
    },
    branches: {
        key: "branches",
        label: "Филиалы",
        path: "/api/v1/branches",
        emptyText: "Филиалы пока не созданы.",
        columns: [
            { label: "Название", render: (item) => escapeHtml(item.name) },
            { label: "ID", render: (item) => `<code>${escapeHtml(item.id)}</code>` }
        ],
        fields: [
            { name: "name", label: "Название филиала", type: "text", required: true }
        ]
    },
    locations: {
        key: "locations",
        label: "Локации",
        path: "/api/v1/locations",
        emptyText: "Локации пока не созданы.",
        columns: [
            { label: "Название", render: (item) => escapeHtml(item.name) },
            { label: "Филиал", render: (item) => escapeHtml(resolveBranchName(item.branch_id)) },
            { label: "ID", render: (item) => `<code>${escapeHtml(item.id)}</code>` }
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
    activeScreen: "assets",
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
    ui.assetsScreen = document.getElementById("assets-screen");
    ui.adminScreen = document.getElementById("admin-screen");
    ui.assetCreatePanel = document.getElementById("asset-create-panel");
    ui.assetCreateForm = document.getElementById("asset-create-form");
    ui.assetRegistryBody = document.getElementById("asset-registry-body");
    ui.assetDetailPanel = document.getElementById("asset-detail-panel");
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
    ui.refreshAssets.addEventListener("click", () => refreshAssets());
    ui.refreshAdminResource.addEventListener("click", () => refreshAdminResource());
    ui.screenNav.addEventListener("click", onScreenNavClick);
    ui.assetCreateForm.addEventListener("submit", onAssetCreateSubmit);
    ui.assetRegistryBody.addEventListener("click", onAssetRegistryClick);
    ui.assetDetailPanel.addEventListener("submit", onAssetDetailSubmit);
    ui.assetDetailPanel.addEventListener("click", onAssetDetailClick);
    ui.adminResourceNav.addEventListener("click", onAdminResourceClick);
    ui.adminTableBody.addEventListener("click", onAdminTableClick);
    ui.adminEditorPanel.addEventListener("submit", onAdminEditorSubmit);
    ui.adminEditorPanel.addEventListener("click", onAdminEditorClick);
}

function restoreContext() {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) {
        ui.assetServiceOrigin.textContent = window.location.origin;
        ui.actor.value = state.context.actor;
        ui.userServiceBaseUrl.value = state.context.userServiceBaseUrl;
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
    state.selectedAdminItemId = null;
    window.localStorage.removeItem(STORAGE_KEY);
    syncContextToForm();
    render();
    showFlash("Контекст пилота очищен.", "info");
}

function hasConnectionContext() {
    return Boolean(state.context.tenantId && state.context.userId && state.context.actor && state.context.userServiceBaseUrl);
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

function validateContext() {
    if (!state.context.tenantId || !state.context.userId || !state.context.actor || !state.context.userServiceBaseUrl) {
        showFlash("Заполните tenant, user, actor и user-service URL, чтобы подключить контур.", "warning");
        return false;
    }
    return true;
}

async function refreshAllData(options = {}) {
    if (!hasConnectionContext()) {
        showFlash("Сначала подключите контекст пилота.", "warning");
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
            await runTask(loadReferenceData, "Не удалось загрузить reference-data из user-service.");
        }

        if (canTenantAdmin()) {
            await runTask(loadRolesCatalog, "Не удалось загрузить роли для admin/setup слоя.");
        } else {
            state.selectedAdminItemId = null;
        }

        render();
        if (!options.silent) {
            showFlash("Данные пилотного контура обновлены.", "success");
        }
    } catch (error) {
        showFlash(error.message, "error");
    }
}

async function refreshAssets() {
    state.assets = await requestJson("asset", "/api/v1/assets");
    if (state.assets.length === 0) {
        state.selectedAssetId = null;
        state.selectedAsset = null;
        state.assetHistory = [];
        state.assetAudit = [];
        render();
        return;
    }

    const nextAssetId = state.selectedAssetId && state.assets.some((item) => item.id === state.selectedAssetId)
        ? state.selectedAssetId
        : state.assets[0].id;
    await loadAssetBundle(nextAssetId);
}

async function loadAssetBundle(assetId) {
    state.selectedAssetId = assetId;
    const [asset, history, audit] = await Promise.all([
        requestJson("asset", `/api/v1/assets/${assetId}`),
        requestJson("asset", `/api/v1/assets/${assetId}/history`),
        requestJson("asset", `/api/v1/assets/${assetId}/audit`)
    ]);

    state.selectedAsset = asset;
    state.assetHistory = history.items ?? [];
    state.assetAudit = audit.items ?? [];
    render();
}

async function loadReferenceData() {
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
}

async function loadRolesCatalog() {
    state.references.roles = await requestJson("user", "/api/v1/roles");
}

function hasResolvedProfile() {
    return Boolean(state.profileLabel);
}

async function resolveUiProfile() {
    const [tenantAdmin, assetMutate, assetRead, referenceRead] = await Promise.all([
        probeMutationAccess(
            "user",
            `/api/v1/roles/${createProbeId()}`,
            buildRoleProbePayload(),
            "Не удалось определить admin/setup доступ через публичный user-service контракт."
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
            "Не удалось определить read-доступ к asset registry через публичный контракт."
        ),
        probeReadAccess(
            "user",
            "/api/v1/users",
            "Не удалось определить read-доступ к reference-data через публичный user-service контракт."
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
        state.activeScreen = canTenantAdmin() ? "admin" : "assets";
    }
    render();
}

async function runTask(task, errorPrefix) {
    try {
        await task();
    } catch (error) {
        throw new Error(`${errorPrefix} ${error.message}`);
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
        showFlash("Текущая роль не имеет mutate доступа к активам.", "warning");
        return;
    }

    const form = event.currentTarget;
    const payload = buildAssetUpsertPayload(new FormData(form));

    try {
        const created = await requestJson("asset", "/api/v1/assets", {
            method: "POST",
            body: payload
        });
        form.reset();
        state.selectedAssetId = created.id;
        await refreshAssets();
        showFlash(`Актив ${created.asset_tag} создан.`, "success");
    } catch (error) {
        showFlash(error.message, "error");
    }
}

function onAssetRegistryClick(event) {
    const row = event.target.closest("[data-asset-id]");
    if (!row) {
        return;
    }
    loadAssetBundle(row.dataset.assetId).catch((error) => showFlash(error.message, "error"));
}

async function onAssetDetailSubmit(event) {
    event.preventDefault();
    const form = event.target;

    if (!state.selectedAssetId) {
        showFlash("Сначала выберите актив.", "warning");
        return;
    }

    try {
        if (form.id === "asset-update-form") {
            const payload = buildAssetUpsertPayload(new FormData(form));
            await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}`, {
                method: "PATCH",
                body: payload
            });
            await refreshAssets();
            showFlash("Карточка актива обновлена без изменения responsibility-полей.", "success");
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
            await refreshAssets();
            showFlash("Assign выполнен через dedicated flow.", "success");
        }

        if (form.id === "asset-transfer-form") {
            const formData = new FormData(form);
            const targetUserId = formDataValue(formData, "target_user_id");
            const targetLocationId = formDataValue(formData, "target_location_id");
            if (!targetUserId && !targetLocationId) {
                showFlash("Transfer требует target user или target location.", "warning");
                return;
            }
            await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}/transfer`, {
                method: "POST",
                body: {
                    target_user_id: targetUserId || null,
                    target_location_id: targetLocationId || null
                }
            });
            await refreshAssets();
            showFlash("Transfer выполнен через dedicated flow.", "success");
        }

        if (form.id === "asset-return-form") {
            await requestJson("asset", `/api/v1/assets/${state.selectedAssetId}/return`, {
                method: "POST"
            });
            await refreshAssets();
            showFlash("Return выполнен через dedicated flow.", "success");
        }
    } catch (error) {
        showFlash(error.message, "error");
    }
}

function onAssetDetailClick(event) {
    const refreshButton = event.target.closest("[data-refresh-asset]");
    if (refreshButton && state.selectedAssetId) {
        loadAssetBundle(state.selectedAssetId).catch((error) => showFlash(error.message, "error"));
    }
}

function onAdminResourceClick(event) {
    const button = event.target.closest("[data-resource-key]");
    if (!button) {
        return;
    }
    state.activeAdminResource = button.dataset.resourceKey;
    state.selectedAdminItemId = null;
    render();
}

function onAdminTableClick(event) {
    const row = event.target.closest("[data-admin-id]");
    if (!row) {
        return;
    }
    state.selectedAdminItemId = row.dataset.adminId;
    render();
}

async function onAdminEditorSubmit(event) {
    event.preventDefault();
    const form = event.target;

    if (!canTenantAdmin()) {
        showFlash("Admin/setup слой доступен только Tenant Admin.", "warning");
        return;
    }

    try {
        if (form.id === "admin-resource-form") {
            const config = ADMIN_RESOURCES[state.activeAdminResource];
            const payload = buildAdminPayload(config, new FormData(form));
            const isUpdate = Boolean(state.selectedAdminItemId);
            const response = await requestJson("user", isUpdate
                ? `${config.path}/${state.selectedAdminItemId}`
                : config.path, {
                method: isUpdate ? "PATCH" : "POST",
                body: payload
            });

            await loadReferenceData();
            await loadRolesCatalog();
            state.selectedAdminItemId = response.id ?? state.selectedAdminItemId;
            render();
            showFlash(`${config.label} ${isUpdate ? "обновлены" : "созданы"}.`, "success");
        }

        if (form.id === "user-role-binding-form") {
            const userId = formDataValue(new FormData(form), "user_id");
            const roleId = formDataValue(new FormData(form), "role_id");
            if (!userId || !roleId) {
                showFlash("Для role binding выберите пользователя и роль.", "warning");
                return;
            }
            await requestJson("user", `/api/v1/users/${userId}/roles`, {
                method: "POST",
                body: { role_id: roleId }
            });
            render();
            showFlash("Role binding выполнен через существующий публичный user-service контракт.", "success");
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
        return;
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
        showFlash("Admin/setup данные обновлены.", "success");
    } catch (error) {
        showFlash(error.message, "error");
    }
}

function render() {
    renderRoleSummary();
    renderScreenNav();
    renderAssetsScreen();
    renderAdminScreen();
    ui.activeScreenLabel.textContent = state.activeScreen;
    ui.assetServiceOrigin.textContent = window.location.origin;
}

function renderRoleSummary() {
    if (!hasConnectionContext()) {
        ui.roleSummaryBadge.textContent = "Контекст пилота не подключён";
        return;
    }
    if (!hasResolvedProfile()) {
        ui.roleSummaryBadge.textContent = "UI-профиль ещё не определён";
        return;
    }

    const markers = [state.profileLabel];
    if (canAssetMutate()) {
        markers.push("asset mutate");
    } else if (canAssetRead()) {
        markers.push("asset read");
    }
    if (canReferenceRead()) {
        markers.push("reference read");
    }

    ui.roleSummaryBadge.textContent = markers.join(" | ");
}

function renderScreenNav() {
    const screens = [];
    if (canAssetRead()) {
        screens.push({ key: "assets", label: "Активы" });
    }
    if (canTenantAdmin()) {
        screens.push({ key: "admin", label: "Admin / Setup" });
    }

    if (!screens.some((screen) => screen.key === state.activeScreen)) {
        state.activeScreen = screens[0]?.key || "assets";
    }

    ui.screenNav.innerHTML = screens.length === 0
        ? `<div class="summary-pill is-muted">Нет доступных UI-разделов для текущей роли.</div>`
        : screens.map((screen) => `
            <button
                class="nav-chip ${screen.key === state.activeScreen ? "is-active" : ""}"
                type="button"
                data-screen="${screen.key}"
            >
                ${escapeHtml(screen.label)}
            </button>
        `).join("");
}

function renderAssetsScreen() {
    const visible = canAssetRead() && state.activeScreen === "assets";
    ui.assetsScreen.hidden = !visible;
    ui.assetCreatePanel.hidden = !canAssetMutate();
    renderAssetRegistry();
    renderAssetDetail();
}

function renderAssetRegistry() {
    if (!canAssetRead()) {
        ui.assetRegistryBody.innerHTML = `
            <tr><td colspan="5" class="empty-cell">Роль не имеет доступа к asset registry.</td></tr>
        `;
        return;
    }

    if (state.assets.length === 0) {
        ui.assetRegistryBody.innerHTML = `
            <tr><td colspan="5" class="empty-cell">Активы в текущем tenant scope не найдены.</td></tr>
        `;
        return;
    }

    ui.assetRegistryBody.innerHTML = state.assets.map((asset) => `
        <tr class="${asset.id === state.selectedAssetId ? "is-active" : ""}" data-asset-id="${escapeHtml(asset.id)}">
            <td>${escapeHtml(asset.asset_tag)}</td>
            <td>${escapeHtml(asset.asset_type)}</td>
            <td>${renderStatusBadge(asset.status)}</td>
            <td>${escapeHtml(resolveUserName(asset.assigned_user_id))}</td>
            <td>${escapeHtml(resolveLocationName(asset.location_id))}</td>
        </tr>
    `).join("");
}

function renderAssetDetail() {
    if (!state.selectedAsset) {
        ui.assetDetailPanel.innerHTML = `
            <div class="empty-state">
                <p class="empty-kicker">Asset Card</p>
                <h3>Карточка ещё не выбрана</h3>
                <p>Выберите актив из реестра, чтобы открыть карточку, историю и аудит.</p>
            </div>
        `;
        return;
    }

    const asset = state.selectedAsset;
    const canMutate = canAssetMutate();

    ui.assetDetailPanel.innerHTML = `
        <div class="stack">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Asset Card</p>
                    <h3>${escapeHtml(asset.asset_tag)}</h3>
                </div>
                <button class="button button-secondary" data-refresh-asset type="button">Обновить карточку</button>
            </div>

            <div class="asset-card">
                <div class="asset-summary-grid">
                    <article class="summary-card">
                        <p>Status</p>
                        <strong>${renderStatusBadge(asset.status)}</strong>
                    </article>
                    <article class="summary-card">
                        <p>Тип актива</p>
                        <strong>${escapeHtml(asset.asset_type)}</strong>
                    </article>
                    <article class="summary-card">
                        <p>Ответственный</p>
                        <strong>${escapeHtml(resolveUserName(asset.assigned_user_id))}</strong>
                    </article>
                    <article class="summary-card">
                        <p>Локация</p>
                        <strong>${escapeHtml(resolveLocationName(asset.location_id))}</strong>
                    </article>
                    <article class="summary-card">
                        <p>Серийный номер</p>
                        <strong>${escapeHtml(asset.serial_number)}</strong>
                    </article>
                    <article class="summary-card">
                        <p>Asset ID</p>
                        <strong><code>${escapeHtml(asset.id)}</code></strong>
                    </article>
                </div>

                ${canMutate ? `
                    <div class="detail-grid">
                        <section class="panel panel-tight">
                            <div class="panel-heading">
                                <div>
                                    <p class="section-kicker">CRUD v1</p>
                                    <h3>Редактировать master record</h3>
                                </div>
                            </div>
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
                                    <button class="button button-primary" type="submit">Сохранить изменения</button>
                                </div>
                            </form>
                            <p class="muted-note">
                                Generic PATCH в UI intentionally не отправляет <code>assigned_user_id</code> и
                                <code>location_id</code>.
                            </p>
                        </section>

                        <section class="panel panel-tight">
                            <div class="panel-heading">
                                <div>
                                    <p class="section-kicker">Responsibility Flows</p>
                                    <h3>Dedicated entry points</h3>
                                </div>
                            </div>

                            <form id="asset-assign-form" class="form-grid">
                                <label class="field">
                                    <span>Assign -> пользователь</span>
                                    <select name="target_user_id" required>
                                        <option value="">Выберите пользователя</option>
                                        ${buildUserOptions()}
                                    </select>
                                </label>
                                <div class="button-row button-row-inline">
                                    <button class="button button-primary" type="submit">Assign</button>
                                </div>
                            </form>

                            <form id="asset-transfer-form" class="form-grid">
                                <label class="field">
                                    <span>Transfer -> пользователь</span>
                                    <select name="target_user_id">
                                        <option value="">Не менять пользователя</option>
                                        ${buildUserOptions()}
                                    </select>
                                </label>
                                <label class="field">
                                    <span>Transfer -> локация</span>
                                    <select name="target_location_id">
                                        <option value="">Не менять локацию</option>
                                        ${buildLocationOptions()}
                                    </select>
                                </label>
                                <div class="button-row button-row-inline">
                                    <button class="button button-primary" type="submit">Transfer</button>
                                </div>
                            </form>

                            <form id="asset-return-form" class="form-grid">
                                <div class="button-row button-row-inline">
                                    <button class="button button-secondary" type="submit">Return</button>
                                </div>
                            </form>
                        </section>
                    </div>
                ` : `
                    <div class="setup-order">
                        Текущая роль видит только read path: registry -> card -> history -> audit.
                    </div>
                `}

                <div class="detail-grid detail-grid-wide">
                    <section class="panel panel-tight">
                        <div class="panel-heading">
                            <div>
                                <p class="section-kicker">History</p>
                                <h3>История ответственности</h3>
                            </div>
                        </div>
                        ${renderHistoryTable()}
                    </section>

                    <section class="panel panel-tight">
                        <div class="panel-heading">
                            <div>
                                <p class="section-kicker">Audit</p>
                                <h3>Локальный аудит</h3>
                            </div>
                        </div>
                        ${renderAuditTable()}
                    </section>
                </div>
            </div>
        </div>
    `;
}

function renderHistoryTable() {
    if (state.assetHistory.length === 0) {
        return `<p class="muted-note">Для выбранного актива история ещё не сформирована.</p>`;
    }

    return `
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Fact</th>
                        <th>Когда</th>
                        <th>Ответственный</th>
                        <th>Локация</th>
                    </tr>
                </thead>
                <tbody>
                    ${state.assetHistory.map((item) => `
                        <tr>
                            <td>${escapeHtml(item.fact_type)}</td>
                            <td>${escapeHtml(formatTimestamp(item.occurred_at))}</td>
                            <td>
                                <ul class="compact-list">
                                    <li>до: ${escapeHtml(resolveUserName(item.previous_assigned_user_id))}</li>
                                    <li>после: ${escapeHtml(resolveUserName(item.new_assigned_user_id))}</li>
                                </ul>
                            </td>
                            <td>
                                <ul class="compact-list">
                                    <li>до: ${escapeHtml(resolveLocationName(item.previous_location_id))}</li>
                                    <li>после: ${escapeHtml(resolveLocationName(item.new_location_id))}</li>
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
                        <th>Action</th>
                        <th>Когда</th>
                        <th>Actor</th>
                        <th>Payload</th>
                    </tr>
                </thead>
                <tbody>
                    ${state.assetAudit.map((item) => `
                        <tr>
                            <td>${escapeHtml(item.action)}</td>
                            <td>${escapeHtml(formatTimestamp(item.occurred_at))}</td>
                            <td>
                                <div>${escapeHtml(item.actor || "-")}</div>
                                <div><code>${escapeHtml(item.correlation_id || "-")}</code></div>
                            </td>
                            <td><pre class="json-block">${escapeHtml(formatAuditPayload(item.payload))}</pre></td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderAdminScreen() {
    const visible = canTenantAdmin() && state.activeScreen === "admin";
    ui.adminScreen.hidden = !visible;
    if (!canTenantAdmin()) {
        return;
    }

    renderAdminNav();
    renderAdminTable();
    renderAdminEditor();
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

    if (items.length === 0) {
        ui.adminTableBody.innerHTML = `<tr><td colspan="${config.columns.length}" class="empty-cell">${escapeHtml(config.emptyText)}</td></tr>`;
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
    const heading = selectedItem ? `Редактировать: ${config.label}` : `Создать: ${config.label}`;

    ui.adminEditorPanel.innerHTML = `
        <div class="stack">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Editor</p>
                    <h3>${escapeHtml(heading)}</h3>
                </div>
                <button class="button button-ghost" type="button" data-admin-reset>Новая запись</button>
            </div>

            <form id="admin-resource-form" class="form-grid">
                ${config.fields.map((field) => renderAdminField(field, selectedItem)).join("")}
                <div class="button-row button-row-inline">
                    <button class="button button-primary" type="submit">
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
        return `
            <label class="field">
                <span>${escapeHtml(field.label)}</span>
                <select name="${escapeAttribute(field.name)}" ${field.required ? "required" : ""}>
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
    const roleOptions = state.references.roles.length === 0
        ? `<option value="">Сначала создайте или загрузите роли</option>`
        : [
            `<option value="">Выберите роль</option>`,
            ...state.references.roles.map((role) => `<option value="${escapeAttribute(role.id)}">${escapeHtml(role.name)}</option>`)
        ].join("");

    return `
        <section class="panel panel-tight">
            <div class="panel-heading">
                <div>
                    <p class="section-kicker">Role Assignment</p>
                    <h3>Bind role to user</h3>
                </div>
            </div>

            ${selectedUser ? `
                <p class="muted-note">
                    Этот экран использует только публичные versioned contracts. Текущие роли выбранного пользователя здесь
                    не читаются, но новая привязка роли доступна через существующий
                    <code>POST /api/v1/users/{userId}/roles</code>.
                </p>

                <form id="user-role-binding-form" class="form-grid">
                    <input type="hidden" name="user_id" value="${escapeAttribute(selectedUser.id)}">
                    <label class="field">
                        <span>Роль</span>
                        <select name="role_id" required>
                            ${roleOptions}
                        </select>
                    </label>
                    <div class="button-row button-row-inline">
                        <button class="button button-primary" type="submit">Назначить роль</button>
                    </div>
                </form>
            ` : `
                <p class="muted-note">Сначала выберите пользователя в таблице, затем назначьте ему роль через существующий <code>POST /api/v1/users/{userId}/roles</code>.</p>
            `}
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
        <option value="${escapeAttribute(user.id)}">${escapeHtml(user.name)} | ${escapeHtml(user.id)}</option>
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

function resolveUserName(userId) {
    if (!userId) {
        return "-";
    }
    if (!canReferenceRead()) {
        return String(userId);
    }
    const user = state.references.users.find((item) => item.id === userId);
    return user ? `${user.name} | ${user.id}` : String(userId);
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
    return location ? `${location.name} | ${resolveBranchName(location.branch_id)}` : String(locationId);
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

function showFlash(message, tone) {
    const node = document.createElement("div");
    node.className = `flash flash-${tone}`;
    node.textContent = message;
    ui.flashStack.prepend(node);
    window.setTimeout(() => node.remove(), 7000);
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
