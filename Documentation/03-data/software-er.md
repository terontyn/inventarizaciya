# Software ER

Status: draft
Owner: architecture

## Purpose
Минимальная ER-модель первого Software contour.

## Scope
Этот документ покрывает только:
- software-side record representation
- relation to tracked users
- relation to tracked assets
- current allocation visibility

## Data Ownership
- software tables принадлежат software context
- `user_id` является cross-context reference на existing user domain
- `asset_id` является cross-context reference на `asset-service`
- software context не канонизирует дублирование asset master fields
- software context не канонизирует inventory custody / readiness ownership
- software context не канонизирует maintenance ownership

## Minimality Rule
Первый contour не добавляет:
- procurement / renewals model
- supplier references
- finance / depreciation fields
- compliance-heavy SAM model
- reporting-first model

## Minimal Software Representation

### Tables

#### software_records
Minimal conceptual fields:
- `id`
- `tenant_id`
- `name`

#### software_user_allocations
Minimal conceptual fields:
- `id`
- `tenant_id`
- `software_record_id`
- `user_id`

#### software_asset_allocations
Minimal conceptual fields:
- `id`
- `tenant_id`
- `software_record_id`
- `asset_id`

## Constraints
- все software tables tenant-scoped;
- `user_id` является cross-context reference на existing user domain;
- `asset_id` является cross-context reference на `asset-service`;
- software context не канонизирует дублирование user/asset master fields;
- first contour канонизирует only current allocation visibility;
- duplicate current relations должны быть запрещены на уровне current-state representation.

## Minimality rule
Первый contour не фиксирует:
- renewals model;
- supplier / vendor model;
- finance / depreciation fields;
- compliance-heavy license optimization model;
- software history beyond current visibility;
- reporting-heavy software model.

## Not canonized in first contour
В этом contour не фиксируются как обязательные business fields:
- `type`
- `key`
- `seat_like_count`
- `status`

## Current-State Allocation Clarification

### Current relation meaning
`software_user_allocations`
и `software_asset_allocations`
представляют only current relations.

Следовательно:
- наличие allocation row
  означает,
  что corresponding current relation существует сейчас;
- отсутствие allocation row
  означает,
  что corresponding current relation сейчас не существует.

### Duplicate current relation meaning
Duplicate current relation
означает повтор одной и той же tenant-scoped current pair:

- `software_record_id + user_id`
- `software_record_id + asset_id`

Такие дубликаты должны быть запрещены
на уровне current-state representation.

### History rule
Separate canonical software history surface
beyond current visibility
в first contour не требуется.

Audit trail остается достаточным minimum fact trail
до отдельного последующего решения.