# Asset ER

Status: draft
Owner: architecture
Last Updated: 2026-04-07

## Purpose
Минимальная ER-модель Foundation Slice для asset-service.

## Scope
Этот документ покрывает только мастер-запись `Asset` для CRUD v1.

## Modeling Rules
1. Таблица `assets` использует обязательный `tenant_id`.
2. Таблица использует технический PK `id`.
3. CRUD v1 ограничен только мастер-записью `Asset`.
4. Таблицы `asset_assignments`, `asset_transfers`, `asset_retirements` пока не добавляются в каноническую ER-модель v1.
5. `assigned_user_id` и `location_id` — это cross-service reference IDs; их нельзя валидировать прямым чтением БД user-service.
6. `assigned_user_id` и `location_id` являются history-sensitive fields и исключены из generic PATCH.
7. `DELETE` для Asset не включается в v1 до отдельного ADR по delete semantics.

## Table

### assets
| Column | Type | Notes |
|---|---|---|
| id | UUID | PK |
| tenant_id | UUID | tenant-scoped ownership |
| asset_tag | TEXT | logical business identifier внутри tenant |
| serial_number | TEXT | serial |
| asset_type | TEXT | type/category for v1 |
| status | TEXT | current asset status |
| assigned_user_id | UUID | logical reference to User |
| location_id | UUID | logical reference to Location |

## Relationships

### tenant ownership
- `assets.tenant_id` определяет принадлежность записи tenant

### cross-service references
- `assets.assigned_user_id` — ссылка на `users.id` через API user-service
- `assets.location_id` — ссылка на `locations.id` через API user-service

## Constraints
- `UNIQUE (tenant_id, asset_tag)` — обязательное ограничение;
- актив всегда принадлежит tenant;
- cross-tenant references недопустимы;
- история назначения и перемещений не должна теряться;
- generic update нельзя рассматривать как право менять history-sensitive fields.

## Domain Boundary
Следующие сущности существуют в домене, но **не включаются в ER v1**, потому что их минимальный набор полей еще не зафиксирован:
- AssetAssignment
- AssetTransfer
- AssetRetirement

## Generic Update Restriction
Generic PATCH **не должен** менять:
- `assigned_user_id`
- `location_id`

Эти поля меняются только отдельными responsibility flows вне generic CRUD v1.

## Notes
- Валидация `assigned_user_id` и `location_id` должна идти через API user-service.
- Прямой доступ к БД другого сервиса запрещен.
- Все tenant-aware запросы и проверки должны выполняться в tenant scope.
- В v1 этот файл описывает только минимальный реестр активов.

## Minimal Business Fields for Slice 3

Чтобы реализация Slice 3 могла быть выполнена без выдумывания business attributes,
в Foundation Slice v1 фиксируется следующий минимальный physical field set для Asset master record.

### assets
Дополнительно к `id`, `tenant_id`:
- `asset_tag`
- `serial_number`
- `asset_type`
- `status`
- `assigned_user_id` (optional)
- `location_id` (optional)

## Notes on Slice 3 Minimality
- Это минимальный business field set только для Slice 3.
- `assigned_user_id` и `location_id` остаются history-sensitive fields.
- Generic PATCH не должен менять `assigned_user_id` и `location_id`.
- Поля вроде `barcode`, `qr_code`, `model_id`, `manufacturer_id`, `purchase_date`, `cost`, `condition_status`, `notes`, `created_at`, `updated_at` пока не фиксируются как часть канона.
- Если позже понадобится richer model, она должна добавляться отдельным изменением docs, а не выдумываться в реализации.

## Responsibility History Clarification
ER v1 по-прежнему не канонизирует physical tables для responsibility history flows.

На текущем этапе фиксируется только следующее:
- отдельная canonical entity/table `AssetReturn` не вводится;
- history model должна уметь хранить distinguishable append-only fact type для `Return`;
- final physical storage schema для responsibility history остается open decision;
- command contracts `assign / transfer / return / retire` не требуют добавления новых канонических master-record полей в таблицу `assets`.

## Status Field Clarification

Поле `assets.status` в MVP / Pilot Readiness использует минимальный канонический словарь:
- `ACTIVE`
- `RETIRED`

### Semantics
- `status` отражает operational state Asset, а не responsibility state.
- Отсутствие текущего assigned user не требует отдельного status value.
- Состояние `ACTIVE` + no current assigned user является валидным.
- Post-return состояние хранит `status = ACTIVE`.
- Retirement переводит запись в `status = RETIRED`.

### Restrictions
- command contracts `assign / transfer / return` не требуют новых status values;
- переход в `RETIRED` не должен рассматриваться как generic PATCH correction path;
- `RETIRED` не означает delete и не требует новых master-record полей в `assets`.

## Open Questions
- delete semantics: hard delete / soft delete / no delete / retire only;
- нужен ли отдельный asset history store уже в следующем slice;
- потребуется ли после MVP richer status vocabulary для post-MVP contours.
- какая final physical storage model будет использоваться для append-only responsibility history, включая distinguishable fact type для `Return`;
- нужны ли later отдельные canonical history tables после отдельного решения.

## Not Included Yet
Намеренно не добавлены поля из раннего широкого blueprint, например:
- `barcode`
- `qr_code`
- `asset_type_id`
- `model_id`
- `manufacturer_id`
- `lifecycle_status`
- `condition_status`
- `purchase_date`
- `commissioning_date`
- `warranty_end_date`
- `retirement_date`
- `cost`
- `currency`
- `department_id`
- `parent_asset_id`
- `inventory_number`
- `notes`
- `created_at`
- `updated_at`
- `created_by`
- `updated_by`
- `version`

Они не должны считаться каноном Foundation Slice v1 без отдельного подтверждения.


## Printable Label v1 Clarification

Printable asset label v1 не требует изменений canonical ER v1.

### Confirmed rule
Для printable label v1 используются только уже существующие поля таблицы `assets`:
- `asset_type`
- `asset_tag`
- `serial_number`

### Explicitly not added for v1
Следующие поля не добавляются в ER v1 ради printable label:
- `inventory_number`
- `barcode`
- `qr_code`
- любые отдельные label-specific columns

### Semantics
Для MVP / Pilot поле `asset_tag` допускается использовать
как current printable inventory identifier.
Это не создает новую отдельную canonical identifier family вне текущего Asset master record.