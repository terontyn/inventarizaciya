# Inventory ER

Status: draft
Owner: architecture

## Purpose
Минимальная ER-модель первого Inventory / Warehouse contour.

## Scope
Этот документ покрывает:
- current storage custody
- current warehouse placement
- minimal placement references
- minimal readiness refinement

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

## Readiness Refinement Clarification

Следующий data-level refinement
должен сохранить current storage custody representation
и добавить отдельную narrow readiness dimension.

### Warehouse mapping refinement

#### warehouses
Minimal conceptual fields become:
- `id`
- `tenant_id`
- `branch_id`
- `location_id` (optional)

#### storage_places
Minimal conceptual fields remain:
- `id`
- `tenant_id`
- `warehouse_id`

#### inventory_records
Minimal conceptual fields become:
- `id`
- `tenant_id`
- `asset_id`
- `warehouse_id`
- `storage_place_id`
- `readiness_state`

### Current-state rule
- наличие current `InventoryRecord`
  означает only `in storage now`;
- readiness не заменяет сам факт storage custody,
  а уточняет его.

### Recommended readiness vocabulary
Для этого refinement
фиксируется narrow two-value vocabulary:
- `READY`
- `NOT_READY`

### Canonical interpretation
- отсутствие current `InventoryRecord`
  означает `not in storage now`;
- current `InventoryRecord` + `readiness_state = READY`
  означает `in storage and ready`;
- current `InventoryRecord` + `readiness_state = NOT_READY`
  означает `in storage but not ready`.

### Warehouse mapping clarification
- `Warehouse.branch_id`
  является mandatory same-tenant reference
  на existing `Branch`;
- `Warehouse.location_id`
  является optional same-tenant reference
  на existing `Location`;
- optional `location_id`
  трактуется только как narrow physical anchor,
  а не как новая topology hierarchy.

### Still deferred
Этот refinement по-прежнему не фиксирует:
- full warehouse topology;
- separate move-history platform;
- stocktaking / reconciliation model;
- broad warehouse state machine;
- final assign eligibility logic.


## Readiness Current-State Semantics

### Existence rule
`readiness_state`
существует только как часть current `InventoryRecord`.

Следовательно:
- без current `InventoryRecord`
  readiness_state не существует;
- отсутствие current `InventoryRecord`
  по-прежнему означает only `not in storage now`.

### Initial value rule
При создании current storage custody representation
через `place-in-storage`
initial `readiness_state`
должен быть `NOT_READY`.

### Update rule
Изменение `readiness_state`
не должно создаваться неявно
через `move`
или через assign handoff processing.

### History rule
Separate canonical readiness-history table / model
в этом refinement не требуется.

Audit trail остается достаточным minimum fact trail
до отдельного последующего решения.