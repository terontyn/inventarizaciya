# reporting-service

`reporting-service` owns the first narrow Reporting contour as an asset-centric, read-only, cross-context operational visibility layer.

## Current State

- Versioned public APIs are implemented only for read-only operational visibility:
  - `GET /api/v1/reporting/assets`
  - `GET /api/v1/reporting/assets/{asset_id}`
- The service derives current visibility from existing owner-context surfaces in `asset-service`, `inventory-service`, `maintenance-service`, `software-service`, and `ticket-service`.
- The primary read model is `AssetOperationalCurrentView`.
- Partial current-state composition is allowed; unavailable secondary slices are exposed safely without inventing replacement facts.
- Owner contexts remain authoritative for their corresponding fact slices.
- Public reporting read access is limited to `Tenant Admin`, `Asset Operator`, and `Support Reviewer`.
- The service does not expose write APIs and does not own write workflows.

## Scope Notes

- The service does not become a BI platform, KPI suite, dashboard catalog, or free-form analytics surface.
- The service does not take ownership of asset, inventory, maintenance, software, or ticket facts.
- The service does not promise a strict synchronous cross-context snapshot.
- Around ADR-0009 handoff, reporting may briefly lag inventory-derived visibility; the owner context remains authoritative.

## Test Verification

- Reporting-service tests require Java 17.
- Canonical command: `./gradlew :services:reporting-service:test`

## Source of Truth

Use the root `Documentation/` directory for architecture, domain, API, workflow, and ADR decisions.
