# Inventory ER

Status: draft
Owner: architecture

## Purpose
Минимальная ER-модель первого Inventory / Warehouse contour.

## Scope
Этот документ покрывает только:
- current storage custody
- current warehouse placement
- minimal placement references

## Tables

### warehouses
Minimal conceptual fields:
- `id`
- `tenant_id`

### storage_places
Minimal conceptual fields:
- `id`
- `tenant_id`
- `warehouse_id`

### inventory_records
Minimal conceptual fields:
- `id`
- `tenant_id`
- `asset_id`
- `warehouse_id`
- `storage_place_id`

## Data Ownership
- `warehouses`, `storage_places`, `inventory_records` принадлежат `inventory-service`
- `asset_id` является cross-context reference на `asset-service`
- inventory context не канонизирует дублирование asset master fields

## Constraints
- все inventory tables являются tenant-scoped
- `StoragePlace` принадлежит `Warehouse`
- `InventoryRecord` не заменяет Asset master record
- один Asset не должен иметь более одного current storage custody representation одновременно

## Minimality Rule
Первый contour не добавляет:
- stock item catalog
- stock balances
- purchase orders
- supplier references
- finance / depreciation fields
- barcode / QR fields
- label / printer fields

## Open Points
- exact physical representation current-vs-history для `InventoryRecord`
- exact mapping between inventory `Warehouse` and existing `Branch / Location`
- separate storage-move history beyond audit

## Current-State Representation Clarification
Первый contour канонизирует только one-current representation
для storage custody:

- у одного Asset не более одного current `InventoryRecord`;
- наличие current `InventoryRecord`
  означает,
  что Asset сейчас находится в storage custody;
- отсутствие current `InventoryRecord`
  означает,
  что inventory context сейчас не представляет Asset как находящийся в storage custody.

Separate canonical history table / model
для storage entry / move / exit
в первом contour не требуется.

Final physical closeout / history representation
остается open decision
и не фиксируется в этой ER-модели beyond audit.

## ADR-0009 Current-state closeout clarification

Для первого contour
successful inventory-side handoff consumption
не требует отдельной canonical exit table.

Минимально достаточно следующего:
- до successful handoff processing существует current `InventoryRecord`;
- после successful handoff processing
  current `InventoryRecord` больше не существует
  как current storage custody representation;
- повторная обработка того же handoff
  должна трактоваться как idempotent no-op,
  если current representation уже отсутствует.