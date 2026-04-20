# Software API

Status: draft
Owner: architecture

## Purpose
Минимальный API-контракт первого Software contour.


## API Principles
- все API versioned
- tenant isolation обязателен
- software context не переопределяет asset identity
- software context не переопределяет responsibility lifecycle
- software context не переопределяет inventory custody / readiness ownership
- software context не переопределяет maintenance ownership
- cross-context interaction идет только через reference ids


## Scope
Этот документ покрывает только:
- software record visibility
- current software-to-user allocation visibility
- current software-to-asset allocation visibility
- minimal write actions для current allocation model

## Minimal API Surface

### Read
- `GET /api/v1/software/records`
- `GET /api/v1/software/records/{id}`
- `GET /api/v1/software/users/{user_id}/software`
- `GET /api/v1/software/assets/{asset_id}/software`

### Write
- `POST /api/v1/software/records`
- `POST /api/v1/software/records/{id}/allocate-to-user`
- `POST /api/v1/software/records/{id}/allocate-to-asset`
- `POST /api/v1/software/records/{id}/remove-from-user`
- `POST /api/v1/software/records/{id}/remove-from-asset`

## Minimal command semantics

### Create software record
`POST /api/v1/software/records`

Request minimum:
- `name`

Success outcome:
- created tenant-scoped `SoftwareRecord`

### Allocate to user
`POST /api/v1/software/records/{id}/allocate-to-user`

Request minimum:
- `user_id`

Success outcome:
- current software-to-user relation exists

### Allocate to asset
`POST /api/v1/software/records/{id}/allocate-to-asset`

Request minimum:
- `asset_id`

Success outcome:
- current software-to-asset relation exists

### Remove from user
`POST /api/v1/software/records/{id}/remove-from-user`

Request minimum:
- `user_id`

Success outcome:
- current software-to-user relation no longer exists

### Remove from asset
`POST /api/v1/software/records/{id}/remove-from-asset`

Request minimum:
- `asset_id`

Success outcome:
- current software-to-asset relation no longer exists

## Boundary rule
Этот API:
- не является SAM optimization API;
- не является procurement / renewals / supplier / finance API;
- не является compliance-heavy program API;
- не переопределяет ownership user / asset;
- не переопределяет inventory readiness ownership;
- не переопределяет maintenance workflow ownership.

## Not fixed in this document
- full request / response schema
- error model
- history model
- renewals / supplier / finance surface
- compliance-heavy analytics
- reporting-heavy software read models
- richer software field model beyond current minimum

## Workflow Semantics Clarification

### Explicit action rule
Все first-contour write actions
трактуются как explicit operator actions:
- create software record
- allocate to user
- allocate to asset
- remove from user
- remove from asset

### Allocation preconditions
Allocate commands минимально предполагают:
- tenant scope корректен;
- target `SoftwareRecord` существует;
- target reference id существует
  как already tracked user или asset;
- duplicate current relation отсутствует.

### Removal preconditions
Remove commands минимально предполагают:
- tenant scope корректен;
- target `SoftwareRecord` существует;
- corresponding current relation существует.

### Boundary to existing domains
Этот API не канонизирует,
что:
- allocation to user зависит от responsibility lifecycle;
- allocation to asset зависит от inventory readiness;
- allocation to asset зависит от maintenance state.

Software relations остаются orthogonal current visibility model
поверх already tracked users и assets.

### Audit implication
Create / allocate / remove actions
должны трактоваться как critical writes
и попадать в audit.

### History implication
First contour не требует отдельного software history API
beyond current-state visibility + audit.