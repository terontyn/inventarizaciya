# Asset Domain

Status: approved
Owner: architecture
Last Updated: 2026-03-27

## Scope
Asset Domain реализуется внутри asset-service.

## Entities
- Asset
- AssetAssignment
- AssetTransfer
- AssetRetirement

## Confirmed Asset Fields
- asset_tag
- serial_number
- asset_type
- status
- assigned_user_id
- location_id

## Invariants
- актив принадлежит tenant;
- asset_tag уникален внутри tenant;
- история назначения и перемещений не теряется.

## Confirmed Decisions
- Asset CRUD v1 управляет только мастер-записью Asset.
- Generic PATCH не должен менять assigned_user_id и location_id.
- Операции assign / transfer / return / retire являются отдельными domain flows.
- История ответственности является append-only.
- asset-service хранит только tenant-scoped reference IDs на user/org entities.
- Cross-context validation выполняется через user-service API, а не через прямой доступ к БД.

## CRUD v1 Boundary
Foundation Slice v1 включает:
- POST /api/v1/assets
- GET /api/v1/assets
- GET /api/v1/assets/{id}
- PATCH /api/v1/assets/{id}

CRUD v1 не включает:
- dedicated command APIs для responsibility flows в этом документе;
- DELETE /api/v1/assets/{id}.

## Delete
Delete semantics пока не утверждена.
До ADR-0005 удаление остается open question.

## Open Questions
- final delete semantics: hard delete / soft delete / no delete / retire only;
- когда именно появляются отдельные command endpoints для AssetAssignment / AssetTransfer / AssetRetirement;
- какая модель хранения history будет финальной после MVP.