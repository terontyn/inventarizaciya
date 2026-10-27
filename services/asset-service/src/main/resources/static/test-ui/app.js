/* ═══════════════════════════════════════════════════════
   ITAM · IT Asset Management — SPA Application Logic
   Sandbox v0.8 — In-Memory State Only
   ═══════════════════════════════════════════════════════ */

'use strict';

// ══════════════════════════════════════════════════════════
// MOCK DATA
// ══════════════════════════════════════════════════════════

const MOCK_DEPARTMENTS = [
  { id: 'dept-it',  name: 'IT-отдел',              head: 'Николаев А.В.' },
  { id: 'dept-hr',  name: 'Отдел кадров',           head: 'Соколова М.И.' },
  { id: 'dept-buh', name: 'Бухгалтерия',            head: 'Петрова Н.К.' },
  { id: 'dept-dev', name: 'Разработка',             head: 'Козлов Д.С.' },
  { id: 'dept-sup', name: 'Служба поддержки',       head: 'Морозов И.П.' },
];

const MOCK_LOCATIONS = [
  { id: 'loc-msk-1', name: 'Москва — Офис 1 (Тверская)', branch: 'Москва' },
  { id: 'loc-msk-2', name: 'Москва — Офис 2 (Садовая)', branch: 'Москва' },
  { id: 'loc-spb-1', name: 'Санкт-Петербург — Главный офис', branch: 'СПБ' },
  { id: 'loc-ekb-1', name: 'Екатеринбург — Представительство', branch: 'ЕКБ' },
];

const MOCK_USERS = [
  { id: 'u-1', name: 'Иванов Алексей Петрович',     initials: 'АИ', position: 'Системный администратор', dept: 'dept-it',  location: 'loc-msk-1', role: 'ADMIN' },
  { id: 'u-2', name: 'Смирнова Ольга Юрьевна',      initials: 'ОС', position: 'Менеджер HR',             dept: 'dept-hr',  location: 'loc-msk-1', role: 'EMPLOYEE' },
  { id: 'u-3', name: 'Козлов Дмитрий Сергеевич',    initials: 'ДК', position: 'Ведущий разработчик',     dept: 'dept-dev', location: 'loc-msk-2', role: 'EMPLOYEE' },
  { id: 'u-4', name: 'Петрова Наталья Кирилловна',  initials: 'НП', position: 'Главный бухгалтер',       dept: 'dept-buh', location: 'loc-msk-1', role: 'EMPLOYEE' },
  { id: 'u-5', name: 'Морозов Иван Павлович',        initials: 'ИМ', position: 'Специалист поддержки',   dept: 'dept-sup', location: 'loc-spb-1', role: 'EMPLOYEE' },
];

const MOCK_ASSETS_BASE = [
  // --- IT отдел / Иванов
  { id: 'a-001', assetTag: 'ASSET-0001', serialNumber: 'SN-THP-7821', assetType: 'LAPTOP',  status: 'ACTIVE',  assignedUserId: 'u-1', locationId: 'loc-msk-1', displayName: 'ThinkPad X1 Carbon',  note: 'Основной рабочий ноутбук admin' },
  { id: 'a-002', assetTag: 'ASSET-0002', serialNumber: 'SN-MON-3341', assetType: 'MONITOR', status: 'ACTIVE',  assignedUserId: 'u-1', locationId: 'loc-msk-1', displayName: 'Dell UltraSharp 27"', note: '' },
  { id: 'a-003', assetTag: 'ASSET-0003', serialNumber: 'SN-IPH-9901', assetType: 'PHONE',   status: 'ACTIVE',  assignedUserId: 'u-1', locationId: 'loc-msk-1', displayName: 'iPhone 14 Pro',       note: 'Корпоративный телефон' },
  // --- HR / Смирнова
  { id: 'a-004', assetTag: 'ASSET-0004', serialNumber: 'SN-MAC-2201', assetType: 'LAPTOP',  status: 'ACTIVE',  assignedUserId: 'u-2', locationId: 'loc-msk-1', displayName: 'MacBook Air M2',     note: '' },
  { id: 'a-005', assetTag: 'ASSET-0005', serialNumber: 'SN-MON-5502', assetType: 'MONITOR', status: 'ACTIVE',  assignedUserId: 'u-2', locationId: 'loc-msk-1', displayName: 'LG 24" FHD',         note: 'Монитор HR отдела' },
  // --- Dev / Козлов
  { id: 'a-006', assetTag: 'ASSET-0006', serialNumber: 'SN-MBP-4411', assetType: 'LAPTOP',  status: 'ACTIVE',  assignedUserId: 'u-3', locationId: 'loc-msk-2', displayName: 'MacBook Pro M3 16"', note: 'Dev-машина Козлова' },
  { id: 'a-007', assetTag: 'ASSET-0007', serialNumber: 'SN-MON-7721', assetType: 'MONITOR', status: 'ACTIVE',  assignedUserId: 'u-3', locationId: 'loc-msk-2', displayName: 'Samsung 32" 4K',     note: '' },
  { id: 'a-008', assetTag: 'ASSET-0008', serialNumber: 'SN-MON-7722', assetType: 'MONITOR', status: 'ACTIVE',  assignedUserId: 'u-3', locationId: 'loc-msk-2', displayName: 'Samsung 32" 4K #2',  note: 'Второй монитор' },
  // --- Бухгалтерия / Петрова
  { id: 'a-009', assetTag: 'ASSET-0009', serialNumber: 'SN-DSK-1101', assetType: 'DESKTOP', status: 'ACTIVE',  assignedUserId: 'u-4', locationId: 'loc-msk-1', displayName: 'HP ProDesk 400 G9',  note: '' },
  { id: 'a-010', assetTag: 'ASSET-0010', serialNumber: 'SN-PRT-3301', assetType: 'PRINTER', status: 'ACTIVE',  assignedUserId: 'u-4', locationId: 'loc-msk-1', displayName: 'HP LaserJet Pro',    note: 'Принтер бухгалтерии, hostname: prn-buh-01' },
  // --- Поддержка / Морозов
  { id: 'a-011', assetTag: 'ASSET-0011', serialNumber: 'SN-THP-9911', assetType: 'LAPTOP',  status: 'ACTIVE',  assignedUserId: 'u-5', locationId: 'loc-spb-1', displayName: 'ThinkPad L14',       note: 'hostname: ws-sup-ekb-05' },
  { id: 'a-012', assetTag: 'ASSET-0012', serialNumber: 'SN-IPH-5511', assetType: 'PHONE',   status: 'ACTIVE',  assignedUserId: 'u-5', locationId: 'loc-spb-1', displayName: 'Samsung Galaxy S23', note: 'Внутренний номер: 1147' },
  // --- Не назначено
  { id: 'a-013', assetTag: 'ASSET-0013', serialNumber: 'SN-DSK-4401', assetType: 'DESKTOP', status: 'ACTIVE',  assignedUserId: null,  locationId: 'loc-msk-2', displayName: 'Dell OptiPlex 7090', note: 'Резерв склада' },
  { id: 'a-014', assetTag: 'ASSET-0014', serialNumber: 'SN-MON-8801', assetType: 'MONITOR', status: 'ACTIVE',  assignedUserId: null,  locationId: 'loc-ekb-1', displayName: 'Acer 24" FHD',       note: 'Резерв ЕКБ' },
  { id: 'a-015', assetTag: 'ASSET-0015', serialNumber: 'SN-PRT-8801', assetType: 'PRINTER', status: 'ACTIVE',  assignedUserId: null,  locationId: 'loc-spb-1', displayName: 'Canon i-SENSYS',     note: 'hostname: prn-spb-02' },
  { id: 'a-016', assetTag: 'ASSET-0016', serialNumber: 'SN-OTH-0011', assetType: 'OTHER',   status: 'RETIRED', assignedUserId: null,  locationId: null,         displayName: 'Cisco IP-телефон 8841', note: 'Списан 2025-11' },
];

// ══════════════════════════════════════════════════════════
// APPLICATION STATE
// ══════════════════════════════════════════════════════════

const state = {
  currentUser: null,     // { id, name, role, ... }
  assets: [],            // active (non-archived) assets
  archivedAssets: [],    // archived assets
  requests: [],          // employee requests (in-memory)

  // Admin UI state
  selectedAssetId: null,
  activeSection: 'all-assets',
  activeDetailTab: 'info',
  filters: { type: 'ALL', status: 'ALL', search: '' },
  sort: 'tag-asc',

  // Org tree state (in-memory only, no localStorage)
  openDepts: new Set(),        // Set of dept IDs that are expanded
  selectedEmployeeId: null,    // ID of selected employee in tree
};

// Initialize assets from mock
function initAssets() {
  // Put RETIRED ones in archived
  state.assets = MOCK_ASSETS_BASE.filter(a => a.status !== 'RETIRED').map(a => ({ ...a }));
  state.archivedAssets = MOCK_ASSETS_BASE.filter(a => a.status === 'RETIRED').map(a => ({ ...a }));
}

// ══════════════════════════════════════════════════════════
// HELPERS
// ══════════════════════════════════════════════════════════

function getUserById(id) { return MOCK_USERS.find(u => u.id === id) || null; }
function getDeptById(id)  { return MOCK_DEPARTMENTS.find(d => d.id === id) || null; }
function getLocById(id)   { return MOCK_LOCATIONS.find(l => l.id === id) || null; }
function getAssetById(id) { return [...state.assets, ...state.archivedAssets].find(a => a.id === id) || null; }

function typeLabel(type) {
  return { LAPTOP: 'Ноутбук', DESKTOP: 'ПК', MONITOR: 'Монитор', PRINTER: 'Принтер', PHONE: 'Телефон', OTHER: 'Прочее' }[type] || type;
}

function typeIconClass(type) {
  return { LAPTOP: 'icon-laptop', DESKTOP: 'icon-desktop', MONITOR: 'icon-monitor', PRINTER: 'icon-printer', PHONE: 'icon-phone', OTHER: 'icon-other' }[type] || 'icon-other';
}

function typeIcon(type) {
  const icons = {
    LAPTOP:  `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>`,
    DESKTOP: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="2" y1="20" x2="22" y2="20"/></svg>`,
    MONITOR: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>`,
    PRINTER: `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/></svg>`,
    PHONE:   `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>`,
    OTHER:   `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
  };
  return icons[type] || icons.OTHER;
}

function statusBadge(status) {
  if (status === 'ACTIVE')   return `<span class="badge badge-active">ACTIVE</span>`;
  if (status === 'RETIRED')  return `<span class="badge badge-retired">RETIRED</span>`;
  if (status === 'ARCHIVED') return `<span class="badge badge-archived">ARCHIVED</span>`;
  return `<span class="badge">${status}</span>`;
}

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('ru-RU', { day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' });
}

function escHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function highlightText(text, query) {
  if (!query) return escHtml(text);
  const safe = escHtml(text);
  const safeQ = escHtml(query);
  if (!safeQ) return safe;
  const re = new RegExp(`(${safeQ.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
  return safe.replace(re, '<mark class="search-highlight">$1</mark>');
}

// ══════════════════════════════════════════════════════════
// TOAST NOTIFICATIONS
// ══════════════════════════════════════════════════════════

function showToast(message, type = 'info') {
  const existing = document.querySelector('.toast');
  if (existing) existing.remove();

  const icons = {
    success: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>`,
    error:   `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>`,
    info:    `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`,
  };

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span class="toast-icon">${icons[type] || icons.info}</span><span>${escHtml(message)}</span>`;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('hiding');
    setTimeout(() => toast.remove(), 220);
  }, 3000);
}

// ══════════════════════════════════════════════════════════
// AUTH
// ══════════════════════════════════════════════════════════

function login(userId) {
  const user = getUserById(userId);
  if (!user) return;
  resetAllState();
  state.currentUser = user;
  renderApp();
}

function logout() {
  resetAllState();
  state.currentUser = null;
  renderLogin();
}

function resetAllState() {
  state.selectedAssetId = null;
  state.activeSection = 'all-assets';
  state.activeDetailTab = 'info';
  state.filters = { type: 'ALL', status: 'ALL', search: '' };
  state.sort = 'tag-asc';
  // Org tree state (in-memory)
  state.openDepts = new Set();
  state.selectedEmployeeId = null;
  // Keep assets & requests (in-memory persisted during session)
}

// ══════════════════════════════════════════════════════════
// SCREEN SWITCH
// ══════════════════════════════════════════════════════════

function renderLogin() {
  document.getElementById('login-screen').classList.remove('hidden');
  document.getElementById('app').classList.add('hidden');
  // Reset form
  document.getElementById('login-username').value = '';
  document.getElementById('login-password').value = '';
  document.getElementById('login-error').classList.add('hidden');
}

function renderApp() {
  document.getElementById('login-screen').classList.add('hidden');
  document.getElementById('app').classList.remove('hidden');
  document.getElementById('app').classList.add('fade-in');

  const user = state.currentUser;
  // Topbar
  document.getElementById('topbar-username').textContent = user.name.split(' ')[0] + ' ' + user.name.split(' ')[1];
  const avatar = document.getElementById('topbar-avatar');
  avatar.textContent = user.initials;
  const roleBadge = document.getElementById('topbar-role-badge');
  roleBadge.textContent = user.role === 'ADMIN' ? 'Администратор' : 'Сотрудник';
  roleBadge.className = 'topbar-role-badge ' + (user.role === 'ADMIN' ? 'role-badge-admin' : 'role-badge-employee');

  const sidebar = document.getElementById('sidebar');
  const searchZone = document.getElementById('topbar-search-zone');

  if (user.role === 'ADMIN') {
    sidebar.classList.remove('hidden');
    renderAdminSearchBar();
    document.getElementById('topbar-requests-badge').classList.remove('hidden');
    updateRequestsBadge();
    populateSidebarTrees();
    navigateTo('all-assets');
  } else {
    sidebar.classList.add('hidden');
    searchZone.innerHTML = '';
    document.getElementById('topbar-requests-badge').classList.add('hidden');
    navigateTo('employee');
  }
}

// ══════════════════════════════════════════════════════════
// ADMIN SEARCH BAR
// ══════════════════════════════════════════════════════════

function renderAdminSearchBar() {
  const zone = document.getElementById('topbar-search-zone');
  zone.innerHTML = `
    <div class="search-bar">
      <svg class="search-bar-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
      </svg>
      <input id="global-search" type="text" placeholder="Поиск по тегу, SN, имени, заметке..." autocomplete="off" />
    </div>`;

  document.getElementById('global-search').addEventListener('input', e => {
    state.filters.search = e.target.value.trim();
    if (state.activeSection === 'all-assets') renderAssetsList();
  });
}

// ══════════════════════════════════════════════════════════
// NAVIGATION
// ══════════════════════════════════════════════════════════

const ALL_SECTIONS = ['all-assets', 'archive', 'org', 'locations', 'requests', 'settings', 'employee'];

function navigateTo(section, meta = {}) {
  state.activeSection = section;

  // Сброс выбора сотрудника в дереве при обычной навигации
  if (section !== 'employee') {
    // Восстановить панель заявки если была скрыта
    const requestPanel = document.querySelector('.employee-request-panel');
    if (requestPanel) requestPanel.style.display = '';
    // Снять подсветку сотрудника в дереве
    document.querySelectorAll('.org-employee-item.emp-selected').forEach(el => el.classList.remove('emp-selected'));
    state.selectedEmployeeId = null;
  }

  // Hide all sections
  ALL_SECTIONS.forEach(s => {
    const el = document.getElementById(`section-${s}`);
    if (el) el.classList.add('hidden');
  });

  // Show target
  const target = document.getElementById(`section-${section}`);
  if (target) {
    target.classList.remove('hidden');
    target.classList.add('fade-in');
    setTimeout(() => target.classList.remove('fade-in'), 300);
  }

  // Update sidebar active state
  document.querySelectorAll('.tree-item').forEach(el => el.classList.remove('tree-item-active', 'active'));
  const navEl = document.getElementById(`nav-${section}`);
  if (navEl) navEl.classList.add('tree-item-active');

  // Module name in topbar
  const moduleNames = {
    'all-assets': 'Все активы',
    'archive': 'Архив',
    'org': 'Оргструктура',
    'locations': 'Локации',
    'requests': 'Заявки',
    'settings': 'Настройки',
    'employee': 'Личный кабинет',
  };
  document.getElementById('topbar-module').textContent = moduleNames[section] || 'Инвентаризация';

  // Close detail pane on section change
  closeDetailPane();

  // Render section content
  switch (section) {
    case 'all-assets': renderAssetsList(); break;
    case 'archive':    renderArchiveList(); break;
    case 'org':        renderOrgSection(meta.deptId); break;
    case 'locations':  renderLocSection(meta.locId); break;
    case 'requests':   renderAdminRequests(); break;
    case 'employee':   renderEmployeeView(); break;
    case 'settings':   break; // static
  }
}

// ══════════════════════════════════════════════════════════
// SIDEBAR TREES
// ══════════════════════════════════════════════════════════

function populateSidebarTrees() {
  // Badge counts
  document.getElementById('tree-badge-all').textContent = state.assets.length;
  document.getElementById('tree-badge-archive').textContent = state.archivedAssets.length;
  updateRequestsBadge();

  // Org tree — accordion до уровня ФИО
  renderOrgTree();

  // Locations tree
  const locTree = document.getElementById('loc-tree');
  locTree.innerHTML = MOCK_LOCATIONS.map(l => {
    const count = state.assets.filter(a => a.locationId === l.id).length;
    return `<li class="tree-item tree-child-item" data-loc="${l.id}">
      <button class="tree-btn" onclick="navigateTo('locations', {locId:'${l.id}'})">
        <span>${escHtml(l.name)}</span>
        <span class="tree-badge">${count}</span>
      </button>
    </li>`;
  }).join('');
}

/* ──────────────────────────────────────────────────────────
   ORG TREE: аккордеон Отдел → ФИО сотрудников
   Весь стейт хранится в state.openDepts (Set) и
   state.selectedEmployeeId (in-memory, без localStorage).
   ────────────────────────────────────────────────────────── */
function renderOrgTree() {
  const orgTree = document.getElementById('org-tree');

  orgTree.innerHTML = MOCK_DEPARTMENTS.map(d => {
    // Сотрудники этого отдела
    const deptUsers = MOCK_USERS.filter(u => u.dept === d.id);
    // Активы отдела (для бейджа)
    const assetCount = state.assets.filter(a => {
      const u = getUserById(a.assignedUserId);
      return u && u.dept === d.id;
    }).length;

    const isOpen = state.openDepts.has(d.id);

    // Строки сотрудников
    const employeeRows = deptUsers.map(u => {
      const isSelected = state.selectedEmployeeId === u.id;
      return `<button
          class="org-employee-item${isSelected ? ' emp-selected' : ''}"
          id="emp-tree-${u.id}"
          onclick="selectEmployeeFromTree('${u.id}')"
          title="${escHtml(u.name)} · ${escHtml(u.position)}"
        >
          <span class="org-employee-dot"></span>
          <span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${escHtml(u.name)}</span>
        </button>`;
    }).join('');

    return `<li class="org-dept-node${isOpen ? ' dept-open' : ''}" id="dept-node-${d.id}">
      <button
        class="org-dept-btn"
        onclick="toggleDeptNode('${d.id}')"
        title="${escHtml(d.name)} · Руководитель: ${escHtml(d.head)}"
      >
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0;opacity:.7">
          <rect x="2" y="7" width="20" height="14" rx="2"/>
          <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/>
        </svg>
        <span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${escHtml(d.name)}</span>
        <span class="tree-badge" style="margin-right:2px">${assetCount}</span>
        <svg class="org-dept-chevron" width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M6 9l6 6 6-6"/>
        </svg>
      </button>
      <div class="org-dept-employees">
        ${deptUsers.length > 0 ? employeeRows : '<span style="display:block;padding:4px 8px;font-size:0.75rem;color:var(--text-disabled)">Нет сотрудников</span>'}
      </div>
    </li>`;
  }).join('');
}

/* Переключение раскрытия узла-отдела (аккордеон) */
function toggleDeptNode(deptId) {
  const node = document.getElementById(`dept-node-${deptId}`);
  if (!node) return;

  if (state.openDepts.has(deptId)) {
    state.openDepts.delete(deptId);
    node.classList.remove('dept-open');
  } else {
    state.openDepts.add(deptId);
    node.classList.add('dept-open');
  }
}

/* Клик по ФИО в дереве:
   1. Подсвечивает строку бирюзовым
   2. Переключает контекст на renderEmployeeView (Информационный лист)
   3. Если роль Админа — фильтрует реестр активов по этому человеку */
function selectEmployeeFromTree(userId) {
  // Снять предыдущее выделение
  document.querySelectorAll('.org-employee-item.emp-selected').forEach(el => el.classList.remove('emp-selected'));

  // Если кликаем повторно по тому же — снять выделение и сбросить фильтр
  if (state.selectedEmployeeId === userId) {
    state.selectedEmployeeId = null;
    // Вернуть к полному реестру активов
    if (state.currentUser && state.currentUser.role === 'ADMIN') {
      state.filters = { type: 'ALL', status: 'ALL', search: '' };
      navigateTo('all-assets');
    }
    return;
  }

  state.selectedEmployeeId = userId;

  // Подсветить выбранный элемент
  const empBtn = document.getElementById(`emp-tree-${userId}`);
  if (empBtn) empBtn.classList.add('emp-selected');

  const user = getUserById(userId);
  if (!user) return;

  if (state.currentUser && state.currentUser.role === 'ADMIN') {
    // Режим Администратора:
    // 1. Показываем Информационный лист сотрудника
    showEmployeeInfoSheet(user);
    // 2. Фильтруем реестр активов только по этому человеку
    filterAssetsByEmployee(userId);
  } else {
    // Если по какой-то причине вызван для сотрудника — просто показываем профиль
    showEmployeeInfoSheet(user);
  }
}

/* Отображает информационный лист выбранного сотрудника
   (reuses section-employee, но заполняет данными другого пользователя) */
function showEmployeeInfoSheet(user) {
  const dept = getDeptById(user.dept);
  const loc  = getLocById(user.location);
  const myAssets = state.assets.filter(a => a.assignedUserId === user.id);

  // Группировка по типу
  const groups = {};
  myAssets.forEach(a => {
    if (!groups[a.assetType]) groups[a.assetType] = [];
    groups[a.assetType].push(a);
  });

  const cardEl = document.getElementById('employee-card');
  if (!cardEl) return;

  cardEl.innerHTML = `
    <div class="employee-profile">
      <div class="employee-avatar" style="background:linear-gradient(135deg,var(--accent),var(--violet))">${escHtml(user.initials)}</div>
      <div class="employee-name">${escHtml(user.name)}</div>
      <div class="employee-role-label">${escHtml(user.position)}</div>
      <span class="employee-dept">${dept ? escHtml(dept.name) : '—'}</span>
    </div>

    ${loc ? `<div class="employee-asset-hint" style="margin-bottom:12px">📍 ${escHtml(loc.name)}</div>` : ''}

    <div class="employee-assets-header">
      <span class="employee-assets-title">Закреплённое оборудование</span>
      <span class="employee-assets-count">Устройств: ${myAssets.length}</span>
    </div>

    ${myAssets.length === 0 ? `<div class="empty-state" style="padding:30px 0"><p>Нет назначенного оборудования</p></div>` : ''}

    ${Object.entries(groups).map(([type, assets]) => `
      <div class="employee-device-group">
        <div class="employee-device-group-title">
          <span class="${typeIconClass(type)}" style="width:16px;height:16px;display:flex;align-items:center">${typeIcon(type)}</span>
          <span>${typeLabel(type)}</span>
          <span class="group-count">${assets.length}</span>
        </div>
        ${assets.map(a => {
          const iconCls = typeIconClass(a.assetType);
          return `<div class="employee-asset-item">
            <div class="employee-asset-item-icon ${iconCls}">${typeIcon(a.assetType)}</div>
            <div class="employee-asset-item-info">
              <div class="employee-asset-item-name">${escHtml(a.displayName || a.assetTag)}</div>
              <div class="employee-asset-item-tag">${escHtml(a.assetTag)}</div>
            </div>
            ${statusBadge(a.status)}
          </div>`;
        }).join('')}
      </div>
    `).join('')}
  `;

  // Скрыть форму заявки в режиме просмотра из дерева (только инфо-лист)
  const requestPanel = document.querySelector('.employee-request-panel');
  if (requestPanel && state.currentUser && state.currentUser.role === 'ADMIN') {
    requestPanel.style.display = 'none';
  }

  // Навигация к секции employee
  ALL_SECTIONS.forEach(s => {
    const el = document.getElementById(`section-${s}`);
    if (el) el.classList.add('hidden');
  });
  const empSection = document.getElementById('section-employee');
  if (empSection) {
    empSection.classList.remove('hidden');
    empSection.classList.add('fade-in');
    setTimeout(() => empSection.classList.remove('fade-in'), 300);
  }

  // Обновить заголовок топбара
  document.getElementById('topbar-module').textContent = user.name.split(' ').slice(0, 2).join(' ');
  state.activeSection = 'employee';

  // Снять активность с пунктов сайдбара
  document.querySelectorAll('.tree-item').forEach(el => el.classList.remove('tree-item-active', 'active'));
}

/* Фильтрует реестр активов только по сотруднику (только для Админа) */
function filterAssetsByEmployee(userId) {
  const user = getUserById(userId);
  if (!user) return;

  // Перейти в секцию all-assets с применением фильтра по пользователю
  // Используем поисковый фильтр по имени пользователя
  state.filters = { type: 'ALL', status: 'ALL', search: user.name.split(' ').slice(0, 2).join(' ') };

  // Обновить поисковую строку в UI если есть
  const searchInput = document.getElementById('global-search');
  if (searchInput) searchInput.value = user.name.split(' ').slice(0, 2).join(' ');
}


function updateRequestsBadge() {
  const pending = state.requests.filter(r => r.status === 'PENDING').length;
  document.getElementById('tree-badge-requests').textContent = pending;
  const badge = document.getElementById('topbar-requests-badge');
  const countEl = document.getElementById('requests-count-badge');
  if (pending > 0) {
    badge.classList.remove('hidden');
    countEl.textContent = pending;
  } else {
    badge.classList.add('hidden');
  }
  const summaryEl = document.getElementById('requests-summary-line');
  if (summaryEl) summaryEl.textContent = `Входящие запросы · Ожидают обработки: ${pending}`;
}

// ══════════════════════════════════════════════════════════
// FILTER & SORT
// ══════════════════════════════════════════════════════════

function getFilteredSortedAssets() {
  let list = [...state.assets];

  // Type filter
  if (state.filters.type !== 'ALL') {
    list = list.filter(a => a.assetType === state.filters.type);
  }
  // Status filter
  if (state.filters.status !== 'ALL') {
    list = list.filter(a => a.status === state.filters.status);
  }
  // Search
  if (state.filters.search) {
    const q = state.filters.search.toLowerCase();
    list = list.filter(a => {
      return (
        (a.assetTag || '').toLowerCase().includes(q) ||
        (a.serialNumber || '').toLowerCase().includes(q) ||
        (a.displayName || '').toLowerCase().includes(q) ||
        (a.note || '').toLowerCase().includes(q) ||
        typeLabel(a.assetType).toLowerCase().includes(q)
      );
    });
  }

  // Sort
  list.sort((a, b) => {
    switch (state.sort) {
      case 'tag-asc':  return (a.assetTag || '').localeCompare(b.assetTag || '');
      case 'tag-desc': return (b.assetTag || '').localeCompare(a.assetTag || '');
      case 'type-asc': return typeLabel(a.assetType).localeCompare(typeLabel(b.assetType));
      case 'name-asc': return (a.displayName || '').localeCompare(b.displayName || '');
      default: return 0;
    }
  });

  return list;
}

// ══════════════════════════════════════════════════════════
// RENDER: ASSETS LIST
// ══════════════════════════════════════════════════════════

function renderAssetsList() {
  const list = getFilteredSortedAssets();
  const total = state.assets.length;
  const container = document.getElementById('assets-list');

  // Summary
  const summaryEl = document.getElementById('assets-summary-line');
  const filtered = list.length;
  summaryEl.textContent = filtered === total
    ? `Всего активов: ${total}`
    : `Показано: ${filtered} из ${total} · применены фильтры`;

  // Фича №1: рендер чипсов активных фильтров
  renderActiveFilterChips();

  // Render
  if (list.length === 0) {
    container.innerHTML = `<div class="empty-state">
      <div class="empty-icon"><svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg></div>
      <p>Активов не найдено</p>
      <span>Попробуйте изменить фильтры или поисковый запрос</span>
    </div>`;
    return;
  }

  const q = state.filters.search;
  container.innerHTML = list.map(asset => buildAssetCard(asset, q)).join('');
}

// ══════════════════════════════════════════════════════════
// ФИЧА №1: ЧИПСЫ АКТИВНЫХ ФИЛЬТРОВ
// ══════════════════════════════════════════════════════════

/* Рендерит ряд плашек-чипсов под поисковой строкой если хотя бы один
   фильтр активен. Весь стейт — in-memory (никакого localStorage). */
function renderActiveFilterChips() {
  const container = document.getElementById('active-filter-chips');
  if (!container) return;

  const chips = [];

  // Тип устройства
  if (state.filters.type !== 'ALL') {
    chips.push({
      key: 'type',
      label: `Тип: ${typeLabel(state.filters.type)}`,
      onRemove: () => {
        state.filters.type = 'ALL';
        // Сбросить чип в filter-bar
        document.querySelectorAll('[data-filter="type"]').forEach(c => c.classList.remove('chip-active'));
        const allChip = document.querySelector('[data-filter="type"][data-value="ALL"]');
        if (allChip) allChip.classList.add('chip-active');
        renderAssetsList();
      }
    });
  }

  // Статус
  if (state.filters.status !== 'ALL') {
    const statusLabels = { ACTIVE: 'ACTIVE', RETIRED: 'RETIRED', ARCHIVED: 'ARCHIVED' };
    chips.push({
      key: 'status',
      label: `Статус: ${statusLabels[state.filters.status] || state.filters.status}`,
      onRemove: () => {
        state.filters.status = 'ALL';
        document.querySelectorAll('[data-filter="status"]').forEach(c => c.classList.remove('chip-active'));
        const allChip = document.querySelector('[data-filter="status"][data-value="ALL"]');
        if (allChip) allChip.classList.add('chip-active');
        renderAssetsList();
      }
    });
  }

  // Поисковая строка
  if (state.filters.search) {
    chips.push({
      key: 'search',
      label: `Поиск: «${state.filters.search}»`,
      onRemove: () => {
        state.filters.search = '';
        const searchInput = document.getElementById('global-search');
        if (searchInput) searchInput.value = '';
        renderAssetsList();
      }
    });
  }

  // Если нет активных фильтров — скрыть блок
  if (chips.length === 0) {
    container.innerHTML = '';
    container.style.display = 'none';
    return;
  }

  container.style.display = 'flex';
  container.innerHTML = chips.map((chip, i) => `
    <span class="filter-chip" id="fchip-${chip.key}">
      ${escHtml(chip.label)}
      <button class="filter-chip-remove" onclick="removeFilterChip(${i})" title="Сбросить фильтр" aria-label="Удалить фильтр ${escHtml(chip.label)}">
        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </span>
  `).join('');

  // Сохраняем кеш функций удаления в глобальный массив (in-memory)
  renderActiveFilterChips._handlers = chips.map(c => c.onRemove);
}

// Вызывается через onclick в HTML (inline), использует сохранённый массив handlers
function removeFilterChip(idx) {
  const handlers = renderActiveFilterChips._handlers;
  if (handlers && handlers[idx]) handlers[idx]();
}

function buildAssetCard(asset, searchQuery = '') {
  const user = getUserById(asset.assignedUserId);
  const loc  = getLocById(asset.locationId);
  const iconCls = typeIconClass(asset.assetType);
  const isSelected = state.selectedAssetId === asset.id;

  const titleText = asset.displayName || asset.assetTag;
  const titleHtml = highlightText(titleText, searchQuery);
  const tagHtml   = highlightText(asset.assetTag, searchQuery);
  const snHtml    = highlightText(asset.serialNumber, searchQuery);

  return `<div class="asset-card ${isSelected ? 'selected' : ''}"
               id="card-${asset.id}"
               onclick="selectAsset('${asset.id}')">
    <div class="asset-card-icon ${iconCls}">${typeIcon(asset.assetType)}</div>
    <div class="asset-card-body">
      <div class="asset-card-title">${titleHtml}</div>
      <div class="asset-card-meta">${tagHtml} · ${snHtml}</div>
    </div>
    <div class="asset-card-right">
      ${statusBadge(asset.status)}
      <div class="asset-card-user">${user ? escHtml(user.name.split(' ').slice(0,2).join(' ')) : '<span style="color:var(--text-disabled)">Не назначен</span>'}</div>
    </div>
  </div>`;
}

// ══════════════════════════════════════════════════════════
// RENDER: ARCHIVE LIST
// ══════════════════════════════════════════════════════════

function renderArchiveList() {
  const container = document.getElementById('archive-list');
  const list = state.archivedAssets;

  // Фича №2: стильный teal-бейдж с реактивным счётчиком
  const summaryEl = document.getElementById('archive-summary-line');
  if (summaryEl) {
    summaryEl.innerHTML = list.length === 0
      ? `Архив пуст`
      : `Законсервировано объектов: <span class="archive-count-badge">${list.length}</span>`;
  }

  if (list.length === 0) {
    container.innerHTML = `<div class="empty-state">
      <div class="empty-icon"><svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><line x1="10" y1="12" x2="14" y2="12"/></svg></div>
      <p>Архив пуст</p>
      <span>Заархивированные активы появятся здесь</span>
    </div>`;
    return;
  }
  // Archived assets: show in main list, clicking opens detail pane in archive context
  container.innerHTML = list.map(a => {
    const user = getUserById(a.assignedUserId);
    const iconCls = typeIconClass(a.assetType);
    const isSelected = state.selectedAssetId === a.id;
    return `<div class="asset-card ${isSelected ? 'selected' : ''}"
                 id="card-${a.id}"
                 onclick="selectArchivedAsset('${a.id}')">
      <div class="asset-card-icon ${iconCls}">${typeIcon(a.assetType)}</div>
      <div class="asset-card-body">
        <div class="asset-card-title">${escHtml(a.displayName || a.assetTag)}</div>
        <div class="asset-card-meta">${escHtml(a.assetTag)} · ${escHtml(a.serialNumber || '—')}</div>
      </div>
      <div class="asset-card-right">
        ${statusBadge(a.status === 'RETIRED' ? 'RETIRED' : 'ARCHIVED')}
        <div class="asset-card-user">${user ? escHtml(user.name.split(' ').slice(0,2).join(' ')) : '<span style="color:var(--text-disabled)">Не назначен</span>'}</div>
      </div>
    </div>`;
  }).join('');
}

// ══════════════════════════════════════════════════════════
// RENDER: ORG SECTION
// ══════════════════════════════════════════════════════════

function renderOrgSection(deptId) {
  const dept = getDeptById(deptId);
  const title = document.getElementById('org-section-title');
  const sub   = document.getElementById('org-subtitle');
  const list  = document.getElementById('org-assets-list');

  if (!dept) {
    // Show all
    title.textContent = 'Оргструктура';
    sub.textContent = 'Все отделы';
    let html = '';
    MOCK_DEPARTMENTS.forEach(d => {
      const dAssets = state.assets.filter(a => { const u = getUserById(a.assignedUserId); return u && u.dept === d.id; });
      if (dAssets.length === 0) return;
      html += `<div class="group-header"><span>${escHtml(d.name)}</span><span class="group-count">${dAssets.length}</span></div>`;
      html += dAssets.map(a => buildAssetCard(a)).join('');
    });
    list.innerHTML = html || `<div class="empty-state"><p>Нет данных</p></div>`;
    return;
  }

  title.textContent = dept.name;
  const dAssets = state.assets.filter(a => { const u = getUserById(a.assignedUserId); return u && u.dept === dept.id; });
  sub.textContent = `Руководитель: ${dept.head} · Активов: ${dAssets.length}`;
  list.innerHTML = dAssets.length ? dAssets.map(a => buildAssetCard(a)).join('') : `<div class="empty-state"><p>Нет активов</p></div>`;
}

// ══════════════════════════════════════════════════════════
// RENDER: LOCATIONS SECTION
// ══════════════════════════════════════════════════════════

function renderLocSection(locId) {
  const loc   = getLocById(locId);
  const title = document.getElementById('loc-section-title');
  const sub   = document.getElementById('loc-subtitle');
  const list  = document.getElementById('loc-assets-list');

  if (!loc) {
    title.textContent = 'Локации';
    sub.textContent = 'Все локации';
    let html = '';
    MOCK_LOCATIONS.forEach(l => {
      const lAssets = state.assets.filter(a => a.locationId === l.id);
      if (lAssets.length === 0) return;
      html += `<div class="group-header"><span>${escHtml(l.name)}</span><span class="group-count">${lAssets.length}</span></div>`;
      html += lAssets.map(a => buildAssetCard(a)).join('');
    });
    list.innerHTML = html || `<div class="empty-state"><p>Нет данных</p></div>`;
    return;
  }

  title.textContent = loc.name;
  const lAssets = state.assets.filter(a => a.locationId === loc.id);
  sub.textContent = `Филиал: ${loc.branch} · Активов: ${lAssets.length}`;
  list.innerHTML = lAssets.length ? lAssets.map(a => buildAssetCard(a)).join('') : `<div class="empty-state"><p>Нет активов</p></div>`;
}

// ══════════════════════════════════════════════════════════
// ASSET SELECTION & DETAIL PANE
// ══════════════════════════════════════════════════════════

function selectAsset(id) {
  // Deselect
  document.querySelectorAll('.asset-card.selected').forEach(el => el.classList.remove('selected'));

  if (state.selectedAssetId === id) {
    state.selectedAssetId = null;
    closeDetailPane();
    return;
  }

  state.selectedAssetId = id;
  const card = document.getElementById(`card-${id}`);
  if (card) card.classList.add('selected');
  renderDetailPane(id);
}

function selectArchivedAsset(id) {
  document.querySelectorAll('.asset-card.selected').forEach(el => el.classList.remove('selected'));
  if (state.selectedAssetId === id) {
    state.selectedAssetId = null;
    closeDetailPane();
    return;
  }
  state.selectedAssetId = id;
  const card = document.getElementById(`card-${id}`);
  if (card) card.classList.add('selected');
  renderDetailPane(id);
}

function closeDetailPane() {
  state.selectedAssetId = null;
  document.querySelectorAll('.asset-card.selected').forEach(el => el.classList.remove('selected'));
  const pane = document.getElementById('asset-detail-pane');
  pane.classList.add('hidden');
  pane.innerHTML = '';

  // Restore full width for list
  const listPane = document.getElementById('assets-list-pane');
  if (listPane) listPane.style.flex = '1';
}

function renderDetailPane(assetId) {
  const asset = getAssetById(assetId);
  if (!asset) return;

  const pane = document.getElementById('asset-detail-pane');
  pane.classList.remove('hidden');

  const user = getUserById(asset.assignedUserId);
  const loc  = getLocById(asset.locationId);
  const dept = user ? getDeptById(user.dept) : null;
  const iconCls = typeIconClass(asset.assetType);
  const isArchived = state.archivedAssets.some(a => a.id === assetId);

  state.activeDetailTab = 'info';

  pane.innerHTML = `
    <div class="detail-header">
      <div class="detail-icon ${iconCls}">${typeIcon(asset.assetType)}</div>
      <div class="detail-title-group">
        <div class="detail-title">${escHtml(asset.displayName || asset.assetTag)}</div>
        <div class="detail-tag">${escHtml(asset.assetTag)}</div>
      </div>
      <button class="detail-close" onclick="closeDetailPane()" title="Закрыть">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <div class="detail-tabs">
      <button class="detail-tab active" data-tab="info" onclick="switchDetailTab('info')">Информация</button>
      <button class="detail-tab" data-tab="responsibility" onclick="switchDetailTab('responsibility')">Ответственность</button>
      <button class="detail-tab" data-tab="lifecycle" onclick="switchDetailTab('lifecycle')">Жизненный цикл</button>
    </div>

    <div class="detail-body">
      <!-- TAB: INFO -->
      <div class="detail-tab-panel active" id="tab-info">
        <div class="detail-field"><span class="detail-field-label">Статус</span>${statusBadge(asset.status)}</div>
        <div class="detail-field"><span class="detail-field-label">Тип устройства</span><span class="detail-field-value">${typeLabel(asset.assetType)}</span></div>
        <div class="detail-field"><span class="detail-field-label">Asset Tag</span><span class="detail-field-value mono">${escHtml(asset.assetTag)}</span></div>
        <div class="detail-field"><span class="detail-field-label">Серийный номер</span><span class="detail-field-value mono">${escHtml(asset.serialNumber || '—')}</span></div>
        <div class="detail-field"><span class="detail-field-label">Отображаемое имя</span><span class="detail-field-value">${escHtml(asset.displayName || '—')}</span></div>
        <div class="detail-field">
          <span class="detail-field-label">Примечание</span>
          <textarea class="detail-note-area" id="detail-note-area" placeholder="Добавить примечание...">${escHtml(asset.note || '')}</textarea>
          <button class="btn-secondary" style="margin-top:6px;font-size:0.75rem;" onclick="saveNote('${asset.id}')">Сохранить заметку</button>
        </div>
      </div>

      <!-- TAB: RESPONSIBILITY -->
      <div class="detail-tab-panel" id="tab-responsibility">
        <div class="detail-field"><span class="detail-field-label">Назначен пользователю</span><span class="detail-field-value">${user ? escHtml(user.name) : '<span class="muted">Не назначен</span>'}</span></div>
        <div class="detail-field"><span class="detail-field-label">Должность</span><span class="detail-field-value">${user ? escHtml(user.position) : '—'}</span></div>
        <div class="detail-field"><span class="detail-field-label">Отдел</span><span class="detail-field-value">${dept ? escHtml(dept.name) : '—'}</span></div>
        <div class="detail-field"><span class="detail-field-label">Локация</span><span class="detail-field-value">${loc ? escHtml(loc.name) : '—'}</span></div>
        <div class="detail-field"><span class="detail-field-label">Филиал</span><span class="detail-field-value">${loc ? escHtml(loc.branch) : '—'}</span></div>
        ${asset.note && asset.note.includes('hostname') ? `<div class="detail-field"><span class="detail-field-label">Технические сведения</span><span class="detail-field-value mono" style="font-size:0.75rem;word-break:break-all">${escHtml(asset.note)}</span></div>` : ''}
      </div>

      <!-- TAB: LIFECYCLE -->
      <div class="detail-tab-panel" id="tab-lifecycle">
        <div class="detail-field"><span class="detail-field-label">Текущий статус</span>${statusBadge(isArchived ? 'ARCHIVED' : asset.status)}</div>
        <div class="detail-field"><span class="detail-field-label">Тип актива</span><span class="detail-field-value">${typeLabel(asset.assetType)}</span></div>

        <div class="lifecycle-section">
          <div class="lifecycle-title">Операции жизненного цикла</div>
          <div class="lifecycle-actions">
            ${!isArchived ? `
            <button class="lifecycle-btn lifecycle-btn-archive" onclick="archiveAsset('${asset.id}')">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><line x1="10" y1="12" x2="14" y2="12"/></svg>
              Архивировать актив
            </button>
            <button class="lifecycle-btn lifecycle-btn-retire" onclick="retireAsset('${asset.id}')">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg>
              Списать актив (RETIRED)
            </button>` : `
            <button class="lifecycle-btn lifecycle-btn-unarchive" onclick="unarchiveAsset('${asset.id}')">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><polyline points="10 12 12 14 14 12"/><line x1="12" y1="9" x2="12" y2="14"/></svg>
              Разархивировать
            </button>`}
          </div>
        </div>

        <div style="margin-top:20px;padding:12px;background:var(--bg-card);border-radius:var(--radius-sm);border:1px solid var(--border-subtle);">
          <div style="font-size:0.6875rem;color:var(--text-disabled);text-transform:uppercase;letter-spacing:0.06em;margin-bottom:4px">Sandbox — Примечание</div>
          <p style="font-size:0.75rem;color:var(--text-muted);line-height:1.6">Операции выполняются in-memory. Retire помечает актив статусом RETIRED и перемещает в архив. Разархивирование возвращает актив в реестр.</p>
        </div>
      </div>
    </div>`;
}

function switchDetailTab(tab) {
  state.activeDetailTab = tab;
  document.querySelectorAll('.detail-tab').forEach(el => {
    el.classList.toggle('active', el.dataset.tab === tab);
  });
  document.querySelectorAll('.detail-tab-panel').forEach(el => {
    el.classList.toggle('active', el.id === `tab-${tab}`);
  });
}

function saveNote(assetId) {
  const textarea = document.getElementById('detail-note-area');
  if (!textarea) return;
  const asset = getAssetById(assetId);
  if (!asset) return;
  asset.note = textarea.value;
  showToast('Примечание сохранено', 'success');
}

// ══════════════════════════════════════════════════════════
// LIFECYCLE OPERATIONS
// ══════════════════════════════════════════════════════════

function archiveAsset(assetId) {
  const idx = state.assets.findIndex(a => a.id === assetId);
  if (idx === -1) return;
  const [asset] = state.assets.splice(idx, 1);
  state.archivedAssets.push(asset);
  closeDetailPane();
  renderAssetsList();
  populateSidebarTrees();
  showToast(`Актив "${asset.displayName || asset.assetTag}" архивирован`, 'info');
}

function unarchiveAsset(assetId) {
  const idx = state.archivedAssets.findIndex(a => a.id === assetId);
  if (idx === -1) return;
  const [asset] = state.archivedAssets.splice(idx, 1);
  // Restore to ACTIVE if was RETIRED
  if (asset.status === 'RETIRED') asset.status = 'ACTIVE';
  state.assets.push(asset);
  closeDetailPane();
  renderArchiveList();
  populateSidebarTrees();
  showToast(`Актив "${asset.displayName || asset.assetTag}" возвращён в реестр`, 'success');
}

function retireAsset(assetId) {
  const idx = state.assets.findIndex(a => a.id === assetId);
  if (idx === -1) return;
  const [asset] = state.assets.splice(idx, 1);
  asset.status = 'RETIRED';
  asset.assignedUserId = null;
  state.archivedAssets.push(asset);
  closeDetailPane();
  renderAssetsList();
  populateSidebarTrees();
  showToast(`Актив "${asset.displayName || asset.assetTag}" списан (RETIRED)`, 'info');
}

// ══════════════════════════════════════════════════════════
// ADMIN REQUESTS VIEW
// ══════════════════════════════════════════════════════════

function renderAdminRequests() {
  const container = document.getElementById('admin-requests-list');
  const emptyEl = document.getElementById('requests-empty');

  updateRequestsBadge();

  if (state.requests.length === 0) {
    container.innerHTML = '';
    container.appendChild(emptyEl);
    emptyEl.classList.remove('hidden');
    return;
  }

  // Фича №3: расширенные статусы заявок (PENDING / APPROVED / REJECTED)
  container.innerHTML = state.requests.map((req, idx) => {
    const user = getUserById(req.userId);
    const initials = user ? user.initials : '??';
    const userName = user ? user.name : 'Неизвестный';

    // Определяем нормализованный статус (совместимость со старым ACCEPTED)
    const normalizedStatus = (() => {
      if (req.status === 'APPROVED' || req.status === 'ACCEPTED') return 'APPROVED';
      if (req.status === 'REJECTED') return 'REJECTED';
      return 'PENDING';
    })();

    // Блок действий / бейдж статуса
    let statusHtml;
    if (normalizedStatus === 'APPROVED') {
      statusHtml = `<div class="request-card-status request-status-approved">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
        В работе
      </div>`;
    } else if (normalizedStatus === 'REJECTED') {
      statusHtml = `<div class="request-card-status request-status-rejected">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        Отклонена
      </div>`;
    } else {
      // PENDING — показываем кнопки действий
      statusHtml = `<div class="request-card-actions">
        <button class="req-btn-accept" onclick="handleRequest(${idx}, 'accept')">&#10003; Принять в работу</button>
        <button class="req-btn-reject" onclick="handleRequest(${idx}, 'reject')">&#10005; Отклонить</button>
      </div>`;
    }

    // Модификатор класса карточки под статус
    const cardMod = normalizedStatus === 'APPROVED' ? 'req-approved'
      : normalizedStatus === 'REJECTED' ? 'req-rejected'
      : 'req-pending';

    return `<div class="request-card ${cardMod}">
      <div class="request-card-header">
        <div class="request-card-avatar">${escHtml(initials)}</div>
        <div class="request-card-meta">
          <div class="request-card-user">${escHtml(userName)}</div>
          <div class="request-card-date">${formatDate(req.date)}</div>
        </div>
        <span class="request-card-type-badge">${escHtml(req.type)}</span>
      </div>
      <div class="request-card-text">${escHtml(req.description)}</div>
      ${statusHtml}
    </div>`;
  }).join('');
}

function handleRequest(idx, action) {
  if (!state.requests[idx]) return;
  // Используем новые статусы (всё in-memory, localStorage не используется)
  state.requests[idx].status = action === 'accept' ? 'APPROVED' : 'REJECTED';
  renderAdminRequests();
  updateRequestsBadge();
  showToast(
    action === 'accept' ? 'Заявка принята в работу' : 'Заявка отклонена',
    action === 'accept' ? 'success' : 'error'
  );
}

// ══════════════════════════════════════════════════════════
// EMPLOYEE VIEW
// ══════════════════════════════════════════════════════════

function renderEmployeeView() {
  const user = state.currentUser;
  const dept = getDeptById(user.dept);
  const loc  = getLocById(user.location);

  // My assets (active only, not archived)
  const myAssets = state.assets.filter(a => a.assignedUserId === user.id);

  // Group by type
  const groups = {};
  myAssets.forEach(a => {
    if (!groups[a.assetType]) groups[a.assetType] = [];
    groups[a.assetType].push(a);
  });

  // Employee card
  const cardEl = document.getElementById('employee-card');
  cardEl.innerHTML = `
    <div class="employee-profile">
      <div class="employee-avatar">${escHtml(user.initials)}</div>
      <div class="employee-name">${escHtml(user.name)}</div>
      <div class="employee-role-label">${escHtml(user.position)}</div>
      <span class="employee-dept">${dept ? escHtml(dept.name) : '—'}</span>
    </div>

    <div class="employee-assets-header">
      <span class="employee-assets-title">Закреплённое оборудование</span>
      <span class="employee-assets-count">Назначено устройств: ${myAssets.length}</span>
    </div>

    ${myAssets.length === 0 ? `<div class="empty-state" style="padding:30px 0"><p>Нет назначенного оборудования</p></div>` : ''}

    ${Object.entries(groups).map(([type, assets]) => `
      <div class="employee-device-group">
        <div class="employee-device-group-title">
          <span class="${typeIconClass(type)}" style="width:16px;height:16px;display:flex;align-items:center">${typeIcon(type)}</span>
          <span>${typeLabel(type)}</span>
          <span class="group-count">${assets.length}</span>
        </div>
        ${assets.map(a => {
          const iconCls = typeIconClass(a.assetType);
          return `<div class="employee-asset-item">
            <div class="employee-asset-item-icon ${iconCls}">${typeIcon(a.assetType)}</div>
            <div class="employee-asset-item-info">
              <div class="employee-asset-item-name">${escHtml(a.displayName || a.assetTag)}</div>
              <div class="employee-asset-item-tag">${escHtml(a.assetTag)}${a.note && (a.note.includes('hostname') || a.note.includes('номер')) ? ' · ' + escHtml(a.note) : ''}</div>
            </div>
            ${statusBadge(a.status)}
          </div>`;
        }).join('')}
      </div>
    `).join('')}

    ${loc ? `<div class="employee-asset-hint">Локация: ${escHtml(loc.name)}</div>` : ''}
  `;

  // Reset request form state
  const form = document.getElementById('employee-request-form');
  const success = document.getElementById('req-success');
  const errEl = document.getElementById('req-error');
  if (form) { form.reset(); form.classList.remove('hidden'); }
  if (success) success.classList.add('hidden');
  if (errEl) errEl.classList.add('hidden');
}

// ══════════════════════════════════════════════════════════
// ADD ASSET MODAL
// ══════════════════════════════════════════════════════════

function openAddAssetModal() {
  // Populate selects
  const locSel = document.getElementById('new-location');
  locSel.innerHTML = '<option value="">Не указана</option>' +
    MOCK_LOCATIONS.map(l => `<option value="${l.id}">${escHtml(l.name)}</option>`).join('');

  const userSel = document.getElementById('new-assigned-user');
  userSel.innerHTML = '<option value="">Не назначен</option>' +
    MOCK_USERS.map(u => `<option value="${u.id}">${escHtml(u.name)}</option>`).join('');

  document.getElementById('add-asset-modal').classList.remove('hidden');
}

function closeAddAssetModal() {
  document.getElementById('add-asset-modal').classList.add('hidden');
  document.getElementById('add-asset-form').reset();
}

function confirmAddAsset() {
  const tag     = document.getElementById('new-asset-tag').value.trim();
  const serial  = document.getElementById('new-serial').value.trim();
  const type    = document.getElementById('new-type').value;
  const locId   = document.getElementById('new-location').value;
  const name    = document.getElementById('new-display-name').value.trim();
  const note    = document.getElementById('new-note').value.trim();
  const userId  = document.getElementById('new-assigned-user').value;

  if (!tag || !type) {
    showToast('Укажите Asset Tag и тип устройства', 'error');
    return;
  }

  // Check uniqueness
  if (state.assets.some(a => a.assetTag === tag) || state.archivedAssets.some(a => a.assetTag === tag)) {
    showToast('Asset Tag уже существует', 'error');
    return;
  }

  const newAsset = {
    id: 'a-' + Date.now(),
    assetTag: tag,
    serialNumber: serial || null,
    assetType: type,
    status: 'ACTIVE',
    assignedUserId: userId || null,
    locationId: locId || null,
    displayName: name || null,
    note: note || null,
  };

  state.assets.push(newAsset);
  closeAddAssetModal();
  renderAssetsList();
  populateSidebarTrees();
  showToast(`Актив "${newAsset.displayName || newAsset.assetTag}" добавлен`, 'success');
}

// ══════════════════════════════════════════════════════════
// EVENT LISTENERS SETUP
// ══════════════════════════════════════════════════════════

function setupEventListeners() {

  // ── LOGIN FORM
  document.getElementById('login-form').addEventListener('submit', e => {
    e.preventDefault();
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value.trim();
    if (!username || !password) {
      document.getElementById('login-error').classList.remove('hidden');
      return;
    }
    document.getElementById('login-error').classList.add('hidden');
    // Simulate brief loading
    const btn = document.getElementById('login-btn');
    const btnText = btn.querySelector('.btn-text');
    const spinner = btn.querySelector('.btn-spinner');
    btnText.classList.add('hidden');
    spinner.classList.remove('hidden');
    btn.disabled = true;
    setTimeout(() => {
      btnText.classList.remove('hidden');
      spinner.classList.add('hidden');
      btn.disabled = false;
      // Generic login → admin
      login('u-1');
    }, 800);
  });

  // ── QUICK LOGIN BUTTONS
  document.getElementById('quick-admin').addEventListener('click', () => {
    animateQuickBtn('quick-admin', () => login('u-1'));
  });
  document.getElementById('quick-employee').addEventListener('click', () => {
    animateQuickBtn('quick-employee', () => login('u-2'));
  });

  // ── PASSWORD TOGGLE
  document.getElementById('pwd-toggle').addEventListener('click', () => {
    const input = document.getElementById('login-password');
    input.type = input.type === 'password' ? 'text' : 'password';
  });

  // ── LOGOUT
  document.getElementById('logout-btn').addEventListener('click', () => {
    document.getElementById('topbar-user').classList.remove('open');
    setTimeout(logout, 150);
  });

  // ── TOPBAR USER DROPDOWN
  document.getElementById('topbar-user').addEventListener('click', e => {
    const el = document.getElementById('topbar-user');
    el.classList.toggle('open');
    // Close on outside click
    e.stopPropagation();
  });
  document.addEventListener('click', () => {
    document.getElementById('topbar-user').classList.remove('open');
  });

  // ── SIDEBAR TOGGLE
  document.getElementById('sidebar-toggle').addEventListener('click', () => {
    document.getElementById('sidebar').classList.toggle('collapsed');
  });

  // ── SIDEBAR NAVIGATION
  document.getElementById('nav-all-assets').addEventListener('click', () => navigateTo('all-assets'));
  document.getElementById('nav-archive').addEventListener('click', () => navigateTo('archive'));
  document.getElementById('nav-requests').addEventListener('click', () => navigateTo('requests'));
  document.getElementById('nav-settings').addEventListener('click', () => navigateTo('settings'));

  // Requests badge click
  document.getElementById('topbar-requests-badge').addEventListener('click', () => navigateTo('requests'));

  // ── ORG TREE TOGGLE
  document.getElementById('org-group-toggle').addEventListener('click', () => {
    const group = document.getElementById('nav-org');
    group.classList.toggle('open');
    if (!group.classList.contains('open')) {
      // If we were in org, go to all-assets
    }
  });

  // ── LOC TREE TOGGLE
  document.getElementById('loc-group-toggle').addEventListener('click', () => {
    const group = document.getElementById('nav-locations');
    group.classList.toggle('open');
  });

  // ── FILTER CHIPS
  document.getElementById('filter-type-chips').addEventListener('click', e => {
    const chip = e.target.closest('[data-filter="type"]');
    if (!chip) return;
    document.querySelectorAll('[data-filter="type"]').forEach(c => c.classList.remove('chip-active'));
    chip.classList.add('chip-active');
    state.filters.type = chip.dataset.value;
    state.selectedAssetId = null;
    closeDetailPane();
    renderAssetsList();
  });

  document.addEventListener('click', e => {
    const chip = e.target.closest('[data-filter="status"]');
    if (!chip) return;
    document.querySelectorAll('[data-filter="status"]').forEach(c => c.classList.remove('chip-active'));
    chip.classList.add('chip-active');
    state.filters.status = chip.dataset.value;
    state.selectedAssetId = null;
    closeDetailPane();
    renderAssetsList();
  });

  // ── SORT
  document.getElementById('sort-select').addEventListener('change', e => {
    state.sort = e.target.value;
    renderAssetsList();
  });

  // ── ADD ASSET BUTTON
  document.getElementById('btn-add-asset').addEventListener('click', openAddAssetModal);
  document.getElementById('close-add-modal').addEventListener('click', closeAddAssetModal);
  document.getElementById('cancel-add-modal').addEventListener('click', closeAddAssetModal);
  document.getElementById('confirm-add-asset').addEventListener('click', confirmAddAsset);

  // Modal overlay click to close
  document.getElementById('add-asset-modal').addEventListener('click', e => {
    if (e.target === document.getElementById('add-asset-modal')) closeAddAssetModal();
  });

  // ── EMPLOYEE REQUEST FORM
  document.getElementById('employee-request-form').addEventListener('submit', e => {
    e.preventDefault();
    const typeEl = document.querySelector('input[name="req-type"]:checked');
    const descEl = document.getElementById('req-description');
    const errEl  = document.getElementById('req-error');

    if (!typeEl || !descEl.value.trim()) {
      errEl.classList.remove('hidden');
      return;
    }
    errEl.classList.add('hidden');

    // Animate submit
    const btn = document.getElementById('submit-request-btn');
    const btnText = btn.querySelector('.btn-text');
    const spinner = btn.querySelector('.btn-spinner');
    btnText.classList.add('hidden');
    spinner.classList.remove('hidden');
    btn.disabled = true;

    setTimeout(() => {
      const req = {
        id: 'req-' + Date.now(),
        userId: state.currentUser.id,
        type: typeEl.value,
        description: descEl.value.trim(),
        date: new Date().toISOString(),
        status: 'PENDING',
      };
      state.requests.push(req);

      btnText.classList.remove('hidden');
      spinner.classList.add('hidden');
      btn.disabled = false;

      // Show success
      document.getElementById('employee-request-form').classList.add('hidden');
      document.getElementById('req-success').classList.remove('hidden');
    }, 900);
  });

  // New request button
  document.getElementById('req-new-btn').addEventListener('click', () => {
    document.getElementById('req-success').classList.add('hidden');
    const form = document.getElementById('employee-request-form');
    form.reset();
    form.classList.remove('hidden');
  });
}

// ══════════════════════════════════════════════════════════
// ANIMATION HELPERS
// ══════════════════════════════════════════════════════════

function animateQuickBtn(btnId, callback) {
  const btn = document.getElementById(btnId);
  btn.style.transform = 'scale(0.97)';
  btn.style.opacity = '0.8';
  setTimeout(() => {
    btn.style.transform = '';
    btn.style.opacity = '';
    callback();
  }, 200);
}

// ══════════════════════════════════════════════════════════
// BOOTSTRAP
// ══════════════════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {
  initAssets();
  setupEventListeners();
  renderLogin();

  // Keyboard shortcut: Escape closes detail/modal
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') {
      if (!document.getElementById('add-asset-modal').classList.contains('hidden')) {
        closeAddAssetModal();
        return;
      }
      if (state.selectedAssetId) {
        closeDetailPane();
      }
    }
  });
});
