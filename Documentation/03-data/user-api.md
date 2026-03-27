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