# ticket-service

`ticket-service` owns the first narrow Ticket contour as an operational issue / request coordination layer for already tracked assets.

## Current State

- Versioned public APIs are implemented for open ticket read, ticket-by-id read, asset-linked open ticket read, explicit `open ticket`, and explicit `close ticket`.
- `Ticket` uses narrow current-state semantics only: `OPEN` and `CLOSED`.
- Multiple current `OPEN` tickets for the same asset remain allowed in the first contour.
- Public ticket read access is limited to `Tenant Admin`, `Asset Operator`, and `Support Reviewer`.
- Public ticket mutate access is limited to `Tenant Admin` and `Asset Operator`.
- Cross-context asset existence checks reuse existing `asset-service` APIs; there is no direct DB coupling to other services.
- Critical ticket writes create durable local audit records.
- Ticket open/close does not mutate inventory, maintenance, software, or responsibility lifecycle automatically.

## Scope Notes

- The service does not own asset identity, inventory custody/readiness, maintenance workflow, or software allocations.
- The service does not implement generic helpdesk / ITSM, queue, SLA, escalation, omnichannel, or umbrella orchestration semantics.
- The service does not expose a separate ticket history surface beyond current-state visibility and audit.

## Test Verification

- Ticket-service tests require Java 17.
- Canonical command: `./gradlew :services:ticket-service:test`

## Source of Truth

Use the root `Documentation/` directory for architecture, domain, API, workflow, and ADR decisions.
