# Productized Tenant-Facing Operational UX — Scope for Implementation

Status: candidate
Owner: product
Filename: productized-tenant-facing-operational-ux-scope.md

## 1. Decision

Approved direction-gate movement:

**Productized Tenant-Facing Operational UX Expansion**

This scope packet defines the next UX development scope required to move the existing pilot-level UI toward a more productized tenant-facing operational UX.

This is not:
- pilot testing;
- managed demo workstream;
- pilot readiness re-check;
- release-ready claim;
- new bounded context;
- new backend/API scope by default.

Implementation remains blocked until this scope packet is explicitly approved.

## 2. Correct Interpretation

The project already has:
- internally validated MVP / Pilot baseline;
- existing Pilot UI as managed pilot / internal pilot surface;
- completed narrow Wave 2 operational surfaces;
- no executable First-Tenant Operational Validation until a real tenant / first adopter / real usage loop exists.

Therefore, the next UX work is not about “staying in pilot”.

The correct interpretation is:

**Productize the existing operational UI experience for tenant-facing usage over already approved MVP and narrow Wave 2 surfaces.**

## 3. Productized UX Goal

The goal is to make the existing operational surfaces:

- easier to navigate;
- clearer by role;
- more continuous between registry, card, flows, history and current-state visibility;
- understandable for day-to-day tenant-facing operational use;
- ready for later First-Tenant Operational Validation evidence capture once a real tenant exists.

The goal is not to add new business domains or new backend capabilities.

## 4. Included Personas

Included personas:

### 4.1 Tenant Admin

Tenant Admin can access and manage setup/reference-data surfaces for:

- roles;
- users;
- departments;
- branches;
- locations.

Tenant Admin journey must clarify setup sequence and missing prerequisites.

### 4.2 Asset Operator

Asset Operator can use operational asset surfaces:

- asset registry;
- asset card;
- create asset;
- update asset master record;
- assign asset;
- transfer asset;
- return asset;
- view relevant current-state operational information.

Asset Operator must not receive reference-data management UX as a default role capability.

### 4.3 Support Reviewer

Support Reviewer receives read-only operational visibility:

- registry;
- asset card;
- history / audit;
- current-state operational visibility where available.

Support Reviewer must not receive write entry points.

### 4.4 Other Personas

No additional personas are included unless separately grounded and approved.

## 5. Included Existing Surfaces

### 5.1 MVP Asset Surfaces

Included:

- asset registry;
- asset card;
- asset create;
- asset update;
- dedicated assign flow;
- dedicated transfer flow;
- dedicated return flow;
- history / audit read visibility.

Responsibility-changing operations must remain dedicated flows.
They must not be hidden inside generic asset edit.

### 5.2 Admin / Setup Surfaces

Included:

- roles;
- users;
- departments;
- branches;
- locations.

UX must make setup dependencies clear, especially before first asset create / assign / transfer / return.

### 5.3 Inventory Visibility

Included only as visibility over existing inventory surface, if present in UI:

- assets in storage;
- warehouse / storage placement;
- readiness: ready / not ready;
- link or section from asset card where relevant.

This does not open WMS, stocktaking, reconciliation, barcode, scanning or warehouse-heavy expansion.

### 5.4 Maintenance Visibility

Included only as visibility over existing maintenance surface, if present in UI:

- current open maintenance work;
- work type;
- serviceability outcome;
- link or section from asset card where relevant.

This does not open vendor workflow, cost workflow, cancellation/reopen semantics or ITSM.

### 5.5 Software Visibility

Included only as visibility over existing software surface, if present in UI:

- current software records;
- current software-to-user relations;
- current software-to-asset relations;
- asset-linked software visibility.

This does not open SAM, procurement, renewals, supplier, finance or compliance-heavy scope.

### 5.6 Ticket Visibility

Included only as visibility over existing ticket surface, if present in UI:

- current open tickets;
- single ticket visibility;
- asset-linked current ticket visibility.

This does not open requester / assignee model, queue, SLA, escalation, omnichannel request model or generic helpdesk.

### 5.7 Reporting Visibility

Included only as current-state operational visibility:

- asset-centric current operational view;
- in storage / ready / not ready;
- current open maintenance;
- current open tickets;
- current software relations.

Reporting remains read-only, derived, current-state-first and asset-centric.

It must not become:
- dashboard suite;
- BI platform;
- free-form report builder;
- historical analytics layer;
- source of truth;
- orchestration layer.

## 6. Included User Journeys

### 6.1 Tenant Admin Setup Journey

Scope includes UX refinement for:

1. roles;
2. users with role assignment;
3. branches;
4. locations;
5. departments.

Required UX outcomes:

- clear setup landing;
- visible missing prerequisites;
- empty states explaining what must be created first;
- success states after setup actions;
- clear separation between setup surfaces and operator surfaces.

### 6.2 Asset Operator Registry-to-Card Journey

Scope includes UX refinement for:

- entering asset registry;
- identifying an asset;
- opening asset card;
- seeing current state;
- choosing the next valid operational action;
- returning to registry after action completion.

### 6.3 Asset Create / Update Journey

Scope includes UX refinement for:

- asset create;
- asset master-record update;
- clear distinction between master fields and responsibility-changing flows;
- validation / error copy;
- success confirmation;
- return to asset card.

Out of scope:

- editing assigned user or location through generic asset edit if that would bypass dedicated flows.

### 6.4 Responsibility Flow Journey

Scope includes UX refinement for dedicated:

- assign;
- transfer;
- return.

Each flow must include:

- clear intent;
- required inputs;
- precondition messaging;
- success state;
- failure state;
- return path to asset card / registry;
- history/audit visibility after action where available.

### 6.5 History / Audit Review Journey

Scope includes UX refinement for:

- asset history visibility;
- audit visibility for critical actions;
- read-only review path for Support Reviewer;
- clear “who changed what and when” interpretation.

### 6.6 Current-State Operational Visibility Journey

Scope includes UX refinement for showing relevant current-state facts across existing surfaces:

- storage / readiness;
- maintenance;
- software;
- tickets;
- reporting-derived current view.

The UX must make clear when a view is owner-context direct visibility and when it is derived reporting visibility.

### 6.7 Role-Based Visibility Journey

Scope includes UX refinement for:

- visible but disabled actions only where helpful;
- hidden actions where role should not see them;
- read-only states for Support Reviewer;
- clear navigation differences between Tenant Admin, Asset Operator and Support Reviewer.

This does not create a new RBAC model.

## 7. Explicit Non-Goals

This scope does not include:

- production auth;
- IAM platform;
- self-service onboarding;
- SSO;
- MFA;
- SCIM;
- IdP integration;
- new auth service;
- IAM backend;
- BFF;
- new UI backend;
- new bounded context;
- new Wave map;
- new backend/API family;
- new endpoints solely for UI convenience;
- BI / dashboard / analytics expansion;
- historical reporting;
- KPI catalog;
- free-form report builder;
- WMS;
- stocktaking;
- reconciliation;
- barcode / QR;
- printer integration;
- scanning;
- bulk print;
- ITSM;
- helpdesk queue;
- SLA / escalation;
- requester / assignee ticket model;
- SAM;
- procurement;
- renewals;
- supplier / finance / compliance-heavy software scope;
- release-ready tenant-facing claim;
- First-Tenant Operational Validation claim;
- synthetic tests as replacement for real tenant validation.

## 8. Backend/API Change Policy

Default policy:

**No backend/API changes.**

The UX scope must first consume existing APIs and existing domain boundaries.

A backend/API change can be proposed only if all conditions are met:

1. the UX journey is already approved in this scope;
2. existing API cannot support it;
3. UX-only workaround would misrepresent domain meaning;
4. the change is minimal;
5. the change does not create:
   - new owner;
   - new standalone model family;
   - new primary API family;
   - orchestration layer;
   - BI/reporting capability family;
   - IAM/auth platform scope.

Any such change must be documented separately before implementation.

## 9. Recommended UX Scope

Recommended scope name:

**Productized Tenant-Facing Operational UX v1**

Included work:

- role-aligned navigation shell;
- Tenant Admin setup area refinement;
- Asset Operator operational area refinement;
- Support Reviewer read-only review area refinement;
- asset registry UX refinement;
- asset card UX refinement;
- create/update asset UX refinement;
- dedicated assign / transfer / return UX refinement;
- history/audit visibility refinement;
- current-state operational visibility across existing surfaces;
- UX copy;
- empty states;
- error states;
- success states;
- continuity between surfaces;
- clear owner-vs-derived visibility copy where reporting is involved.

## 10. Acceptance Criteria

This scope is ready for implementation planning when:

- included personas are fixed;
- included surfaces are listed;
- included journeys are described;
- explicit non-goals are documented;
- backend/API policy is documented;
- each journey has entry point, success state, failure/empty state and return path;
- role-based visibility is defined without creating new RBAC semantics;
- current-state visibility remains inside existing domain boundaries;
- reporting remains read-only and derived;
- no release-ready claim is made;
- no First-Tenant Operational Validation claim is made;
- stop conditions are listed;
- docs delta is approved.

UX scope is done when:

- a Codex-ready implementation planning packet can be prepared without guessing product intent;
- implementation can proceed using existing backend/API surfaces by default;
- any exception is separately justified before code work.

## 11. Stop Conditions

Stop and return to product/architecture decision if work requires:

- new backend endpoint solely for UI convenience;
- new API/read-model family;
- new bounded context;
- new BFF;
- new auth service;
- production identity establishment;
- self-service onboarding;
- SSO / MFA / SCIM / IdP;
- dashboard / BI / analytics expansion;
- historical reporting;
- WMS / stocktaking / reconciliation;
- ITSM / queue / SLA / escalation;
- SAM / procurement / renewals / finance / compliance;
- barcode / QR / printer / scanning integration;
- orchestration layer;
- reporting-side write ownership;
- release-ready claim;
- real tenant validation claim without real tenant evidence.

## 12. Candidate Docs Delta

Create:

- `/Documentation/06-delivery/productized-tenant-facing-operational-ux-scope.md`

After approval, optionally align:

- `/Documentation/06-delivery/roadmap.md`
- `/Documentation/06-delivery/release-plan.md`
- `/Documentation/00-overview/system-summary.md`

Do not update domain/API docs unless a separately justified backend/API exception is approved.

## 13. Next Step After Approval

After this scope packet is approved, prepare:

**Implementation Planning Packet for Productized Tenant-Facing Operational UX v1**

That packet should be Codex-ready and must include:

- exact screens / routes only after product scope approval;
- per-persona navigation;
- per-surface UX tasks;
- no backend/API changes unless separately justified;
- stop conditions preserved.

No code implementation should begin before this scope packet is approved.