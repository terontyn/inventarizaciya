# IT Asset Management SaaS Monorepo

This repository is the working monorepo for the IT Asset Management SaaS project.
Canonical product, architecture, domain, data, API, and ADR documents stay in the
root `Documentation/` directory and remain the source of truth.

## Current Status

- `services/user-service/` already contains the Slice 1 and Slice 2 baseline for tenant, org reference, user, role, and role-binding paths.
- `services/user-service/` is the managed-pilot setup surface for roles, users, departments, branches, locations, and tenant-scoped role assignment.
- `services/asset-service/` already contains Asset CRUD v1, dedicated `assign` / `transfer` / `return` flows, and `/history` + `/audit` read endpoints.
- `services/asset-service/` now also serves the minimal pilot UI contour at `/`, reusing only the current `asset-service` and `user-service` contracts.
- `services/asset-service/` keeps `retire` behind the default pilot cutline, so the endpoint remains forbidden for the approved pilot role set.
- `Documentation/` is still the canonical source of truth for scope and architecture decisions.

## Repository Layout

- `Documentation/` keeps approved and draft project documentation in the root.
- `services/user-service/` owns the Identity / Organization bounded context implementation path.
- `services/asset-service/` owns the Asset Management bounded context implementation path.
- `ops/` contains local docker, Kubernetes placeholders, and monitoring scaffolding.
- `scripts/` contains development, CI, and local database bootstrap helpers.
- `.github/workflows/` contains repository checks and service build/test workflows.

## Notes

- The repository is no longer bootstrap-only.
- Both backend services are implemented and should be treated as active service modules rather than placeholders.
- Service boundaries stay explicit; there is no shared business/domain module.
