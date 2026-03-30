# IT Asset Management SaaS Monorepo

This repository is the bootstrap monorepo for the IT Asset Management SaaS project.
Canonical product, architecture, domain, data, API, and ADR documents stay in the
root `Documentation/` directory and remain the source of truth.

## Repository Layout

- `Documentation/` keeps the approved and draft project documentation in the root.
- `services/user-service/` is the first real implementation target for Foundation Slice 1.
- `services/asset-service/` is present as a skeleton for the next service slice.
- `ops/` contains local docker, Kubernetes placeholders, and monitoring scaffolding.
- `scripts/` contains development, CI, and local database bootstrap helpers.
- `.github/workflows/` contains minimal repository checks.

## Intent

- The repository now has a service-oriented layout without adding business logic.
- Business payloads, DTOs, endpoints, domain entities, and domain migrations are intentionally absent.
- `Documentation/` was kept as-is instead of being renamed, so existing source-of-truth paths remain stable.
- Service boundaries stay explicit; there is no shared business/domain module.

## First Service Target

`services/user-service/` is prepared for the next task, where Slice 1 can add:

- tenant boundary foundations
- tenant-scoped org references
- tenant enforcement
- durable local audit for critical writes

`services/asset-service/` stays as a skeleton only until the asset-focused slice starts.
