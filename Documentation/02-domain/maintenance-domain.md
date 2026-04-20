# Maintenance Domain

Status: draft
Owner: architecture

## Purpose
Maintenance domain добавляет serviceability layer
для already tracked physical assets,
не переопределяя asset identity,
asset master record,
inventory custody
или responsibility lifecycle.

## Scope
Этот документ фиксирует только narrow first Maintenance contour:
- serviceability semantics
- maintenance work record
- inspection / repair / servicing lifecycle

## Core Rule
Maintenance отвечает на вопрос,
почему Asset сейчас не serviceable
и как он проходит maintenance loop
до serviceable outcome.

## Ownership rule
- `asset-service` остается owner asset identity / master record / responsibility lifecycle
- `inventory-service` остается owner storage custody / placement / readiness semantics
- maintenance context не становится owner final assign decision logic

## Relation to inventory
Inventory-side `NOT_READY`
и maintenance-side required servicing
не являются одной и той же сущностью.

- inventory readiness остается inventory-owned current-state signal
- maintenance loop остается maintenance-owned serviceability workflow

## Out of Scope
- generic service desk / enterprise ticketing platform
- full ITSM suite
- inventory custody ownership
- final assign decision ownership
- procurement / supplier / finance / depreciation
- reporting-first contour

## Minimal Maintenance Work Model

Для narrow first Maintenance contour
достаточно одного maintenance-side work representation:
- `MaintenanceWork`

`MaintenanceWork` не является asset master record
и не переопределяет asset identity.

### Minimal work-type vocabulary
Для first contour
достаточно narrow work-type vocabulary:
- `INSPECTION`
- `REPAIR`
- `SERVICING`

### Minimal current-state semantics
Maintenance context требует собственную narrow current-state semantics,
но не broad workflow state machine.

Минимально достаточно:
- `OPEN`
- `COMPLETED`

### Serviceability interpretation
- наличие current open `MaintenanceWork`
  означает,
  что Asset maintenance-side currently not yet serviceable;
- отсутствие current open `MaintenanceWork`
  означает,
  что maintenance context не представляет Asset
  как currently under maintenance;
- successful completion maintenance work
  означает maintenance-side serviceable outcome,
  но не канонизирует inventory readiness автоматически.

## Relation to inventory readiness
Inventory-side `NOT_READY`
и maintenance-side open work
не являются одной и той же сущностью.

- inventory readiness остается inventory-owned current-state signal;
- maintenance loop остается maintenance-owned serviceability workflow;
- maintenance completion не переопределяет inventory ownership
  над `READY / NOT_READY`.

  ## Maintenance Workflow Semantics

### Dedicated workflow rule
Для first contour
maintenance work lifecycle
фиксируется как narrow explicit workflow:
- open maintenance work
- complete maintenance work

Этот workflow:
- остается maintenance-owned;
- не переопределяет inventory readiness ownership;
- не переопределяет final assign decision logic;
- не открывает broad ITSM workflow.

### Open-work rule
Opening maintenance work
является explicit operator action.

Минимальные preconditions:
- Asset already tracked;
- tenant scope корректен;
- у Asset нет другого current `OPEN` maintenance work.

Successful opening означает только следующее:
- создан current open `MaintenanceWork`;
- Asset maintenance-side currently not yet serviceable.

### Completion rule
Completion maintenance work
является explicit operator action.

Минимальные preconditions:
- maintenance work существует;
- tenant scope корректен;
- work currently находится в `OPEN`.

Successful completion означает только следующее:
- `MaintenanceWork` represented as `COMPLETED`;
- Asset больше не имеет current open maintenance work;
- maintenance context больше не представляет Asset
  как currently under maintenance.

### Boundary to inventory readiness
Opening maintenance work
не канонизирует automatic inventory `NOT_READY`.

Completion maintenance work
не канонизирует automatic inventory `READY`.

Если inventory-side readiness change нужен,
он должен выполняться
как отдельный explicit inventory-side action.

### Narrowness rule
Для first contour
не требуются:
- cancellation semantics;
- reopen semantics;
- separate maintenance history surface beyond audit.

## Post-Closeout Clarification

Для current Wave 2 stage
первый narrow `Maintenance` contour
считается достаточным
как minimal serviceability layer.

### Current interpretation
На этом этапе maintenance уже покрывает:
- minimal maintenance work representation;
- open work;
- complete work;
- current open work visibility;
- maintenance-side serviceability outcome.

### Deferred by default
Следующие возможные расширения
не считаются default next step:
- richer outcome taxonomy;
- cancellation semantics;
- reopen semantics;
- vendor / external servicing workflow;
- maintenance history beyond audit;
- serviceability-to-readiness coordination refinement.

Эти темы остаются deferred
до отдельного product decision.