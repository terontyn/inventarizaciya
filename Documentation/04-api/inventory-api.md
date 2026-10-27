# Inventory API

Status: draft
Owner: architecture

## Purpose
Минимальный API-контракт первого Inventory / Warehouse contour.

## Scope
Этот документ покрывает:
- warehouse reference read
- storage placement read
- current storage custody read
- inventory-side place-in-storage command
- inventory-side move command
- readiness-aware inventory-side read refinement

## API Principles
- все API versioned
- tenant isolation обязателен
- `inventory-service` не переопределяет asset identity
- `inventory-service` не переопределяет responsibility lifecycle
- cross-context interaction идет только через reference ids
- critical write operations должны попадать в audit

## Read Surface

### Warehouses
- `GET /api/v1/warehouses`

### Storage places
- `GET /api/v1/warehouses/{id}/storage-places`

### Assets currently in storage
- `GET /api/v1/inventory/assets/in-storage`

### Single asset inventory-side view
- `GET /api/v1/inventory/assets/{asset_id}`

## Command Surface

### Place asset into storage custody
Candidate endpoint:
- `POST /api/v1/inventory/assets/{asset_id}/place-in-storage`

Request minimum:
- `warehouse_id`
- `storage_place_id` (optional if warehouse-level placement is enough)

Success outcome:
- Asset is now represented in current storage custody
- current warehouse placement is fixed in inventory context

### Move asset inside storage / warehouse placement
Candidate endpoint:
- `POST /api/v1/inventory/assets/{asset_id}/move`

Request minimum:
- target `warehouse_id` and/or target `storage_place_id`

Success outcome:
- current warehouse placement is changed in inventory context

## Visibility Clarification
`GET /api/v1/inventory/assets/in-storage`
должен отвечать на вопрос:
- what is in storage now

`GET /api/v1/inventory/assets/{asset_id}`
должен отвечать на вопрос:
- where it is placed now

Inventory-side visibility `available for next assignment`
не переопределяет final assign eligibility;
она только показывает,
что Asset сейчас находится в storage custody.


## Workflow Semantics Clarification

### Place-in-storage
`POST /api/v1/inventory/assets/{asset_id}/place-in-storage`
трактуется как explicit inventory-side intake
в current storage custody.

Этот command:
- не переопределяет asset identity;
- не переопределяет responsibility lifecycle;
- не является частью existing `return`.

### Move
`POST /api/v1/inventory/assets/{asset_id}/move`
является единым inventory-side move workflow
с intra-warehouse и inter-warehouse вариантами.

Этот command:
- меняет current warehouse/storage placement;
- не трактуется как responsibility `transfer`.

### ADR-0009 Clarification: Handoff to assign

Для первого contour
handoff из storage custody в existing assign flow
не открывает отдельный public inventory exit API.

При этом допустим internal-only integration contract:

- successful assign in `asset-service`
  инициирует internal async handoff signal;
- `inventory-service`
  consume’ит current storage custody representation
  для соответствующего tenant-scoped asset;
- synchronous inventory completion
  не является условием assign success.

Этот документ по-прежнему не канонизирует
public handoff payload schema
или transport technology choice.

### Availability note
Inventory-side visibility `available for next assignment`
означает только,
что Asset currently represented in storage custody now.

Это не канонизирует final assign eligibility
и не заменяет existing assign validations.

## Availability / Readiness Refinement

Следующий API refinement
должен позволять inventory-side read surfaces
различать:

- `not in storage now`
- `in storage and ready`
- `in storage but not ready`

### Boundary rule
Inventory-side readiness:
- означает только storage-side operational readiness;
- не переопределяет final assign eligibility;
- не заменяет existing assign validations в `asset-service`.

### Read-surface refinement

#### Warehouses
`GET /api/v1/warehouses`
должен отражать minimal warehouse anchoring:
- `branch_id`
- `location_id` (optional)

#### Assets currently in storage
`GET /api/v1/inventory/assets/in-storage`
должен по-прежнему отвечать на вопрос:
- what is in storage now

Дополнительно этот read surface
должен позволять отличать:
- in storage and ready
- in storage but not ready

Минимально достаточно,
чтобы inventory-side read model
включал:
- `readiness_state`

#### Single asset inventory-side view
`GET /api/v1/inventory/assets/{asset_id}`
должен позволять отличать:
- asset not in storage now;
- asset in storage and ready;
- asset in storage but not ready.

### Recommended readiness vocabulary
Для этого refinement
достаточно narrow two-value vocabulary:
- `READY`
- `NOT_READY`

### Handoff rule remains unchanged
ADR-0009 сохраняется без изменений:
- successful assign в `asset-service`
  остается canonical trigger point
  для inventory-side storage custody consumption;
- readiness semantics
  не открывает отдельный public inventory exit API.

## Not Fixed In This Document
- exact public handoff contract from storage custody into existing assign flow
- exact public handoff contract from existing return outcome back into storage custody
- full request / response schema
- query/filter parameter canon
- move-history model beyond audit
- bulk APIs
- broad warehouse state machine
- final assign eligibility logic


## Readiness Workflow Clarification

### Public mutation justification
Следующий slice оправдывает отдельный public readiness mutation command,
потому что readiness refinement
иначе остается без inventory-side system of action.

При этом этот документ по-прежнему не канонизирует:
- exact endpoint;
- payload schema;
- error model.

### Place-in-storage relation
`place-in-storage`
должен создавать current `InventoryRecord`
с initial `readiness_state = NOT_READY`.

### Move relation
`move`
не должен автоматически менять `readiness_state`.

### Assign handoff relation
Readiness refinement
не меняет ADR-0009 handoff rule:

- successful assign
  остается canonical trigger point
  для storage custody consumption;
- readiness semantics
  не открывает новый public inventory exit API;
- readiness semantics
  не канонизирует new synchronous precondition
  для assign success.

### Audit implication
Readiness change
должен трактоваться как critical inventory-side write
и попадать в audit.

### History implication
Separate readiness history surface
beyond audit
в этом slice не требуется.

## Maintenance Boundary Clarification

Maintenance-side open / completed work
не являются inventory-owned readiness mutation сами по себе.

Следовательно:
- opening maintenance work
  не канонизирует automatic `NOT_READY`;
- maintenance completion
  не канонизирует automatic `READY`.

Если inventory-side readiness change нужен,
он должен выполняться
через отдельный inventory-side readiness workflow.