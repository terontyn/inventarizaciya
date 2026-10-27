# Ticket ER

Status: draft
Owner: architecture

## Purpose
Минимальная ER-модель первого Ticket contour.

## Scope
Этот документ покрывает только:
- ticket-side record representation
- relation to tracked asset
- minimal current coordination semantics

## Data Ownership
- ticket tables принадлежат ticket context
- `asset_id` является cross-context reference на `asset-service`
- ticket context не канонизирует дублирование asset master fields
- ticket context не канонизирует inventory custody / readiness ownership
- ticket context не канонизирует maintenance workflow ownership
- ticket context не канонизирует software allocation ownership

## Minimal Ticket Representation

### Tables

#### tickets
Minimal conceptual fields:
- `id`
- `tenant_id`
- `asset_id`
- `ticket_state`
- `opened_at`
- `closed_at` (optional until closed)

## Constraints
- все ticket tables tenant-scoped
- `asset_id` является cross-context reference на `asset-service`
- ticket context не канонизирует дублирование asset master fields
- ticket context не канонизирует inventory custody / readiness ownership
- ticket context не канонизирует maintenance workflow ownership
- ticket context не канонизирует software allocation ownership

## Minimal vocabularies

### ticket_state
- `OPEN`
- `CLOSED`

## Current-state interpretation
- current `OPEN` ticket означает,
  что corresponding ticket-side coordination loop
  сейчас остается открытой;
- `CLOSED` ticket означает,
  что corresponding ticket-side coordination loop
  закрыт;
- `CLOSED` ticket не канонизирует automatic mutation
  в inventory / maintenance / software / responsibility lifecycle.

## Minimality Rule
Первый contour не добавляет:
- queue / SLA / escalation model
- omnichannel request model
- generic service desk taxonomy
- reporting-heavy ticket model
- umbrella workflow ownership over other contexts

## History rule
Separate canonical ticket history surface / model
beyond current-state representation + audit
в first contour не требуется.

Audit trail остается достаточным minimum fact trail
до отдельного последующего решения.

## Ticket Multiplicity Clarification

### Current-state representation rule
First contour не канонизирует uniqueness rule
`one current OPEN ticket per asset`.

Следовательно:
- один tenant-scoped asset
  может иметь multiple current `OPEN` tickets;
- `tickets` table
  не требует canonical unique constraint
  на current `OPEN` ticket per `asset_id`.

### Minimal business-field clarification
Этот contour не фиксирует
обязательные business fields
beyond already confirmed minimum:
- `asset_id`
- `ticket_state`
- `opened_at`
- `closed_at`

Fields вроде:
- requester
- assignee
- category
- queue
- SLA marker
на этом этапе не входят
в canonical minimal ER.

### Independence clarification
Если один tenant-scoped asset
имеет multiple current `OPEN` tickets,
они представляют отдельные current rows
без canonical blocker semantics между ними.

Следовательно:
- close одного ticket row
  не должен канонически означать
  close других current `OPEN` rows
  того же `asset_id`;
- current-state representation
  остается ticket-by-ticket,
  а не umbrella-per-asset.