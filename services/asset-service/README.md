# asset-service

`asset-service` owns the Asset Management bounded context for the Foundation Slice and MVP.

## Current State

- Asset Registry Core for Asset master record
- Versioned CRUD v1 for `Asset`
- Tenant-scoped storage and repository enforcement
- Local durable audit for create/update asset
- GET-based tenant-scoped validation of `assigned_user_id` and `location_id` via `user-service`
- No delete semantics or responsibility-flow public APIs

## Intent

This module intentionally keeps responsibility-changing flows out of generic CRUD.
Assignment / transfer / return / retire remain outside this slice.

## Source of Truth

Use the root `Documentation/` directory for architecture, domain, API, and ADR decisions.

## Test Verification

- Asset-service tests require Java 17.
- Canonical command: `./gradlew :services:asset-service:test`
