# ADR-0002 Role Permission Model

Status: proposed
Date: 2026-03-27

## Context
В bounded context упомянуты Permissions, но они не раскрыты в текущих domain docs.
Также не зафиксирована кардинальность User ↔ Role.

## Decision
Для Foundation Slice:
- поддерживать many-to-many между User и Role через relation table user_roles;
- отдельную доменную модель Permissions не вводить в MVP;
- роль считать tenant-scoped.

## Why
- это безопаснее, чем жестко принять one-role-per-user без подтверждения;
- не блокирует базовый RBAC;
- не заставляет сейчас выдумывать полноценную permission system.

## Consequences
Плюсы:
- гибкость;
- минимальный RBAC уже возможен;
- меньше риск переделки security слоя.

Минусы:
- permissions появятся позже отдельным слоем;
- часть авторизации на MVP будет завязана на role-level semantics.

## Follow-up
Обновить:
- /docs/02-domain/user-domain.md
- /docs/03-data/user-er.md
- /docs/04-api/user-api.md