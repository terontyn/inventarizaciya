# inventory-service

`inventory-service` owns the first Inventory / Warehouse contour as a narrow storage-aware layer for already tracked physical assets.

## Current State

- Versioned public APIs are implemented for warehouse read, storage-place read, current storage custody read, `place-in-storage`, and `move`.
- Current storage custody uses one-current `InventoryRecord` representation per asset.
- Public inventory read access is limited to `Tenant Admin`, `Asset Operator`, and `Support Reviewer`.
- Public inventory mutate access is limited to `Tenant Admin` and `Asset Operator`.
- Cross-context asset existence and role resolution reuse existing service APIs; there is no direct DB coupling to other services.
- Critical inventory writes create durable local audit records.
- Successful `assign` in `asset-service` can trigger internal-only async inventory-side custody consumption.
- Replay of the same assign handoff is treated as an idempotent no-op when no current inventory record remains.
- `return` does not create storage custody implicitly.

## Scope Notes

- The service does not own asset identity, asset CRUD, or responsibility lifecycle.
- The service does not expose a public inventory exit API.
- The service does not implement full WMS / ERP, procurement, supplier, finance, depreciation, reporting, barcode/QR, scanning, or printer scope.

## Test Verification

- Inventory-service tests require Java 17.
- Canonical command: `./gradlew :services:inventory-service:test`

## Source of Truth

Use the root `Documentation/` directory for architecture, domain, API, workflow, and ADR decisions.
