# Ticket API

Status: draft
Owner: architecture

## Purpose
Минимальный API-контракт первого Ticket contour.

## Scope
Этот документ покрывает только:
- ticket-side read visibility
- ticket lifecycle commands
- tracked-asset-linked current ticket visibility

## API Principles
- все API versioned
- tenant isolation обязателен
- ticket context не переопределяет asset identity
- ticket context не переопределяет responsibility lifecycle
- ticket context не переопределяет inventory custody / readiness ownership
- ticket context не переопределяет maintenance workflow ownership
- ticket context не переопределяет software ownership
- cross-context interaction идет только через reference ids

## Boundary rule
Ticket API не трактуется как:
- generic enterprise helpdesk API
- full ITSM API
- SLA / queue / escalation API
- umbrella workflow API поверх inventory / maintenance / software
- reporting-first API

## Minimal API Surface

### Read
- `GET /api/v1/tickets/open`
- `GET /api/v1/tickets/{id}`
- `GET /api/v1/tickets/assets/{asset_id}`

### Write / lifecycle
- `POST /api/v1/tickets/assets/{asset_id}`
- `POST /api/v1/tickets/{id}/close`

## Minimal command semantics

### Open ticket
`POST /api/v1/tickets/assets/{asset_id}`

Request minimum:
- body minimum не фиксируется в этом contour

Success outcome:
- created current `OPEN` ticket
  for the given tenant-scoped tracked asset

### Close ticket
`POST /api/v1/tickets/{id}/close`

Request minimum:
- body minimum не фиксируется в этом contour

Success outcome:
- corresponding `Ticket`
  is represented as `CLOSED`

## Ticket-side current view
`GET /api/v1/tickets/assets/{asset_id}`
должен отвечать на вопросы:
- какие current open tickets сейчас связаны с данным tracked asset;
- закрыта соответствующая coordination loop или нет.

## Workflow Semantics Clarification

### Open ticket
`POST /api/v1/tickets/assets/{asset_id}`
трактуется как explicit operator action
для создания current `OPEN` ticket.

Минимально этот command:
- применим только к already tracked tenant-scoped asset;
- не канонизирует automatic inventory readiness mutation;
- не канонизирует automatic maintenance work creation;
- не канонизирует automatic software mutation;
- не канонизирует responsibility lifecycle mutation.

### Close ticket
`POST /api/v1/tickets/{id}/close`
трактуется как explicit operator action
для завершения current open ticket-side coordination loop.

Минимально этот command:
- применим только к existing tenant-scoped current `OPEN` ticket;
- переводит ticket в `CLOSED`;
- не канонизирует automatic mutation
  в inventory / maintenance / software / responsibility lifecycle.

### Boundary to existing domains
Этот API не канонизирует, что:
- ticket opening автоматически меняет inventory `READY / NOT_READY`;
- ticket opening автоматически создает maintenance work;
- ticket opening автоматически создает software mutation;
- ticket closing автоматически означает maintenance-side serviceable outcome;
- ticket closing автоматически означает inventory-side `READY`;
- ticket closing автоматически означает software-side completion state.

Если action в другом контексте действительно нужен,
он остается отдельным explicit action
внутри owner context.

### Audit implication
Open / close actions
должны трактоваться как critical writes
и попадать в audit.

### History implication
First contour не требует отдельного ticket history API
beyond current-state visibility + audit.

## Not fixed in this document
- full request / response schema
- error model
- requester / assignee model
- queue / SLA / escalation model
- omnichannel request model
- exact cross-context action-link model
- reporting-heavy ticket read models
- umbrella orchestration semantics

## Closeout Clarification

Текущий ticket API baseline
считается достаточным
для narrow current ticket stage.

### Current interpretation
Минимального API surface достаточно,
чтобы покрыть:
- open ticket visibility
- single ticket visibility
- asset-linked current ticket visibility
- explicit open / close actions

### Deferred by default
Следующие API expansions
не открываются по умолчанию:
- queue APIs
- SLA APIs
- escalation APIs
- omnichannel request APIs
- umbrella workflow APIs
- separate ticket history APIs

## Asset-linked visibility clarification

### Current view scope
`GET /api/v1/tickets/assets/{asset_id}`
остается asset-linked current visibility surface.

Этот endpoint должен трактоваться как ответ на вопрос:
- какие current open tickets сейчас связаны с данным tracked asset

без открытия:
- requester / assignee model
- queue model
- SLA / escalation model
- umbrella orchestration semantics

### Open-ticket multiplicity rule
First contour допускает,
что один tracked asset
может иметь multiple current `OPEN` tickets`.

Этот API не канонизирует rule
`one open ticket per asset`.

### Minimal request clarification
Для `POST /api/v1/tickets/assets/{asset_id}`
path `asset_id`
остается достаточным minimal business anchor.

Дополнительные mandatory request fields
в этом contour не фиксируются.

### Independent-loop clarification
Если `GET /api/v1/tickets/assets/{asset_id}`
показывает multiple current `OPEN` tickets,
этот current view
должен трактоваться
как набор независимых ticket-side coordination loops.

Следовательно:
- `POST /api/v1/tickets/assets/{asset_id}`
  не канонизирует blocker rule
  against another current `OPEN` ticket
  того же asset;
- `POST /api/v1/tickets/{id}/close`
  закрывает только addressed ticket
  и не канонизирует automatic close
  других current `OPEN` tickets
  того же asset.