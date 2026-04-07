# asset-service

`asset-service` owns the Asset Management bounded context for the Foundation Slice and MVP.

## Current State

- Asset Registry Core for Asset master record
- Versioned CRUD v1 for `Asset`
- Tenant-scoped storage and repository enforcement
- Local durable audit for create/update asset
- GET-based tenant-scoped validation of `assigned_user_id` and `location_id` via `user-service`
- Asset-scoped responsibility command flows and operational history/audit read endpoints are implemented
- No delete semantics

## Intent

This module intentionally keeps responsibility-changing flows out of generic CRUD.
Generic CRUD remains limited to the Asset master record, while responsibility changes stay on dedicated command paths.

## Source of Truth

Use the root `Documentation/` directory for architecture, domain, API, and ADR decisions.

## Test Verification

- Asset-service tests require Java 17.
- Canonical command: `./gradlew :services:asset-service:test`
