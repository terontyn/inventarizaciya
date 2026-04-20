# Maintenance API

Status: draft
Owner: architecture

## Purpose
Минимальный API-контракт первого Maintenance contour.

## Scope
Этот документ покрывает только:
- maintenance-side read visibility
- maintenance-side work lifecycle commands
- serviceability-related current view for already tracked assets

## API Principles
- все API versioned
- tenant isolation обязателен
- maintenance context не переопределяет asset identity
- maintenance context не переопределяет inventory custody ownership
- maintenance context не переопределяет inventory readiness ownership
- maintenance context не переопределяет responsibility lifecycle
- cross-context interaction идет только через reference ids

## Boundary rule
Maintenance API не трактуется как:
- generic service desk API
- enterprise ticketing API
- procurement / supplier / finance API

## Minimal API Surface

### Read
- `GET /api/v1/maintenance/work/open`
- `GET /api/v1/maintenance/work/{id}`
- `GET /api/v1/maintenance/assets/{asset_id}`

### Write / lifecycle
- `POST /api/v1/maintenance/assets/{asset_id}/work`
- `POST /api/v1/maintenance/work/{id}/complete`

## Minimal command semantics

### Open maintenance work
`POST /api/v1/maintenance/assets/{asset_id}/work`

Request minimum:
- `work_type`

Success outcome:
- created current open `MaintenanceWork`
  for the given tenant-scoped asset.

### Complete maintenance work
`POST /api/v1/maintenance/work/{id}/complete`

Request minimum:
- body minimum не фиксируется в этом contour

Success outcome:
- corresponding `MaintenanceWork`
  is represented as `COMPLETED`;
- asset no longer has current open maintenance work.

## Maintenance-side current view
`GET /api/v1/maintenance/assets/{asset_id}`
должен отвечать на вопросы:
- есть ли current open maintenance work;
- какого он типа;
- maintenance-side serviceable outcome достигнут или нет.

## Boundary rule
Этот API:
- не является generic ticketing API;
- не является SLA / queue / escalation API;
- не переопределяет inventory readiness ownership;
- не переопределяет final assign decision logic.

## Not fixed in this document
- full request / response schema
- error model
- failure / cancellation taxonomy
- vendor workflow
- cost model
- automatic transition into inventory `READY`

## Workflow Semantics Clarification

### Open work
`POST /api/v1/maintenance/assets/{asset_id}/work`
трактуется как explicit operator action
для создания current open `MaintenanceWork`.

Минимально этот command:
- применим только к already tracked tenant-scoped asset;
- не допускает более одного current `OPEN` work на один Asset;
- не канонизирует inventory readiness mutation автоматически.

### Complete work
`POST /api/v1/maintenance/work/{id}/complete`
трактуется как explicit operator action
для завершения current open maintenance work.

Минимально этот command:
- применим только к existing tenant-scoped current `OPEN` work;
- переводит work в `COMPLETED`;
- закрывает current open maintenance representation для Asset.

### Inventory readiness boundary
Этот API не канонизирует:
- automatic transition в inventory `NOT_READY`
  при opening work;
- automatic transition в inventory `READY`
  при completion work.

Если inventory-side readiness mutation нужна,
она остается отдельным inventory-owned action.

### History implication
First contour не требует отдельной maintenance history surface
beyond current-state representation + audit.

## Closeout Clarification

Текущий maintenance API baseline
считается достаточным
для narrow current maintenance stage.

### Current interpretation
Минимального API surface достаточно,
чтобы покрыть:
- open maintenance work;
- complete maintenance work;
- current open work visibility;
- asset-scoped maintenance-side current view.

### Deferred by default
Следующие API expansions
не открываются по умолчанию:
- cancellation APIs;
- reopen APIs;
- vendor / external servicing APIs;
- cost-related APIs;
- separate maintenance history APIs.