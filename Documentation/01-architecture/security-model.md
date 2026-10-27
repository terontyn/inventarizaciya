# Security Model

Status: approved
Owner: architecture
Last Updated: 2026-04-02

## Purpose
Зафиксировать текущую security-модель Foundation Slice и MVP Pilot Readiness на уровне tenant isolation, role-based access и pilot RBAC cutline.

## Scope
Этот документ покрывает только:
- tenant-scoped access model;
- role-based access model для MVP;
- pilot RBAC cutline для первого tenant;
- security boundaries, которые уже обязательны для текущего MVP contour.

Этот документ не покрывает:
- permission storage design;
- JWT claims design;
- ACL tables;
- ABAC / policy engine;
- direct user permissions;
- enterprise-grade RBAC redesign.

## Current Decisions
- модель авторизации: tenant-scoped RBAC;
- User ↔ Role = many-to-many;
- permissions назначаются только ролям;
- direct user permissions не используются в Foundation Slice;
- ABAC / policy engine не входит в MVP;
- tenant isolation обязателен для всех reads / writes;
- cross-tenant visibility и cross-tenant administration запрещены.

## Asset Security Boundary
- generic PATCH Asset не меняет `assigned_user_id` и `location_id`;
- responsibility-changing operations выполняются только через dedicated flows;
- create / update / assign / transfer / return / retire относятся к critical writes и должны попадать в audit;
- `DELETE /api/v1/assets/{id}` не входит в v1.

## Pilot RBAC Cutline

Для MVP Pilot Readiness используется минимальный tenant-scoped RBAC cutline.

### Pilot Personas
- Tenant Admin
- Asset Operator
- Support Reviewer

### Minimum Permission Matrix

| Capability | Tenant Admin | Asset Operator | Support Reviewer |
|---|---|---|---|
| View users / departments / branches / locations | Yes | Reference read-only | Read-only |
| View roles | Yes | No | No |
| Manage users | Yes | No | No |
| Manage roles | Yes | No | No |
| Manage departments / branches / locations | Yes | No | No |
| View asset registry / asset card | Yes | Yes | Yes |
| Create asset | Yes | Yes | No |
| Update asset master via CRUD v1 | Yes | Yes | No |
| Assign asset | Yes | Yes | No |
| Transfer asset | Yes | Yes | No |
| Return asset | Yes | Yes | No |
| View history | Yes | Yes | Yes |
| View audit | Yes | Yes | Yes |
| Retire asset | No in default pilot cutline | No | No |
| Delete asset | No | No | No |

### Pilot Permission Rules
- все permissions остаются tenant-scoped;
- permissions назначаются только ролям;
- direct user permissions не используются;
- ABAC / policy engine не входят в MVP;
- responsibility/location change via generic PATCH запрещено;
- responsibility-changing operations выполняются только через dedicated flows;
- `retire` не входит в обязательный default pilot role set.

### Explicit Forbidden For Pilot
- cross-tenant visibility or administration;
- direct user permissions;
- ABAC / policy engine;
- responsibility/location change via generic PATCH;
- asset DELETE in pilot v1;
- Support Reviewer data mutation;
- Asset Operator management of users / roles / org dictionaries.

## Pilot Readiness Note
Pilot RBAC cutline нужен только для current MVP contour:
- users / roles / departments / branches / locations;
- asset registry / asset card;
- create / update asset в границах CRUD v1;
- assign / transfer / return;
- history / audit visibility.

Этот cutline не расширяет MVP в inventory / warehouse, SAM, maintenance, reporting module или другие post-MVP направления.

`Retirement` присутствует в доменном каноне, но не входит в обязательный pilot-ready permission set по умолчанию.

## Pilot Reference-Data Ownership Clarification

Для первого pilot tenant reference-data ownership намеренно минимален и остается внутри текущего tenant-scoped RBAC cutline.

### Ownership by persona
- Tenant Admin владеет созданием и поддержкой:
  - roles
  - users
  - departments
  - branches
  - locations
- Asset Operator не управляет users / roles / org dictionaries;
- Asset Operator использует только разрешенный reference read-only access;
- Support Reviewer остается read-only и не мутирует reference data.

### Setup Order
Минимальный порядок подготовки reference data before pilot start:
1. roles
2. pilot users with role assignment
3. branches
4. locations
5. departments

### Flow Preconditions
- before first asset create:
  - существует tenant-scoped actor с правом Create asset;
  - role-based access already configured;
  - любые `assigned_user_id` / `location_id`, если они реально передаются на create-path, уже существуют в tenant scope;
- before first assign:
  - существует Asset;
  - существует target User в том же tenant;
  - actor имеет Assign permission;
- before first transfer:
  - существует Asset;
  - существует target User и/или target Location в зависимости от transfer scenario;
  - actor имеет Transfer permission;
- before first return:
  - существует Asset;
  - существует active current user responsibility;
  - actor имеет Return permission.

### Explicit Exclusions
- reference-data ownership model не вводит новый master-data module;
- reference-data ownership model не вводит bulk import / migration engine;
- asset-side extra catalogs не являются обязательной частью pilot setup;
- `departments` входят в minimum tenant-managed set, но не являются hard precondition для каждого первого flow.

## Open After Pilot
После пилота могут оставаться открытыми:
- finer-grained admin permissions внутри Tenant Admin;
- отдельное разбиение permissions для assign / transfer / return;
- отдельное разбиение view history и view audit;
- retirement permission и retirement readiness cutline;
- exact permission keys и их физическая модель хранения.

## Out of Scope
- direct user permission overrides;
- ABAC conditions;
- policy engine rules;
- enterprise-grade RBAC redesign;
- permission storage schema;
- JWT claims design;
- ACL tables.