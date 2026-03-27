# Asset Domain

Status: draft
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

## CRUD v1 Boundary
Foundation Slice v1 включает CRUD только для мастер-записи Asset.

Публичные workflow/API для:
- AssetAssignment
- AssetTransfer
- AssetRetirement

не входят в generic CRUD v1 и должны проектироваться отдельно.

## Generic PATCH Restriction
До отдельного ADR нельзя считать, что generic PATCH может безопасно менять:
- assigned_user_id
- location_id

Эти поля влияют на доменную историю ответственности и перемещений.

## Delete
DELETE для Asset не считается утвержденным до ADR по delete semantics.