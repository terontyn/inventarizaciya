# User Domain

Status: draft
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

## Confirmed Rules
- пользователь принадлежит tenant;
- роли и права ограничены tenant;
- филиалы, отделы и локации принадлежат tenant;
- активы и заявки в других доменах будут ссылаться на user / org structure.

## Temporary Modeling Rule
До утверждения ADR по tenancy:
- все tenant-scoped записи считаются имеющими tenant_id.

## Temporary RBAC Rule
До утверждения ADR по role / permission model:
- связь User ↔ Role не считается окончательно зафиксированной;
- если RBAC assignment нужен уже в Foundation Slice, допускается временная relation table user_roles.

## Open Questions
- кардинальность User ↔ Role;
- нужны ли отдельные Permissions уже в Foundation Slice;
- иерархия Department / Branch / Location;
- привязывается ли User к Department / Branch / Location напрямую.