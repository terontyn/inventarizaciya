# Operational Workspace UX v2 / Release UI Layout & Navigation Cutline

Operational Workspace UX v2 scope doc: accepted
Canonical doc: approved
Implementation: implemented and accepted as frontend-only refine-in-place UI implementation

## 1. Purpose

This scope defines the docs-first scope for:

**Operational Workspace UX v2 / Release UI Layout & Navigation Cutline**

This scope is a release-facing layout, navigation and readability refinement over the already implemented Productized Tenant-Facing Operational UX v1.

It defines the approved scope boundary only.

It is not:
- an implementation packet;
- a Codex task;
- a release-ready claim;
- a First-Tenant Operational Validation claim;
- a backend/API change request;
- a BFF / UI backend request;
- an auth/IAM expansion;
- a new bounded context;
- a new Wave 2 product surface.

The purpose is to define how the existing operational UI should be structured so that it feels less like a long vertical-scroll workspace and more like a clear tenant-facing operational workspace.

## 2. Current Baseline

Current baseline:

**internally validated backend + minimal pilot UI/access baseline with Productized Tenant-Facing Operational UX v1 implemented**

`Productized Tenant-Facing Operational UX v1` is already closed as:
- implemented;
- accepted;
- frontend-only UX refinement;
- existing static Pilot UI refinement;
- no production backend/API changes;
- test-only smoke coverage updated;
- no new bounded context;
- no BFF / UI backend;
- no auth/IAM expansion;
- no new Wave 2 UI surfaces;
- no release-ready claim;
- no First-Tenant Operational Validation claim.

## 3. Current UI Problem Statement

The current UI is working, but it still has release-facing usability and navigation friction.

Observed problem:
- the workspace still feels like a long vertical-scroll flow;
- registry, asset card, forms, responsibility flows, history, audit and setup are not separated strongly enough;
- navigation hierarchy is weak for tenant-facing operational use;
- history/audit are overloaded and hard to read quickly;
- some wording is too technical;
- RU/EN wording is mixed;
- UUID-heavy display reduces readability;
- continuity between registry, card, actions and review areas can be improved.

This problem is a UX structure, navigation and readability problem.

It is not evidence that the product needs:
- dashboard;
- BI;
- analytics;
- Asset 360;
- global search;
- saved views;
- bulk actions;
- backend/API expansion.

## 4. Why This Is Separate From Productized UX v1

Productized Tenant-Facing Operational UX v1 already refined the existing static Pilot UI into a more productized operational experience.

Operational Workspace UX v2 is separate because it addresses the next layer of friction:

- v1 improved the existing tenant-facing operational surfaces;
- v2 defines the release-facing workspace layout and navigation cutline;
- v2 does not reopen v1 as a greenfield implementation;
- v2 does not add new backend capabilities;
- v2 does not add new product domains;
- v2 does not make the product release-ready by itself.

Correct interpretation:

**UX v2 is a layout/navigation cutline over existing accepted UX v1 surfaces.**

## 5. Included Personas

Included personas remain unchanged.

### 5.1 Tenant Admin

Tenant Admin needs clear access to setup/admin surfaces:

- roles;
- users;
- departments;
- branches;
- locations.

UX goal:
- make setup sequence clearer;
- show missing prerequisites;
- separate setup/admin from asset operations;
- avoid making setup feel like a mixed part of asset card workflow.

### 5.2 Asset Operator

Asset Operator needs clear operational asset work paths:

- asset registry;
- asset detail/card;
- asset create;
- asset update;
- assign;
- transfer;
- return;
- history/audit review where relevant.

UX goal:
- make the registry-to-card-to-action path obvious;
- keep dedicated responsibility flows separate from generic master-record edit;
- provide clear return path after each action;
- avoid exposing setup/admin management as default operator workflow.

### 5.3 Support Reviewer

Support Reviewer needs read-only operational visibility:

- asset registry;
- asset detail/card;
- history;
- audit;
- current state visible through existing surfaces.

UX goal:
- make review path clear;
- make history/audit readable;
- hide or disable write actions according to existing role boundaries;
- avoid creating new write permissions or new RBAC semantics.

## 6. Included Existing Surfaces

This scope includes only already existing surfaces.

### 6.1 Workspace Landing

Allowed:
- release-facing workspace entry point;
- role-aware orientation;
- navigation to existing surfaces;
- short explanation of available work areas.

Not allowed:
- KPI dashboard;
- BI widgets;
- analytics summaries;
- cross-context executive overview;
- new reporting module.

### 6.2 Asset Registry

Allowed:
- clearer registry page/section;
- improved row readability;
- clearer asset identity display;
- better current-state summary using already available fields;
- link/open path to asset card;
- local UI-only sorting/grouping only where it can be done over already available frontend data without backend/API assumptions.

Not allowed:
- global search;
- Elasticsearch;
- saved views;
- persistent filters/preferences;
- bulk actions;
- new server-side filtering requirements;
- new registry read model.

### 6.3 Asset Detail / Card

Allowed:
- structured asset card layout;
- local sections or local tabs inside the existing asset surface;
- better visual separation between master data, current state, actions and review;
- contextual action area for assign / transfer / return;
- improved return path to registry.

Important guard:

Local tabs or sections are layout-only inside the existing asset surface.

They must not become:
- Asset 360;
- cross-context tabs;
- cross-context aggregation;
- new owner-context composition.

Allowed local sections:
- Overview;
- Master Data;
- Current Responsibility;
- Actions;
- History / Audit.

Not allowed:
- Asset 360 cross-context implementation;
- tickets / maintenance / software / inventory tabs by default;
- new cross-context aggregation;
- new backend read model;
- new owner-context composition.

### 6.4 Asset Create / Update

Allowed:
- clearer create/update forms;
- better helper copy;
- better validation/error/success copy;
- clear distinction between master-record edit and responsibility-changing flows.

Not allowed:
- changing responsibility through generic edit if it bypasses dedicated assign / transfer / return flows;
- new fields not already supported by existing contracts;
- new DTO assumptions.

### 6.5 Assign / Transfer / Return

Allowed:
- dedicated action pages/sections/modals over existing flows;
- clearer intent copy;
- clearer required inputs;
- clearer success/failure states;
- visible return path to asset card / registry;
- history/audit visibility after completion where already available.

Not allowed:
- merging responsibility flows into generic asset update;
- adding new responsibility commands;
- adding new workflow engine semantics;
- adding custom automation.

### 6.6 History / Audit

Allowed:
- improved readability;
- clearer event grouping;
- human-readable labels;
- de-emphasized raw UUIDs;
- clearer “who / what / when / result” structure;
- separate history and audit views if this is frontend-only over existing data.

Not allowed:
- new audit backend;
- new history model;
- new event contract;
- new analytics/history reporting layer;
- compliance-heavy audit expansion.

### 6.7 Admin / Setup

Allowed:
- clearer setup navigation;
- separate setup area from asset operations;
- sections for existing reference data:
  - roles;
  - users;
  - departments;
  - branches;
  - locations;
- setup sequence guidance;
- missing prerequisite empty states.

Not allowed:
- new master-data module;
- import engine;
- SCIM;
- IdP integration;
- IAM platform;
- self-service onboarding;
- new permission model.

## 7. Proposed UX v2 Cutline

The UX v2 cutline is:

**Restructure the existing operational UI into a release-facing workspace with clear navigation hierarchy, section separation and readability polish, without adding new backend/API capabilities or new product surfaces.**

Allowed UX changes:
- workspace shell;
- navigation hierarchy;
- page/section grouping;
- local tabs inside existing surfaces;
- local anchors;
- contextual action areas;
- improved copy;
- improved empty/loading/error/success states;
- UUID de-emphasis;
- history/audit readability improvements;
- registry-card-action-review continuity improvements.

Forbidden scope:
- dashboard;
- BI;
- analytics;
- Asset 360;
- global search;
- saved views;
- persistent user preferences;
- bulk actions;
- BFF;
- UI backend;
- backend/API changes by default;
- new endpoints;
- DTO/read-model assumptions;
- auth/IAM expansion;
- new bounded context;
- new Wave 2 surfaces.

## 8. Target Layout / Navigation Model

### 8.1 Workspace Structure

Target structure:

1. Workspace Landing
2. Asset Registry
3. Asset Detail / Card
4. Responsibility Actions
5. History / Audit Review
6. Admin / Setup

This is a navigation model over existing surfaces.

It is not a new product module map.

### 8.2 Navigation Hierarchy

Allowed hierarchy:

- Primary navigation:
  - Workspace
  - Assets
  - Review
  - Setup

- Secondary navigation inside Assets:
  - Registry
  - Asset Card
  - Create / Update
  - Assign / Transfer / Return

- Secondary navigation inside Review:
  - History
  - Audit

- Secondary navigation inside Setup:
  - Roles
  - Users
  - Departments
  - Branches
  - Locations

This hierarchy must remain role-aware:
- Tenant Admin sees setup/admin management;
- Asset Operator sees operational asset actions;
- Support Reviewer sees read-only review paths.

### 8.3 Asset Card Sections

Allowed asset card sections:

- Overview
- Master Data
- Current Responsibility
- Actions
- History / Audit

Important rule:
- “Actions” may expose assign / transfer / return entry points;
- master data edit must not replace dedicated responsibility flows;
- local tabs or sections are layout-only inside the existing asset surface;
- tabs must not become Asset 360 or cross-context composition.

### 8.4 How To Remove Long-Scroll Feeling

The long-scroll feeling should be reduced by:

- splitting large vertical blocks into navigable surfaces;
- using local section tabs or anchors;
- keeping contextual actions near the asset card;
- moving setup/admin into its own navigation area;
- making history/audit a focused review area;
- reducing raw technical payload display;
- using readable labels and summaries before raw IDs.

This must be achieved without backend/API changes by default.

## 9. UX Polish Scope

### 9.1 Remove Technical Wording

Replace internal/technical wording with operational wording.

Examples:
- “header-based context” should be hidden or explained only where necessary;
- “payload” should not be shown as primary user wording;
- raw service names should not dominate user-facing copy;
- internal technical failures should be translated into operational guidance.

### 9.2 Unify Language

The UI should use consistent Russian user-facing copy.

Allowed exceptions:
- stable product/domain terms that are intentionally kept in English;
- technical labels only where needed for internal/admin clarity.

Mixed RU/EN copy should be reduced.

### 9.3 De-emphasize UUIDs

UUIDs should not be the primary way users recognize assets, users, branches or locations.

Preferred display:
- asset tag;
- asset type;
- serial number;
- user display name where available;
- location/branch name where available;
- UUID only as secondary/debug/reference detail.

If only UUID is available from existing data:
- show it in secondary style;
- avoid making it the main table/card headline.

No new backend lookup may be assumed only to hide UUIDs.

### 9.4 Improve History / Audit Readability

History/audit should be easier to read as operational evidence.

Preferred structure:
- event type;
- actor / user;
- changed object;
- previous value;
- new value;
- timestamp;
- correlation/reference detail only as secondary information.

Raw audit JSON/payload should not be the main first-level presentation unless no other frontend-only interpretation is possible.

### 9.5 Improve Registry / Card / Action Continuity

The user should clearly understand:

- how to enter registry;
- how to open card;
- what current state means;
- which actions are valid next;
- what happened after assign / transfer / return;
- how to return to card or registry;
- where to review history/audit after an action.

## 10. Frontend-Only Definition

A change is frontend-only if it:

- uses existing static UI delivery model;
- uses existing service contracts;
- does not require new backend endpoints;
- does not require DTO changes;
- does not require new read models;
- does not require server-side persistence;
- does not require new auth/IAM behavior;
- does not require BFF / UI backend;
- does not require new bounded context;
- does not require new cross-service orchestration.

Examples of frontend-only allowed changes:
- layout restructuring;
- navigation grouping;
- local tabs/anchors;
- copy cleanup;
- table/card readability improvements;
- visual hierarchy;
- empty/loading/error/success state wording;
- local display formatting;
- secondary display of UUIDs;
- role-aware hiding/disabled states based on already available role context;
- local sorting/grouping over already available frontend data.

## 11. Backend/API Cutline

Backend/API changes are out of scope by default.

The following are not allowed inside this scope:

- new endpoints;
- endpoint shape changes;
- new DTO fields;
- new read models;
- new BFF;
- new UI backend;
- new aggregation service;
- new cross-context composition API;
- new auth/IAM model;
- new persistence for user preferences;
- saved views;
- server-side bulk operations;
- global search infrastructure;
- Elasticsearch;
- dashboard/BI data APIs.

If a UX idea requires any of the above, it must be removed from this scope or escalated into a separate product/architecture decision.

## 12. Explicit Non-Goals

This scope does not include:

- implementation packet;
- Codex task;
- release-ready claim;
- First-Tenant Operational Validation claim;
- dashboard;
- BI;
- analytics;
- KPI catalog;
- reporting center;
- Asset 360 cross-context implementation;
- Service Desk integration;
- Maintenance integration;
- Software integration;
- Inventory tabs by default;
- global search;
- Elasticsearch;
- saved views;
- persistent user preferences;
- bulk actions;
- backend/API changes by default;
- new endpoints;
- DTO/read-model assumptions;
- BFF;
- UI backend;
- auth/IAM expansion;
- self-service onboarding;
- SSO;
- MFA;
- SCIM;
- IdP integration;
- new bounded context;
- new Wave 2 surfaces;
- workflow automation;
- import/export engine;
- barcode / QR / scanning;
- printer integration;
- bulk print.

## 13. Stop Conditions

Stop the scope or return to product/architecture review if any of the following appears:

1. The design requires a new backend endpoint.
2. The design requires a new DTO or read model.
3. The design requires a BFF or UI backend.
4. The design requires saved server-side user preferences.
5. The design introduces bulk actions.
6. The design introduces global search or Elasticsearch.
7. The design becomes a dashboard or BI surface.
8. The design becomes Asset 360 or cross-context implementation.
9. The design opens Service Desk / Maintenance / Software / Inventory tabs by default.
10. The design changes auth/IAM semantics.
11. The design creates a new bounded context.
12. The design claims release readiness.
13. The design claims First-Tenant Operational Validation.
14. The design reopens Productized UX v1 as greenfield implementation.

## 14. Acceptance Criteria

This scope packet is acceptable when:

- it defines UX v2 as release UI layout/navigation cutline;
- it keeps the work frontend-only;
- it includes only existing surfaces;
- it preserves current personas:
  - Tenant Admin;
  - Asset Operator;
  - Support Reviewer;
- it defines a clear workspace/navigation hierarchy;
- it explains how to reduce long-scroll feeling;
- it improves registry/card/action/history continuity;
- it improves history/audit readability;
- it removes or reduces technical wording;
- it unifies user-facing language;
- it de-emphasizes UUID-heavy display;
- it explicitly rejects backend/API changes by default;
- it explicitly rejects BFF/UI backend;
- it explicitly rejects auth/IAM expansion;
- it explicitly rejects dashboard/BI/analytics;
- it explicitly rejects Asset 360 cross-context implementation;
- it explicitly rejects global search/saved views/bulk actions;
- it does not make release-ready claim;
- it does not make First-Tenant Operational Validation claim;
- it does not authorize implementation until separately approved.

## 15. Documentation Follow-up

This canonical scope document is:

`/Documentation/06-delivery/operational-workspace-ux-v2-release-layout-navigation-cutline.md`

Do not update immediately as part of the same change:
- `/Documentation/06-delivery/roadmap.md`;
- `/Documentation/06-delivery/release-plan.md`;
- `/Documentation/00-overview/system-summary.md`.

Optional follow-up docs alignment may add short pointers later, but only as pointers.

Those pointers must state that:
- Operational Workspace UX v2 is approved as scope only;
- it remains frontend-only;
- it is not an implementation packet;
- it does not authorize Codex/code work;
- it does not create release-ready or First-Tenant Operational Validation claim.

## 16. Review Outcome

Scope approval review verdict:

**approved after minor corrections applied**

This document is accepted as the canonical scope doc for:

**Operational Workspace UX v2 / Release UI Layout & Navigation Cutline**

This approval does not authorize implementation, Codex task creation, backend/API work, BFF/UI backend, auth/IAM expansion, release-ready claim or First-Tenant Operational Validation claim.

Any implementation planning must be opened separately after this canonical scope doc exists.

## 17. Final Scope Position

Operational Workspace UX v2 is approved as a canonical scope boundary.

Correct final interpretation:

**Operational Workspace UX v2 / Release UI Layout & Navigation Cutline is a frontend-only workspace structure, navigation and readability refinement over existing accepted operational UI surfaces.**

It is not:
- release-ready claim;
- First-Tenant Operational Validation claim;
- backend/API expansion;
- new bounded context;
- new Wave 2 surface;
- Asset 360;
- dashboard/BI;
- global search;
- saved views;
- bulk actions;
- implementation packet.



## 18. Implementation Closeout

Operational Workspace UX v2 has been implemented and accepted.

The implementation is accepted as:

**frontend-only refine-in-place layout, navigation and readability refinement over existing accepted operational UI surfaces.**

### Closeout facts

The implementation refined the existing static UI in place.

It improved:

- workspace layout;
- navigation hierarchy;
- section separation;
- registry / card / action / review / setup continuity;
- history / audit readability;
- RU-facing copy consistency;
- technical wording cleanup.

Codex-reported changed files:

- `app.js`;
- `index.html`;
- `app.css`;
- `MinimalPilotUiSmokeTest.java`.

Correction pass changed:

- `app.js`;
- `index.html`;
- `MinimalPilotUiSmokeTest.java`.

Verification notes:

- focused smoke passed;
- full service test suite passed before correction pass;
- focused UI smoke passed after correction pass;
- Browser visual smoke could not run due Node OS access denied;
- video review accepted UX result after correction pass.

### Boundary preservation

Implementation did not introduce:

- backend/API changes;
- new endpoints;
- DTO / read-model changes;
- BFF / UI backend;
- auth/IAM expansion;
- new frontend framework / router / microfrontend / UI service;
- new bounded context;
- new Wave 2 surface;
- dashboard / BI / analytics;
- Asset 360;
- global search / Elasticsearch;
- saved views / persistent preferences;
- bulk actions.

Assign / transfer / return remain dedicated responsibility flows.

Generic edit remains master-record-only.

### Non-claim rule

This implementation closeout does not create:

- release-ready claim;
- production tenant readiness claim;
- First-Tenant Operational Validation claim;
- real tenant validation claim;
- real day-to-day operational validation claim.

The project remains under the existing validation ceiling:

**Project Stabilization / First-Adopter-Gated Hold**