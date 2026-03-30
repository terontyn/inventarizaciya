# User API

Status: draft
Owner: architecture
Last Updated: 2026-03-30

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
4. Tenant определяется на API boundary из аутентифицированного контекста.
5. Сервис не принимает tenant как свободный бизнес-параметр.
6. Прямой доступ к БД другого сервиса запрещен.
7. Критичные write-операции должны попадать в audit по принятому audit contract.
8. Этот документ фиксирует только минимальные endpoints v1 и не фиксирует payload schema, пока она не подтверждена отдельными docs/ADR.

## Service Ownership
user-service владеет:
- Tenant
- User
- Role
- Department
- Branch
- Location

Clarifications:
- отдельная сущность `Org` в Foundation Slice не поддерживается;
- permissions являются частью role model, но отдельная `Permissions API` в v1 не вводится;
- direct user permissions в Foundation Slice не поддерживаются;
- `Department` — организационная / управленческая ось;
- `Branch` и `Location` — физическая ось;
- `Location` принадлежит `Branch`;
- `User` может иметь reference на `Department`;
- обязательная прямая привязка `User` к `Branch / Location` в MVP не требуется.

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
- все сущности user-service, кроме самой tenant boundary, считаются tenant-scoped;
- `User ↔ Role` = many-to-many;
- permissions назначаются только ролям;
- cross-tenant references недопустимы;
- активы и другие домены должны ссылаться на user/org structure через API user-service, а не через shared DB.

## Write Operations Requiring Audit
Следующие write-операции считаются критичными и должны попадать в audit:
- create tenant
- create/update user
- create/update role
- create/update department
- create/update branch
- create/update location

## Canonical Payload Minimum for Slice 1

Этот раздел фиксирует только минимальный payload minimum для сущностей Slice 1.
Он нужен, чтобы реализация Slice 1 могла быть выполнена без выдумывания business attributes.

### Tenant

#### POST /api/v1/tenants
Request minimum:
- `name`

Response minimum:
- `id`
- `name`

### Department

#### POST /api/v1/departments
Request minimum:
- `name`

#### PATCH /api/v1/departments/{id}
Request minimum:
- `name`

Response minimum:
- `id`
- `name`

### Branch

#### POST /api/v1/branches
Request minimum:
- `name`

#### PATCH /api/v1/branches/{id}
Request minimum:
- `name`

Response minimum:
- `id`
- `name`

### Location

#### POST /api/v1/locations
Request minimum:
- `name`
- `branch_id`

#### PATCH /api/v1/locations/{id}
Request minimum:
- `name`
- `branch_id`

Response minimum:
- `id`
- `name`
- `branch_id`

## Payload Rules
- `tenant_id` не передается как свободный бизнес-параметр в request payload;
- tenant scope определяется на API boundary из аутентифицированного контекста;
- `branch_id` для Location должен указывать на Branch того же tenant;
- этот раздел фиксирует только minimum для Slice 1 и не канонизирует более широкую response schema, error model или pagination/filter parameters.

## Not Fixed In This Document
Намеренно не фиксируются:
- request body fields;
- response body fields;
- error model;
- pagination/filter parameter names;
- exact role-assignment API between User and Role;
- permission matrix / permission keys;
- list endpoint for `GET /api/v1/tenants`, если он вообще понадобится в v1.

## Out of Scope
- Permissions API
- invitation / onboarding API
- authentication / identity provider integration API
- массовые bulk-операции
- delete endpoints
- отдельные role-binding endpoints, пока они не подтверждены в scope

## Open Questions
- какие поля входят в payload для каждой сущности;
- нужен ли `GET /api/v1/tenants` в v1;
- как именно выражается API-контракт назначения ролей пользователям;
- какой документ станет каноническим источником permission keys / permission matrix.