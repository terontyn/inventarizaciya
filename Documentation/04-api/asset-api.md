# Asset API

Status: draft
Owner: architecture
Last Updated: 2026-03-30

## Purpose
Минимальный API-контракт Foundation Slice для asset-service.

## Scope
Этот документ покрывает только Asset CRUD v1 для мастер-записи `Asset`.

## API Principles
1. Все API versioned.
2. asset-service владеет bounded context Asset Management.
3. tenant isolation обязателен.
4. Tenant определяется на API boundary из аутентифицированного контекста.
5. Сервис не принимает tenant как свободный бизнес-параметр.
6. Прямой доступ к БД user-service запрещен.
7. Валидация `assigned_user_id` и `location_id`, если они присутствуют в create-path, выполняется через API user-service.
8. Critical writes должны попадать в audit по принятому audit contract.
9. Generic CRUD v1 не должен ломать доменный инвариант истории.

## Service Ownership
asset-service в Foundation Slice v1 владеет:
- Asset

Следующие доменные концепты существуют, но их полноценные workflow/API не входят в этот документ:
- AssetAssignment
- AssetTransfer
- AssetRetirement

## Endpoints v1
- `POST /api/v1/assets`
- `GET /api/v1/assets`
- `GET /api/v1/assets/{id}`
- `PATCH /api/v1/assets/{id}`

## Confirmed Asset Fields Relevant To API
- `asset_tag`
- `serial_number`
- `asset_type`
- `status`
- `assigned_user_id`
- `location_id`

## Business Rules
- актив принадлежит tenant;
- `asset_tag` уникален внутри tenant;
- история назначения и перемещений не должна теряться;
- `assigned_user_id` и `location_id` являются cross-service reference IDs;
- cross-service reference IDs должны валидироваться через API user-service в tenant scope;
- `assigned_user_id` и `location_id` являются history-sensitive fields;
- generic PATCH не должен менять `assigned_user_id` и `location_id`.

## Write Operations Requiring Audit
Следующие write-операции считаются критичными и должны попадать в audit:
- create asset
- update asset

## Restrictions
- `DELETE /api/v1/assets/{id}` не включается в v1;
- generic PATCH не используется для responsibility-changing operations;
- assignment / transfer / return / retire не считаются частью generic CRUD v1.

## Canonical Payload Minimum for Slice 3

Этот раздел фиксирует только минимальный payload minimum для Asset Registry Core.
Он нужен, чтобы реализация Slice 3 могла быть выполнена без выдумывания public API contract.

### Asset

#### POST /api/v1/assets
Request minimum:
- `asset_tag`
- `serial_number`
- `asset_type`
- `status`
- `assigned_user_id` (optional)
- `location_id` (optional)

Response minimum:
- `id`
- `asset_tag`
- `serial_number`
- `asset_type`
- `status`
- `assigned_user_id`
- `location_id`

#### PATCH /api/v1/assets/{id}
Request minimum:
- `asset_tag`
- `serial_number`
- `asset_type`
- `status`

Response minimum:
- `id`
- `asset_tag`
- `serial_number`
- `asset_type`
- `status`
- `assigned_user_id`
- `location_id`

## Slice 3 Payload Rules
- `tenant_id` не передается как свободный бизнес-параметр в request payload;
- `assigned_user_id`, если передан на create-path, должен указывать на User того же tenant;
- `location_id`, если передан на create-path, должен указывать на Location того же tenant;
- generic PATCH не канонизирует изменение `assigned_user_id` и `location_id`;
- этот раздел фиксирует только minimum для Slice 3 и не канонизирует более широкую response schema, error model, pagination/filter parameters или responsibility-flow APIs.

## Not Fixed In This Document
Намеренно не фиксируются:
- request body fields;
- response body fields;
- error model;
- pagination/filter parameter names;
- delete semantics;
- отдельные command endpoints для assignment / transfer / return / retire;
- asset history storage model;
- event contract;
- audit payload format.

## Out of Scope
- `DELETE /api/v1/assets/{id}`
- `POST /api/v1/assets/{id}/assign`
- `POST /api/v1/assets/{id}/transfer`
- `POST /api/v1/assets/{id}/return`
- `POST /api/v1/assets/{id}/retire`
- bulk import/export
- attachment API
- barcode / QR workflows

## Open Questions
- какая delete semantics будет принята: hard delete / soft delete / no delete / retire only;
- когда появляются отдельные API для AssetAssignment / AssetTransfer / AssetRetirement;
- какая модель хранения history будет канонической после MVP;
- какие дополнительные поля сверх уже зафиксированного minimum входят в payload Asset v1.