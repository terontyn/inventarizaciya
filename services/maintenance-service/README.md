# maintenance-service

`maintenance-service` owns the first narrow Maintenance contour as a serviceability layer for already tracked physical assets.

## Current State

- Versioned public APIs are implemented for current open maintenance work read, work-by-id read, asset-scoped maintenance current view, explicit `open work`, and explicit `complete work`.
- `MaintenanceWork` uses narrow vocabularies only: `INSPECTION`, `REPAIR`, `SERVICING` and `OPEN`, `COMPLETED`.
- One asset cannot have more than one current `OPEN` maintenance work at the same time.
- Public maintenance read access is limited to `Tenant Admin`, `Asset Operator`, and `Support Reviewer`.
- Public maintenance mutate access is limited to `Tenant Admin` and `Asset Operator`.
- Cross-context asset existence and role resolution reuse existing service APIs; there is no direct DB coupling to other services.
- Critical maintenance writes create durable local audit records.
- The service does not mutate inventory readiness automatically on open or complete.

## Scope Notes

- The service does not own asset identity, asset CRUD, responsibility lifecycle, inventory custody, or inventory readiness.
- The service does not expose a separate maintenance history surface beyond audit.
- The service does not implement helpdesk / ITSM, spare-parts, supplier, finance, depreciation, reporting, vendor workflow, or cost model scope.

## Test Verification

- Maintenance-service tests require Java 17.
- Canonical command: `./gradlew :services:maintenance-service:test`

## Source of Truth

Use the root `Documentation/` directory for architecture, domain, API, workflow, and ADR decisions.
