# asset-service

`asset-service` owns the Asset Management bounded context for the Foundation Slice and MVP.

## Current State

- Asset Registry Core for Asset master record
- Versioned CRUD v1 for `Asset`
- Pilot read access is limited to `Tenant Admin`, `Asset Operator`, and `Support Reviewer`
- Pilot mutate access is limited to `Tenant Admin` and `Asset Operator`
- Tenant-scoped storage and repository enforcement
- Local durable audit for create/update asset
- GET-based tenant-scoped validation of `assigned_user_id` and `location_id` via `user-service`
- Asset-scoped responsibility command flows and operational history/audit read endpoints are implemented
- Minimal pilot UI contour is served from the same service at `/`
- Generic PATCH cannot modify `assigned_user_id` or `location_id`
- `retire` exists in the service surface but remains forbidden by the default managed-pilot cutline
- No delete semantics

## Intent

This module intentionally keeps responsibility-changing flows out of generic CRUD.
Generic CRUD remains limited to the Asset master record, while responsibility changes stay on dedicated command paths.
History and audit remain readable to the approved pilot read roles without opening any reporting/export scope.

## Minimal Pilot UI

- The service now serves a thin static UI shell at `/`.
- The UI reuses existing `asset-service` contracts for registry, card, create/update, assign, transfer, return, history, and audit.
- The UI reuses existing `user-service` contracts for roles, users, departments, branches, locations, and user-role binding.
- Generic asset edit in the UI intentionally excludes `assigned_user_id` and `location_id`; responsibility changes remain on dedicated flows.
- The UI does not surface `retire`, printing, reporting, inventory, barcode/QR, or any new backend contour.

## Source of Truth

Use the root `Documentation/` directory for architecture, domain, API, and ADR decisions.

## Test Verification

- Asset-service tests require Java 17.
- Canonical command: `./gradlew :services:asset-service:test`
