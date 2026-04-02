# MVP Scope

Status: approved
Owner: product
Last Updated: 2026-04-02

## MVP Name
Asset Accountability MVP

## Goal
Первый релиз должен отвечать без Excel на вопрос:
что у нас есть, где это находится, за кем закреплено и кто это менял.

## In Scope
- tenant-aware foundation для данных и доступа;
- users, roles, departments, branches, locations;
- реестр активов;
- карточка актива с текущим владельцем / локацией / статусом;
- assign / transfer / return;
- audit и история критических действий;
- минимально необходимый admin layer для поддержки этого контура.

## Pilot Readiness Interpretation

Pilot Readiness в рамках текущего MVP означает не новый модуль, а доведение уже утвержденного контура до operational use одним tenant.

Minimal admin layer для MVP включает только:
- tenant admin management of users / roles / departments / branches / locations;
- operator execution of asset CRUD v1 + assign / transfer / return;
- reviewer read-only visibility of registry / card / history / audit.

Pilot Readiness не расширяет scope в:
- inventory / warehouse;
- software licenses / SAM;
- maintenance & repair;
- reporting module;
- heavy BI;
- expanded integrations;
- custom workflows.

Наличие `retirement` в доменном каноне не делает его автоматически обязательным pilot gate, пока он не входит в explicit MVP loop.

## Pilot Reference-Data Setup Interpretation

Для первого pilot tenant minimum reference-data ownership / setup model ограничен текущим MVP contour и не вводит новый master-data module.

### Minimum tenant-managed areas
- roles
- users
- departments
- branches
- locations

### Ownership
- Tenant Admin владеет созданием и поддержкой roles / users / departments / branches / locations;
- Asset Operator не управляет reference data и использует только разрешенный reference read-only access;
- Support Reviewer остается read-only.

### Setup order before pilot start
1. roles
2. pilot users with role assignment
3. branches
4. locations
5. departments

### Clarifications
- `departments` входят в minimum tenant-managed reference-data set для MVP contour, но не являются hard precondition для каждого первого flow;
- отдельные asset-side reference catalogs не являются обязательной частью pilot setup model;
- exact Asset status vocabulary уже фиксирован каноном (`ACTIVE`, `RETIRED`) и не является tenant-managed reference dictionary.

## Out of Scope
- ticket / service desk;
- inventory / warehouse;
- software licenses / SAM;
- maintenance & repair;
- reporting как отдельный модуль;
- сложная аналитика и heavy BI;
- расширенные интеграции;
- кастомные workflow / automation features.

## Foundation Slice vs MVP
Foundation Slice — минимальный вертикальный путь.
MVP 6–8 недель — тот же путь, доведенный до pilot-ready состояния.

## Value
Один tenant должен перестать вести базовый учет техники в таблицах
и начать видеть:
- кто чем пользуется;
- где находится актив;
- кто и когда менял критичные данные.