# User Domain

Status: approved
Owner: architecture
Last Updated: 2026-03-27

## Scope
User Domain реализуется внутри user-service и покрывает tenant-scoped identity и org references.

## Entities
- Tenant
- User
- Role
- Department
- Branch
- Location

## Confirmed Decisions
- User ↔ Role = many-to-many.
- Foundation Slice использует tenant-scoped RBAC.
- Permissions назначаются только ролям.
- Direct user permissions в Foundation Slice не поддерживаются.
- Department — организационная / управленческая ось.
- Branch и Location — физические измерения.
- Location принадлежит Branch.
- User обязан быть tenant-scoped.
- User может иметь reference на Department.
- Прямая обязательная привязка User к Branch / Location не требуется для MVP.

## Confirmed Rules
- пользователь принадлежит tenant;
- роли и права ограничены tenant;
- филиалы, отделы и локации принадлежат tenant;
- активы и другие домены ссылаются на user/org structure через API user-service;
- cross-tenant references недопустимы.

## Not Included Yet
Намеренно не фиксируются:
- отдельная таблица permissions;
- ABAC / policy engine;
- direct user permission overrides;
- обязательная привязка User к Branch / Location.

## Open Questions
- точный список permission keys для permission matrix;
- нужны ли later прямые user overrides;
- нужны ли later дополнительные user-org references сверх Department.