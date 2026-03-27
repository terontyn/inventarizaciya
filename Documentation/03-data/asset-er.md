# Asset ER

Status: draft
Owner: architecture
Last Updated: 2026-03-27

## Purpose
Минимальная ER-модель Foundation Slice для asset-service.

## Scope
Этот документ покрывает только мастер-запись `Asset` для CRUD v1.

## Modeling Rules
1. До утверждения tenancy ADR таблица `assets` использует явный `tenant_id`.
2. Таблица использует технический PK `id`.
3. CRUD v1 ограничен только мастер-записью `Asset`.
4. Таблицы `asset_assignments`, `asset_transfers`, `asset_retirements` пока не добавляются в каноническую ER-модель v1.
5. `assigned_user_id` и `location_id` — это cross-service reference IDs; их нельзя валидировать прямым чтением БД user-service.
6. `DELETE` для Asset не включается в v1 до отдельного ADR по delete semantics.

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
- история назначения и перемещений не должна теряться;
- generic update нельзя рассматривать как “любое поле можно переписать без доменных последствий”.

## Domain Boundary
Следующие сущности существуют в домене, но **не включаются в ER v1**, потому что их минимальный набор полей еще не зафиксирован:
- AssetAssignment
- AssetTransfer
- AssetRetirement

## Generic Update Restriction
До ADR по lifecycle/history не считать безопасным generic PATCH для:
- `assigned_user_id`
- `location_id`

Эти поля влияют на доменную историю ответственности и перемещений.

## Notes
- Валидация `assigned_user_id` и `location_id` должна идти через API user-service.
- Прямой доступ к БД другого сервиса запрещен.
- В v1 этот файл описывает только минимальный реестр активов.

## Open Questions
- delete semantics: hard delete / soft delete / no delete / retire only;
- нужен ли отдельный asset history store уже в следующем slice;
- когда именно появляются `AssetAssignment / AssetTransfer / AssetRetirement` как отдельные таблицы;
- можно ли менять `assigned_user_id` и `location_id` через generic PATCH или только через отдельные domain actions.

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

Они полезны как future candidate fields, но не должны считаться каноном Foundation Slice v1 без отдельного подтверждения.