# user-service

`user-service` owns the Identity / Organization bounded context for the Foundation Slice and the managed-pilot setup/admin path.

## Current State

- Versioned tenant, role, user, department, branch, and location APIs are implemented.
- Tenant-scoped user-role binding is implemented at `POST /api/v1/users/{userId}/roles`.
- Pilot RBAC is implemented for `Tenant Admin`, `Asset Operator`, and `Support Reviewer`.
- Tenant Admin owns reference-data mutation for roles, users, departments, branches, and locations.
- Asset Operator and Support Reviewer keep read-only reference access; they do not manage reference data.
- Durable local audit is recorded for critical writes.
- Internal tenant-scoped role lookup is exposed for `asset-service` at `/internal/api/v1/users/{userId}/roles`.
- Browser CORS is enabled for the minimal pilot UI origin so the static UI served by `asset-service` can call existing public and internal contracts without a BFF.

## Managed Pilot Setup Path

After a managed/bootstrap tenant admin already exists, the currently implemented setup path is:

- roles
- pilot users with role assignment
- branches
- locations
- departments

This service does not introduce self-service onboarding or bootstrap provisioning of the very first tenant admin.

## Test Verification

- User-service tests require Java 17.
- Canonical command: `./gradlew :services:user-service:test`

## Source of Truth

Use the root `Documentation/` directory for architecture, domain, API, and ADR decisions.
