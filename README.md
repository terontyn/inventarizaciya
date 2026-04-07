# IT Asset Management SaaS Monorepo

This repository is the working monorepo for the IT Asset Management SaaS project.
Canonical product, architecture, domain, data, API, and ADR documents stay in the
root `Documentation/` directory and remain the source of truth.

## Current Status

- `services/user-service/` already contains Slice 1 of the Foundation Slice.
- Slice 1 currently covers tenant boundary, tenant-scoped org references, tenant enforcement, and durable local audit for critical writes.
- `services/asset-service/` already contains the Asset Registry Core plus the currently implemented hardening and responsibility/history slices.
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
- `asset-service` is implemented and should be treated as an active service module rather than a placeholder.
- Service boundaries stay explicit; there is no shared business/domain module.
