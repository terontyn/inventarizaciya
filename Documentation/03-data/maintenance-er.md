# Maintenance ER

Status: draft
Owner: architecture

## Purpose
Минимальная ER-модель первого Maintenance contour.

## Scope
Этот документ покрывает только:
- maintenance-side work representation
- relation to tracked asset
- minimal serviceability semantics

## Data Ownership
- maintenance tables принадлежат maintenance context
- `asset_id` является cross-context reference на `asset-service`
- maintenance context не канонизирует дублирование asset master fields
- maintenance context не канонизирует inventory custody ownership
- maintenance context не канонизирует inventory readiness ownership

## Minimality Rule
Первый contour не добавляет:
- spare-parts / procurement model
- supplier references
- finance / depreciation fields
- generic helpdesk ticket model
- broad ITSM workflow model

## Minimal Maintenance Work Representation

### Tables

#### maintenance_works
Minimal conceptual fields:
- `id`
- `tenant_id`
- `asset_id`
- `work_type`
- `work_state`
- `opened_at`
- `completed_at` (optional until completed)

## Constraints
- все maintenance tables tenant-scoped;
- `asset_id` является cross-context reference на `asset-service`;
- maintenance context не канонизирует дублирование asset master fields;
- maintenance context не канонизирует inventory custody ownership;
- maintenance context не канонизирует inventory readiness ownership;
- в narrow first contour
  один Asset не должен иметь более одного current `OPEN` maintenance work одновременно.

## Minimal vocabularies
### work_type
- `INSPECTION`
- `REPAIR`
- `SERVICING`

### work_state
- `OPEN`
- `COMPLETED`

## Relation to inventory readiness
Maintenance outcome остается отдельным
от inventory `READY / NOT_READY`.

Этот contour не фиксирует automatic readiness transition
как часть maintenance ER.

## Workflow Current-State Clarification

### Open-work uniqueness
В first contour
у одного Asset не более одного current `OPEN` maintenance work одновременно.

### Current-state interpretation
- current `OPEN` work означает,
  что Asset currently under maintenance;
- `COMPLETED` work означает,
  что corresponding maintenance loop завершен;
- completion убирает current open maintenance representation,
  но не канонизирует inventory readiness automatically.

### History rule
Separate canonical maintenance history surface
beyond current-state representation
в first contour не требуется.

Audit trail остается достаточным minimum fact trail
до отдельного последующего решения.

## Closeout Clarification

Текущая maintenance ER-модель
считается достаточной
для narrow current maintenance stage.

### Current interpretation
Минимальной модели `maintenance_works`
достаточно,
чтобы представить:
- current open maintenance work;
- completed maintenance work;
- minimal maintenance-side serviceability outcome.

### Deferred by default
Separate maintenance history surface / model
beyond current-state representation + audit
по-прежнему остается deferred
и не открывается как default next step.