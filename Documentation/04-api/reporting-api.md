# Reporting API

Status: draft
Owner: architecture

## Purpose
Минимальный API-контракт первого Reporting contour.

## Scope
Этот документ покрывает только:
- current-state operational reporting visibility
- asset-centric cross-context read surfaces

## API Principles
- все API versioned
- tenant isolation обязателен
- reporting context read-only on first contour
- reporting context не переопределяет ownership existing domains
- reporting context не становится BI / analytics platform API
- reporting context не канонизирует orchestration semantics



## Minimal cutline
Первый Reporting contour
должен оставаться current-state-first
и asset-centric.

## Minimal API Surface

### Read
- `GET /api/v1/reporting/assets`
- `GET /api/v1/reporting/assets/{asset_id}`

## API interpretation

### Asset list view
`GET /api/v1/reporting/assets`
должен давать asset-centric current-state operational visibility
поверх existing owner contexts.

### Single asset view
`GET /api/v1/reporting/assets/{asset_id}`
должен давать current operational picture
для одного tracked asset.

## Minimal reporting semantics
Эти read surfaces должны позволять видеть:
- где asset находится сейчас;
- ready он или not ready;
- есть ли current open maintenance work;
- есть ли current open tickets;
- какие current software relations сейчас существуют.

## Boundary rule
Этот API:
- остается read-only;
- не является KPI platform API;
- не является heavy BI API;
- не является free-form analytics API;
- не переопределяет ownership source contexts;
- не канонизирует orchestration semantics.

## Not fixed in this document
- full request / response schema
- filter / pagination canon
- historical analytics APIs
- KPI / dashboard catalog
- free-form report builder semantics
- reporting-side write ownership

## Current-Visibility Interpretation

### Read-only interpretation
`GET /api/v1/reporting/assets`
и `GET /api/v1/reporting/assets/{asset_id}`
остаются read-only surfaces
для derived operational current visibility.

Этот API:
- не канонизирует write ownership;
- не канонизирует orchestration ownership;
- не открывает business mutation workflow в reporting.

### Consistency interpretation
First contour не канонизирует strict cross-context synchronicity
для reporting responses.

Следовательно:
- response должен трактоваться
  как current operational picture
  as currently derived by reporting;
- short-lived lag
  относительно owner-context latest updates
  допустим;
- eventual consistency
  не делает reporting source of truth.

### Source-of-truth rule
При расхождении
между reporting response
и owner-context dedicated surface
authoritative source
остается owner context
для соответствующего fact slice.

### History / KPI rule
Этот API first contour
не требует:
- historical reporting APIs;
- KPI / dashboard APIs;
- free-form analytics APIs.