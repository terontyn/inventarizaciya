# Reporting Read Models

Status: draft
Owner: architecture

## Purpose
Минимальные read-model boundaries первого Reporting contour.

## Scope
Этот документ покрывает только:
- asset-centric current-state reporting views
- cross-context operational visibility
- reporting-side read composition

## Read-model rule
Первый Reporting contour
остается current-state-first
и asset-centric.

Он не открывает:
- historical BI-heavy model
- free-form analytics model
- write ownership over source contexts

## Minimal reporting questions
Первый contour должен уметь отвечать на вопросы:
- где asset находится сейчас;
- ready он или not ready;
- есть ли current open maintenance work;
- есть ли current open tickets;
- какие current software relations существуют.

## Boundary rule
Reporting read models:
- не переопределяют source-of-truth existing contexts;
- не канонизируют new operational facts;
- не канонизируют orchestration semantics;
- не требуют heavy KPI / dashboard catalog на первом шаге.

## Primary Read-Model Shape

### Recommended primary read model
Первый Reporting contour
фиксирует один primary asset-centric read model:
- `AssetOperationalCurrentView`

### Conceptual minimum
Этот read model должен уметь содержать:
- `asset_id`
- current location / placement summary
- `in_storage_now`
- `readiness_state` (только если inventory-side storage custody represented now)
- `has_open_maintenance_work`
- `has_open_tickets`
- current asset-linked software relations summary

### Source-of-truth rule
Каждый из этих reporting-side fields
остается derived read visibility
поверх owner contexts
и не становится новым canonical operational fact.

### Current-state-first rule
Первый contour не фиксирует:
- historical reporting model;
- KPI / dashboard catalog;
- free-form analytics model;
- write ownership over source contexts.

## Current-State Consistency Clarification

### Source-derived rule
Каждое поле `AssetOperationalCurrentView`
остается derived read visibility
поверх corresponding owner context.

Следовательно:
- reporting-side field
  не становится новым canonical fact;
- owner context остается authoritative source
  для соответствующего current-state slice.

### Lag / eventual-consistency rule
First contour допускает eventual consistency
между owner-context updates
и reporting-side current view.

Следовательно:
- reporting read model
  не обещает strict synchronous snapshot
  across all source contexts;
- short-lived lag
  между source update
  и reporting visibility
  допустим;
- temporary mixed composition
  of fresher and staler current slices
  допустима.

### Inventory handoff note
Around ADR-0009 handoff
reporting может кратковременно показывать
inventory-derived current visibility,
которая еще не догнала latest assign-side change.

Это не меняет source-of-truth rule
и не делает reporting owner
соответствующего operational fact.

### History / KPI deferral
First contour не требует:
- separate reporting history layer;
- trend model;
- KPI / dashboard catalog;
- free-form analytics model.

## Closeout Clarification

Текущий reporting read-model baseline
считается достаточным
для current Wave 2 stage.

### Current interpretation
Текущего asset-centric read model
достаточно,
чтобы представить:
- current operational picture по tracked asset;
- current derived visibility
  поверх inventory / maintenance / software / ticket owner contexts.

### Deferred by default
Следующие расширения
не открываются по умолчанию:
- broader aggregation beyond current asset-centric model;
- historical read models;
- trend-oriented read models;
- KPI / dashboard-oriented read models.



## Post-Wave-2 Validation Interpretation

### Validation-target rule
На post-Wave-2 hardening / validation stage
`AssetOperationalCurrentView`
остается sufficient validation target
for derived visibility alignment.

Этот stage
не требует:
- новой reporting read-model family;
- нового validation snapshot model;
- broader aggregation family.

### Comparison-basis rule
Для каждого derived slice
comparison basis
остается owner-context dedicated surface
соответствующего bounded context.

### Alignment interpretation
Validation alignment
не требует strict synchronous equality
между каждым owner-side response slice
и reporting-side derived visibility.

Достаточно подтвердить,
что derived reporting view:
- semantically соответствует owner-side current-state fact;
- допускает only bounded lag;
- не ломает operational meaning;
- не создает reporting-owned canonical fact.

### Non-expansion rule
Этот validation use
не оправдывает автоматически:
- historical reporting layer;
- KPI / dashboard family;
- free-form analytics family;
- новую standalone read-model family.

## Validation Outcome Clarification

Для post-Wave-2 validation
`AssetOperationalCurrentView`
должен использоваться
как derived visibility target only.

Следовательно:
- confirmed alignment
  означает semantic compatibility
  with owner-side current-state facts;
- accepted lag
  не трактуется как defect by default,
  если lag bounded and approved-semantics-compatible;
- concrete mismatch
  требует gap closure only inside approved scope;
- need for broader reporting family
  трактуется как separate expansion,
  а не как continuation of this stage.