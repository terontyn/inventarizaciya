# ADR-0002 Ролевая и permission-модель для Foundation Slice

Status: accepted
Date: 2026-03-27

## Context
В bounded context упомянуты Permissions, но минимальная модель Foundation Slice их не раскрывает.
Также ранее не была зафиксирована кардинальность User ↔ Role.

## Options
1. One user = one role
2. Many-to-many User ↔ Role, permissions только через roles
3. Many-to-many User ↔ Role + direct user permissions
4. ABAC / policy-based model в Foundation Slice

## Decision
Для Foundation Slice принимается:
- tenant-scoped RBAC;
- User ↔ Role = many-to-many;
- permissions назначаются только ролям;
- direct user permissions не вводятся;
- ABAC / policy engine не входит в MVP.

В physical model v1 достаточно:
- roles
- user_roles

Permission catalog может быть документирован отдельно и не обязан быть отдельной таблицей в Foundation Slice.

## Consequences
Плюсы:
- закрывается вопрос кардинальности User ↔ Role;
- появляется достаточная модель для MVP и pilot readiness;
- security-слой остается простым и расширяемым.

Минусы:
- прямые user overrides откладываются;
- потребуется отдельная permission matrix;
- later возможна эволюция модели авторизации.

## Rejected For MVP
- one-role-per-user как жесткая модель
- direct user permissions
- ABAC / policy engine