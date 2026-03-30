# Permission Matrix

Status: draft
Owner: security
Last Updated: 2026-03-27

## Purpose
Шаблон permission matrix для tenant-scoped RBAC.

## Current Decisions
- модель авторизации: tenant-scoped RBAC;
- User ↔ Role = many-to-many;
- permissions назначаются только ролям;
- direct user permissions не используются в Foundation Slice;
- ABAC / policy engine не входит в MVP.

## Important Rule
Этот документ пока не должен содержать выдуманные permission keys.
Он создается как каркас для дальнейшего заполнения после явного согласования security scope.

## Suggested Structure

| Permission Key | Description | Entity Scope | Assigned To Roles | Notes |
|---|---|---|---|---|
| TBD | TBD | TBD | TBD | TBD |

## Initial Areas To Cover Later
- tenant administration
- user management
- role management
- department management
- branch management
- location management
- asset registry
- asset responsibility flows
- audit visibility

## Out of Scope For Now
- direct user permission overrides
- ABAC conditions
- policy engine rules