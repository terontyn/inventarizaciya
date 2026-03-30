# User ER

Status: draft
Owner: architecture
Last Updated: 2026-03-30

## Purpose
Минимальная ER-модель Foundation Slice для user-service.

## Scope
Этот документ покрывает только минимальную каноническую ER-модель для:
- Tenant
- User
- Role
- Department
- Branch
- Location

## Modeling Rules
1. `tenants` является tenant boundary и не является tenant-scoped таблицей.
2. Все tenant-scoped таблицы используют обязательный `tenant_id`.
3. Отдельная сущность `Org` в Foundation Slice не вводится.
4. Authorization model в Foundation Slice = tenant-scoped RBAC.
5. Связь `User ↔ Role` моделируется как many-to-many через `user_roles`.
6. Отдельная таблица `permissions` в Foundation Slice не добавляется.
7. `Department` является организационной / управленческой осью.
8. `Branch` и `Location` являются физической осью.
9. `Location` принадлежит `Branch`.
10. `User` может иметь optional reference на `Department`.
11. Прямая обязательная привязка `User` к `Branch` / `Location` в MVP не требуется.

## Tables

### tenants
| Column | Type | Notes |
|---|---|---|
| id | UUID | PK |

### users
| Column | Type | Notes |
|---|---|---|
| id | UUID | PK |
| tenant_id | UUID | mandatory tenant scope |
| department_id | UUID | optional reference to Department |

### roles
| Column | Type | Notes |
|---|---|---|
| id | UUID | PK |
| tenant_id | UUID | mandatory tenant scope |

### user_roles
| Column | Type | Notes |
|---|---|---|
| tenant_id | UUID | mandatory tenant scope |
| user_id | UUID | reference to User |
| role_id | UUID | reference to Role |

### departments
| Column | Type | Notes |
|---|---|---|
| id | UUID | PK |
| tenant_id | UUID | mandatory tenant scope |

### branches
| Column | Type | Notes |
|---|---|---|
| id | UUID | PK |
| tenant_id | UUID | mandatory tenant scope |

### locations
| Column | Type | Notes |
|---|---|---|
| id | UUID | PK |
| tenant_id | UUID | mandatory tenant scope |
| branch_id | UUID | reference to Branch |

## Relationships
- `users.department_id` -> `departments.id` as optional same-tenant reference
- `user_roles.user_id` -> `users.id` in same tenant
- `user_roles.role_id` -> `roles.id` in same tenant
- `locations.branch_id` -> `branches.id` in same tenant

## Constraints
- все tenant-scoped таблицы обязаны иметь `tenant_id`;
- cross-tenant references недопустимы;
- `UNIQUE (tenant_id, user_id, role_id)` является минимальным ограничением для `user_roles`;
- `Location` не существует без `Branch` в канонической org-модели v1;
- direct user permissions не поддерживаются;
- отдельная таблица `permissions` не входит в Foundation Slice v1.

## Notes
- Этот документ намеренно не фиксирует бизнес-атрибуты вроде `name`, `code` и другие поля, не подтвержденные в domain docs.
- Этот документ намеренно не фиксирует прямую обязательную связь `User` с `Branch` или `Location`.
- Этот документ отражает data consequences уже принятых ADR-0001 / ADR-0002 / ADR-0003.

## Minimal Business Fields for Slice 1

Чтобы реализация Slice 1 могла быть выполнена без выдумывания business attributes,
в Foundation Slice v1 фиксируется следующий минимальный physical field set.

### tenants
Дополнительно к `id`:
- `name`

### departments
Дополнительно к `id`, `tenant_id`:
- `name`

### branches
Дополнительно к `id`, `tenant_id`:
- `name`

### locations
Дополнительно к `id`, `tenant_id`, `branch_id`:
- `name`

## Notes on Minimality
- Это минимальный business field set только для Slice 1.
- Поля вроде `code`, `description`, `address`, `external_id`, `created_at`, `updated_at` пока не фиксируются как часть канона.
- Если позже понадобится richer model, она должна добавляться отдельным изменением docs, а не выдумываться в реализации.


## Open Questions
- какие именно бизнес-атрибуты входят в payload и физическую модель `Tenant / User / Role / Department / Branch / Location`;
- как именно будет выражен API-контракт для назначения ролей пользователям;
- какой документ станет каноническим для permission keys / permission matrix;
- нужны ли later дополнительные user-org references сверх `department_id`.

## Not Included Yet
Намеренно не добавлены:
- отдельная таблица `permissions`
- direct user permission overrides
- ABAC / policy engine
- обязательная связь `User` -> `Branch`
- обязательная связь `User` -> `Location`
- любые дополнительные атрибуты, не подтвержденные в domain docs или accepted ADR