# user-service

`user-service` owns the Identity / Organization bounded context for the Foundation Slice.

## Current State

- Bootstrap skeleton only
- No business endpoints
- No DTO or payload definitions
- No domain entities or repositories
- No domain migrations

## Next Intended Slice

The next implementation task can safely build Slice 1 here:

- tenant boundary
- tenant-scoped departments, branches, and locations
- tenant enforcement at API and repository boundaries
- durable local audit for critical writes

## Source of Truth

Use the root `Documentation/` directory for architecture, domain, API, and ADR decisions.
