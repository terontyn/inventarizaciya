# software-service

`software-service` owns the first narrow Software contour as a software-aware operational layer for already tracked users and assets.

## Current State

- Versioned public APIs are implemented for software record read, software record detail read, current software-by-user read, current software-by-asset read, explicit record create, explicit allocate-to-user, explicit allocate-to-asset, explicit remove-from-user, and explicit remove-from-asset.
- The first contour keeps only a minimal `SoftwareRecord` field set with `name`.
- Current many-to-many software-to-user and software-to-asset relations are represented as current-state rows only.
- Duplicate current relations are rejected for both user allocations and asset allocations.
- Public software read access is limited to `Tenant Admin`, `Asset Operator`, and `Support Reviewer`.
- Public software mutate access is limited to `Tenant Admin` and `Asset Operator`.
- Cross-context user and asset existence checks reuse existing `user-service` and `asset-service` APIs; there is no direct DB coupling to other services.
- Critical software writes create durable local audit records.

## Scope Notes

- The service does not own asset identity, responsibility lifecycle, inventory custody/readiness, or maintenance workflow.
- The service does not implement full SAM, renewals, supplier, finance, compliance-heavy logic, or reporting-first software scope.
- The service does not expose a separate software history surface beyond current-state visibility and audit.

## Test Verification

- Software-service tests require Java 17.
- Canonical command: `./gradlew :services:software-service:test`

## Source of Truth

Use the root `Documentation/` directory for architecture, domain, API, workflow, and ADR decisions.
