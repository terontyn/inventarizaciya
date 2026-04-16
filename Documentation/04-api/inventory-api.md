# Inventory API

Status: draft
Owner: architecture

## Purpose
Минимальный API-контракт первого Inventory / Warehouse contour.

## Scope
Этот документ покрывает только:
- warehouse reference read
- storage placement read
- current storage custody read
- inventory-side place-in-storage command
- inventory-side move command

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

## Not Fixed In This Document
- exact public handoff contract from storage custody into existing assign flow
- exact public handoff contract from existing return outcome back into storage custody
- full request / response schema
- separate inventory state vocabulary
- move-history model beyond audit
- warehouse-to-branch/location mapping
- bulk APIs

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