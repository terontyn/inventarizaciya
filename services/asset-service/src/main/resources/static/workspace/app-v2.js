/* ═══════════════════════════════════════════════════════
   ITAM · IT Asset Management — SPA Application Logic
   Sandbox v0.8 — In-Memory State Only
   ═══════════════════════════════════════════════════════ */

'use strict';

// ══════════════════════════════════════════════════════════
// MOCK DATA
// ══════════════════════════════════════════════════════════

let USER_SERVICE_BASE = window.location.port === '8781' ? 'http://127.0.0.1:8780' : 'http://localhost:8080';
let ASSET_SERVICE_BASE = window.location.protocol === 'file:' ? 'http://127.0.0.1:8781' : window.location.origin;

if (window.location.hostname.endsWith('.github.dev')) {
    // Dynamic GitHub Codespaces URLs
    const origin = window.location.origin;
    ASSET_SERVICE_BASE = origin;
    USER_SERVICE_BASE = origin.replace('-8081.app.github.dev', '-8080.app.github.dev');
} else if (window.location.protocol !== 'file:' && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
    // Fallback for other remote setups
    ASSET_SERVICE_BASE = window.location.origin;
    USER_SERVICE_BASE = window.location.origin.replace('8081', '8080');
}

const API_BASE = ASSET_SERVICE_BASE;
const USER_API_BASE = USER_SERVICE_BASE;

function getHeaders() {
    return {
        'Content-Type': 'application/json',
        'X-Authenticated-Tenant-Id': '00000000-0000-0000-0000-000000000001',
        'X-Authenticated-User-Id': state.currentUser ? state.currentUser.id : 'a1111111-1111-1111-1111-111111111111',
        'X-Authenticated-Actor': state.currentUser ? 'User-' + state.currentUser.id.substring(0, 8) : 'System'
    };
}

// ══════════════════════════════════════════════════════════
// APPLICATION STATE
// ══════════════════════════════════════════════════════════

const state = {
  currentUser: null,     // { id, name, role, ... }
  assets: [],            // active (non-archived) assets
  archivedAssets: [],    // archived assets
  requests: [],          // employee requests
  users: [],
  departments: [],
  locations: [],
  branches: [],
  assetTypes: [],
  simCards: [],
  cartridges: [],

  // Admin UI state
  selectedAssetId: null,
  activeSection: 'all-assets',
  activeDetailTab: 'info',
  filters: { type: 'ALL', status: 'ALL', search: '' },
  sort: 'tag-asc',

  // Org tree state (in-memory only, no localStorage)
  openDepts: new Set(),        // Set of dept IDs that are expanded
  selectedEmployeeId: null,    // ID of selected employee in tree
  employeeRequestsFilter: 'ALL',

  // View & Bulk Selection
  viewMode: 'card', // 'card' | 'table'
  selectedAssets: new Set(),

  // Settings
  settingsActiveTab: 'general',

  // Column picker
  visibleColumns: ['tag', 'type', 'name', 'user', 'location', 'status'],
};

function toCamel(o) {
  if (Array.isArray(o)) return o.map(i => toCamel(i));
  if (o !== null && typeof o === 'object') {
    return Object.keys(o).reduce((res, key) => {
      const camelKey = key.replace(/_([a-z])/g, g => g[1].toUpperCase());
      res[camelKey] = toCamel(o[key]);
      return res;
    }, {});
  }
  return o;
}

function toSnake(o) {
  if (Array.isArray(o)) return o.map(i => toSnake(i));
  if (o !== null && typeof o === 'object') {
    return Object.keys(o).reduce((res, key) => {
      const snakeKey = key.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`);
      res[snakeKey] = toSnake(o[key]);
      return res;
    }, {});
  }
  return o;
}

async function loadAllData() {
    try {
        const fetchWithTimeout = (url, options, timeout = 3000) => {
            return Promise.race([
                fetch(url, options),
                new Promise((_, reject) => setTimeout(() => reject(new Error('Timeout ' + url)), timeout))
            ]);
        };
        const [uRes, dRes, lRes, aRes, arcRes, reqRes, bRes, atRes, scRes, cartRes] = await Promise.all([
            fetchWithTimeout(`${USER_SERVICE_BASE}/api/v1/users`, { headers: getHeaders() }),
            fetchWithTimeout(`${USER_SERVICE_BASE}/api/v1/departments`, { headers: getHeaders() }),
            fetchWithTimeout(`${USER_SERVICE_BASE}/api/v1/locations`, { headers: getHeaders() }),
            fetchWithTimeout(`${ASSET_SERVICE_BASE}/api/v1/assets`, { headers: getHeaders() }),
            fetchWithTimeout(`${ASSET_SERVICE_BASE}/api/v1/assets?visibility=archived`, { headers: getHeaders() }),
            fetchWithTimeout(`${API_BASE}/api/v1/requests`, { headers: getHeaders() }),
            fetchWithTimeout(`${USER_SERVICE_BASE}/api/v1/branches`, { headers: getHeaders() }),
            fetchWithTimeout(`${ASSET_SERVICE_BASE}/api/v1/asset-types`, { headers: getHeaders() }).catch(() => ({ok: false})),
            fetchWithTimeout(`${ASSET_SERVICE_BASE}/api/v1/sim-cards`, { headers: getHeaders() }).catch(() => ({ok: false})),
            fetchWithTimeout(`${ASSET_SERVICE_BASE}/api/v1/cartridges`, { headers: getHeaders() }).catch(() => ({ok: false}))
        ]);

        if (uRes.ok) {
            const usersData = toCamel(await uRes.json());
            state.users = usersData.map(u => {
                u.role = u.id === 'a1111111-1111-1111-1111-111111111111' ? 'ADMIN' : 'EMPLOYEE';
                const parts = u.name.split(' ');
                u.initials = parts.length > 1 ? (parts[0][0] + parts[1][0]).toUpperCase() : u.name.substring(0, 2).toUpperCase();
                return u;
            });
        }
        if (dRes.ok) state.departments = toCamel(await dRes.json());
        if (lRes.ok) state.locations = toCamel(await lRes.json());
        if (aRes.ok) state.assets = toCamel(await aRes.json());
        if (arcRes.ok) state.archivedAssets = toCamel(await arcRes.json());
        if (reqRes.ok) state.requests = toCamel(await reqRes.json());
        if (bRes && bRes.ok) state.branches = toCamel(await bRes.json());
        if (atRes && atRes.ok) state.assetTypes = toCamel(await atRes.json());
        if (scRes && scRes.ok) state.simCards = toCamel(await scRes.json());
        if (cartRes && cartRes.ok) state.cartridges = toCamel(await cartRes.json());
    } catch (e) {
        console.error('Error loading data', e);
        showToast('Backend недоступен. Показаны демонстрационные данные, изменения не сохранятся: ' + e.message, 'error');

        if (state.users.length === 0) {
            state.users = [
                { id: 'a1111111-1111-1111-1111-111111111111', name: 'Админ Тестов', role: 'ADMIN', initials: 'АТ', departmentId: 'd1', position: 'IT Администратор' },
                { id: 'e2222222-2222-2222-2222-222222222222', name: 'Иван Сотрудников', role: 'EMPLOYEE', initials: 'ИС', departmentId: 'd2', position: 'Менеджер' }
            ];
            state.departments = [
                { id: 'd1', name: 'IT Отдел' },
                { id: 'd2', name: 'Отдел продаж' }
            ];
            state.locations = [
                { id: 'l1', name: 'Главный офис', branchId: 'b1' }
            ];
            state.branches = [
                { id: 'b1', name: 'Москва' }
            ];
            state.assets = [
                { id: '1', assetTag: 'LAP-001', assetType: 'PC_LAPTOP', displayName: 'MacBook Pro 16', status: 'ACTIVE', assignedUserId: 'a1111111-1111-1111-1111-111111111111', locationId: 'l1', serialNumber: 'SN12345' },
                { id: '2', assetTag: 'MON-001', assetType: 'MONITOR', displayName: 'Dell UltraSharp', status: 'ACTIVE', assignedUserId: 'e2222222-2222-2222-2222-222222222222', locationId: 'l1', serialNumber: 'SN67890' }
            ];
            state.requests = [
                { id: 'req-1', type: 'Ноутбук', description: 'Запрос на новый ПК', status: 'NEW', userName: 'Иван Сотрудников', createdAt: new Date().toISOString() }
            ];
        }
    }
}

// ══════════════════════════════════════════════════════════
// HELPERS
// ══════════════════════════════════════════════════════════

function getUserById(id) { return state.users.find(u => u.id === id) || null; }
function getDeptById(id)  { return state.departments.find(d => d.id === id) || null; }
function getLocById(id)   { return state.locations.find(l => l.id === id) || null; }
function getAssetById(id) { return [...state.assets, ...state.archivedAssets].find(a => a.id === id) || null; }
function getBranchName(branchId) {
  if (!branchId) return '—';
  const branch = (state.branches || []).find(b => b.id === branchId);
  return branch ? branch.name : String(branchId);
}

function typeLabel(type) {
  return { PC_LAPTOP: 'Ноутбук', DESKTOP: 'ПК', MONITOR: 'Монитор', PRINTER: 'Принтер', STATIONARY_PHONE: 'Телефон', OTHER: 'Другое' }[type] || type;
}

function typeIconClass(type) {
  return { PC_LAPTOP: 'icon-laptop', DESKTOP: 'icon-desktop', MONITOR: 'icon-monitor', PRINTER: 'icon-printer', STATIONARY_PHONE: 'icon-phone', OTHER: 'icon-other' }[type] || 'icon-other';
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

function getChatHistory(resolutionComment) {
    if (!resolutionComment) return [];
    try {
        const parsed = JSON.parse(resolutionComment);
        if (Array.isArray(parsed)) return parsed;
    } catch(e) {}
    return [{ role: 'admin', text: resolutionComment, date: new Date().toISOString() }];
}

function renderChatHistoryHtml(history, isEmployeeView = false) {
    if (!history || history.length === 0) return '';

    const lastMsg = history[history.length - 1];
    const isLastMsgFromEmployee = lastMsg.role === 'employee';
    let hasNewMessage = false;
    if (isEmployeeView && !isLastMsgFromEmployee) hasNewMessage = true;
    if (!isEmployeeView && isLastMsgFromEmployee) hasNewMessage = true;

    let html = `<details class="chat-accordion" style="margin-top: 12px; background: var(--bg-primary); border-radius: 8px; border: 1px solid var(--border-subtle); overflow: hidden;">
        <summary style="cursor: pointer; display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; background: var(--bg-secondary); font-weight: 500; font-size: 13px; color: var(--text-primary); user-select: none; list-style: none;">
            <div style="display: flex; align-items: center; gap: 8px;">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
                <span>Чат (${history.length})</span>
            </div>
            ${hasNewMessage ? '<span style="display: flex; align-items: center; gap: 6px; color: var(--error); font-size: 11px; font-weight: 600; background: var(--error-soft); padding: 2px 8px; border-radius: 12px;"><span style="width: 6px; height: 6px; border-radius: 50%; background: var(--error); display: inline-block;"></span>Новое сообщение</span>' : '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="opacity: 0.5;"><polyline points="6 9 12 15 18 9"></polyline></svg>'}
        </summary>
        <div class="chat-history-content" style="padding: 12px; display: flex; flex-direction: column; gap: 8px; max-height: 300px; overflow-y: auto; background: var(--bg-primary);">`;

    history.forEach(msg => {
        const isEmployee = msg.role === 'employee';
        const align = isEmployee ? 'align-self: flex-end;' : 'align-self: flex-start;';
        const bg = isEmployee ? 'background: #0ea5e9; color: #ffffff;' : 'background: var(--bg-secondary); border: 1px solid var(--border-subtle); color: var(--text-primary);';

        let author = isEmployee ? 'Сотрудник' : 'Администратор';
        if (isEmployeeView && isEmployee) author = 'Вы';
        if (!isEmployeeView && !isEmployee) author = 'Вы';

        html += `<div style="${align} max-width: 85%; padding: 8px 12px; border-radius: 12px; ${bg}">
            <div style="font-size: 11px; margin-bottom: 4px; opacity: 0.8;">${author} • ${msg.date ? new Date(msg.date).toLocaleString() : ''}</div>
            <div style="font-size: 13px; white-space: pre-wrap; line-height: 1.4;">${escHtml(msg.text)}</div>
        </div>`;
    });
    html += '</div></details>';
    return html;
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
    warning: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`,
  };

  const timeout = (type === 'error' || type === 'warning') ? 4500 : 3000;
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span class="toast-icon">${icons[type] || icons.info}</span><span class="toast-msg">${escHtml(message)}</span><button class="toast-close" aria-label="Закрыть">&times;</button>`;
  document.body.appendChild(toast);

  const dismiss = () => {
    toast.classList.add('hiding');
    setTimeout(() => toast.remove(), 220);
  };
  toast.querySelector('.toast-close').addEventListener('click', dismiss);

  let timer = setTimeout(dismiss, timeout);
  toast.addEventListener('mouseenter', () => clearTimeout(timer));
  toast.addEventListener('mouseleave', () => { timer = setTimeout(dismiss, 1500); });
}

// Field validation helper
function setFieldError(fieldId, errorId, show) {
  const field = document.getElementById(fieldId);
  const errEl = document.getElementById(errorId);
  if (field) field.classList.toggle('field-invalid', show);
  if (errEl) errEl.classList.toggle('hidden', !show);
}
function clearFieldErrors(...pairs) {
  pairs.forEach(([f, e]) => setFieldError(f, e, false));
}


// ══════════════════════════════════════════════════════════
// AUTH
// ══════════════════════════════════════════════════════════

async function login(username) {
  console.log('login called', username);
  try {
      // Since there is no auth endpoint in user-service, we simulate login.
      // Load all data to get users
      await loadAllData();

      let user = null;
      if (username === 'admin' || username === 'a1111111-1111-1111-1111-111111111111') {
          user = state.users.find(u => u.id === 'a1111111-1111-1111-1111-111111111111') || state.users.find(u => u.role === 'ADMIN') || state.users[0];
      } else if (username === 'employee' || username === 'e2222222-2222-2222-2222-222222222222') {
          user = state.users.find(u => u.id === 'e2222222-2222-2222-2222-222222222222') || state.users.find(u => u.role !== 'ADMIN') || state.users[1];
      } else {
          // If typed manually
          user = state.users.find(u => u.name.toLowerCase().includes(username.toLowerCase()));
      }

      if (!user) {
          alert('User not found in state.users! Username searched: ' + username + '. state.users length: ' + state.users.length);
          document.getElementById('login-error').classList.remove('hidden');
          return;
      }

      resetAllState();
      state.currentUser = user;

      // Re-load data just in case with correct headers
      await loadAllData();
      renderApp();
  } catch (e) {
      console.error('Login error', e);
      alert('Login error: ' + e.message + '\n' + e.stack);
      document.getElementById('login-error').classList.remove('hidden');
  }
}

function logout() {
  localStorage.removeItem('token');
  resetAllState();
  state.currentUser = null;
  renderLogin();
}

function resetAllState() {
  state.selectedAssetId = null;
  state.activeSection = 'all-assets';
  state.activeDetailTab = 'info';
  state.filters = { type: 'ALL', status: 'ALL', assignment: 'ALL', search: '' };
  state.technicalSearchResults = [];
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
    <div class="search-bar" style="display:flex; align-items:center; width:100%; max-width:600px;">
      <div style="position:relative; flex:1;">
        <svg class="search-bar-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="position:absolute; left:12px; top:12px; color:var(--text-muted)">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
        </svg>
        <input id="global-search" type="text" placeholder="Поиск по тегу, SN, имени, заметке..." autocomplete="off" style="width:100%;" />
      </div>
      <button class="btn-icon" id="btn-scanner" title="Сканировать штрихкод" style="margin-left:8px;" onclick="openScannerModal()">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 7V5a2 2 0 0 1 2-2h2"/><path d="M17 3h2a2 2 0 0 1 2 2v2"/><path d="M21 17v2a2 2 0 0 1-2 2h-2"/><path d="M7 21H5a2 2 0 0 1-2-2v-2"/><rect x="7" y="7" width="10" height="10" rx="1"/></svg>
      </button>
    </div>`;

  let searchTimeout = null;
  document.getElementById('global-search').addEventListener('input', e => {
    state.filters.search = e.target.value.trim();
    if (state.activeSection === 'all-assets') {
      renderAssetsList(); // Local search update instantly

      clearTimeout(searchTimeout);
      const q = state.filters.search;
      if (q.length >= 2) {
        searchTimeout = setTimeout(async () => {
          try {
            const res = await fetch(`${API_BASE}/api/v1/assets/technical-attribute-search?q=${encodeURIComponent(q)}`, {
              headers: getHeaders()
            });
            if (res.ok) {
              const data = await res.json();
              state.technicalSearchResults = toCamel(data).results || [];
              renderAssetsList(); // Re-render with backend matches
            }
          } catch (err) {
            console.error('Tech search failed', err);
          }
        }, 300);
      } else {
        state.technicalSearchResults = [];
        renderAssetsList();
      }
    }
  });
}

// ══════════════════════════════════════════════════════════
// NAVIGATION
// ══════════════════════════════════════════════════════════

const ALL_SECTIONS = ['dashboard', 'all-assets', 'archive', 'org', 'locations', 'requests', 'settings', 'employee', 'sim-cards', 'cartridges', 'peripherals'];


function navigateTo(section, meta = {}) {
  state.activeSection = section;

  // Сброс выбора сотрудника в дереве при обычной навигации
  if (section !== 'employee') {
    // Восстановить панель заявки если была скрыта
    const requestPanel = document.querySelector('.employee-request-panel');
    if (requestPanel) requestPanel.style.display = '';

    // Always clear search filter when navigating to all-assets
    if (section === 'all-assets') {
      state.filters.search = '';
      const searchInput = document.getElementById('global-search');
      if (searchInput) searchInput.value = '';
    }

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
    'dashboard': 'Дашборд',
    'all-assets': 'Все активы',
    'archive': 'Архив',
    'org': 'Оргструктура',
    'locations': 'Локации',
    'requests': 'Заявки',
    'settings': 'Настройки',
    'employee': 'Личный кабинет',
    'sim-cards': 'SIM-карты',
    'cartridges': 'Картриджи',
    'peripherals': 'Периферия',
  };

  document.getElementById('topbar-module').textContent = moduleNames[section] || 'Инвентаризация';

  // Close detail pane on section change
  closeDetailPane();

  // Render section content
  switch (section) {
    case 'dashboard':   renderDashboard(); break;
    case 'all-assets':  renderAssetsList(); break;
    case 'archive':     renderArchiveList(); break;
    case 'org':         renderOrgSection(meta.deptId); break;
    case 'locations':   renderLocSection(meta.locId); break;
    case 'requests':    renderAdminRequests(); break;
    case 'employee':    renderEmployeeView(); break;
    case 'settings':    renderSettingsSection(state.settingsActiveTab || 'general'); break;
    case 'sim-cards':   renderSimCardsSection(); break;
    case 'cartridges':  renderCartridgesSection(); break;
    case 'peripherals': renderPeripheralsSection(); break;
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
  locTree.innerHTML = state.locations.map(l => {
    const count = state.assets.filter(a => a.locationId === l.id).length;
    return `<li class="tree-item tree-child-item" data-loc="${l.id}">
      <div style="display:flex;align-items:center;">
        <button class="tree-btn" style="flex:1;" onclick="navigateTo('locations', {locId:'${l.id}'})">
          <span>${escHtml(l.name)}</span>
          <span class="tree-badge">${count}</span>
        </button>
        <button class="btn-icon" onclick="openEditLocModal('${l.id}')" title="Редактировать локацию" style="padding:4px;background:none;border:none;cursor:pointer;color:var(--text-muted);"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg></button>
      </div>
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

  orgTree.innerHTML = state.departments.map(d => {
    // Сотрудники этого отдела
    const deptUsers = state.users.filter(u => u.departmentId === d.id);
    // Активы отдела (для бейджа)
    const assetCount = state.assets.filter(a => {
      const u = getUserById(a.assignedUserId);
      return u && u.departmentId === d.id;
    }).length;

    const isOpen = state.openDepts.has(d.id);

    // Строки сотрудников
    const employeeRows = deptUsers.map(u => {
      const isSelected = state.selectedEmployeeId === u.id;
      return `<button
          class="org-employee-item${isSelected ? ' emp-selected' : ''}"
          id="emp-tree-${u.id}"
          onclick="selectEmployeeFromTree('${u.id}')"
          title="${escHtml(u.name)}${u.position ? ' · ' + escHtml(u.position) : ''}"
        >
          <span class="org-employee-dot"></span>
          <span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${escHtml(u.name)}</span>
        </button>`;
    }).join('');

    return `<li class="org-dept-node${isOpen ? ' dept-open' : ''}" id="dept-node-${d.id}">
      <div style="display:flex;align-items:center;">
        <button
          class="org-dept-btn"
          style="flex:1;"
          onclick="toggleDeptNode('${d.id}')"
          title="${escHtml(d.name)}"
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
        <button class="btn-icon" onclick="openEditDeptModal('${d.id}')" title="Редактировать отдел" style="padding:4px;background:none;border:none;cursor:pointer;color:var(--text-muted);"><svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg></button>
      </div>
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
async function showEmployeeInfoSheet(user) {
  const dept = getDeptById(user.departmentId);
  const loc  = getLocById(user.locationId);
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
      <div class="employee-role-label">${user.position ? escHtml(user.position) : escHtml(user.role === 'ADMIN' ? 'Администратор' : 'Сотрудник')}</div>
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
            <div class="employee-asset-item-info" id="emp-asset-info-${a.id}">
              <div class="employee-asset-item-name">${escHtml(a.displayName || a.assetTag)}</div>
              <div class="employee-asset-item-tag">${escHtml(a.assetTag)}</div>
            </div>
            ${statusBadge(a.status)}

          </div>`;
        }).join('')}
      </div>
    `).join('')}
  `;

  try {
    const res = await fetch(`${API_BASE}/api/v1/assets/employee-sheet-technical-hints?user_id=${user.id}`, {
      headers: getHeaders()
    });
    if (res.ok) {
      const data = toCamel(await res.json());
      (data.assets || []).forEach(hintAsset => {
        const infoEl = document.getElementById(`emp-asset-info-${hintAsset.assetId}`);
        if (infoEl && hintAsset.technicalHints) {
          const hintsHtml = Object.entries(hintAsset.technicalHints)
            .map(([k, v]) => `<div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 4px;">${escHtml(k)}: <span style="color:var(--text-main);">${escHtml(v)}</span></div>`)
            .join('');
          if (hintsHtml) {
            infoEl.insertAdjacentHTML('beforeend', hintsHtml);
          }
        }
      });
    }
  } catch (e) {
    console.error('Failed to load technical hints', e);
  }

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
  const pending = state.requests.filter(r => r.status === 'NEW').length;
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
  // Assignment filter
  if (state.filters.assignment === 'ASSIGNED') {
    list = list.filter(a => a.assignedUserId != null);
  } else if (state.filters.assignment === 'UNASSIGNED') {
    list = list.filter(a => a.assignedUserId == null);
  }
  // Search
  if (state.filters.search) {
    const q = state.filters.search.toLowerCase();
    const techSearchIds = new Set((state.technicalSearchResults || []).map(r => r.assetId));

    list = list.filter(a => {
      return (
        techSearchIds.has(a.id) ||
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

  const selectAllHeader = document.getElementById('list-select-all-header');
  const q = state.filters.search || '';
  if (state.viewMode === 'table') {
    if (selectAllHeader) selectAllHeader.classList.add('hidden');
    let tableHtml = `<div class="asset-table-wrapper"><table class="asset-table">
      <thead>
        <tr>
          <th width="40" style="text-align:center;">
            <label class="checkbox-container">
              <input type="checkbox" id="select-all-table" ${list.length > 0 && state.selectedAssets.size === list.length ? 'checked' : ''} onchange="toggleSelectAll(this.checked, ${JSON.stringify(list.map(a => a.id)).replace(/"/g, "'")})">
              <span class="checkmark"></span>
            </label>
          </th>
          <th onclick="toggleSort('tag-asc', 'tag-desc')">Тег / Серийник</th>
          <th onclick="toggleSort('type-asc', 'type-desc')">Тип / Модель</th>
          <th onclick="toggleSort('name-asc', 'name-desc')">Имя</th>
          <th>Ответственный</th>
          <th>Локация</th>
          <th>Статус</th>
        </tr>
      </thead>
      <tbody>`;
    tableHtml += list.map(asset => {
      const techSearchRes = (state.technicalSearchResults || []).find(r => r.assetId === asset.id);
      const techSearchField = techSearchRes ? techSearchRes.matchedField.fieldName : null;
      return buildAssetTableRow(asset, q, techSearchField);
    }).join('');
    tableHtml += `</tbody></table></div>`;
    container.innerHTML = tableHtml;
  } else {
    if (selectAllHeader) {
      selectAllHeader.classList.remove('hidden');
      const selectAllInput = document.getElementById('select-all-assets');
      if (selectAllInput) {
        selectAllInput.checked = (list.length > 0 && state.selectedAssets.size === list.length);
        selectAllInput.onchange = (e) => toggleSelectAll(e.target.checked, list.map(a => a.id));
      }
    }
    container.innerHTML = list.map(asset => {
      const techSearchRes = (state.technicalSearchResults || []).find(r => r.assetId === asset.id);
      const techSearchField = techSearchRes ? techSearchRes.matchedField.fieldName : null;
      return buildAssetCard(asset, q, techSearchField);
    }).join('');
  }
}

function toggleSort(asc, desc) {
  state.sort = state.sort === asc ? desc : asc;
  const select = document.getElementById('sort-select');
  if (select) select.value = state.sort;
  renderAssetsList();
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

  if (chips.length > 0) {
      container.innerHTML += `
        <button class="btn-secondary" style="margin-left:auto; padding: 2px 8px; font-size: 0.75rem; border: none; background: transparent; color: var(--accent);" onclick="clearAllFilters()">
          Очистить фильтры
        </button>
      `;
  }

  // Сохраняем кеш функций удаления в глобальный массив (in-memory)
  renderActiveFilterChips._handlers = chips.map(c => c.onRemove);
}

// Вызывается через onclick в HTML (inline), использует сохранённый массив handlers
function removeFilterChip(idx) {
  const handlers = renderActiveFilterChips._handlers;
  if (handlers && handlers[idx]) handlers[idx]();
}

function clearAllFilters() {
  state.filters.type = 'ALL';
  state.filters.status = 'ALL';
  state.filters.assignment = 'ALL';
  state.filters.search = '';
  document.querySelectorAll('[data-filter]').forEach(c => c.classList.remove('chip-active'));
  const searchInput = document.getElementById('global-search');
  if (searchInput) searchInput.value = '';
  renderAssetsList();
}

window.copyToClipboard = function(e, text) {
  e.stopPropagation();
  navigator.clipboard.writeText(text).then(() => {
    showToast('Скопировано: ' + text, 'success');
  });
};

function buildAssetCard(asset, searchQuery = '', techSearchField = null) {
  const user = getUserById(asset.assignedUserId);
  const loc  = getLocById(asset.locationId);
  const iconCls = typeIconClass(asset.assetType);
  const isSelected = state.selectedAssets.has(asset.id);
  const isActive = state.selectedAssetId === asset.id;

  const titleText = asset.displayName || asset.assetTag;
  const titleHtml = highlightText(titleText, searchQuery);
  const copyIcon = (val) => `<svg onclick="copyToClipboard(event, '${val}')" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="cursor:pointer; margin-left:4px; vertical-align:middle; color:var(--text-muted);"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>`;
  const tagHtml   = highlightText(asset.assetTag, searchQuery) + (asset.assetTag ? copyIcon(asset.assetTag) : '');
  const snHtml    = highlightText(asset.serialNumber, searchQuery) + (asset.serialNumber ? copyIcon(asset.serialNumber) : '');

  return `<div class="asset-card ${isActive ? 'selected' : ''}" id="card-${asset.id}" onclick="selectAsset('${asset.id}')" style="cursor:pointer;">
    <div style="padding: 14px 0 14px 14px; display: flex; align-items: center;" onclick="event.stopPropagation()">
      <label class="checkbox-container">

        <input type="checkbox" class="asset-checkbox" value="${asset.id}" ${isSelected ? 'checked' : ''} onchange="toggleAssetSelection('${asset.id}', this.checked)">
        <span class="checkmark"></span>
      </label>
    </div>
    <div class="asset-card-icon ${iconCls}">${typeIcon(asset.assetType)}</div>
    <div class="asset-card-body">

      <div class="asset-card-title">${titleHtml}</div>
      <div class="asset-card-meta">${tagHtml} · ${snHtml}</div>
      ${techSearchField ? `<div style="font-size:0.7rem;color:var(--accent);margin-top:4px;">🔍 Найдено по: ${escHtml(techSearchField)}</div>` : ''}
    </div>
    <div class="asset-card-right">
      ${statusBadge(asset.status)}
      <div class="asset-card-user">${user ? escHtml(user.name.split(' ').slice(0,2).join(' ')) : '<span style="color:var(--text-disabled)">Не назначен</span>'}</div>
    </div>
  </div>`;
}

function buildAssetTableRow(asset, searchQuery = '', techSearchField = null) {
  const user = getUserById(asset.assignedUserId);
  const loc  = getLocById(asset.locationId);
  const isSelected = state.selectedAssets.has(asset.id);

  const titleText = asset.displayName || asset.assetTag;
  const titleHtml = highlightText(titleText, searchQuery);
  const tagHtml   = highlightText(asset.assetTag, searchQuery);
  const snHtml    = highlightText(asset.serialNumber, searchQuery);

  return `<tr class="${isSelected ? 'selected' : ''}" id="row-${asset.id}" onclick="selectAsset('${asset.id}')" style="cursor:pointer;">
    <td style="text-align:center;" onclick="event.stopPropagation()">
      <label class="checkbox-container">
        <input type="checkbox" class="asset-checkbox" value="${asset.id}" ${isSelected ? 'checked' : ''} onchange="toggleAssetSelection('${asset.id}', this.checked)">
        <span class="checkmark"></span>
      </label>
    </td>
    <td>
      <div>${tagHtml}</div>
      <div style="font-size:0.75rem; color:var(--text-muted);">${snHtml}</div>
    </td>
    <td>
      <div style="display:flex; align-items:center; gap:6px;">
        <span style="color:var(--text-muted);">${typeIcon(asset.assetType)}</span>
        <span>${typeLabel(asset.assetType)}</span>
      </div>
    </td>
    <td>${titleHtml}</td>
    <td>${user ? escHtml(user.name) : '<span style="color:var(--text-disabled)">Не назначен</span>'}</td>
    <td>${loc ? escHtml(loc.name) : '<span style="color:var(--text-disabled)">Не указана</span>'}</td>
    <td>${statusBadge(asset.status)}</td>
  </tr>`;
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
    state.departments.forEach(d => {
      const dAssets = state.assets.filter(a => { const u = getUserById(a.assignedUserId); return u && u.departmentId === d.id; });
      if (dAssets.length === 0) return;
      html += `<div class="group-header"><span>${escHtml(d.name)}</span><span class="group-count">${dAssets.length}</span></div>`;
      html += dAssets.map(a => buildAssetCard(a)).join('');
    });
    list.innerHTML = html || `<div class="empty-state"><p>Нет данных</p></div>`;
    return;
  }

  title.textContent = dept.name;
  const dAssets = state.assets.filter(a => { const u = getUserById(a.assignedUserId); return u && u.departmentId === dept.id; });
  sub.innerHTML = `Отдел: ${escHtml(dept.name)} · Активов: ${dAssets.length} <button class="btn-secondary" style="padding: 2px 8px; font-size: 0.75rem; margin-left: 8px;" onclick="openEditDeptModal('${dept.id}')">Ред.</button>`;
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
    state.locations.forEach(l => {
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
  sub.innerHTML = `Филиал: ${escHtml(getBranchName(loc.branchId))} · Активов: ${lAssets.length} <button class="btn-secondary" style="padding: 2px 8px; font-size: 0.75rem; margin-left: 8px;" onclick="openEditLocModal('${loc.id}')">Ред.</button>`;
  list.innerHTML = lAssets.length ? lAssets.map(a => buildAssetCard(a)).join('') : `<div class="empty-state"><p>Нет активов</p></div>`;
}

// ══════════════════════════════════════════════════════════
// ASSET SELECTION & DETAIL PANE
// ══════════════════════════════════════════════════════════

function selectAsset(id) {
  // Remove selection from all cards
  document.querySelectorAll('.asset-card.selected, tr.selected').forEach(el => el.classList.remove('selected'));

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
  const dept = user ? getDeptById(user.departmentId) : null;
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

      <button class="btn-icon" onclick="openEditAssetModal('${asset.id}')" title="Редактировать" style="margin-left:16px; background:none; border:none; cursor:pointer; color:var(--text-muted);">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
      </button>
      <button class="detail-close" onclick="closeDetailPane()" title="Закрыть" style="margin-left: 12px;">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </button>
    </div>

    <div class="detail-tabs">
      <button class="detail-tab active" data-tab="info" onclick="switchDetailTab('info')">Информация</button>
      <button class="detail-tab" data-tab="responsibility" onclick="switchDetailTab('responsibility')">Ответственность</button>
      <button class="detail-tab" data-tab="lifecycle" onclick="switchDetailTab('lifecycle')">Жизненный цикл</button>
      <button class="detail-tab" data-tab="requests" onclick="switchDetailTabRequests('')">Заявки</button>
      <button class="detail-tab" data-tab="peripherals" onclick="switchDetailTab('peripherals')">Периферия</button>
    </div>

    <div class="detail-body">
      <!-- TAB: INFO -->
      <div class="detail-tab-panel active" id="tab-info">
        <div class="detail-field"><span class="detail-field-label">Статус</span>${statusBadge(asset.status)}</div>
        <div class="detail-field"><span class="detail-field-label">Тип устройства</span><span class="detail-field-value">${typeLabel(asset.assetType)}</span></div>
        <div class="detail-field"><span class="detail-field-label">Asset Tag</span><span class="detail-field-value mono">${escHtml(asset.assetTag)}</span></div>
        <div class="detail-field">
          <span class="detail-field-label">Серийный номер</span>
          <span class="detail-field-value mono disp-field">${escHtml(asset.serialNumber || '—')}</span>
        </div>
        <div class="detail-field">
          <span class="detail-field-label">Отображаемое имя</span>
          <span class="detail-field-value disp-field">${escHtml(asset.displayName || '—')}</span>
        </div>
        <div class="detail-field">
          <span class="detail-field-label">IP-адрес</span>
          <span class="detail-field-value disp-field mono">${escHtml(asset.ipAddress || '—')}</span>
        </div>
        ${asset.parentAssetId ? `
        <div class="detail-field">
          <span class="detail-field-label">Подключен к</span>
          <span class="detail-field-value disp-field"><a href="#" onclick="renderDetailPane('${asset.parentAssetId}')" style="color:var(--accent); text-decoration:underline;">${escHtml(getAssetById(asset.parentAssetId)?.displayName || getAssetById(asset.parentAssetId)?.assetTag || asset.parentAssetId)}</a></span>
        </div>
        ` : ''}
        ${state.assets.filter(a => a.parentAssetId === asset.id).length > 0 ? `
        <div class="detail-field" style="grid-column: 1 / -1;">
          <span class="detail-field-label">Периферия (Подключено)</span>
          <div class="detail-field-value" style="display:flex; flex-direction:column; gap:4px; margin-top:4px;">
            ${state.assets.filter(a => a.parentAssetId === asset.id).map(a => `<a href="#" onclick="renderDetailPane('${a.id}')" style="color:var(--text-secondary);"><span style="display:inline-block; width:6px; height:6px; border-radius:50%; background:var(--accent); margin-right:6px;"></span>${escHtml(a.displayName || a.assetTag)} (${typeLabel(a.assetType)})</a>`).join('')}
          </div>
        </div>
        ` : ''}
        <div class="detail-field" style="grid-column: 1 / -1;">
          <span class="detail-field-label">Примечание</span>
          <div class="detail-field-value disp-field" style="white-space:pre-wrap; line-height:1.4;">${escHtml(asset.note || '—')}</div>
        </div>
                <div style="display:flex; flex-direction:column; align-items:center; margin-top:24px; grid-column: 1 / -1; gap:6px;">
            <div id="detail-qr-container" style="cursor:pointer; padding: 12px; border-radius: 8px; border: 1px dashed var(--border-default); display: flex; align-items: center; justify-content: center; width: 120px; height: 120px; background: white; transition:box-shadow 0.2s;" title="Нажмите для просмотра и печати" onmouseover="this.style.boxShadow='0 0 0 3px var(--accent)'" onmouseout="this.style.boxShadow='none'" onclick="openQrPrintModal('${asset.assetTag}', 'ITAM-ASSET:${asset.id}', '${asset.assetType}', '${escHtml(asset.displayName || asset.assetTag)}', '${user ? escHtml(user.name) : ''}', '${loc ? escHtml(loc.name) : ''}')"></div>
          </div>
</div>

      <!-- TAB: RESPONSIBILITY -->
      <div class="detail-tab-panel" id="tab-responsibility">
        <div class="detail-field">
          <span class="detail-field-label">Назначен пользователю</span>
          <span class="detail-field-value disp-field">${user ? escHtml(user.name) : '<span class="muted">Не назначен</span>'}</span>
        </div>
        <div class="detail-field disp-field"><span class="detail-field-label">Должность</span><span class="detail-field-value">${user ? (user.position ? escHtml(user.position) : (user.role === 'ADMIN' ? 'Администратор' : 'Сотрудник')) : '—'}</span></div>
        <div class="detail-field disp-field"><span class="detail-field-label">Отдел</span><span class="detail-field-value">${dept ? escHtml(dept.name) : '—'}</span></div>
        <div class="detail-field">
          <span class="detail-field-label">Локация</span>
          <span class="detail-field-value disp-field">${loc ? escHtml(loc.name) : '—'}</span>
        </div>
        <div class="detail-field disp-field"><span class="detail-field-label">Филиал</span><span class="detail-field-value">${loc ? escHtml(getBranchName(loc.branchId)) : '—'}</span></div>
        ${asset.note && asset.note.includes('hostname') ? `<div class="detail-field"><span class="detail-field-label">Технические сведения</span><span class="detail-field-value mono" style="font-size:0.75rem;word-break:break-all">${escHtml(asset.note)}</span></div>` : ''}
      </div>

      <!-- TAB: LIFECYCLE -->
      <div class="detail-tab-panel" id="tab-lifecycle">
        <div class="detail-field"><span class="detail-field-label">Текущий статус</span>${statusBadge(isArchived ? 'ARCHIVED' : asset.status)}</div>
        <div class="detail-field"><span class="detail-field-label">Тип актива</span><span class="detail-field-value">${typeLabel(asset.assetType)}</span></div>

          <div class="lifecycle-section">
          <div class="lifecycle-title">Операции жизненного цикла</div>
          <div class="lifecycle-actions">
            <button class="lifecycle-btn" onclick="openLabelPrintModal('${asset.id}')">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/></svg>
              Печать этикетки
            </button>
            ${!isArchived ? (asset.status === 'RETIRED' ? `
            <button class="lifecycle-btn lifecycle-btn-archive" onclick="archiveAsset('${asset.id}')">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><line x1="10" y1="12" x2="14" y2="12"/></svg>
              Архивировать списанный актив
            </button>` : `
            <button class="lifecycle-btn lifecycle-btn-retire" onclick="retireAsset('${asset.id}')">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg>
              Списать актив (RETIRED)
            </button>`) : `
            <button class="lifecycle-btn lifecycle-btn-unarchive" onclick="unarchiveAsset('${asset.id}')">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><polyline points="10 12 12 14 14 12"/><line x1="12" y1="9" x2="12" y2="14"/></svg>
              Разархивировать
            </button>`}
          </div>
        </div>

        <div style="margin-top:20px;padding:12px;background:var(--bg-card);border-radius:var(--radius-sm);border:1px solid var(--border-subtle);">
          <div style="font-size:0.6875rem;color:var(--text-disabled);text-transform:uppercase;letter-spacing:0.06em;margin-bottom:4px">Sandbox — Примечание</div>
          <p style="font-size:0.75rem;color:var(--text-muted);line-height:1.6">Списание помечает актив статусом RETIRED и оставляет его в рабочем реестре. Архив скрывает уже списанный актив из рабочих списков. Разархивирование возвращает списанный актив в реестр без восстановления ACTIVE.</p>
        </div>
      </div>
      <!-- TAB: REQUESTS -->
      <div class="detail-tab-panel" id="tab-requests"></div>

      <!-- TAB: PERIPHERALS -->
      <div class="detail-tab-panel" id="tab-peripherals">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
          <h4 style="margin:0;font-size:0.9rem;color:var(--text-primary);">Периферия и дочерние устройства</h4>
          <div style="display:flex;gap:6px;">
            <button class="btn-secondary" style="padding:4px 10px;font-size:0.8rem;" onclick="openAttachPeripheralModal('${asset.id}')">Привязать</button>
            <button class="btn-primary" style="padding:4px 10px;font-size:0.8rem;" onclick="openCreatePeripheralModal('${asset.id}')">+ Создать</button>
          </div>
        </div>
        <div id="peripheral-list-${asset.id}" style="display:flex;flex-direction:column;gap:6px;">
          ${(() => {
            const children = state.assets.filter(a => a.parentAssetId === asset.id);
            if (children.length === 0) return '<span style="color:var(--text-disabled);font-size:0.875rem;">Нет привязанных устройств</span>';
            return children.map(c => {
              const cIcon = typeIconClass(c.assetType);
              return '<div style="display:flex;align-items:center;gap:10px;padding:8px 10px;background:var(--bg-input);border-radius:var(--radius-sm);border:1px solid var(--border-subtle);">' +
                '<div class="asset-card-icon ' + cIcon + '" style="width:30px;height:30px;flex-shrink:0;">' + typeIcon(c.assetType) + '</div>' +
                '<div style="flex:1;">' +
                  '<div style="font-size:0.875rem;font-weight:600;color:var(--text-primary);">' + escHtml(c.displayName || c.assetTag) + '</div>' +
                  '<div style="font-size:0.75rem;color:var(--text-muted);">' + typeLabel(c.assetType) + ' · ' + escHtml(c.assetTag) + '</div>' +
                '</div>' +
                statusBadge(c.status) +
                '<button class="btn-icon" onclick="event.stopPropagation(); detachPeripheral(\'' + c.id + '\', \'' + asset.id + '\')" title="Отвязать" style="color:var(--text-muted);">' +
                  '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>' +
                '</button>' +
              '</div>';
            }).join('');
          })()}
        </div>
      </div>
    </div>`;

  // Populate requests tab
  const _reqPanel = pane.querySelector('#tab-requests');
  if (_reqPanel && typeof _renderDetailRequests === 'function') {
    _reqPanel.innerHTML = _renderDetailRequests(assetId);
  }

  // QR Code — deterministic via api.qrserver.com (same image every time)
  // Use requestAnimationFrame to ensure DOM is ready, then insert QR
  requestAnimationFrame(() => {
    const qrBox = document.getElementById('detail-qr-container');
    if (qrBox) {
      const qrText = 'ITAM-ASSET:' + asset.id; // один неизменяемый код на актив
      const imgUrl = `https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=${encodeURIComponent(qrText)}&margin=4&color=0f172a&bgcolor=ffffff`;
      const img = document.createElement('img');
      img.src = imgUrl;
      img.style.cssText = 'width:100px;height:100px;display:block;';
      img.alt = `QR ${qrText}`;
      img.onerror = function() {
        qrBox.innerHTML = `<div style="width:96px;height:96px;display:flex;align-items:center;justify-content:center;border:2px dashed #ccc;font-size:0.6rem;text-align:center;color:#666;padding:4px;box-sizing:border-box;">${qrText}</div>`;
      };
      qrBox.innerHTML = '';
      qrBox.appendChild(img);
    }
  });

  // Mobile layout adjust
  if (window.innerWidth <= 768) {
    const listPane = document.getElementById('assets-list-pane');
    if (listPane) listPane.style.display = 'none';
  }
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

async function saveNote(assetId) {
  const textarea = document.getElementById('detail-note-area');
  if (!textarea) return;
  const asset = getAssetById(assetId);
  if (!asset) return;
  const newNote = textarea.value;
  try {
    const res = await fetch(`${API_BASE}/api/v1/assets/${assetId}`, {
      method: 'PATCH',
      headers: getHeaders(),
      body: JSON.stringify({ note: newNote })
    });
    if (res.ok) {
      asset.note = newNote;
      showToast('Примечание сохранено', 'success');
    } else {
      // Fallback: save in-memory if API fails
      asset.note = newNote;
      showToast('Примечание сохранено (локально)', 'info');
    }
  } catch (e) {
    asset.note = newNote;
    showToast('Примечание сохранено (оффлайн)', 'info');
  }
}

// ══════════════════════════════════════════════════════════
// LIFECYCLE OPERATIONS
// ══════════════════════════════════════════════════════════

async function archiveAsset(assetId) {
  const idx = state.assets.findIndex(a => a.id === assetId);
  if (idx === -1) return;
  if (state.assets[idx].status !== 'RETIRED') {
    showToast('Архивировать можно только списанный актив', 'error');
    return;
  }
  try {
    const res = await fetch(`${API_BASE}/api/v1/assets/${assetId}/archive`, {
      method: 'POST',
      headers: getHeaders()
    });
    if (!res.ok) throw new Error(`archiveAsset API error ${res.status}`);
  } catch (e) {
    console.warn('archiveAsset error', e);
    showToast('Ошибка архивирования актива', 'error');
    return;
  }
  const [asset] = state.assets.splice(idx, 1);
  asset.archivedAt = new Date().toISOString();
  state.archivedAssets.push(asset);
  closeDetailPane();
  renderAssetsList();
  populateSidebarTrees();
  showToast(`Актив "${asset.displayName || asset.assetTag}" архивирован`, 'info');
}

async function unarchiveAsset(assetId) {
  const idx = state.archivedAssets.findIndex(a => a.id === assetId);
  if (idx === -1) return;
  try {
    const res = await fetch(`${API_BASE}/api/v1/assets/${assetId}/unarchive`, {
      method: 'POST',
      headers: getHeaders()
    });
    if (!res.ok) throw new Error(`unarchiveAsset API error ${res.status}`);
  } catch (e) {
    console.warn('unarchiveAsset error', e);
    showToast('Ошибка разархивирования актива', 'error');
    return;
  }
  const [asset] = state.archivedAssets.splice(idx, 1);
  asset.archivedAt = null;
  state.assets.push(asset);
  closeDetailPane();
  renderArchiveList();
  populateSidebarTrees();
  showToast(`Актив "${asset.displayName || asset.assetTag}" возвращён в реестр`, 'success');
}

async function retireAsset(assetId) {
  const idx = state.assets.findIndex(a => a.id === assetId);
  if (idx === -1) return;
  if (state.assets[idx].status === 'RETIRED') {
    showToast('Актив уже списан', 'info');
    return;
  }
  try {
    const res = await fetch(`${API_BASE}/api/v1/assets/${assetId}/retire`, {
      method: 'POST',
      headers: getHeaders()
    });
    if (!res.ok) throw new Error(`retireAsset API error ${res.status}`);
  } catch (e) {
    console.warn('retireAsset error', e);
    showToast('Ошибка списания актива', 'error');
    return;
  }
  const asset = state.assets[idx];
  asset.status = 'RETIRED';
  asset.assignedUserId = null;
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
      if (req.status === 'NEEDS_WORK') return 'NEEDS_WORK';
      if (req.status === 'RESOLVED') return 'RESOLVED';
      return 'PENDING';
    })();

    const chatHistory = getChatHistory(req.resolutionComment);
    let commentHtml = renderChatHistoryHtml(chatHistory, false);

    // Блок действий / бейдж статуса
    let statusHtml;
    if (normalizedStatus === 'APPROVED') {
      statusHtml = `
      <div style="margin-top: 12px; display: flex; gap: 8px;">
        <input type="text" id="admin-chat-input-${req.id}" placeholder="Написать сообщение сотруднику..." style="flex:1; border-radius:6px; padding:8px; border: 1px solid var(--border-subtle); font-size: 13px;">
        <button class="btn-secondary" style="padding: 4px 12px; white-space: nowrap;" onclick="sendChatMessage('${req.id}', 'admin', 'admin-chat-input-${req.id}')">Отправить</button>
      </div>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 12px;">
        <div class="request-card-status request-status-approved">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
          В работе
        </div>
        <button class="btn-primary" style="padding: 4px 12px; font-size: 13px; background: #059669; color: #ffffff; border-color: #059669;" onclick="handleRequest(${idx}, 'resolve')">&#10003; Выполнено</button>
      </div>`;
    } else if (normalizedStatus === 'REJECTED') {
      statusHtml = `<div class="request-card-status request-status-rejected" style="margin-top: 8px;">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        Отклонена
      </div>`;
    } else if (normalizedStatus === 'NEEDS_WORK') {
      statusHtml = `<div class="request-card-status" style="margin-top: 8px; color: var(--warning); background: var(--warning-soft); padding: 4px 8px; border-radius: 4px; font-size: 0.75rem; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
        На доработке
      </div>`;
    } else if (normalizedStatus === 'RESOLVED') {
      statusHtml = `<div class="request-card-status" style="margin-top: 8px; color: #059669; background: #ecfdf5; padding: 4px 8px; border-radius: 4px; font-size: 0.75rem; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
        Завершено
      </div>`;
    } else {
      // PENDING — показываем кнопки действий и поле для комментария
      statusHtml = `
      <div class="request-comment-box" style="margin-top: 12px;">
        <textarea id="req-comment-input-${idx}" placeholder="Ваш комментарий (опционально)..." style="width:100%; height: 50px; resize:none; border-radius:6px; padding:8px; border: 1px solid var(--border-subtle); margin-bottom: 8px; font-family: inherit; font-size: 13px; box-sizing: border-box; background: var(--bg-primary); color: var(--text-primary);"></textarea>
      </div>
      <div class="request-card-actions" style="flex-wrap: wrap; gap: 8px;">
        <button class="req-btn-accept" onclick="handleRequest(${idx}, 'accept')">&#10003; Принять в работу</button>
        <button class="req-btn-reject" onclick="handleRequest(${idx}, 'reject')">&#10005; Отклонить</button>
        <button class="req-btn-reject" style="background: var(--warning-soft); color: var(--warning); border: 1px solid var(--warning-soft);" onclick="handleRequest(${idx}, 'revision')">&#9888; На доработку</button>
      </div>`;
    }

    // Модификатор класса карточки под статус
    const cardMod = normalizedStatus === 'APPROVED' ? 'req-approved'
      : normalizedStatus === 'REJECTED' ? 'req-rejected'
      : normalizedStatus === 'NEEDS_WORK' ? 'req-needs-work'
      : normalizedStatus === 'RESOLVED' ? 'req-resolved'
      : 'req-pending';

    // Бейдж доработки
    let resubmitBadge = '';
    if (normalizedStatus === 'PENDING' && chatHistory.length > 0 && chatHistory[chatHistory.length - 1].role === 'employee') {
      resubmitBadge = `<span style="background: var(--accent-color); color: white; padding: 2px 6px; border-radius: 4px; font-size: 10px; margin-left: 8px; vertical-align: middle;">ДОРАБОТАНА</span>`;
    }

    return `<div class="request-card ${cardMod}">
      <div class="request-card-header">
        <div class="request-card-avatar">${escHtml(initials)}</div>
        <div class="request-card-meta">
          <div class="request-card-user">${escHtml(userName)}${resubmitBadge}</div>
          <div class="request-card-date">${formatDate(req.date)}</div>
        </div>
        <span class="request-card-type-badge">${escHtml(req.type)}</span>
      </div>
      <div class="request-card-text" style="white-space: pre-wrap;">${escHtml(req.description)}</div>
      ${commentHtml}
      ${statusHtml}
    </div>`;
  }).join('');
}

async function handleRequest(idx, action) {
  const req = state.requests[idx];
  if (!req) return;

  let resolution;
  if (action === 'accept') resolution = 'APPROVED';
  else if (action === 'reject') resolution = 'REJECTED';
  else if (action === 'revision') resolution = 'NEEDS_WORK';
  else if (action === 'resolve') resolution = 'RESOLVED';

  const commentInput = document.getElementById(`req-comment-input-${idx}`);
  const comment = commentInput ? commentInput.value.trim() : '';

  let newResolutionComment = req.resolutionComment || '';
  const history = getChatHistory(req.resolutionComment);
  const adminText = comment || (action === 'accept' ? 'Заявка принята в работу' : action === 'reject' ? 'Заявка отклонена' : action === 'revision' ? 'Заявка отправлена на доработку' : 'Статус изменен');
  history.push({ role: 'admin', text: adminText, date: new Date().toISOString() });
  newResolutionComment = JSON.stringify(history);

  try {
      const res = await fetch(`${API_BASE}/api/v1/requests/${req.id}/resolve`, {
          method: 'PATCH',
          headers: getHeaders(),
          body: JSON.stringify(toSnake({ status: resolution, resolutionComment: newResolutionComment }))
      });
      if (!res.ok) throw new Error('Failed to resolve');
      req.status = resolution;
      req.resolutionComment = newResolutionComment;
      renderAdminRequests();
      updateRequestsBadge();

      let toastMsg = 'Заявка обработана';
      if (action === 'accept') toastMsg = 'Заявка принята в работу';
      else if (action === 'reject') toastMsg = 'Заявка отклонена';
      else if (action === 'revision') toastMsg = 'Заявка отправлена на доработку';
      else if (action === 'resolve') toastMsg = 'Заявка выполнена';

      showToast(toastMsg, action === 'accept' || action === 'resolve' ? 'success' : (action === 'revision' ? 'warning' : 'error'));
  } catch (e) {
      showToast('Ошибка обработки заявки', 'error');
  }
}

// ══════════════════════════════════════════════════════════
// EMPLOYEE VIEW
// ══════════════════════════════════════════════════════════

function renderEmployeeView() {
  const user = state.currentUser;
  const dept = getDeptById(user.departmentId);
  const loc  = getLocById(user.locationId);

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
      <div class="employee-role-label">${user.position ? escHtml(user.position) : escHtml(user.role === 'ADMIN' ? 'Администратор' : 'Сотрудник')}</div>
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
            <div class="employee-asset-item-info" id="dashboard-asset-info-${a.id}">
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

  // Render Employee Requests
  updateEmployeeRequestsContainer(user.id);
}

function updateEmployeeRequestsContainer(userId) {
  const reqContainer = document.getElementById('employee-requests-container');
  if (reqContainer) {
    let activeReqs = state.requests.filter(r => r.userId === userId && r.status !== 'RESOLVED' && r.status !== 'CLOSED');
    if (state.employeeRequestsFilter !== 'ALL') {
      activeReqs = activeReqs.filter(r => {
        if (state.employeeRequestsFilter === 'APPROVED') return r.status === 'APPROVED' || r.status === 'ACCEPTED';
        if (state.employeeRequestsFilter === 'NEEDS_WORK') return r.status === 'NEEDS_WORK';
        if (state.employeeRequestsFilter === 'REJECTED') return r.status === 'REJECTED';
        return true;
      });
    }

    if (activeReqs.length === 0) {
      reqContainer.innerHTML = `
        <div class="empty-state" style="padding: 40px 20px; text-align: center; background: var(--bg-secondary); border-radius: 12px; border: 1px dashed var(--border-subtle);">
          <div style="color: var(--text-tertiary); margin-bottom: 12px;">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              <line x1="9" y1="10" x2="15" y2="10"/>
              <line x1="9" y1="14" x2="11" y2="14"/>
            </svg>
          </div>
          <p style="color: var(--text-secondary); font-size: 15px; margin: 0;">У вас пока нет активных заявок</p>
        </div>
      `;
    } else {
      reqContainer.innerHTML = activeReqs.map(r => {
        let statusDisplay = statusBadge(r.status);
        if (r.status === 'NEEDS_WORK') {
            statusDisplay = `<span style="background:var(--warning-soft);color:var(--warning);padding:2px 8px;border-radius:12px;font-size:12px;font-weight:600;">На доработке</span>`;
        } else if (r.status === 'REJECTED') {
            statusDisplay = `<span style="background:var(--error-soft);color:var(--error);padding:2px 8px;border-radius:12px;font-size:12px;font-weight:600;">Отклонена</span>`;
        } else if (r.status === 'APPROVED') {
            statusDisplay = `<span style="background:var(--success-soft);color:var(--success);padding:2px 8px;border-radius:12px;font-size:12px;font-weight:600;">В работе</span>`;
        }

        const chatHistory = getChatHistory(r.resolutionComment);
        let commentHtml = renderChatHistoryHtml(chatHistory, true);

        let actionHtml = '';
        if (r.status === 'NEEDS_WORK') {
            actionHtml = `
            <div style="margin-top: 12px;">
              <textarea id="employee-comment-input-${r.id}" placeholder="Напишите ваш ответ администратору..." style="width:100%; height: 50px; resize:none; border-radius:6px; padding:8px; border: 1px solid var(--border-subtle); margin-bottom: 8px; font-family: inherit; font-size: 13px; box-sizing: border-box; background: var(--bg-primary); color: var(--text-primary);"></textarea>
              <button class="btn-primary" style="padding: 4px 12px; font-size: 13px;" onclick="resubmitRequest('${r.id}')">Отправить повторно</button>
            </div>`;
        } else if (r.status === 'APPROVED') {
            actionHtml = `
            <div style="margin-top: 12px; display: flex; gap: 8px;">
              <input type="text" id="employee-chat-input-${r.id}" placeholder="Написать сообщение администратору..." style="flex:1; border-radius:6px; padding:8px; border: 1px solid var(--border-subtle); font-size: 13px;">
              <button class="btn-secondary" style="padding: 4px 12px; white-space: nowrap;" onclick="sendChatMessage('${r.id}', 'employee', 'employee-chat-input-${r.id}')">Отправить</button>
            </div>`;
        }

        return `<div class="request-card" style="margin-bottom: 12px;">
          <div class="request-header" style="display: flex; justify-content: space-between; margin-bottom: 8px;">
            <span class="request-type" style="font-weight: 500;">${escHtml(r.type)}</span>
            ${statusDisplay}
          </div>
          <div class="request-desc" style="font-size: 14px; color: var(--text-secondary); white-space: pre-wrap;">${escHtml(r.description)}</div>
          ${commentHtml}
          ${actionHtml}
          <div class="request-date" style="font-size: 12px; color: var(--text-tertiary); margin-top: 8px;">${new Date(r.createdAt || r.date).toLocaleString()}</div>
        </div>`;
      }).join('');
    }
  }
}

async function resubmitRequest(reqId) {
    const req = state.requests.find(r => r.id === reqId);
    if (!req) return;

    const commentInput = document.getElementById(`employee-comment-input-${reqId}`);
    const empComment = commentInput ? commentInput.value.trim() : '';

    let newResolutionComment = req.resolutionComment || '';
    const history = getChatHistory(req.resolutionComment);
    const empText = empComment || '(Заявка отправлена на повторную проверку)';
    history.push({ role: 'employee', text: empText, date: new Date().toISOString() });
    newResolutionComment = JSON.stringify(history);

    try {
        const res = await fetch(`${API_BASE}/api/v1/requests/${reqId}/resolve`, {
            method: 'PATCH',
            headers: getHeaders(),
            body: JSON.stringify(toSnake({ status: 'NEW', resolutionComment: newResolutionComment, employeeComment: '' }))
        });
        if (!res.ok) throw new Error('Failed to resubmit');

        req.status = 'NEW';
        req.resolutionComment = newResolutionComment;

        renderEmployeeView();
        showToast('Заявка отправлена повторно', 'success');
    } catch (e) {
        showToast('Ошибка при отправке', 'error');
    }
}

async function sendChatMessage(reqId, role, inputId) {
    const req = state.requests.find(r => r.id === reqId);
    if (!req) return;

    const commentInput = document.getElementById(inputId);
    const comment = commentInput ? commentInput.value.trim() : '';
    if (!comment) return;

    const history = getChatHistory(req.resolutionComment);
    history.push({ role: role, text: comment, date: new Date().toISOString() });
    const newResolutionComment = JSON.stringify(history);

    try {
        const res = await fetch(`${API_BASE}/api/v1/requests/${reqId}/resolve`, {
            method: 'PATCH',
            headers: getHeaders(),
            body: JSON.stringify(toSnake({ status: req.status, resolutionComment: newResolutionComment, employeeComment: '' }))
        });
        if (!res.ok) throw new Error('Failed to send message');

        req.resolutionComment = newResolutionComment;
        if (commentInput) commentInput.value = '';

        if (role === 'admin') renderAdminRequests();
        else renderEmployeeView();
    } catch (e) {
        showToast('Ошибка при отправке сообщения', 'error');
    }
}

// ══════════════════════════════════════════════════════════
// ADD ASSET MODAL
// ══════════════════════════════════════════════════════════

function openAddAssetModal() {
  // Populate selects
  const locSel = document.getElementById('new-location');
  locSel.innerHTML = '<option value="">Не указана</option>' +
    state.locations.map(l => `<option value="${l.id}">${escHtml(l.name)}</option>`).join('');

  const userSel = document.getElementById('new-assigned-user');
  userSel.innerHTML = '<option value="">Не назначен</option>' +
    state.users.map(u => `<option value="${u.id}">${escHtml(u.name)}</option>`).join('');

  const parentSel = document.getElementById('new-parent-asset');
  if (parentSel) {
    parentSel.innerHTML = '<option value="">Не указан (самостоятельный)</option>' +
      state.assets.map(a => `<option value="${a.id}">${escHtml(a.displayName || a.assetTag)} (${typeLabel(a.assetType)})</option>`).join('');
  }

  clearFieldErrors(
    ['new-asset-tag', 'new-asset-tag-error'],
    ['new-type', 'new-type-error']
  );
  document.getElementById('add-asset-form').reset();
  document.getElementById('add-asset-modal').classList.remove('hidden');
}

function closeAddAssetModal() {
  document.getElementById('add-asset-modal').classList.add('hidden');
  document.getElementById('add-asset-form').reset();
}

function openEditAssetModal(assetId) {
  const asset = getAssetById(assetId);
  if (!asset) return;
  document.getElementById('edit-asset-id').value = asset.id;
  document.getElementById('edit-asset-tag').value = asset.assetTag || '';
  document.getElementById('edit-serial').value = asset.serialNumber || '';
  document.getElementById('edit-type').value = asset.assetType || 'OTHER';
  document.getElementById('edit-display-name').value = asset.displayName || '';
  document.getElementById('edit-note').value = asset.note || '';

  const userSel = document.getElementById('edit-assigned-user');
  userSel.innerHTML = '<option value="">Не назначен</option>' +
    state.users.map(u => `<option value="${u.id}" ${asset.assignedUserId === u.id ? 'selected' : ''}>${escHtml(u.name)}</option>`).join('');

  const locSel = document.getElementById('edit-location');
  locSel.innerHTML = '<option value="">Не указана</option>' +
    state.locations.map(l => `<option value="${l.id}" ${asset.locationId === l.id ? 'selected' : ''}>${escHtml(l.name)}</option>`).join('');

  const parentSel = document.getElementById('edit-parent-asset');
  if (parentSel) {
    parentSel.innerHTML = '<option value="">Не подключен</option>' +
      state.assets.filter(a => a.id !== asset.id).map(a => `<option value="${a.id}" ${asset.parentAssetId === a.id ? 'selected' : ''}>${escHtml(a.displayName || a.assetTag)}</option>`).join('');
  }

  const ipInput = document.getElementById('edit-ip-address');
  if (ipInput) ipInput.value = asset.ipAddress || '';

  document.getElementById('edit-asset-modal').classList.remove('hidden');
}

function closeEditAssetModal() {
  document.getElementById('edit-asset-modal').classList.add('hidden');
  document.getElementById('edit-asset-form').reset();
}

async function confirmEditAsset() {
  const assetId = document.getElementById('edit-asset-id').value;
  const asset = getAssetById(assetId);
  if (!asset) return;

  const newAssignedUserId = document.getElementById('edit-assigned-user').value || null;
  const newLocationId = document.getElementById('edit-location').value || null;

  const parentSel = document.getElementById('edit-parent-asset');
  const ipInput = document.getElementById('edit-ip-address');

  const payload = {
    assetTag: document.getElementById('edit-asset-tag').value.trim() || asset.assetTag,
    serialNumber: document.getElementById('edit-serial').value.trim() || asset.serialNumber,
    assetType: document.getElementById('edit-type').value || asset.assetType,
    displayName: document.getElementById('edit-display-name').value.trim() || null,
    note: document.getElementById('edit-note').value.trim() || null,
    parentAssetId: parentSel ? (parentSel.value || null) : null,
    ipAddress: ipInput ? (ipInput.value.trim() || null) : null
  };

  try {
    const res = await fetch(`${API_BASE}/api/v1/assets/${assetId}`, {
      method: 'PATCH',
      headers: getHeaders(),
      body: JSON.stringify(toSnake(payload))
    });
    if (!res.ok) throw new Error('Failed to update asset base fields');

    // Handle assignment or location change
    if (newAssignedUserId !== asset.assignedUserId || newLocationId !== asset.locationId) {
      if (!newAssignedUserId && !newLocationId) {
        // Return asset
        const retRes = await fetch(`${API_BASE}/api/v1/assets/${assetId}/return`, {
          method: 'POST',
          headers: getHeaders()
        });
        if (!retRes.ok) throw new Error('Failed to return asset');
      } else {
        // Transfer asset
        const transRes = await fetch(`${API_BASE}/api/v1/assets/${assetId}/transfer`, {
          method: 'POST',
          headers: getHeaders(),
          body: JSON.stringify(toSnake({ targetUserId: newAssignedUserId, targetLocationId: newLocationId }))
        });
        if (!transRes.ok) throw new Error('Failed to transfer asset');
      }
    }

    // Refresh assets from backend to get full updated values
    const refRes = await fetch(`${API_BASE}/api/v1/assets`, { headers: getHeaders() });
    if (refRes.ok) {
      const refreshed = toCamel(await refRes.json());
      state.assets = Array.isArray(refreshed) ? refreshed : (refreshed.results || []);
    }

    showToast('Актив успешно сохранён', 'success');
    closeEditAssetModal();
    renderAssetsList();
    if (state.selectedAssetId === assetId) renderDetailPane(assetId);
  } catch (e) {
    console.error(e);
    showToast('Ошибка сохранения актива', 'error');
  }
}

async function confirmAddAsset() {
  const tag     = document.getElementById('new-asset-tag').value.trim();
  const serial  = document.getElementById('new-serial').value.trim();
  const type    = document.getElementById('new-type').value;
  const locId   = document.getElementById('new-location').value;
  const name    = document.getElementById('new-display-name').value.trim();
  const note    = document.getElementById('new-note').value.trim();
  const userId  = document.getElementById('new-assigned-user').value;
  const parentAssetEl = document.getElementById('new-parent-asset');
  const ipAddressEl = document.getElementById('new-ip-address');
  const parentAssetId = parentAssetEl ? (parentAssetEl.value || null) : null;
  const ipAddress = ipAddressEl ? (ipAddressEl.value.trim() || null) : null;

  let hasErrors = false;
  if (!tag) { setFieldError('new-asset-tag', 'new-asset-tag-error', true); hasErrors = true; }
  else { setFieldError('new-asset-tag', 'new-asset-tag-error', false); }
  if (!type) { setFieldError('new-type', 'new-type-error', true); hasErrors = true; }
  else { setFieldError('new-type', 'new-type-error', false); }
  if (hasErrors) { showToast('Заполните обязательные поля', 'error'); return; }

  // Check uniqueness
  if (state.assets.some(a => a.assetTag === tag) || state.archivedAssets.some(a => a.assetTag === tag)) {
    showToast('Asset Tag уже существует', 'error');
    return;
  }

  const payload = {
    assetTag: tag,
    serialNumber: serial || '-',
    assetType: type,
    assignedUserId: userId || null,
    locationId: locId || null,
    displayName: name || null,
    note: note || null,
    parentAssetId: parentAssetId,
    ipAddress: ipAddress
  };

  try {
    const res = await fetch(`${API_BASE}/api/v1/assets`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(toSnake(payload))
    });

    if (!res.ok) throw new Error('Failed to create asset');

    const newAsset = toCamel(await res.json());
    state.assets.push(newAsset);
    closeAddAssetModal();
    renderAssetsList();
    populateSidebarTrees();
    showToast(`Актив "${newAsset.displayName || newAsset.assetTag}" добавлен`, 'success');
  } catch (e) {
    console.error(e);
    showToast('Ошибка при добавлении актива', 'error');
  }
}

// (DICTIONARY CRUD functions moved below — see new versions)
// ══════════════════════════════════════════════════════════
// EVENT LISTENERS SETUP
// ══════════════════════════════════════════════════════════

function setupEventListeners() {
  console.log('setupEventListeners called');

  // Employee requests filters
  document.addEventListener('click', e => {
    const btn = e.target.closest('.emp-req-filter-btn');
    if (btn) {
      document.querySelectorAll('.emp-req-filter-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.employeeRequestsFilter = btn.dataset.filter;
      updateEmployeeRequestsContainer(state.currentUser.id);
    }
  });

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
      login('admin');
    }, 800);
  });

  // ── QUICK LOGIN BUTTONS
  document.getElementById('quick-admin').addEventListener('click', () => {
    animateQuickBtn('quick-admin', () => login('a1111111-1111-1111-1111-111111111111'));
  });
  document.getElementById('quick-employee').addEventListener('click', () => {
    animateQuickBtn('quick-employee', () => login('e2222222-2222-2222-2222-222222222222'));
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
  document.getElementById('nav-dashboard')?.addEventListener('click', () => navigateTo('dashboard'));
  document.getElementById('nav-all-assets').addEventListener('click', () => navigateTo('all-assets'));
  document.getElementById('nav-archive').addEventListener('click', () => navigateTo('archive'));
  document.getElementById('nav-requests').addEventListener('click', () => navigateTo('requests'));
  document.getElementById('nav-settings').addEventListener('click', () => navigateTo('settings'));
  document.getElementById('nav-sim-cards')?.addEventListener('click', () => navigateTo('sim-cards'));
  document.getElementById('nav-cartridges')?.addEventListener('click', () => navigateTo('cartridges'));
  document.getElementById('nav-peripherals')?.addEventListener('click', () => navigateTo('peripherals'));

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

    setTimeout(async () => {
      try {
        const payload = {
          userName: state.currentUser.name,
          type: typeEl.value,
          description: descEl.value.trim()
        };
        const res = await fetch(`${API_BASE}/api/v1/requests`, {
          method: 'POST',
          headers: getHeaders(),
          body: JSON.stringify(toSnake(payload))
        });

        if (!res.ok) {
          const errText = await res.text();
          console.error("Failed to create request. Server responded with:", res.status, errText);
          throw new Error('Failed to create request');
        }

        const newReq = await res.json();
        state.requests.push(toCamel(newReq));

        btnText.classList.remove('hidden');
        spinner.classList.add('hidden');
        btn.disabled = false;

        document.getElementById('employee-request-form').classList.add('hidden');
        showToast('Заявка успешно отправлена', 'success');
        document.getElementById('req-success').classList.remove('hidden');

        updateEmployeeRequestsContainer(state.currentUser.id);
      } catch(e) {
        console.error("Support request error:", e);
        btnText.classList.remove('hidden');
        spinner.classList.add('hidden');
        btn.disabled = false;
        errEl.textContent = 'Ошибка отправки заявки.';
        errEl.classList.remove('hidden');
      }
    }, 400);
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
  const originalHtml = btn.innerHTML;
  btn.style.transform = 'scale(0.97)';
  btn.style.opacity = '0.8';
  setTimeout(() => {
    btn.style.transform = '';
    btn.style.opacity = '0.8';
    btn.style.pointerEvents = 'none';

    // Show spinner
    btn.innerHTML = `
      <div style="display:flex;align-items:center;justify-content:center;width:100%;height:100%;">
        <div class="btn-spinner" style="display:block;border-color:var(--text-color);border-bottom-color:transparent;"></div>
        <span style="margin-left:8px;">Загрузка...</span>
      </div>
    `;

    // Call the original callback, which is an async function
    const res = callback();
    if (res && res.finally) {
      res.finally(() => {
        btn.innerHTML = originalHtml;
        btn.style.opacity = '';
        btn.style.pointerEvents = '';
      });
    } else {
      btn.innerHTML = originalHtml;
      btn.style.opacity = '';
      btn.style.pointerEvents = '';
    }
  }, 200);
}

// ══════════════════════════════════════════════════════════
// BOOTSTRAP
// ══════════════════════════════════════════════════════════

function initApp() {
  setupEventListeners();
  renderLogin();

  // Keyboard shortcut: Escape closes detail/modal
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') {
      const openModals = document.querySelectorAll('.modal-overlay:not(.hidden)');
      if (openModals.length > 0) {
        openModals.forEach(modal => modal.classList.add('hidden'));
        return;
      }
      if (state.selectedAssetId) {
        closeDetailPane();
      }
    }
  });
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initApp);
} else {
  initApp();
}

// =====================================
// MISSING MODALS & SECTIONS IMPLEMENTATION
// =====================================

// Locations
function openLocModal() {
  document.getElementById('add-loc-modal').classList.remove('hidden');
  const branchSelect = document.getElementById('new-loc-branch');
  branchSelect.innerHTML = (state.branches || []).map(b => `<option value="${b.id}">${b.name}</option>`).join('');
}
function openAddLocModal() { openLocModal(); } // alias
function closeLocModal() { document.getElementById('add-loc-modal').classList.add('hidden'); }
async function confirmAddLoc() {
  const name = document.getElementById('new-loc-name').value.trim();
  const branchId = document.getElementById('new-loc-branch').value;
  if (!name || !branchId) return showToast('Заполните обязательные поля', 'error');
  try {
    const res = await fetch(`${USER_SERVICE_BASE}/api/v1/locations`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(toSnake({ name, branchId }))
    });
    if (!res.ok) throw new Error();
    const loc = toCamel(await res.json());
    state.locations.push(loc);
    closeLocModal();
    populateSidebarTrees();
    showToast('Локация добавлена', 'success');
  } catch (e) {
    showToast('Ошибка при добавлении', 'error');
  }
}

// Depts
function openDeptModal() { document.getElementById('add-dept-modal').classList.remove('hidden'); }
function closeDeptModal() { document.getElementById('add-dept-modal').classList.add('hidden'); }
async function confirmAddDept() {
  const name = document.getElementById('new-dept-name').value.trim();
  if (!name) return showToast('Заполните обязательные поля', 'error');
  try {
    const res = await fetch(`${USER_SERVICE_BASE}/api/v1/departments`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(toSnake({ name }))
    });
    if (!res.ok) throw new Error();
    const dept = toCamel(await res.json());
    state.departments.push(dept);
    closeDeptModal();
    populateSidebarTrees();
    showToast('Отдел добавлен', 'success');
  } catch (e) {
    showToast('Ошибка при добавлении', 'error');
  }
}

// Users
function openUserModal() {
  document.getElementById('add-user-modal').classList.remove('hidden');
  const deptSelect = document.getElementById('new-user-dept');
  deptSelect.innerHTML = '<option value="">— Не выбрано —</option>' + (state.departments || []).map(d => `<option value="${d.id}">${d.name}</option>`).join('');
}
function openAddUserModal() { openUserModal(); } // alias
function closeUserModal() { document.getElementById('add-user-modal').classList.add('hidden'); }
async function confirmAddUser() {
  const name = document.getElementById('new-user-name').value.trim();
  const deptId = document.getElementById('new-user-dept').value || null;
  if (!name) return showToast('Заполните обязательные поля', 'error');
  try {
    const res = await fetch(`${USER_SERVICE_BASE}/api/v1/users`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(toSnake({ name, departmentId: deptId }))
    });
    if (!res.ok) throw new Error();
    const updated = toCamel(await res.json());

    // We expect {id, name, departmentId} from UserResponse
    // but the app state users have 'role', 'initials'
    const parts = updated.name.split(' ');
    updated.initials = parts.length > 1 ? (parts[0][0] + parts[1][0]).toUpperCase() : updated.name.substring(0, 2).toUpperCase();
    updated.role = 'EMPLOYEE'; // Add default role or keep if present
    state.users.push(updated);

    closeUserModal();
    if (state.activeSection === 'users') renderUsersSection();
    populateSidebarTrees();
    showToast('Сотрудник добавлен', 'success');
  } catch (e) {
    showToast('Ошибка при добавлении', 'error');
  }
}

function renderUsersSection() {
  document.getElementById('users-summary-line').textContent = `Всего сотрудников: ${state.users.length}`;
  const tbody = document.getElementById('users-tbody');
  tbody.innerHTML = state.users.map(u => {
    const dept = getDeptById(u.departmentId);
    return `<tr style="cursor:pointer;" onclick="openEditUserModal('${u.id}')">
      <td>
        <div style="font-weight: 500; color: var(--text-main);">${escHtml(u.name)}</div>
      </td>
      <td>${u.role === 'ADMIN' ? 'Администратор' : 'Сотрудник'}</td>
      <td>${dept ? escHtml(dept.name) : '—'}</td>
    </tr>`;
  }).join('');
}

// Edit Asset — duplicate removed; canonical implementation is above (openEditAssetModal / closeEditAssetModal)


// Navigation bindings for new users tab
document.getElementById('nav-users')?.addEventListener('click', () => {
  navigateTo('users');
  renderUsersSection();
});

const _origNavigateTo = navigateTo;
navigateTo = function(section, meta = {}) {
  const wasUsers = state.activeSection === 'users';
  if (section === 'users') {
    state.activeSection = section;
    ALL_SECTIONS.forEach(s => {
      const el = document.getElementById(`section-${s}`);
      if (el) el.classList.add('hidden');
    });
    document.getElementById('section-users').classList.remove('hidden');
    document.getElementById('topbar-module').textContent = 'Пользователи';
    document.querySelectorAll('.tree-item').forEach(el => el.classList.remove('tree-item-active', 'active'));
    document.getElementById('nav-users').classList.add('tree-item-active');
    renderUsersSection();
  } else {
    document.getElementById('section-users')?.classList.add('hidden');
    _origNavigateTo(section, meta);
  }
};

// Edit Loc
function openEditLocModal(id) {
  const loc = getLocById(id);
  if (!loc) return;
  document.getElementById('edit-loc-id').value = loc.id;
  document.getElementById('edit-loc-name').value = loc.name;
  const branchSelect = document.getElementById('edit-loc-branch');
  branchSelect.innerHTML = (state.branches || []).map(b => `<option value="${b.id}">${b.name}</option>`).join('');
  branchSelect.value = loc.branchId || '';
  document.getElementById('edit-loc-modal').classList.remove('hidden');
}
function closeEditLocModal() { document.getElementById('edit-loc-modal').classList.add('hidden'); }
async function confirmEditLoc() {
  const id = document.getElementById('edit-loc-id').value;
  const name = document.getElementById('edit-loc-name').value.trim();
  const branchId = document.getElementById('edit-loc-branch').value;
  if (!name || !branchId) return showToast('Заполните обязательные поля', 'error');
  try {
    const res = await fetch(`${USER_SERVICE_BASE}/api/v1/locations/${id}`, {
      method: 'PATCH',
      headers: getHeaders(),
      body: JSON.stringify(toSnake({ name, branchId }))
    });
    if (!res.ok) throw new Error();
    const updated = toCamel(await res.json());
    const idx = state.locations.findIndex(l => l.id === id);
    if (idx !== -1) state.locations[idx] = updated;
    closeEditLocModal();
    populateSidebarTrees();
    showToast('Локация сохранена', 'success');
  } catch (e) {
    showToast('Ошибка сохранения', 'error');
  }
}

// Edit Dept
function openEditDeptModal(id) {
  const dept = getDeptById(id);
  if (!dept) return;
  document.getElementById('edit-dept-id').value = dept.id;
  document.getElementById('edit-dept-name').value = dept.name;
  document.getElementById('edit-dept-modal').classList.remove('hidden');
}
function closeEditDeptModal() { document.getElementById('edit-dept-modal').classList.add('hidden'); }
async function confirmEditDept() {
  const id = document.getElementById('edit-dept-id').value;
  const name = document.getElementById('edit-dept-name').value.trim();
  if (!name) return showToast('Заполните обязательные поля', 'error');
  try {
    const res = await fetch(`${USER_SERVICE_BASE}/api/v1/departments/${id}`, {
      method: 'PATCH',
      headers: getHeaders(),
      body: JSON.stringify(toSnake({ name }))
    });
    if (!res.ok) throw new Error();
    const updated = toCamel(await res.json());
    const idx = state.departments.findIndex(d => d.id === id);
    if (idx !== -1) state.departments[idx] = updated;
    closeEditDeptModal();
    populateSidebarTrees();
    showToast('Отдел сохранён', 'success');
  } catch (e) {
    showToast('Ошибка сохранения', 'error');
  }
}

// Edit User
function openEditUserModal(id) {
  const user = getUserById(id);
  if (!user) return;
  document.getElementById('edit-user-id').value = user.id;
  document.getElementById('edit-user-name').value = user.name;
  const deptSelect = document.getElementById('edit-user-dept');
  deptSelect.innerHTML = '<option value="">-- Не выбрано --</option>' + (state.departments || []).map(d => `<option value="${d.id}">${d.name}</option>`).join('');
  deptSelect.value = user.departmentId || '';
  document.getElementById('edit-user-modal').classList.remove('hidden');
}
function closeEditUserModal() { document.getElementById('edit-user-modal').classList.add('hidden'); }
async function confirmEditUser() {
  const id = document.getElementById('edit-user-id').value;
  const name = document.getElementById('edit-user-name').value.trim();
  const departmentId = document.getElementById('edit-user-dept').value || null;
  if (!name) return showToast('Заполните обязательные поля', 'error');
  try {
    const res = await fetch(`${USER_SERVICE_BASE}/api/v1/users/${id}`, {
      method: 'PATCH',
      headers: getHeaders(),
      body: JSON.stringify(toSnake({ name, departmentId }))
    });
    if (!res.ok) throw new Error();
    const updated = toCamel(await res.json());
    const idx = state.users.findIndex(u => u.id === id);
    if (idx !== -1) {
      // Retain existing local properties like role/initials since backend doesn't return them
      updated.role = state.users[idx].role;
      const parts = updated.name.split(' ');
      updated.initials = parts.length > 1 ? (parts[0][0] + parts[1][0]).toUpperCase() : updated.name.substring(0, 2).toUpperCase();
      state.users[idx] = updated;
    }
    closeEditUserModal();
    if (state.activeSection === 'users') renderUsersSection();
    populateSidebarTrees();
    showToast('Сотрудник сохранён', 'success');
  } catch (e) {
    showToast('Ошибка сохранения', 'error');
  }
}

// ══════════════════════════════════════════════════════════
// PHASE 1: VIEW TOGGLE & BULK ACTIONS
// ══════════════════════════════════════════════════════════

// View Toggle
document.getElementById('view-card-btn')?.addEventListener('click', () => {
  state.viewMode = 'card';
  document.getElementById('view-card-btn').classList.add('active');
  document.getElementById('view-table-btn').classList.remove('active');
  renderAssetsList();
});
document.getElementById('view-table-btn')?.addEventListener('click', () => {
  state.viewMode = 'table';
  document.getElementById('view-table-btn').classList.add('active');
  document.getElementById('view-card-btn').classList.remove('active');
  renderAssetsList();
});

// Selection Logic
function toggleAssetSelection(id, isChecked) {
  if (isChecked) {
    state.selectedAssets.add(id);
  } else {
    state.selectedAssets.delete(id);
  }

  // Update parent select all if in table mode
  if (state.viewMode === 'table') {
    const list = getFilteredSortedAssets();
    const selectAllTable = document.getElementById('select-all-table');
    if (selectAllTable) {
      selectAllTable.checked = (list.length > 0 && state.selectedAssets.size === list.length);
    }
  } else {
    const list = getFilteredSortedAssets();
    const selectAllList = document.getElementById('select-all-assets');
    if (selectAllList) {
      selectAllList.checked = (list.length > 0 && state.selectedAssets.size === list.length);
    }
  }

  // Update rows/cards visually
  const el = document.getElementById(state.viewMode === 'table' ? `row-${id}` : `card-${id}`);
  if (el) {
    if (isChecked) el.classList.add('selected');
    else el.classList.remove('selected');
  }
  updateFloatingActionBar();
}

function toggleSelectAll(isChecked, allIds) {
  allIds.forEach(id => {
    if (isChecked) state.selectedAssets.add(id);
    else state.selectedAssets.delete(id);
  });
  renderAssetsList();
  updateFloatingActionBar();
}

function clearSelection() {
  state.selectedAssets.clear();
  renderAssetsList();
  updateFloatingActionBar();
}

// FAB Logic
function updateFloatingActionBar() {
  const count = state.selectedAssets.size;
  const fab = document.getElementById('fab');
  const countEl = document.getElementById('fab-count');
  if (count > 0) {
    countEl.textContent = count;
    fab.classList.add('visible');
  } else {
    fab.classList.remove('visible');
  }
}

// Bulk Location Change
function openBulkLocModal() {
  const select = document.getElementById('bulk-new-location');
  select.innerHTML = '<option value="">Не выбрана</option>' + state.locations.map(l => `<option value="${l.id}">${l.name}</option>`).join('');
  document.getElementById('bulk-location-modal').classList.remove('hidden');
}
function closeBulkLocModal() {
  document.getElementById('bulk-location-modal').classList.add('hidden');
}
async function confirmBulkLoc() {
  const newLocId = document.getElementById('bulk-new-location').value;
  if (!newLocId) return showToast('Выберите локацию', 'error');

  const assetIds = Array.from(state.selectedAssets);
  const promises = assetIds.map(id => fetch(`${ASSET_SERVICE_BASE}/api/v1/assets/${id}`, {
    method: 'PATCH',
    headers: getHeaders(),
    body: JSON.stringify(toSnake({ locationId: newLocId }))
  }));

  try {
    const results = await Promise.all(promises);
    for (let r of results) {
      if (!r.ok) throw new Error();
    }
    showToast(`Локация изменена для ${assetIds.length} активов`, 'success');
    closeBulkLocModal();
    clearSelection();
    await refreshAssets();
  } catch (e) {
    showToast('Ошибка при обновлении локации', 'error');
  }
}

// Bulk Retire
async function confirmBulkRetire() {
  if (!confirm(`Вы действительно хотите списать выбранные активы (${state.selectedAssets.size} шт.)?`)) return;

  const assetIds = Array.from(state.selectedAssets);
  const promises = assetIds.map(id => fetch(`${ASSET_SERVICE_BASE}/api/v1/assets/${id}/retire`, {
    method: 'POST',
    headers: getHeaders()
  }));

  try {
    const results = await Promise.all(promises);
    for (let r of results) {
      if (!r.ok) throw new Error();
    }
    showToast(`Списано активов: ${assetIds.length}`, 'success');
    clearSelection();
    await refreshAssets();
  } catch (e) {
    showToast('Ошибка при списании', 'error');
  }
}

// ══════════════════════════════════════════════════════════
// PHASE 2: DASHBOARD & THEME
// ══════════════════════════════════════════════════════════

// Theme Toggle
const themeBtn = document.getElementById('theme-toggle-btn');
if (themeBtn) {
  themeBtn.addEventListener('click', () => {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    if (isDark) {
      document.documentElement.removeAttribute('data-theme');
      localStorage.setItem('theme', 'light');
    } else {
      document.documentElement.setAttribute('data-theme', 'dark');
      localStorage.setItem('theme', 'dark');
    }
  });
  // Init theme
  if (localStorage.getItem('theme') === 'dark') {
    document.documentElement.setAttribute('data-theme', 'dark');
  }
}

function renderDashboard() {
  const activeCount = state.assets.length;
  const archiveCount = state.archivedAssets.length;
  const requestsCount = state.requests.filter(r => r.status === 'NEW' || r.status === 'IN_PROGRESS').length;
  const totalUsers = state.users.length;

  // Update Stats
  const statsEl = document.getElementById('dashboard-stats');
  if (statsEl) {
    statsEl.innerHTML = `
      <div class="stat-box"><div class="stat-value">${activeCount}</div><div class="stat-label">Активных</div></div>
      <div class="stat-box"><div class="stat-value">${archiveCount}</div><div class="stat-label">В архиве</div></div>
      <div class="stat-box"><div class="stat-value">${requestsCount}</div><div class="stat-label">Заявок</div></div>
      <div class="stat-box"><div class="stat-value">${totalUsers}</div><div class="stat-label">Пользователей</div></div>
    `;
  }

  // Update Pie Chart (CSS/SVG)
  const pieContainer = document.getElementById('dashboard-status-pie');
  const legendContainer = document.getElementById('dashboard-status-legend');
  if (pieContainer && legendContainer) {
    const total = activeCount + archiveCount;
    if (total === 0) {
      pieContainer.innerHTML = '<span style="color:var(--text-muted)">Нет данных</span>';
      legendContainer.innerHTML = '';
    } else {
      const activePct = (activeCount / total) * 100;
      const archivePct = (archiveCount / total) * 100;

      pieContainer.innerHTML = `
        <svg viewBox="0 0 32 32" style="width:150px;height:150px;border-radius:50%;transform:rotate(-90deg);">
          <circle r="16" cx="16" cy="16" fill="var(--bg-base)" />
          <circle r="16" cx="16" cy="16" fill="transparent" stroke="var(--accent)" stroke-width="32" stroke-dasharray="${activePct} 100" />
          <circle r="16" cx="16" cy="16" fill="transparent" stroke="var(--red)" stroke-width="32" stroke-dasharray="${archivePct} 100" stroke-dashoffset="-${activePct}" />
        </svg>
      `;
      legendContainer.innerHTML = `
        <div style="display:flex;align-items:center;gap:8px;"><span style="width:12px;height:12px;background:var(--accent);border-radius:2px;"></span> Активные (${activeCount})</div>
        <div style="display:flex;align-items:center;gap:8px;"><span style="width:12px;height:12px;background:var(--red);border-radius:2px;"></span> Списанные (${archiveCount})</div>
      `;
    }
  }

  // Update Recent List
  const recentEl = document.getElementById('dashboard-recent');
  if (recentEl) {
    const recentAssets = [...state.assets].sort((a, b) => b.id.localeCompare(a.id)).slice(0, 5);
    if (recentAssets.length === 0) {
      recentEl.innerHTML = '<span style="color:var(--text-muted)">Нет недавней активности</span>';
    } else {
      recentEl.innerHTML = recentAssets.map(a => `
        <div class="recent-item" onclick="navigateTo('all-assets'); selectAsset('${a.id}')" style="cursor:pointer;">
          <div class="asset-card-icon ${typeIconClass(a.assetType)}">${typeIcon(a.assetType)}</div>
          <div>
            <div style="font-weight:600;font-size:0.875rem;color:var(--text-primary);">${escHtml(a.displayName || a.assetTag)}</div>
            <div style="font-size:0.75rem;color:var(--text-muted);">${typeLabel(a.assetType)} · ${escHtml(a.serialNumber)}</div>
          </div>
        </div>
      `).join('');
    }
  }
}

// ══════════════════════════════════════════════════════════
// PHASE 3: EXPORT, IMPORT, QR CODE, SCANNER
// ══════════════════════════════════════════════════════════

// ── Export CSV ──
document.getElementById('btn-export-csv')?.addEventListener('click', () => {
  const assetsToExport = state.selectedAssets.size > 0
    ? state.assets.filter(a => state.selectedAssets.has(a.id))
    : getFilteredSortedAssets();

  if (assetsToExport.length === 0) {
    return showToast('Нет активов для экспорта', 'error');
  }

  const headers = ['ID', 'AssetTag', 'SerialNumber', 'Type', 'DisplayName', 'Status', 'AssignedUserId', 'LocationId'];
  const rows = assetsToExport.map(a => [
    a.id,
    a.assetTag || '',
    a.serialNumber || '',
    a.assetType || '',
    a.displayName || '',
    a.status || '',
    a.assignedUserId || '',
    a.locationId || ''
  ].map(v => `"${String(v).replace(/"/g, '""')}"`).join(','));

  const csvContent = [headers.join(','), ...rows].join('\n');
  const blob = new Blob([new Uint8Array([0xEF, 0xBB, 0xBF]), csvContent], { type: 'text/csv;charset=utf-8;' }); // BOM for Excel
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', `assets_export_${new Date().getTime()}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  showToast('Экспорт завершен', 'success');
});

// ── Import CSV ──
function openImportCsvModal() {
  document.getElementById('import-csv-file').value = '';
  document.getElementById('import-csv-modal').classList.remove('hidden');
}
function closeImportCsvModal() {
  document.getElementById('import-csv-modal').classList.add('hidden');
}
async function confirmImportCsv() {
  const fileInput = document.getElementById('import-csv-file');
  if (!fileInput.files || fileInput.files.length === 0) {
    return showToast('Выберите файл CSV', 'error');
  }

  const file = fileInput.files[0];
  const reader = new FileReader();
  reader.onload = async (e) => {
    const text = e.target.result;
    const lines = text.split('\n').filter(l => l.trim() !== '');
    if (lines.length < 2) return showToast('Файл пуст или содержит только заголовки', 'error');

    const promises = [];
    for (let i = 1; i < lines.length; i++) {
      const parts = lines[i].split(',').map(p => p.replace(/^"|"$/g, '').trim());
      const payload = {
        assetTag: parts[0] || `IMPORT-${Date.now()}-${i}`,
        serialNumber: parts[1] || '',
        assetType: parts[2] || 'OTHER',
        displayName: parts[3] || '',
        note: parts[4] || 'Импортировано из CSV',
        status: 'ACTIVE'
      };

      promises.push(fetch(`${ASSET_SERVICE_BASE}/api/v1/assets`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(toSnake(payload))
      }));
    }

    try {
      showToast(`Импорт ${promises.length} активов начат...`, 'success');
      closeImportCsvModal();
      await Promise.all(promises);
      await refreshAssets();
      showToast('Импорт успешно завершен!', 'success');
    } catch (err) {
      showToast('Произошли ошибки при импорте', 'error');
    }
  };
  reader.readAsText(file);
}

// ── QR Code Generator ──
// ── QR helpers: deterministic via api.qrserver.com ──
let _currentQrAssetTag = null; // человекочитаемый тег (для подписей и имён файлов)
let _currentQrText = null;     // неизменяемый payload QR (ITAM-ASSET:<id>)

function _qrImgUrl(text, size) {
  return `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodeURIComponent(text)}&margin=4&color=0f172a&bgcolor=ffffff`;
}

function generateQrForAsset(id) {
  const asset = getAssetById(id);
  if (!asset) return;

  const qrText = 'ITAM-ASSET:' + asset.id; // один неизменяемый код на актив
  _currentQrText = qrText;
  _currentQrAssetTag = asset.assetTag || asset.id || 'N/A';

  document.getElementById('qr-modal-title').textContent = `QR: ${asset.assetTag}`;

  const tagEl = document.getElementById('qr-modal-asset-tag');
  if (tagEl) tagEl.textContent = _currentQrAssetTag;

  const container = document.getElementById('qrcode-container');
  container.innerHTML = `<img src="${_qrImgUrl(qrText, 200)}" style="width:200px;height:200px;display:block;" alt="QR ${qrText}" />`;

  document.getElementById('qr-modal').classList.remove('hidden');
}

function closeQrModal() {
  document.getElementById('qr-modal').classList.add('hidden');
}

function printQr() {
  if (!_currentQrAssetTag) return;
  const imgUrl = _qrImgUrl(_currentQrText || _currentQrAssetTag, 300);
  const win = window.open('', '_blank');
  win.document.write(`<html><head><title>QR: ${_currentQrAssetTag}</title></head><body onload="window.print();window.close()" style="text-align:center;margin-top:40px;">`);
  win.document.write(`<img src="${imgUrl}" style="width:300px;height:300px;" />`);
  win.document.write(`<p style="font-family:monospace;margin-top:12px;">${_currentQrAssetTag}</p>`);
  win.document.write('</body></html>');
  win.document.close();
}

function downloadQrPng() {
  if (!_currentQrAssetTag) return;
  const url = _qrImgUrl(_currentQrText || _currentQrAssetTag, 400);
  const a = document.createElement('a');
  a.href = url;
  a.download = `QR_${_currentQrAssetTag}.png`;
  a.target = '_blank';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}

function downloadQrSvg() {
  if (!_currentQrAssetTag) return;
  // Build minimal SVG with embedded QR via foreignObject (editable vector)
  const size = 200;
  const svgContent = `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="${size}" height="${size + 30}" viewBox="0 0 ${size} ${size + 30}">
  <rect width="${size}" height="${size + 30}" fill="white"/>
  <image href="${_qrImgUrl(_currentQrText || _currentQrAssetTag, size)}" x="0" y="0" width="${size}" height="${size}"/>
  <text x="${size / 2}" y="${size + 20}" text-anchor="middle" font-family="monospace" font-size="12" fill="#0f172a">${_currentQrAssetTag}</text>
</svg>`;
  const blob = new Blob([svgContent], { type: 'image/svg+xml;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `QR_${_currentQrAssetTag}.svg`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

// ── Scanner (html5-qrcode) ──
let html5QrcodeScanner = null;
function openScannerModal() {
  document.getElementById('scanner-modal').classList.remove('hidden');

  if (!html5QrcodeScanner) {
    html5QrcodeScanner = new Html5QrcodeScanner("reader", { fps: 10, qrbox: {width: 250, height: 250} }, false);
  }

  html5QrcodeScanner.render(onScanSuccess, onScanFailure);
}

function closeScannerModal() {
  document.getElementById('scanner-modal').classList.add('hidden');
  if (html5QrcodeScanner) {
    html5QrcodeScanner.clear().catch(e => console.error(e));
  }
}

function onScanSuccess(decodedText, decodedResult) {
  console.log(`Scan result: ${decodedText}`);
  closeScannerModal();
  let searchStr = decodedText;
  // Канонический формат ITAM-ASSET:<id> (+ старый ITAM-ASSET:<id>:<assetTag>)
  if (decodedText.startsWith('ITAM-ASSET:')) {
    const parts = decodedText.split(':');
    const assetId = parts[1];
    const all = (state.assets || []).concat(state.archivedAssets || []);
    const byId = all.find(a => a.id === assetId);
    if (byId) {
      searchStr = byId.assetTag || byId.id; // нашли по неизменяемому id
    } else if (parts.length >= 3) {
      searchStr = parts[2]; // fallback: старые этикетки с тегом
    } else {
      searchStr = assetId;
    }
  }
  // Простой текст (например ASSET-0001) — ищем как есть

  navigateTo('all-assets');
  const searchInput = document.getElementById('global-search');
  if (searchInput) {
    searchInput.value = searchStr;
    searchInput.dispatchEvent(new Event('input'));
  }
  showToast(`Отсканировано: ${searchStr}`, 'success');
}

function onScanFailure(error) {
  // handled internally
}

// ══════════════════════════════════════════════════════════
// SIM-CARDS SECTION — Full CRUD
// ══════════════════════════════════════════════════════════

function renderSimCardsSection() {
  const section = document.getElementById('section-sim-cards');
  if (!section) return;

  const cards = state.simCards || [];
  section.innerHTML = `
    <div class="section-header" style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
      <div>
        <h2 style="font-size:1.25rem;font-weight:700;color:var(--text-primary);margin:0;">SIM-карты</h2>
        <p style="color:var(--text-muted);font-size:0.875rem;margin:4px 0 0;">Всего: ${cards.length}</p>
      </div>
      <button class="btn-primary" style="padding:6px 14px;font-size:0.875rem;" onclick="openAddSimCardModal()">+ Добавить SIM-карту</button>
    </div>
    ${cards.length === 0 ? `
      <div class="empty-state" style="padding:60px 20px;text-align:center;">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" style="color:var(--text-muted);margin-bottom:16px;"><rect x="5" y="2" width="14" height="20" rx="2"/><path d="M15 2v5h5M5 13h14M5 17h14"/></svg>
        <p style="color:var(--text-muted);font-size:0.9rem;">SIM-карты не найдены</p>
        <p style="color:var(--text-tertiary);font-size:0.8rem;">Нажмите «Добавить SIM-карту» для создания</p>
      </div>
    ` : `
      <div class="table-container">
        <table class="table">
          <thead><tr>
            <th>Номер телефона</th><th>Оператор</th><th>Тариф</th><th>Статус</th><th>Сотрудник</th><th>Локация</th><th></th>
          </tr></thead>
          <tbody>
            ${cards.map(c => `<tr style="cursor:pointer;" onclick="openEditSimCardModal('${c.id}')">
              <td style="font-family:monospace;font-weight:600;">${escHtml(c.phoneNumber || '—')}</td>
              <td>${escHtml(c.operator || '—')}</td>
              <td>${escHtml(c.tariff || '—')}</td>
              <td><span class="badge ${c.status === 'ACTIVE' ? 'badge-emerald' : c.status === 'BLOCKED' ? 'badge-amber' : 'badge-slate'}">${escHtml(c.status || '—')}</span></td>
              <td>${escHtml(c.issuedToUserId ? (getUserById(c.issuedToUserId)?.name || c.issuedToUserId) : 'Не назначена')}</td>
              <td>${escHtml(c.issuedToLocationId ? (getLocById(c.issuedToLocationId)?.name || '—') : '—')}</td>
              <td style="text-align:right;">
                <button class="btn-icon" onclick="event.stopPropagation();openEditSimCardModal('${c.id}')" title="Редактировать">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                </button>
              </td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>
    `}
  `;
}

function openAddSimCardModal() {
  // Populate datalists for user and location hints
  const userList = document.getElementById('sim-users-list');
  if (userList) userList.innerHTML = state.users.map(u => `<option value="${escHtml(u.name)}"></option>`).join('');
  const locList = document.getElementById('sim-locations-list');
  if (locList) locList.innerHTML = state.locations.map(l => `<option value="${escHtml(l.name)}"></option>`).join('');
  clearFieldErrors(['new-sim-phone','new-sim-phone-error'],['new-sim-operator','new-sim-operator-error']);
  document.getElementById('add-sim-card-form').reset();
  // Clear text inputs manually (reset doesn't always work)
  const u = document.getElementById('new-sim-user'); if (u) u.value = '';
  const l = document.getElementById('new-sim-location'); if (l) l.value = '';
  document.getElementById('add-sim-card-modal').classList.remove('hidden');
}


async function confirmAddSimCard() {
  const phone = document.getElementById('new-sim-phone').value.trim();
  const operator = document.getElementById('new-sim-operator').value.trim();
  const tariff = document.getElementById('new-sim-tariff').value.trim();
  const status = document.getElementById('new-sim-status').value;
  // Text fields: user gets the typed name, location gets the typed text
  const userName = document.getElementById('new-sim-user').value.trim() || null;
  const locationName = document.getElementById('new-sim-location').value.trim() || null;
  // Try to resolve IDs from names
  const simUser = userName ? state.users.find(u => u.name === userName) : null;
  const simLoc  = locationName ? state.locations.find(l => l.name === locationName) : null;

  let hasErrors = false;
  if (!phone) { setFieldError('new-sim-phone','new-sim-phone-error', true); hasErrors = true; }
  else { setFieldError('new-sim-phone','new-sim-phone-error', false); }
  if (!operator) { setFieldError('new-sim-operator','new-sim-operator-error', true); hasErrors = true; }
  else { setFieldError('new-sim-operator','new-sim-operator-error', false); }
  if (hasErrors) { showToast('Заполните обязательные поля', 'error'); return; }

  try {
    const res = await fetch(`${API_BASE}/api/v1/sim-cards`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(toSnake({
        phoneNumber: phone,
        operator,
        tariff: tariff || null,
        status,
        issuedToUserId: simUser ? simUser.id : null,
        issuedToLocationId: simLoc ? simLoc.id : null,
        issuedToName: userName,
        locationName: locationName
      }))
    });
    if (!res.ok) throw new Error(await res.text());
    const created = toCamel(await res.json());
    state.simCards = state.simCards || [];
    state.simCards.push(created);
    document.getElementById('add-sim-card-modal').classList.add('hidden');
    renderSimCardsSection();
    showToast(`SIM-карта ${phone} добавлена`, 'success');
  } catch(e) {
    console.error(e);
    showToast('Ошибка при добавлении SIM-карты', 'error');
  }
}


function openEditSimCardModal(id) {
  const card = (state.simCards || []).find(c => c.id === id);
  if (!card) return;

  // Populate datalists for text fields
  const userListEdit = document.getElementById('sim-users-list-edit');
  if (userListEdit) userListEdit.innerHTML = state.users.map(u => `<option value="${escHtml(u.name)}"></option>`).join('');
  const locListEdit = document.getElementById('sim-locations-list-edit');
  if (locListEdit) locListEdit.innerHTML = state.locations.map(l => `<option value="${escHtml(l.name)}"></option>`).join('');

  document.getElementById('edit-sim-id').value = card.id;
  document.getElementById('edit-sim-phone').value = card.phoneNumber || '';
  document.getElementById('edit-sim-operator').value = card.operator || '';
  document.getElementById('edit-sim-tariff').value = card.tariff || '';
  document.getElementById('edit-sim-status').value = card.status || 'ACTIVE';

  // Set text values for user/location
  const userInp = document.getElementById('edit-sim-user');
  if (userInp) {
    if (card.issuedToName) {
      userInp.value = card.issuedToName;
    } else if (card.issuedToUserId) {
      const u = getUserById(card.issuedToUserId);
      userInp.value = u ? u.name : '';
    } else {
      userInp.value = '';
    }
  }
  const locInp = document.getElementById('edit-sim-location');
  if (locInp) {
    if (card.locationName) {
      locInp.value = card.locationName;
    } else if (card.issuedToLocationId) {
      const l = getLocById(card.issuedToLocationId);
      locInp.value = l ? l.name : '';
    } else {
      locInp.value = '';
    }
  }

  document.getElementById('edit-sim-card-modal').classList.remove('hidden');
}


function closeEditSimCardModal() {
  document.getElementById('edit-sim-card-modal').classList.add('hidden');
}

async function confirmEditSimCard() {
  const id = document.getElementById('edit-sim-id').value;
  const userName = document.getElementById('edit-sim-user').value.trim() || null;
  const locationName = document.getElementById('edit-sim-location').value.trim() || null;
  // Try to resolve IDs from typed names
  const simUser = userName ? state.users.find(u => u.name === userName) : null;
  const simLoc  = locationName ? state.locations.find(l => l.name === locationName) : null;
  const payload = {
    phoneNumber: document.getElementById('edit-sim-phone').value.trim(),
    operator: document.getElementById('edit-sim-operator').value.trim(),
    tariff: document.getElementById('edit-sim-tariff').value.trim() || null,
    status: document.getElementById('edit-sim-status').value,
    issuedToUserId: simUser ? simUser.id : null,
    issuedToLocationId: simLoc ? simLoc.id : null,
    issuedToName: userName,
    locationName: locationName,
  };
  try {
    const res = await fetch(`${API_BASE}/api/v1/sim-cards/${id}`, {
      method: 'PATCH', headers: getHeaders(), body: JSON.stringify(toSnake(payload))
    });
    if (!res.ok) throw new Error(await res.text());
    const updated = toCamel(await res.json());
    state.simCards = (state.simCards || []).map(c => c.id === id ? updated : c);
    closeEditSimCardModal();
    renderSimCardsSection();
    showToast('SIM-карта обновлена', 'success');
  } catch(e) {
    console.error(e);
    showToast('Ошибка при сохранении', 'error');
  }
}


async function deleteSimCard(id) {
  if (!confirm('Удалить SIM-карту? Это действие необратимо.')) return;
  try {
    const res = await fetch(`${API_BASE}/api/v1/sim-cards/${id}`, { method: 'DELETE', headers: getHeaders() });
    if (!res.ok && res.status !== 204) throw new Error(await res.text());
    state.simCards = (state.simCards || []).filter(c => c.id !== id);
    closeEditSimCardModal();
    renderSimCardsSection();
    showToast('SIM-карта удалена', 'success');
  } catch(e) {
    console.error(e);
    showToast('Ошибка при удалении', 'error');
  }
}

// ══════════════════════════════════════════════════════════
// CARTRIDGES SECTION — Full CRUD
// ══════════════════════════════════════════════════════════

function renderCartridgesSection() {
  const section = document.getElementById('section-cartridges');
  if (!section) return;

  const cartridges = state.cartridges || [];
  const printers = state.assets.filter(a => a.assetType === 'PRINTER');

  section.innerHTML = `
    <div class="section-header" style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
      <div>
        <h2 style="font-size:1.25rem;font-weight:700;color:var(--text-primary);margin:0;">Картриджи</h2>
        <p style="color:var(--text-muted);font-size:0.875rem;margin:4px 0 0;">Всего картриджей: ${cartridges.length} · Принтеров в реестре: ${printers.length}</p>
      </div>
      <button class="btn-primary" style="padding:6px 14px;font-size:0.875rem;" onclick="openAddCartridgeModal()">+ Добавить картридж</button>
    </div>

    <h3 style="font-size:1rem;font-weight:600;color:var(--text-secondary);margin:0 0 12px;">Принтеры</h3>
    ${printers.length === 0 ? '<p style="color:var(--text-muted);font-size:0.875rem;margin-bottom:20px;">Принтеры не найдены в реестре активов</p>' : `
    <div class="table-container" style="margin-bottom:28px;">
      <table class="table">
        <thead><tr><th>Принтер</th><th>Asset Tag</th><th>Локация</th><th>Статус</th><th>Картриджей установлено</th></tr></thead>
        <tbody>
          ${printers.map(p => {
            const installedCarts = cartridges.filter(c => c.printerAssetId === p.id);
            const loc = getLocById(p.locationId);
            return `<tr>
              <td>${escHtml(p.displayName || p.assetTag)}</td>
              <td><span class="mono">${escHtml(p.assetTag)}</span></td>
              <td>${escHtml(loc?.name || '—')}</td>
              <td>${statusBadge(p.status)}</td>
              <td>${installedCarts.length > 0 ? installedCarts.map(c => `<span style="font-size:0.75rem;background:var(--bg-secondary);padding:2px 6px;border-radius:4px;margin-right:4px;">${escHtml(c.model)}</span>`).join('') : '<span style="color:var(--text-muted);">—</span>'}</td>
            </tr>`;
          }).join('')}
        </tbody>
      </table>
    </div>`}

    <h3 style="font-size:1rem;font-weight:600;color:var(--text-secondary);margin:0 0 12px;">Картриджи на складе</h3>
    ${cartridges.length === 0 ? `
      <div class="empty-state" style="padding:60px 20px;text-align:center;">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" style="color:var(--text-muted);margin-bottom:16px;"><rect x="2" y="7" width="20" height="10" rx="2"/><path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2"/></svg>
        <p style="color:var(--text-muted);font-size:0.9rem;">Картриджи не добавлены</p>
      </div>
    ` : `
      <div class="table-container">
        <table class="table">
          <thead><tr>
            <th>Модель</th><th>Совместимость</th><th>Статус</th><th>Принтер</th><th>Локация</th><th></th>
          </tr></thead>
          <tbody>
            ${cartridges.map(c => {
              const printer = c.printerAssetId ? getAssetById(c.printerAssetId) : null;
              const loc = c.locationId ? getLocById(c.locationId) : null;
              return `<tr style="cursor:pointer;" onclick="openEditCartridgeModal('${c.id}')">
                <td style="font-weight:600;">${escHtml(c.model || '—')}</td>
                <td>${escHtml(c.compatibility || '—')}</td>
                <td><span class="badge ${c.status === 'FULL' ? 'badge-emerald' : c.status === 'INSTALLED' ? 'badge-indigo' : 'badge-slate'}">${escHtml(c.status || '—')}</span></td>
                <td>${printer ? escHtml(printer.displayName || printer.assetTag) : '<span style="color:var(--text-muted);">—</span>'}</td>
                <td>${escHtml(loc?.name || '—')}</td>
                <td style="text-align:right;">
                  <button class="btn-icon" onclick="event.stopPropagation();openEditCartridgeModal('${c.id}')" title="Редактировать">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                  </button>
                </td>
              </tr>`;
            }).join('')}
          </tbody>
        </table>
      </div>
    `}
  `;
}

function openAddCartridgeModal() {
  const locSel = document.getElementById('new-cart-location');
  if (locSel) locSel.innerHTML = '<option value="">Не указана</option>' +
    state.locations.map(l => `<option value="${l.id}">${escHtml(l.name)}</option>`).join('');
  const printers = state.assets.filter(a => a.assetType === 'PRINTER');
  const printSel = document.getElementById('new-cart-printer');
  if (printSel) printSel.innerHTML = '<option value="">Не установлен</option>' +
    printers.map(p => `<option value="${p.id}">${escHtml(p.displayName || p.assetTag)}</option>`).join('');
  clearFieldErrors(['new-cart-model','new-cart-model-error'],['new-cart-compat','new-cart-compat-error']);
  document.getElementById('add-cartridge-form').reset();
  document.getElementById('add-cartridge-modal').classList.remove('hidden');
}

async function confirmAddCartridge() {
    const model = document.getElementById('new-cart-model').value.trim();
  const compat = document.getElementById('new-cart-compat').value.trim();
  const status = document.getElementById('new-cart-status').value;
  const locId = document.getElementById('new-cart-location').value || null;
  const printerId = document.getElementById('new-cart-printer').value || null;

  let hasErrors = false;
  if (!model) { setFieldError('new-cart-model','new-cart-model-error', true); hasErrors = true; }
  else { setFieldError('new-cart-model','new-cart-model-error', false); }
  if (!compat) { setFieldError('new-cart-compat','new-cart-compat-error', true); hasErrors = true; }
  else { setFieldError('new-cart-compat','new-cart-compat-error', false); }
  if (hasErrors) { showToast('Заполните обязательные поля', 'error'); return; }

  try {
    const res = await fetch(`${API_BASE}/api/v1/cartridges`, {
      method: 'POST', headers: getHeaders(),
      body: JSON.stringify(toSnake({ model, compatibility: compat, status, locationId: locId, printerAssetId: printerId }))
    });
    if (!res.ok) throw new Error(await res.text());
    const created = toCamel(await res.json());
    state.cartridges = state.cartridges || [];
    state.cartridges.push(created);
    document.getElementById('add-cartridge-modal').classList.add('hidden');
    renderCartridgesSection();
    showToast(`Картридж ${model} добавлен`, 'success');
  } catch(e) {
    console.error(e);
    showToast('Ошибка при добавлении картриджа', 'error');
  }
}

function openEditCartridgeModal(id) {
  const cart = (state.cartridges || []).find(c => c.id === id);
  if (!cart) return;
  document.getElementById('edit-cart-id').value = cart.id;
  document.getElementById('edit-cart-model').value = cart.model || '';
  document.getElementById('edit-cart-compat').value = cart.compatibility || '';
  document.getElementById('edit-cart-status').value = cart.status || 'FULL';
  const locSel = document.getElementById('edit-cart-location');
  if (locSel) locSel.innerHTML = '<option value="">Не указана</option>' +
    state.locations.map(l => `<option value="${l.id}" ${cart.locationId === l.id ? 'selected' : ''}>${escHtml(l.name)}</option>`).join('');
  const printers = state.assets.filter(a => a.assetType === 'PRINTER');
  const printSel = document.getElementById('edit-cart-printer');
  if (printSel) printSel.innerHTML = '<option value="">Не установлен</option>' +
    printers.map(p => `<option value="${p.id}" ${cart.printerAssetId === p.id ? 'selected' : ''}>${escHtml(p.displayName || p.assetTag)}</option>`).join('');
  document.getElementById('edit-cartridge-modal').classList.remove('hidden');
}

function closeEditCartridgeModal() {
  document.getElementById('edit-cartridge-modal').classList.add('hidden');
}

async function confirmEditCartridge() {
  const id = document.getElementById('edit-cart-id').value;
  const payload = {
    model: document.getElementById('edit-cart-model').value.trim(),
    compatibility: document.getElementById('edit-cart-compat').value.trim(),
    status: document.getElementById('edit-cart-status').value,
    locationId: document.getElementById('edit-cart-location').value || null,
    printerAssetId: document.getElementById('edit-cart-printer').value || null,
  };
  try {
    const res = await fetch(`${API_BASE}/api/v1/cartridges/${id}`, {
      method: 'PATCH', headers: getHeaders(), body: JSON.stringify(toSnake(payload))
    });
    if (!res.ok) throw new Error(await res.text());
    const updated = toCamel(await res.json());
    state.cartridges = (state.cartridges || []).map(c => c.id === id ? updated : c);
    closeEditCartridgeModal();
    renderCartridgesSection();
    showToast('Картридж обновлён', 'success');
  } catch(e) {
    console.error(e);
    showToast('Ошибка при сохранении', 'error');
  }
}

async function deleteCartridge(id) {
  if (!confirm('Удалить картридж? Это действие необратимо.')) return;
  try {
    const res = await fetch(`${API_BASE}/api/v1/cartridges/${id}`, { method: 'DELETE', headers: getHeaders() });
    if (!res.ok && res.status !== 204) throw new Error(await res.text());
    state.cartridges = (state.cartridges || []).filter(c => c.id !== id);
    closeEditCartridgeModal();
    renderCartridgesSection();
    showToast('Картридж удалён', 'success');
  } catch(e) {
    console.error(e);
    showToast('Ошибка при удалении', 'error');
  }
}

// ══════════════════════════════════════════════════════════
// RBAC — Roles Modal
// ══════════════════════════════════════════════════════════

// Map of userId → { adminChecked: bool, operatorChecked: bool }
let _rolesEditState = {};
const ROLE_ADMIN_ID = 'f6000000-0000-0000-0000-000000000001';
const ROLE_OPERATOR_ID = 'f6000000-0000-0000-0000-000000000002';

async function openRolesModal() {
  _rolesEditState = {};
  document.getElementById('roles-modal').classList.remove('hidden');
  document.getElementById('roles-modal-loading').style.display = '';
  document.getElementById('roles-modal-table-wrap').style.display = 'none';

  try {
    // Load roles for all users in parallel
    const users = state.users;
    const roleResults = await Promise.all(
      users.map(u =>
        fetch(`${USER_API_BASE}/api/v1/users/${u.id}/roles`, { headers: getHeaders() })
          .then(r => r.ok ? r.json() : [])
          .catch(() => [])
      )
    );

    users.forEach((u, i) => {
      const roleNames = roleResults[i];
      _rolesEditState[u.id] = {
        admin: roleNames.includes('Tenant Admin'),
        operator: roleNames.includes('Asset Operator'),
      };
    });

    const dept = id => state.departments.find(d => d.id === id)?.name || '—';
    const tbody = document.getElementById('roles-tbody');
    tbody.innerHTML = users.map(u => `
      <tr>
        <td style="font-weight:500;">${escHtml(u.name)}</td>
        <td style="color:var(--text-muted);font-size:0.875rem;">${escHtml(dept(u.departmentId))}</td>
        <td style="text-align:center;">
          <input type="checkbox" id="role-admin-${u.id}" ${_rolesEditState[u.id].admin ? 'checked' : ''}
            onchange="_rolesEditState['${u.id}'].admin = this.checked" />
        </td>
        <td style="text-align:center;">
          <input type="checkbox" id="role-op-${u.id}" ${_rolesEditState[u.id].operator ? 'checked' : ''}
            onchange="_rolesEditState['${u.id}'].operator = this.checked" />
        </td>
      </tr>
    `).join('');

    document.getElementById('roles-modal-loading').style.display = 'none';
    document.getElementById('roles-modal-table-wrap').style.display = '';
  } catch(e) {
    console.error(e);
    document.getElementById('roles-modal-loading').textContent = 'Ошибка загрузки ролей';
  }
}

function closeRolesModal() {
  document.getElementById('roles-modal').classList.add('hidden');
}

async function saveRoles() {
  const btn = document.getElementById('save-roles-btn');
  btn.disabled = true;
  btn.textContent = 'Сохранение...';
  let success = 0, errors = 0;

  try {
    const users = state.users;
    for (const u of users) {
      const rs = _rolesEditState[u.id];
      if (!rs) continue;
      const roleIds = [];
      if (rs.admin) roleIds.push(ROLE_ADMIN_ID);
      if (rs.operator) roleIds.push(ROLE_OPERATOR_ID);
      try {
        const res = await fetch(`${USER_API_BASE}/api/v1/users/${u.id}/roles`, {
          method: 'PUT', headers: getHeaders(), body: JSON.stringify({ roleIds })
        });
        if (res.ok) success++; else errors++;
      } catch { errors++; }
    }
    if (errors === 0) showToast(`Роли сохранены для ${success} пользователей`, 'success');
    else showToast(`Сохранено: ${success}, ошибок: ${errors}`, 'warning');
    closeRolesModal();
  } finally {
    btn.disabled = false;
    btn.textContent = 'Сохранить изменения';
  }
}

// ══════════════════════════════════════════════════════════
// LABEL PRINT — Thermal Printer Label (57×40 mm)
// ══════════════════════════════════════════════════════════

let _labelAssetId = null;

function openLabelPrintModal(assetId) {
  const asset = getAssetById(assetId);
  if (!asset) return;
  _labelAssetId = assetId;

  const user = getUserById(asset.assignedUserId);
  const loc = getLocById(asset.locationId);
  const qrText = asset.assetTag || asset.id || 'N/A';
  const today = new Date().toLocaleDateString('ru-RU');

  const container = document.getElementById('label-preview-container');
  container.innerHTML = `
    <div class="label-card" id="label-print-content">
      <div class="label-qr" id="label-qr-box"></div>
      <div class="label-info">
        <div class="label-tag">${escHtml(asset.assetTag)}</div>
        <div class="label-name">${escHtml(asset.displayName || typeLabel(asset.assetType))}</div>
        <div class="label-row"><span>SN:</span><span class="mono">${escHtml(asset.serialNumber || '—')}</span></div>
        <div class="label-row"><span>Тип:</span><span>${escHtml(typeLabel(asset.assetType))}</span></div>
        ${loc ? `<div class="label-row"><span>Место:</span><span>${escHtml(loc.name)}</span></div>` : ''}
        <!-- Поле "Кому" убрано по запросу пользователя -->
        <div class="label-date">${today}</div>
      </div>
    </div>
  `;

  // Generate QR inside label — deterministic via api.qrserver.com
  const qrBox = document.getElementById('label-qr-box');
  const labelQrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=96x96&data=${encodeURIComponent(qrText)}&margin=4&color=000000&bgcolor=ffffff`;
  qrBox.innerHTML = `<img src="${labelQrUrl}" style="width:96px;height:96px;display:block;" alt="QR ${qrText}" />`;

  document.getElementById('label-print-modal').classList.remove('hidden');
}

function closeLabelPrintModal() {
  document.getElementById('label-print-modal').classList.add('hidden');
  _labelAssetId = null;
}

function doPrintLabel() {
  const asset = _labelAssetId ? getAssetById(_labelAssetId) : null;
  if (!asset) return;

  const user = getUserById(asset.assignedUserId);
  const loc = getLocById(asset.locationId);
  const qrText = asset.assetTag || asset.id || 'N/A';
  const today = new Date().toLocaleDateString('ru-RU');
  const qrImgUrl = `https://api.qrserver.com/v1/create-qr-code/?size=130x130&data=${encodeURIComponent(qrText)}&margin=2`;

  const win = window.open('', '_blank', 'width=400,height=300');
  win.document.write(`<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title>Этикетка: ${asset.assetTag}</title>
<style>
  @page { size: 57mm 40mm; margin: 0; }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: Arial, sans-serif; width: 57mm; height: 40mm; overflow: hidden; display: flex; align-items: stretch; }
  .label { display: flex; width: 100%; height: 100%; padding: 2mm; gap: 2mm; }
  .label-qr { flex-shrink: 0; }
  .label-qr img { width: 34mm; height: 34mm; display: block; }
  .label-info { flex: 1; display: flex; flex-direction: column; gap: 0.5mm; overflow: hidden; }
  .tag { font-size: 8pt; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-bottom: 0.3mm solid #000; padding-bottom: 0.5mm; margin-bottom: 0.5mm; }
  .name { font-size: 6.5pt; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .row { font-size: 5.5pt; display: flex; gap: 1mm; white-space: nowrap; }
  .row span:first-child { color: #555; min-width: 7mm; }
  .row span:last-child { font-weight: 500; overflow: hidden; text-overflow: ellipsis; }
  .date { font-size: 5pt; color: #777; margin-top: auto; }
</style>
</head>
<body onload="window.print();setTimeout(()=>window.close(),500)">
<div class="label">
  <div class="label-qr"><img src="${qrImgUrl}" /></div>
  <div class="label-info">
    <div class="tag">${asset.assetTag}</div>
    <div class="name">${asset.displayName || typeLabel(asset.assetType)}</div>
    <div class="row"><span>SN:</span><span>${asset.serialNumber || '—'}</span></div>
    <div class="row"><span>Тип:</span><span>${typeLabel(asset.assetType)}</span></div>
    ${loc ? `<div class="row"><span>Место:</span><span>${loc.name}</span></div>` : ''}
    <!-- Поле "Кому" убрано -->
    <div class="date">${today}</div>
  </div>
</div>
</body>
</html>`);
  win.document.close();
}


// ─────────────────────────────────────────────────────────
// DOWNLOAD LABEL AS PNG
// ─────────────────────────────────────────────────────────
function downloadLabelPNG() {
  const asset = _labelAssetId ? getAssetById(_labelAssetId) : null;
  if (!asset) return;

  const qrText = 'ITAM-ASSET:' + asset.id; // один неизменяемый код на актив
  const loc = getLocById(asset.locationId);
  const today = new Date().toLocaleDateString('ru-RU');

  // Label size: 57mm × 40mm at 96dpi → 215 × 151 px (≈3.78px/mm)
  const PX_PER_MM = 3.78;
  const W = Math.round(57 * PX_PER_MM);  // 215
  const H = Math.round(40 * PX_PER_MM);  // 151
  const PAD = Math.round(2 * PX_PER_MM); // 8
  const QR_SIZE = Math.round(34 * PX_PER_MM); // 128

  const canvas = document.createElement('canvas');
  canvas.width = W;
  canvas.height = H;
  const ctx = canvas.getContext('2d');

  // Background
  ctx.fillStyle = '#ffffff';
  ctx.fillRect(0, 0, W, H);

  // Border
  ctx.strokeStyle = '#cccccc';
  ctx.lineWidth = 1;
  ctx.strokeRect(0.5, 0.5, W - 1, H - 1);

  const qrImg = new Image();
  qrImg.crossOrigin = 'anonymous';
  qrImg.onload = function () {
    // Draw QR
    ctx.drawImage(qrImg, PAD, PAD, QR_SIZE, QR_SIZE);

    // Info column
    const infoX = PAD + QR_SIZE + PAD;
    const infoW = W - infoX - PAD;
    let y = PAD + 4;

    // Tag
    ctx.fillStyle = '#111111';
    ctx.font = 'bold 11px Arial, sans-serif';
    ctx.fillText(truncate(ctx, asset.assetTag, infoW), infoX, y + 11);
    y += 14;
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 0.5;
    ctx.beginPath(); ctx.moveTo(infoX, y); ctx.lineTo(infoX + infoW, y); ctx.stroke();
    y += 4;

    // Name
    ctx.fillStyle = '#222222';
    ctx.font = 'bold 9px Arial, sans-serif';
    ctx.fillText(truncate(ctx, asset.displayName || typeLabel(asset.assetType), infoW), infoX, y + 9);
    y += 13;

    // SN
    ctx.font = '8px Arial, sans-serif';
    ctx.fillStyle = '#555555';
    ctx.fillText('SN:', infoX, y + 8);
    ctx.fillStyle = '#222222';
    ctx.font = 'bold 8px Arial, sans-serif';
    ctx.fillText(truncate(ctx, asset.serialNumber || '—', infoW - 20), infoX + 20, y + 8);
    y += 11;

    // Type
    ctx.font = '8px Arial, sans-serif';
    ctx.fillStyle = '#555555';
    ctx.fillText('Тип:', infoX, y + 8);
    ctx.fillStyle = '#222222';
    ctx.font = 'bold 8px Arial, sans-serif';
    ctx.fillText(truncate(ctx, typeLabel(asset.assetType), infoW - 22), infoX + 22, y + 8);
    y += 11;

    // Location
    if (loc) {
      ctx.font = '8px Arial, sans-serif';
      ctx.fillStyle = '#555555';
      ctx.fillText('Место:', infoX, y + 8);
      ctx.fillStyle = '#222222';
      ctx.font = 'bold 8px Arial, sans-serif';
      ctx.fillText(truncate(ctx, loc.name, infoW - 28), infoX + 28, y + 8);
      y += 11;
    }

    // Date
    ctx.font = '7px Arial, sans-serif';
    ctx.fillStyle = '#999999';
    ctx.fillText(today, infoX, H - PAD - 2);

    // Download
    const link = document.createElement('a');
    link.download = `label-${asset.assetTag}.png`;
    link.href = canvas.toDataURL('image/png');
    link.click();
  };
  qrImg.onerror = function () {
    // Fallback: draw text QR placeholder
    ctx.fillStyle = '#eeeeee';
    ctx.fillRect(PAD, PAD, QR_SIZE, QR_SIZE);
    ctx.fillStyle = '#333333';
    ctx.font = 'bold 9px Arial, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText(qrText, PAD + QR_SIZE / 2, PAD + QR_SIZE / 2);
    ctx.textAlign = 'left';
    const link = document.createElement('a');
    link.download = `label-${asset.assetTag}.png`;
    link.href = canvas.toDataURL('image/png');
    link.click();
  };
  qrImg.src = `https://api.qrserver.com/v1/create-qr-code/?size=${QR_SIZE}x${QR_SIZE}&data=${encodeURIComponent(qrText)}&margin=2&color=000000&bgcolor=ffffff`;
}

function truncate(ctx, text, maxWidth) {
  if (!text) return '';
  if (ctx.measureText(text).width <= maxWidth) return text;
  let t = text;
  while (t.length > 1 && ctx.measureText(t + '…').width > maxWidth) t = t.slice(0, -1);
  return t + '…';
}

// ═══════════════════════════════════════════════════════════
// PHASE 2 — SETTINGS TABS
// ═══════════════════════════════════════════════════════════

function switchSettingsTab(tab) {
  state.settingsActiveTab = tab;
  document.querySelectorAll('.settings-tab').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.stab === tab);
  });
  renderSettingsSection(tab);
}

function renderSettingsSection(tab) {
  tab = tab || state.settingsActiveTab || 'general';
  state.settingsActiveTab = tab;
  const body = document.getElementById('settings-tab-body');
  if (!body) return;
  switch (tab) {
    case 'general':      body.innerHTML = _stGeneral();     break;
    case 'dictionaries': body.innerHTML = _stDicts();       break;
    case 'users':        body.innerHTML = _stUsers();       break;
    case 'integrations': body.innerHTML = _stIntegrations(); break;
    default:             body.innerHTML = _stGeneral();
  }
}

function _stGeneral() {
  return `<div class="settings-grid">
    <div class="settings-card">
      <div class="settings-card-header">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
        <h3>Организация</h3>
      </div>
      <div class="form-group" style="margin-top:8px">
        <label style="font-size:0.8125rem;color:var(--text-secondary);font-weight:500">Название компании</label>
        <input type="text" value="ITAM Demo Corp" style="padding:8px 12px;background:var(--bg-input);border:1px solid var(--border-default);border-radius:var(--radius-sm);color:var(--text-primary);font-size:0.875rem;outline:none;width:100%;font-family:inherit" />
      </div>
      <div class="form-group">
        <label style="font-size:0.8125rem;color:var(--text-secondary);font-weight:500">Часовой пояс</label>
        <select style="padding:8px 12px;background:var(--bg-input);border:1px solid var(--border-default);border-radius:var(--radius-sm);color:var(--text-primary);font-size:0.875rem;outline:none;width:100%;font-family:inherit">
          <option>Europe/Moscow (UTC+3)</option>
          <option>Asia/Yekaterinburg (UTC+5)</option>
          <option>Asia/Novosibirsk (UTC+7)</option>
        </select>
      </div>
      <div class="settings-badge-row" style="margin-top:4px"><span class="badge badge-slate">UI-only</span></div>
    </div>
    <div class="settings-card">
      <div class="settings-card-header">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        <h3>Статистика</h3>
      </div>
      <div style="display:flex;flex-direction:column;gap:8px;margin-top:8px">
        <div style="display:flex;justify-content:space-between;font-size:0.875rem"><span style="color:var(--text-muted)">Активов в реестре</span><strong>${state.assets.length}</strong></div>
        <div style="display:flex;justify-content:space-between;font-size:0.875rem"><span style="color:var(--text-muted)">В архиве</span><strong>${state.archivedAssets.length}</strong></div>
        <div style="display:flex;justify-content:space-between;font-size:0.875rem"><span style="color:var(--text-muted)">Пользователей</span><strong>${state.users.length}</strong></div>
        <div style="display:flex;justify-content:space-between;font-size:0.875rem"><span style="color:var(--text-muted)">Локаций</span><strong>${state.locations.length}</strong></div>
      </div>
      <div class="settings-badge-row" style="margin-top:10px"><span class="badge badge-emerald">Backend Connected</span></div>
    </div>
    <div class="settings-card">
      <div class="settings-card-header">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>
        <h3>Версия системы</h3>
      </div>
      <p>ITAM Asset Management</p>
      <p style="margin-top:4px;font-size:0.75rem;color:var(--text-muted)">Pre-Release · asset-service v0.9 · Spring Boot 3</p>
      <div class="settings-badge-row" style="margin-top:8px">
        <span class="badge badge-amber">Pre-Release</span>
        <span class="badge badge-indigo">Spring Boot 3</span>
      </div>
    </div>
  </div>`;
}

function _stDicts() {
  const typesList = (state.assetTypes || []).map(t =>
    `<div style="display:flex;align-items:center;justify-content:space-between;padding:8px 12px;background:var(--bg-input);border-radius:var(--radius-sm);border:1px solid var(--border-subtle);margin-bottom:6px">
      <span style="font-size:0.875rem;color:var(--text-primary)">${escHtml(t.name)}</span>
      ${(() => { try { return JSON.parse(t.attributes||'{}').isPeripheral ? '<span class="badge badge-amber" style="font-size:0.7rem">Периферия</span>' : ''; } catch(e){return '';} })()}
    </div>`).join('') || '<span style="color:var(--text-muted);font-size:0.875rem">Нет пользовательских типов</span>';

  return `<div class="settings-grid" style="grid-template-columns:2fr 1fr">
    <div class="settings-card">
      <div class="settings-card-header" style="justify-content:space-between">
        <div style="display:flex;align-items:center;gap:10px">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
          <h3>Типы активов</h3>
        </div>
        <button class="btn-primary" style="font-size:0.8125rem;padding:6px 14px" onclick="openAssetTypesModal()">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Добавить
        </button>
      </div>
      <p style="font-size:0.8125rem;color:var(--text-muted);margin:8px 0 12px">Пользовательские категории оборудования. Системные типы (Ноутбук, ПК и т.д.) не отображаются.</p>
      <div>${typesList}</div>
    </div>
    <div class="settings-card">
      <div class="settings-card-header">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="10"/></svg>
        <h3>Статусы</h3>
      </div>
      <div style="display:flex;flex-direction:column;gap:6px;margin-top:8px">
        ${['ACTIVE','IN_REPAIR','IN_STORAGE','RETIRED'].map(s => `<div style="display:flex;align-items:center;gap:8px;padding:6px 10px;background:var(--bg-input);border-radius:var(--radius-sm)">${statusBadge(s)}</div>`).join('')}
      </div>
      <p style="font-size:0.75rem;color:var(--text-disabled);margin-top:10px">Системные (не редактируются)</p>
    </div>
  </div>`;
}

function _stUsers() {
  const usersHtml = state.users.map(u => {
    const dept = getDeptById(u.departmentId);
    const cnt = state.assets.filter(a => a.assignedUserId === u.id).length;
    const roleBadge = u.role === 'ADMIN'
      ? '<span class="badge badge-indigo">Admin</span>'
      : u.role === 'MANAGER'
        ? '<span class="badge badge-amber">Manager</span>'
        : '<span class="badge badge-slate">Employee</span>';
    return `<tr>
      <td style="padding:10px 16px">
        <div style="display:flex;align-items:center;gap:10px">
          <div style="width:32px;height:32px;border-radius:50%;background:linear-gradient(135deg,var(--accent),#0ea5e9);display:flex;align-items:center;justify-content:center;font-size:0.75rem;font-weight:700;color:#fff;flex-shrink:0">${escHtml(u.name.charAt(0))}</div>
          <div>
            <div style="font-size:0.875rem;font-weight:600;color:var(--text-primary)">${escHtml(u.name)}</div>
            <div style="font-size:0.75rem;color:var(--text-muted)">${dept ? escHtml(dept.name) : '—'}</div>
          </div>
        </div>
      </td>
      <td style="padding:10px 16px">${roleBadge}</td>
      <td style="padding:10px 16px;font-size:0.875rem;color:var(--text-secondary)">${cnt}</td>
      <td style="padding:10px 16px">
        <button class="btn-secondary" style="font-size:0.75rem;padding:4px 10px" onclick="openRolesModal()">Роли</button>
      </td>
    </tr>`;
  }).join('');

  return `<div style="padding:0 24px 24px">
    <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;padding-top:4px">
      <div>
        <div style="font-size:0.9375rem;font-weight:600;color:var(--text-primary)">Пользователи системы</div>
        <div style="font-size:0.75rem;color:var(--text-muted);margin-top:2px">Управление ролями и правами доступа</div>
      </div>
      <button class="btn-primary" style="font-size:0.8125rem" onclick="openRolesModal()">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
        Управление ролями
      </button>
    </div>
    <div class="asset-table-wrapper">
      <table class="asset-table">
        <thead><tr>
          <th>Пользователь</th><th>Роль</th><th>Активов</th><th>Действия</th>
        </tr></thead>
        <tbody>${usersHtml || '<tr><td colspan="4" style="text-align:center;color:var(--text-muted);padding:24px">Нет пользователей</td></tr>'}</tbody>
      </table>
    </div>
  </div>`;
}

function _stIntegrations() {
  const items = [
    { name:'Active Directory / LDAP', desc:'Синхронизация пользователей и групп из AD', icon:'<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>' },
    { name:'SSO (SAML 2.0)', desc:'Единый вход через корпоративный IdP', icon:'<rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>' },
    { name:'Jira Service Management', desc:'Создание задач из заявок ITAM', icon:'<path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/>' },
    { name:'Email / SMTP', desc:'Уведомления при назначении и изменении активов', icon:'<path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/>' },
  ];
  return `<div class="settings-grid">${items.map(i => `
    <div class="settings-card">
      <div class="settings-card-header">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">${i.icon}</svg>
        <h3>${i.name}</h3>
      </div>
      <p style="font-size:0.8125rem;color:var(--text-muted);line-height:1.5">${i.desc}</p>
      <div class="settings-badge-row" style="justify-content:space-between;margin-top:10px">
        <span class="badge badge-slate">Не настроено</span>
        <button class="btn-secondary" style="font-size:0.75rem;padding:4px 10px;opacity:0.5;cursor:not-allowed" disabled>Настроить</button>
      </div>
    </div>`).join('')}</div>`;
}

// ═══════════════════════════════════════════════════════════
// PHASE 2 — COLUMN PICKER
// ═══════════════════════════════════════════════════════════

const ALL_COLUMNS = [
  { id:'tag',      label:'Тег / Серийник' },
  { id:'type',     label:'Тип / Модель' },
  { id:'name',     label:'Имя устройства' },
  { id:'user',     label:'Ответственный' },
  { id:'location', label:'Локация' },
  { id:'status',   label:'Статус' },
  { id:'ip',       label:'IP-адрес' },
  { id:'sn',       label:'Серийный номер' },
];

function openColumnPicker() {
  const existing = document.getElementById('column-picker-dropdown');
  if (existing) { existing.remove(); return; }
  const btn = document.getElementById('btn-column-picker');
  if (!btn) return;
  const dropdown = document.createElement('div');
  dropdown.id = 'column-picker-dropdown';
  dropdown.className = 'column-picker-dropdown';
  dropdown.innerHTML = `<div class="column-picker-header">Колонки таблицы</div>
    ${ALL_COLUMNS.map(col => `<label class="column-picker-item">
      <input type="checkbox" value="${col.id}" ${state.visibleColumns.includes(col.id) ? 'checked' : ''} onchange="toggleColumn('${col.id}', this.checked)" />
      ${col.label}
    </label>`).join('')}
    <div class="column-picker-footer">
      <button class="btn-secondary" style="font-size:0.75rem;width:100%" onclick="resetColumns()">Сбросить по умолчанию</button>
    </div>`;
  const rect = btn.getBoundingClientRect();
  dropdown.style.position = 'fixed';
  dropdown.style.top = (rect.bottom + 6) + 'px';
  dropdown.style.right = (window.innerWidth - rect.right) + 'px';
  document.body.appendChild(dropdown);
  setTimeout(() => {
    document.addEventListener('click', function closeCP(e) {
      if (!dropdown.contains(e.target) && e.target !== btn) {
        dropdown.remove();
        document.removeEventListener('click', closeCP);
      }
    });
  }, 50);
}

function toggleColumn(colId, visible) {
  if (visible) {
    if (!state.visibleColumns.includes(colId)) state.visibleColumns.push(colId);
  } else {
    state.visibleColumns = state.visibleColumns.filter(c => c !== colId);
    if (state.visibleColumns.length === 0) state.visibleColumns = ['tag'];
  }
  if (state.viewMode === 'table') renderAssetsList();
}

function resetColumns() {
  state.visibleColumns = ['tag', 'type', 'name', 'user', 'location', 'status'];
  document.getElementById('column-picker-dropdown')?.remove();
  if (state.viewMode === 'table') renderAssetsList();
}

// ═══════════════════════════════════════════════════════════
// PHASE 2 — DETAIL PANE: REQUESTS TAB
// ═══════════════════════════════════════════════════════════

function switchDetailTabRequests(assetId) {
  state.activeDetailTab = 'requests';
  document.querySelectorAll('.detail-tab').forEach(el => {
    el.classList.toggle('active', el.dataset.tab === 'requests');
  });
  document.querySelectorAll('.detail-tab-panel').forEach(el => {
    el.classList.remove('active');
  });
  const panel = document.getElementById('tab-requests');
  if (panel) {
    panel.classList.add('active');
    panel.innerHTML = _renderDetailRequests(assetId);
  }
}

function _renderDetailRequests(assetId) {
  const asset = getAssetById(assetId) || state.archivedAssets.find(a => a.id === assetId);
  if (!asset) return '<p style="color:var(--text-muted);padding:16px">Актив не найден</p>';
  const related = state.requests.filter(r =>
    r.assetId === assetId ||
    (r.description && r.description.includes(asset.assetTag)) ||
    (asset.serialNumber && r.description && r.description.includes(asset.serialNumber))
  );
  if (related.length === 0) {
    return `<div class="empty-state" style="padding:32px 16px">
      <div class="empty-icon"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div>
      <p>Заявок по этому активу нет</p>
    </div>`;
  }
  return related.map(r => {
    const user = getUserById(r.userId);
    const sMap = { 'NEW':'badge-amber', 'IN_PROGRESS':'badge-indigo', 'RESOLVED':'badge-emerald', 'REJECTED':'' };
    const sLabel = { 'NEW':'Новая', 'IN_PROGRESS':'В работе', 'RESOLVED':'Выполнена', 'REJECTED':'Отклонена' };
    return `<div style="padding:12px;background:var(--bg-input);border-radius:var(--radius-sm);border:1px solid var(--border-subtle);margin-bottom:8px;animation:fadeIn 0.2s ease both">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px">
        <span style="font-size:0.8125rem;font-weight:600;color:var(--text-primary)">${escHtml(r.type || 'Заявка')}</span>
        <span class="badge ${sMap[r.status] || 'badge-slate'}">${sLabel[r.status] || r.status}</span>
      </div>
      <p style="font-size:0.8125rem;color:var(--text-secondary);line-height:1.5">${escHtml(r.description || '—')}</p>
      ${user ? `<div style="font-size:0.75rem;color:var(--text-muted);margin-top:6px">От: ${escHtml(user.name)}</div>` : ''}
    </div>`;
  }).join('');
}

// ═══════════════════════════════════════════════════════════
// PHASE 2 — ENHANCED DASHBOARD
// ═══════════════════════════════════════════════════════════

function renderDashboardEnhanced() {
  const el = document.getElementById('dashboard-enhanced');
  if (!el) return;

  const assigned   = state.assets.filter(a => a.assignedUserId).length;
  const unassigned = state.assets.filter(a => !a.assignedUserId).length;
  const inRepair   = state.assets.filter(a => a.status === 'IN_REPAIR').length;
  const inStorage  = state.assets.filter(a => a.status === 'IN_STORAGE').length;
  const retired    = state.assets.filter(a => a.status === 'RETIRED').length;
  const archived   = state.archivedAssets.length;
  const openReqs   = state.requests.filter(r => r.status === 'NEW' || r.status === 'IN_PROGRESS').length;
  const total      = state.assets.length || 1;
  const aPct       = (assigned / total) * 100;
  const uPct       = (unassigned / total) * 100;

  // Top locations
  const locCounts = {};
  state.assets.forEach(a => {
    if (a.locationId) {
      const loc = getLocById(a.locationId);
      const n = loc ? loc.name : a.locationId;
      locCounts[n] = (locCounts[n] || 0) + 1;
    }
  });
  const topLocs = Object.entries(locCounts).sort((a,b) => b[1]-a[1]).slice(0, 5);
  const maxCnt = topLocs[0]?.[1] || 1;

  // Type distribution
  const typeCounts = {};
  state.assets.forEach(a => { const t = typeLabel(a.assetType); typeCounts[t] = (typeCounts[t]||0)+1; });
  const topTypes = Object.entries(typeCounts).sort((a,b) => b[1]-a[1]).slice(0, 5);

  el.innerHTML = `
  <div class="dash-kpi-row">
    <div class="dash-kpi-card" onclick="navigateTo('all-assets')" style="cursor:pointer">
      <div class="dash-kpi-icon dash-kpi-blue"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg></div>
      <div class="dash-kpi-value">${state.assets.length}</div>
      <div class="dash-kpi-label">Всего активов</div>
      <div class="dash-kpi-sub">${inRepair} в ремонте · ${inStorage} на складе</div>
    </div>
    <div class="dash-kpi-card">
      <div class="dash-kpi-icon dash-kpi-green"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg></div>
      <div class="dash-kpi-value">${assigned}</div>
      <div class="dash-kpi-label">Назначено</div>
      <div class="dash-kpi-sub">${unassigned} без владельца</div>
    </div>
    <div class="dash-kpi-card" onclick="navigateTo('requests')" style="cursor:pointer">
      <div class="dash-kpi-icon dash-kpi-amber"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div>
      <div class="dash-kpi-value">${openReqs}</div>
      <div class="dash-kpi-label">Открытых заявок</div>
      <div class="dash-kpi-sub">${state.requests.length} всего</div>
    </div>
    <div class="dash-kpi-card" onclick="navigateTo('archive')" style="cursor:pointer">
      <div class="dash-kpi-icon dash-kpi-rose"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/></svg></div>
      <div class="dash-kpi-value">${archived}</div>
      <div class="dash-kpi-label">В архиве</div>
      <div class="dash-kpi-sub">${retired} списано (active)</div>
    </div>
  </div>

  <div class="dash-charts-row">
    <div class="dash-chart-card">
      <div class="dash-chart-title">Назначено / Свободно</div>
      <div style="display:flex;align-items:center;gap:20px">
        <svg viewBox="0 0 36 36" style="width:110px;height:110px;transform:rotate(-90deg);flex-shrink:0">
          <circle r="15.9" cx="18" cy="18" fill="none" stroke="var(--bg-card)" stroke-width="3.1"/>
          <circle r="15.9" cx="18" cy="18" fill="none" stroke="var(--accent)" stroke-width="3.1"
            stroke-dasharray="${aPct.toFixed(1)} ${(100-aPct).toFixed(1)}" />
          <circle r="15.9" cx="18" cy="18" fill="none" stroke="var(--rose)" stroke-width="3.1"
            stroke-dasharray="${uPct.toFixed(1)} ${(100-uPct).toFixed(1)}" stroke-dashoffset="${(-aPct).toFixed(1)}"/>
        </svg>
        <div style="display:flex;flex-direction:column;gap:8px">
          <div style="display:flex;align-items:center;gap:8px;font-size:0.8125rem">
            <span style="width:10px;height:10px;background:var(--accent);border-radius:2px;flex-shrink:0"></span>
            <span style="color:var(--text-secondary)">Назначено</span>
            <span style="font-weight:700;color:var(--text-primary);margin-left:8px">${assigned}</span>
          </div>
          <div style="display:flex;align-items:center;gap:8px;font-size:0.8125rem">
            <span style="width:10px;height:10px;background:var(--rose);border-radius:2px;flex-shrink:0"></span>
            <span style="color:var(--text-secondary)">Свободно</span>
            <span style="font-weight:700;color:var(--text-primary);margin-left:8px">${unassigned}</span>
          </div>
          <div style="display:flex;align-items:center;gap:8px;font-size:0.8125rem">
            <span style="width:10px;height:10px;background:var(--amber);border-radius:2px;flex-shrink:0"></span>
            <span style="color:var(--text-secondary)">В ремонте</span>
            <span style="font-weight:700;color:var(--text-primary);margin-left:8px">${inRepair}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="dash-chart-card" style="flex:1.5">
      <div class="dash-chart-title">Топ локаций</div>
      ${topLocs.length === 0
        ? '<span style="color:var(--text-muted);font-size:0.875rem">Нет данных</span>'
        : topLocs.map(([name, count]) => `
          <div style="margin-bottom:10px">
            <div style="display:flex;justify-content:space-between;font-size:0.8rem;color:var(--text-secondary);margin-bottom:4px">
              <span style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:70%">${escHtml(name)}</span>
              <span style="font-weight:600;color:var(--text-primary)">${count}</span>
            </div>
            <div style="height:6px;background:var(--bg-base);border-radius:3px;overflow:hidden">
              <div style="height:100%;width:${Math.round(count/maxCnt*100)}%;background:linear-gradient(90deg,var(--accent),#0ea5e9);border-radius:3px;transition:width 0.6s ease"></div>
            </div>
          </div>`).join('')}
    </div>

    <div class="dash-chart-card">
      <div class="dash-chart-title">По типу</div>
      ${topTypes.length === 0
        ? '<span style="color:var(--text-muted);font-size:0.875rem">Нет данных</span>'
        : topTypes.map(([name, count]) => `
          <div style="display:flex;align-items:center;justify-content:space-between;padding:7px 0;border-bottom:1px solid var(--border-subtle)">
            <span style="font-size:0.8125rem;color:var(--text-secondary);overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${escHtml(name)}</span>
            <span class="badge badge-slate">${count}</span>
          </div>`).join('')}
    </div>
  </div>

  <div style="padding:0 24px 24px">
    <div class="dash-chart-card">
      <div class="dash-chart-title">Последние активы</div>
      <div class="recent-list">
        ${[...state.assets].sort((a,b) => b.id.localeCompare(a.id)).slice(0,6).map(a => {
          const u = getUserById(a.assignedUserId);
          return `<div class="recent-item" onclick="navigateTo('all-assets');selectAsset('${a.id}')" style="cursor:pointer">
            <div class="asset-card-icon ${typeIconClass(a.assetType)}" style="width:34px;height:34px">${typeIcon(a.assetType)}</div>
            <div style="flex:1;min-width:0">
              <div style="font-weight:600;font-size:0.875rem;color:var(--text-primary);overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${escHtml(a.displayName || a.assetTag)}</div>
              <div style="font-size:0.75rem;color:var(--text-muted)">${typeLabel(a.assetType)} · ${escHtml(a.serialNumber || a.assetTag)}</div>
            </div>
            ${statusBadge(a.status)}
            <span style="font-size:0.75rem;color:var(--text-muted);white-space:nowrap">${u ? escHtml(u.name) : '—'}</span>
          </div>`;
        }).join('') || '<span style="color:var(--text-muted)">Нет активов</span>'}
      </div>
    </div>
  </div>`;
}

// Hook into existing renderDashboard
const __origRenderDashboard = typeof renderDashboard === 'function' ? renderDashboard : null;
window.renderDashboard = function() {
  if (__origRenderDashboard) __origRenderDashboard();
  renderDashboardEnhanced();
};

function openAssetTypesModal() {
  document.getElementById('add-asset-type-form').reset();
  document.getElementById('add-asset-type-modal').classList.remove('hidden');
}

async function confirmAddAssetType() {
  const name = document.getElementById('new-type-name').value.trim();
  const isPeripheral = document.getElementById('new-type-is-peripheral').checked;
  if (!name) return;

  const payload = {
    name: name,
    attributes: JSON.stringify({ isPeripheral })
  };

  try {
    const res = await fetch(`${API_BASE}/api/v1/asset-types`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error('API Error');
    const created = await res.json();
    state.assetTypes = state.assetTypes || [];
    state.assetTypes.push(toCamel(created));
    document.getElementById('add-asset-type-modal').classList.add('hidden');
    showToast('Тип устройства добавлен', 'success');
    renderSettingsSection('dictionaries');
  } catch (e) {
    console.error(e);
    showToast('Ошибка добавления типа', 'error');
  }
}

let _attachPeripheralMainId = null;
function openAttachPeripheralModal(mainAssetId) {
  _attachPeripheralMainId = mainAssetId;
  // Create modal dynamically if it doesn't exist
  if (!document.getElementById('attach-peripheral-modal-overlay')) {
    const modalHtml = `
      <div class="modal-overlay hidden" id="attach-peripheral-modal-overlay">
        <div class="modal" style="max-width: 400px;">
          <div class="modal-header">
            <h3 class="modal-title">Привязать периферию</h3>
            <button class="modal-close" onclick="document.getElementById('attach-peripheral-modal-overlay').classList.add('hidden')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>Выберите актив</label>
              <select id="attach-peripheral-select" style="width:100%;">
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-secondary" onclick="document.getElementById('attach-peripheral-modal-overlay').classList.add('hidden')">Отмена</button>
            <button class="btn-primary" onclick="confirmAttachPeripheral()">Привязать</button>
          </div>
        </div>
      </div>
    `;
    document.body.insertAdjacentHTML('beforeend', modalHtml);

  }

  const select = document.getElementById('attach-peripheral-select');
  // Filter assets that are available peripherals
  // An asset is a peripheral if its assetType has isPeripheral=true in attributes (or we just allow ANY asset that is not already attached to something else)
  // To be safe, let's allow attaching ANY asset that has parentAssetId == null and is not the main asset itself.
  const available = state.assets.filter(a => a.id !== mainAssetId && !a.parentAssetId && a.status !== 'RETIRED');

  select.innerHTML = '<option value="">-- Выберите актив --</option>' + available.map(a => `<option value="${a.id}">${escHtml(a.displayName || a.assetTag)} (${typeLabel(a.assetType)})</option>`).join('');

  document.getElementById('attach-peripheral-modal-overlay').classList.remove('hidden');
}


async function confirmAttachPeripheral() {
  const periphId = document.getElementById('attach-peripheral-select').value;
  if (!periphId || !_attachPeripheralMainId) return;

  const asset = state.assets.find(a => a.id === periphId);
  if (!asset) return;

  const payload = { ...asset, parentAssetId: _attachPeripheralMainId };

  try {
    const res = await fetch(`${API_BASE}/api/v1/assets/${periphId}`, {
      method: 'PATCH',
      headers: getHeaders(),
      body: JSON.stringify(toSnake(payload))
    });
    if (!res.ok) throw new Error('API Error');
    const updated = await res.json();

    // Update local state
    const idx = state.assets.findIndex(a => a.id === periphId);
    if (idx >= 0) state.assets[idx] = toCamel(updated);

    document.getElementById('attach-peripheral-modal-overlay').classList.add('hidden');
    showToast('Периферия привязана', 'success');

    renderDetailPane(_attachPeripheralMainId);

  } catch(e) {
    console.error(e);
    showToast('Ошибка привязки периферии', 'error');
  }
}

async function detachPeripheral(periphId, parentId) {
  if (!confirm('Отвязать это устройство?')) return;
  try {
    const res = await fetch(`${API_BASE}/api/v1/assets/${periphId}`, {
      method: 'PATCH',
      headers: getHeaders(),
      body: JSON.stringify({ parent_asset_id: null })
    });
    if (!res.ok) throw new Error('API Error');
    const updated = await res.json();
    const idx = state.assets.findIndex(a => a.id === periphId);
    if (idx >= 0) state.assets[idx] = toCamel(updated);
    showToast('Устройство отвязано', 'success');
    renderDetailPane(parentId);
  } catch(e) {
    showToast('Ошибка при отвязывании', 'error');
  }
}

let _createPeripheralParentId = null;
function openCreatePeripheralModal(parentId) {
  _createPeripheralParentId = parentId;
  const modalHtml = `
    <div class="modal-overlay" id="create-peripheral-modal-overlay">
      <div class="modal" style="max-width: 450px;">
        <div class="modal-header">
          <h3 class="modal-title">Создать новую периферию</h3>
          <button class="modal-close" onclick="document.getElementById('create-peripheral-modal-overlay').remove()">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Тип устройства</label>
            <select id="new-periph-type" style="width:100%;">
              ${state.assetTypes.filter(t => { try { return JSON.parse(t.attributes).isPeripheral; } catch(e){return false;} }).map(t => `<option value="${t.name}">${escHtml(t.name)}</option>`).join('')}
            </select>
          </div>
          <div class="form-group">
            <label>Отображаемое имя (например, Мышь Logitech)</label>
            <input type="text" id="new-periph-name" />
          </div>
          <div class="form-group">
            <label>Серийный номер</label>
            <input type="text" id="new-periph-sn" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" onclick="document.getElementById('create-peripheral-modal-overlay').remove()">Отмена</button>
          <button class="btn-primary" onclick="confirmCreatePeripheral()">Создать и привязать</button>
        </div>
      </div>
    </div>
  `;
  if(document.getElementById('create-peripheral-modal-overlay')) document.getElementById('create-peripheral-modal-overlay').remove();
  document.body.insertAdjacentHTML('beforeend', modalHtml);
}


async function confirmCreatePeripheral() {
  const type = document.getElementById('new-periph-type').value;
  const name = document.getElementById('new-periph-name').value.trim();
  const sn = document.getElementById('new-periph-sn').value.trim();

  if (!name || !type) return showToast('Заполните имя и тип', 'error');

  const parent = state.assets.find(a => a.id === _createPeripheralParentId);
  if(!parent) return;

  const payload = {
    tenant_id: parent.tenantId,
    asset_tag: 'P-' + Math.floor(Math.random()*10000),
    asset_type: type,
    status: 'ACTIVE',
    serial_number: sn,
    display_name: name,
    assigned_user_id: parent.assignedUserId, // Inherit user
    location_id: parent.locationId, // Inherit location
    department_id: parent.departmentId, // Inherit department
    parent_asset_id: _createPeripheralParentId
  };

  try {
    const res = await fetch(`${API_BASE}/api/v1/assets`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload)
    });
    if(!res.ok) throw new Error('API Error');
    const created = await res.json();
    state.assets.unshift(toCamel(created));
    document.getElementById('create-peripheral-modal-overlay').remove();
    showToast('Периферия создана', 'success');

    renderDetailPane(_createPeripheralParentId);
  } catch(e) {
    showToast('Ошибка создания', 'error');
  }
}

function renderPeripheralsSection() {
  const section = document.getElementById('section-peripherals');
  if (!section) return;

  const peripherals = state.assets.filter(a => state.assetTypes.find(t => t.name === a.assetType && (() => { try { return JSON.parse(t.attributes).isPeripheral; } catch(e) { return false; } })()));

  section.innerHTML = `
    <div class="section-header" style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
      <div>
        <h2 style="font-size:1.25rem;font-weight:700;color:var(--text-primary);margin:0;">Периферия</h2>
        <p style="color:var(--text-muted);font-size:0.875rem;margin:4px 0 0;">Всего периферийных устройств: ${peripherals.length}</p>
      </div>
      <button class="btn-primary" style="padding:6px 14px;font-size:0.875rem;" onclick="openCreatePeripheralModal(null)">+ Добавить устройство</button>
    </div>
    ${peripherals.length === 0 ? `
      <div class="empty-state" style="padding:60px 20px;text-align:center;">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" style="color:var(--text-muted);margin-bottom:16px;"><rect x="2" y="8" width="20" height="14" rx="2"/><path d="M6 8V6a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2"/></svg>
        <p style="color:var(--text-muted);font-size:0.9rem;">Периферия не найдена</p>
      </div>
    ` : `
      <div class="table-container">
        <table class="table">
          <thead><tr>
            <th>Тег / S/N</th><th>Тип</th><th>Имя</th><th>Статус</th><th>Привязан к</th><th>Сотрудник</th>
          </tr></thead>
          <tbody>
            ${peripherals.map(p => {
              const parent = p.parentAssetId ? state.assets.find(a => a.id === p.parentAssetId) : null;
              const user = p.assignedUserId ? getUserById(p.assignedUserId) : null;
              return `<tr style="cursor:pointer;" onclick="selectAsset('${p.id}')">
                <td><div style="font-weight:600;">${escHtml(p.assetTag)}</div><div style="font-size:0.75rem;color:var(--text-muted);">${escHtml(p.serialNumber || '—')}</div></td>
                <td>${escHtml(p.assetType)}</td>
                <td>${escHtml(p.displayName || '—')}</td>
                <td>${statusBadge(p.status)}</td>
                <td>${parent ? escHtml(parent.displayName || parent.assetTag) : '<span style="color:var(--text-disabled)">Свободно</span>'}</td>
                <td>${user ? escHtml(user.name) : '—'}</td>
              </tr>`;
            }).join('')}
          </tbody>
        </table>
      </div>
    `}
  `;
}

let _currentQrData = null;
function openQrPrintModal(assetTag, qrData, assetType, displayName, userName, locName) {
  _currentQrData = qrData;
  _currentQrText = qrData;
  _currentQrAssetTag = assetTag;
  const modalHtml = `
    <div class="modal-overlay hidden" id="qr-print-modal">
      <div class="modal-content" style="max-width: 420px; text-align:center;">
        <div class="modal-header">
          <h3 class="modal-title">QR-код и этикетка</h3>
          <button class="btn-icon" onclick="document.getElementById('qr-print-modal').remove()">&times;</button>
        </div>
        <div class="modal-body">
          <div id="print-label-area" style="border:1px solid #e2e8f0; padding:16px; margin:0 auto; width: 300px; background:#fff; color:#000; font-family:sans-serif; text-align:left; border-radius:8px;">
            <div style="display:flex; align-items:center; gap:16px;">
              <div id="print-qr-code" style="width:100px; height:100px; flex-shrink:0;"></div>
              <div style="flex:1;">
                <div style="font-weight:bold; font-size:1.1rem; margin-bottom:4px;">${escHtml(assetTag)}</div>
                <div style="font-size:0.8rem; margin-bottom:2px;"><strong>Тип:</strong> ${escHtml(assetType)}</div>
                <div style="font-size:0.8rem; margin-bottom:2px;"><strong>Имя:</strong> ${escHtml(displayName)}</div>
                ${userName ? `<div style="font-size:0.8rem; margin-bottom:2px;"><strong>Ответственный:</strong> ${escHtml(userName)}</div>` : ''}
                ${locName ? `<div style="font-size:0.8rem;"><strong>Локация:</strong> ${escHtml(locName)}</div>` : ''}
              </div>
            </div>
          </div>
          <div style="margin-top:12px; font-family:monospace; font-size:0.85rem; color:var(--text-secondary);">${escHtml(assetTag)}</div>
        </div>
        <div class="modal-footer" style="justify-content:center; gap:8px; flex-wrap:wrap;">
          <button class="btn-primary" onclick="printQrLabel()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px;"><polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/></svg>
            Распечатать
          </button>
          <button class="btn-secondary" onclick="downloadQrPng()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px;"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            Скачать PNG
          </button>
          <button class="btn-secondary" onclick="downloadQrSvg()">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px;"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
            Скачать SVG
          </button>
        </div>
      </div>
    </div>
  `;
  if(document.getElementById('qr-print-modal')) document.getElementById('qr-print-modal').remove();
  document.body.insertAdjacentHTML('beforeend', modalHtml);
  document.getElementById('qr-print-modal').classList.remove('hidden');

  // Deterministic QR — same image every time, with onerror fallback
  const qb = document.getElementById('print-qr-code');
  if (qb) {
    const qrImgUrl = `https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=${encodeURIComponent(qrData)}&margin=4&color=0f172a&bgcolor=ffffff`;
    const img = document.createElement('img');
    img.src = qrImgUrl;
    img.style.cssText = 'width:100px;height:100px;display:block;';
    img.alt = `QR ${qrData}`;
    img.onerror = function() {
      // Fallback: показать текст если изображение не загрузилось
      qb.innerHTML = `<div style="width:100px;height:100px;display:flex;align-items:center;justify-content:center;border:2px dashed #ccc;font-size:0.65rem;text-align:center;color:#666;padding:4px;box-sizing:border-box;">${escHtml(qrData)}</div>`;
    };
    qb.innerHTML = '';
    qb.appendChild(img);
  }
}

function printQrLabel() {
  const content = document.getElementById('print-label-area').innerHTML;
  const printWindow = window.open('', '', 'height=500, width=500');
  printWindow.document.write('<html><head><title>Print Label</title>');
  printWindow.document.write('<style>body { font-family: sans-serif; margin: 0; padding: 20px; display: flex; justify-content: center; } @media print { @page { size: auto; margin: 0mm; } }</style>');
  printWindow.document.write('</head><body>');
  printWindow.document.write('<div style="border: 2px solid #000; padding: 10px; width: 350px;">' + content + '</div>');
  printWindow.document.write('</body></html>');
  printWindow.document.close();
  setTimeout(() => {
    printWindow.print();
  }, 250);
}
