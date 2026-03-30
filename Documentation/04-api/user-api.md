# Asset API

Status: draft
Owner: architecture
Last Updated: 2026-03-27

## Purpose
Минимальный API-контракт Foundation Slice для asset-service.

## Scope
Этот документ покрывает только Asset CRUD v1 для мастер-записи `Asset`.

## API Principles
1. Все API versioned.
2. asset-service владеет bounded context Asset Management.
3. tenant isolation обязателен.
4. Прямой доступ к БД user-service запрещен.
5. Валидация `assigned_user_id` и `location_id`, если они передаются, выполняется через API user-service.
6. Критичные write-операции должны отправляться в audit по отдельному контракту.
7. Generic CRUD v1 не должен ломать доменный инвариант истории.

## Service Ownership
asset-service в Foundation Slice v1 владеет:
- Asset

Следующие доменные концепты существуют, но их полноценные workflow/API не входят в этот документ:
- AssetAssignment
- AssetTransfer
- AssetRetirement

## Endpoints v1
- `POST /api/v1/assets`
- `GET /api/v1/assets`
- `GET /api/v1/assets/{id}`
- `PATCH /api/v1/assets/{id}`

## Confirmed Asset Fields Relevant To API
- `asset_tag`
- `serial_number`
- `asset_type`
- `status`
- `assigned_user_id`
- `location_id`

## Business Rules
- актив принадлежит tenant;
- `asset_tag` уникален внутри tenant;
- история назначения и перемещений не должна теряться;
- `assigned_user_id` и `location_id` являются cross-service reference IDs;
- cross-service reference IDs должны валидироваться через API user-service;
- generic update нельзя трактовать как право свободно переписывать любые поля без доменных последствий.

## Write Operations Requiring Audit
Следующие write-операции считаются критичными и должны попадать в audit:
- create asset
- update asset

## Restrictions
- `DELETE /api/v1/assets/{id}` не включается в v1;
- в этом документе не фиксируется, что `PATCH /api/v1/assets/{id}` может менять `assigned_user_id` и `location_id` как обычные поля без отдельного domain decision;
- assignment / transfer / return не считаются частью generic CRUD v1.

## Not Fixed In This Document
Намеренно не фиксируются:
- request body fields;
- response body fields;
- error model;
- pagination/filter parameter names;
- delete semantics;
- отдельные endpoints для assignment / transfer / return;
- asset history storage model;
- event contract;
- audit payload format.

## Out of Scope
- `DELETE /api/v1/assets/{id}`
- `POST /api/v1/assets/{id}/assign`
- `POST /api/v1/assets/{id}/transfer`
- `POST /api/v1/assets/{id}/return`
- `POST /api/v1/assets/{id}/retire`
- bulk import/export
- attachment API
- barcode / QR workflows

## Open Questions
- можно ли generic PATCH менять `assigned_user_id` и `location_id`;
- когда появляются отдельные API для AssetAssignment / AssetTransfer / AssetRetirement;
- какая delete semantics будет принята: hard delete / soft delete / no delete / retire only;
- какая именно audit integration contract будет утверждена;
- какие поля входят в payload Asset v1 сверх уже подтвержденного минимума.


# User API

Status: draft
Owner: architecture
Last Updated: 2026-03-27

## Purpose
Минимальный API-контракт Foundation Slice для user-service.

## Scope
Этот документ покрывает только API user-service для:
- Tenant
- User
- Role
- Department
- Branch
- Location

## API Principles
1. Все API versioned.
2. user-service владеет bounded context Identity / Organization.
3. tenant isolation обязателен.
4. Прямой доступ к БД другого сервиса запрещен.
5. Критичные write-операции должны отправляться в audit по отдельному контракту.
6. Этот документ фиксирует только минимальные endpoints v1 и не фиксирует payload schema, пока она не подтверждена отдельными docs/ADR.

## Service Ownership
user-service владеет:
- Tenant
- User
- Role
- Department
- Branch
- Location

Permissions не включаются в минимальный API v1, потому что они упомянуты в bounded context, но не раскрыты в текущих domain docs.

## Endpoints v1

### Tenants
- `POST /api/v1/tenants`
- `GET /api/v1/tenants/{id}`

### Users
- `POST /api/v1/users`
- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}`

### Roles
- `POST /api/v1/roles`
- `GET /api/v1/roles`
- `GET /api/v1/roles/{id}`
- `PATCH /api/v1/roles/{id}`

### Departments
- `POST /api/v1/departments`
- `GET /api/v1/departments`
- `GET /api/v1/departments/{id}`
- `PATCH /api/v1/departments/{id}`

### Branches
- `POST /api/v1/branches`
- `GET /api/v1/branches`
- `GET /api/v1/branches/{id}`
- `PATCH /api/v1/branches/{id}`

### Locations
- `POST /api/v1/locations`
- `GET /api/v1/locations`
- `GET /api/v1/locations/{id}`
- `PATCH /api/v1/locations/{id}`

## Business Rules
- все сущности user-service считаются tenant-scoped;
- Tenant является верхней tenant boundary;
- User является tenant-scoped actor;
- Role является tenant-scoped role;
- Department / Branch / Location являются tenant-scoped org references;
- активы и другие домены должны ссылаться на user/org structure через API user-service, а не через shared DB.

## Write Operations Requiring Audit
Следующие write-операции считаются критичными и должны попадать в audit:
- create tenant
- create/update user
- create/update role
- create/update department
- create/update branch
- create/update location

## Not Fixed In This Document
Намеренно не фиксируются:
- request body fields;
- response body fields;
- error model;
- pagination/filter parameter names;
- tenant resolution mechanism на API boundary;
- RBAC permission model;
- кардинальность User ↔ Role;
- org hierarchy между Department / Branch / Location.

## Out of Scope
- Permissions API
- invitation / onboarding API
- authentication / identity provider integration API
- массовые bulk-операции
- delete endpoints

## Open Questions
- нужна ли отдельная Permissions API в MVP;
- как именно реализуется User ↔ Role;
- нужна ли жесткая org hierarchy;
- нужен ли `GET /api/v1/tenants` в v1;
- какие поля входят в payload для каждой сущности.