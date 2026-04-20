# Ticket Domain

Status: draft
Owner: architecture

## Purpose
Ticket domain добавляет narrow operational issue / request coordination layer
для already tracked assets,
не переопределяя asset identity,
inventory ownership,
maintenance ownership
или software ownership.

## Scope
Этот документ фиксирует только narrow first Ticket contour:
- ticket record
- ticket lifecycle
- relation to tracked asset
- current operational issue / request coordination visibility

## Core Rule
Ticket context отвечает на вопрос:
какие operational issues / requests сейчас открыты
и с какими tracked assets они currently related.

## Ownership rule
- `asset-service` остается owner asset identity / master record / responsibility lifecycle / final assign decision logic
- `inventory-service` остается owner storage custody / placement / readiness semantics
- `maintenance-service` остается owner maintenance-side serviceability workflow
- `software-service` остается owner software-side records и current software allocations
- ticket context не становится owner generic service desk / SLA / escalation scope

## Relation to existing domains
- ticket uses tracked `asset_id` only as reference id
- ticket visibility не переопределяет ownership asset
- ticket context не канонизирует inventory readiness
- ticket context не канонизирует maintenance workflow
- ticket context не канонизирует software allocation ownership

## Minimal Ticket Model

Для narrow first Ticket contour
достаточно одного ticket-side work representation:
- `Ticket`

`Ticket` не является asset master record
и не переопределяет asset identity.

### Minimal current-state semantics
Для first contour
достаточно narrow current-state semantics:
- `OPEN`
- `CLOSED`

### Coordination interpretation
- наличие current `OPEN` ticket означает,
  что corresponding operational issue / request coordination
  сейчас остается открытой;
- `CLOSED` ticket означает,
  что corresponding coordination loop закрыт;
- successful close ticket
  не канонизирует automatic mutation
  в inventory / maintenance / software / responsibility lifecycle.

## Ticket Workflow Semantics

### Dedicated workflow rule
Для first contour
ticket lifecycle фиксируется как narrow explicit workflow:
- open ticket
- close ticket

Этот workflow:
- остается ticket-owned;
- не переопределяет responsibility lifecycle;
- не переопределяет inventory readiness ownership;
- не переопределяет maintenance workflow ownership;
- не переопределяет software ownership;
- не открывает broad ITSM workflow.

### Open-ticket rule
Opening ticket
является explicit operator action.

Минимальные preconditions:
- Asset already tracked;
- tenant scope корректен.

Successful opening означает только следующее:
- создан tenant-scoped `Ticket`;
- соответствующая operational issue / request coordination
  represented as current `OPEN`.

### Close-ticket rule
Closing ticket
является explicit operator action.

Минимальные preconditions:
- ticket существует;
- tenant scope корректен;
- ticket currently находится в `OPEN`.

Successful closing означает только следующее:
- `Ticket` represented as `CLOSED`;
- соответствующая ticket-side coordination loop
  больше не represented as current open ticket.

### Boundary to existing domains
Opening или closing ticket
не канонизирует automatic side effects
в других bounded contexts.

Следовательно:
- opening ticket не канонизирует automatic inventory `NOT_READY`;
- opening ticket не канонизирует automatic maintenance work creation;
- opening ticket не канонизирует automatic software mutation;
- closing ticket не канонизирует automatic readiness / serviceability / software outcome;
- если action в другом контексте действительно нужен,
  он остается отдельным explicit action
  внутри owner context.

### Narrowness rule
Для first contour
не требуются:
- SLA semantics;
- queue semantics;
- escalation semantics;
- omnichannel request model;
- umbrella orchestration semantics поверх inventory / maintenance / software;
- separate ticket history surface beyond audit.

## Out of Scope
- generic enterprise helpdesk platform
- full ITSM suite
- SLA / queue / escalation platform
- omnichannel request platform
- reporting-first contour

## Ticket Multiplicity and Minimality Clarification

### Asset-linked-only rule
Для first contour
ticket current visibility остается asset-linked only.

Этот contour не требует:
- requester model
- assignee model
- queue ownership
- SLA / escalation ownership

### Open-ticket multiplicity rule
First contour не фиксирует правило
`one current OPEN ticket per asset`.

Минимально допустимо:
- один tracked asset может иметь multiple current `OPEN` tickets;
- каждый ticket представляет отдельную ticket-side coordination loop;
- ticket context не вводит umbrella coordination semantics
  между такими open tickets.

### Minimal business-field rule
Для first contour
не фиксируются mandatory business fields
beyond minimal current model:
- tracked `asset_id`
- `OPEN / CLOSED` current-state semantics

Более богатые fields
вроде title / category / requester / assignee / reason
остаются deferred
до отдельного product decision.

### Independent-loop clarification
Если у одного tracked asset
одновременно существует несколько current `OPEN` tickets,
каждый из них трактуется
как отдельная ticket-side coordination loop.

Следовательно:
- opening одного ticket
  не блокирует opening другого ticket
  на том же asset;
- closing одного ticket
  не закрывает автоматически
  и не мутирует автоматически
  другие current `OPEN` tickets
  на том же asset;
- first contour не вводит canonical blocker semantics
  между такими open tickets.