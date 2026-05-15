# Backlog Epics

Status: approved
Owner: product
Last Updated: 2026-05-14

## Wave 1 Epics

### Epic 1. Tenant & Access Foundation
Цель: заложить tenant-aware основу первого релиза.

### Epic 2. User & Org Structure
Цель: создать справочник пользователей и оргструктуры, на который будут опираться активы.

### Epic 3. Asset Registry Core
Цель: дать базовый реестр и карточку актива как ядро первой бизнес-ценности.

### Epic 4. Audit Trail Foundation
Цель: обеспечить обязательный audit и историю изменений для критических операций.

### Epic 5. Asset Accountability Flows
Цель: реализовать выдачу, перемещение и возврат как ключевой операционный value loop.

### Epic 6. MVP Pilot Readiness
Цель: довести существующий Asset Accountability MVP до состояния first-tenant operational use без расширения scope.

Acceptance criteria:
- определены pilot personas:
  - Tenant Admin
  - Asset Operator
  - Support Reviewer
- утвержден minimum permission matrix для pilot-ready состояния;
- зафиксированы allowed / forbidden boundaries для pilot roles;
- users / roles / departments / branches / locations достаточны для первого tenant;
- asset registry и asset card usable для ежедневного базового учета;
- responsibility changes не происходят через generic PATCH;
- assign / transfer / return доступны как dedicated flows;
- history / audit видимы для operational support и review;
- определен минимальный admin / support layer для первого tenant.
- зафиксирован minimum reference-data ownership / setup model для первого pilot tenant;
- подтвержден minimum tenant-managed reference-data set:
  - roles
  - users
  - departments
  - branches
  - locations
- ownership rules зафиксированы:
  - Tenant Admin создает и поддерживает reference data;
  - Asset Operator использует только разрешенный reference read-only access;
  - Support Reviewer остается read-only;
- setup order before pilot start зафиксирован:
  - roles
  - pilot users with role assignment
  - branches
  - locations
  - departments
- reference-data setup model не вводит новый master-data module, import engine или extra asset-side catalogs.

Non-goals:
- inventory / warehouse;
- software licenses / SAM;
- maintenance & repair;
- reporting module;
- heavy BI;
- expanded integrations;
- custom workflows;
- retirement как обязательный pilot gate.

### Deferred owner + AI product usability review candidates after Operational Workspace UX Stabilization v3

Status: candidate-only / non-blocking  
Evidence type: controlled synthetic / internal owner-style review evidence only  
FTOV status: not started / not executable  
Release-ready claim: no  

Owner + AI product usability review confirms that the current Operational Workspace UI is usable for controlled internal/synthetic review, but still has meaningful daily-use friction.

The review does not invalidate the accepted UX Stabilization v3 closeout.

The review does not create a default implementation workstream.

## A. Minor frontend UX hardening candidates

These items are candidate-only frontend refinements and may be handled later without changing product scope:

- add explicit success/error notifications after mutating actions:
  - create asset;
  - edit asset master data;
  - assign;
  - transfer;
  - return;
  - setup create/update;
  - role assignment.
- improve role assignment confirmation:
  - show clear success state after assignment;
  - make assigned roles easier to see in the user/setup area if already available from existing data.
- further reduce UUID / technical ID visual noise:
  - keep technical values copyable;
  - avoid showing secondary UUID lines where they do not help daily operation.
- reduce residual audit wording / correlation details:
  - keep raw service details inside technical disclosure;
  - make first-level audit text more user-facing.
- improve setup table row affordance:
  - hover;
  - pointer cursor;
  - selected-row highlight;
  - clearer “click to edit” cue.
- minor navigation polish:
  - reduce subtle scroll overshoot near already-visible sections;
  - reduce confusion when auto-scroll lands in history/audit instead of the asset card.
- improve context value readability:
  - keep copy/tooltip affordances;
  - add clearer copy around where Tenant ID / User ID values come from in controlled environments.

## B. Product usability gaps

These items make daily use weaker or less confident, but do not automatically require a new product module:

- users may not always understand that an action succeeded because feedback is implicit;
- asset location is less visible than responsibility and may be missed if the user does not open the asset card / responsibility tab;
- assign / transfer / return are separated correctly, but the difference still requires reading explanatory copy;
- long vertical layout can slow daily operation even after anchor navigation improvements;
- role assignment status is not visible enough for Tenant Admin confidence.

These items may justify a future narrow frontend-only usability hardening pass if explicitly approved.

Any future pass must remain:

- frontend-only;
- refine-in-place;
- based on existing UI files and existing API contracts;
- limited to already approved Operational Workspace surfaces.

If resolution requires backend/API, new endpoint, DTO/read-model change, BFF/UI backend, auth/IAM expansion or new product surface, stop and escalate to separate product decision.

## C. Product expansion / parity gaps

These items explain why the current product feels weaker than Hardware Inspector-like mature ITAM tools.

They require separate product decision and must not be treated as immediate UX defects:

- barcode / QR code scanning and label workflows;
- bulk import/export of assets and users;
- advanced search, filtering and sorting;
- dashboards, reports and analytics;
- detailed asset catalogue / asset model enrichment;
- attachments, documents, photos or acts;
- printable labels beyond already approved minimal label discussion;
- Asset 360-style cross-context view;
- inventory / warehouse expansion;
- maintenance / repair expansion;
- full Hardware Inspector feature parity.

These items are not authorized by this backlog note.

## Non-authorization rule

This section does not authorize:

- implementation work;
- Codex work;
- backend/API changes;
- new endpoints;
- DTO/read-model changes;
- database/migration changes;
- BFF / UI backend;
- auth/IAM expansion;
- new role model;
- new bounded context;
- new Wave 2 surface;
- dashboard / BI / analytics implementation;
- Asset 360;
- global search / Elasticsearch;
- saved views / persistent preferences;
- bulk actions;
- barcode / QR / scanning / printer integration;
- import/export engine;
- Hardware Inspector parity implementation;
- release-ready claim;
- FTOV start claim;
- FTOV exit claim.

## Current interpretation

The current UI remains accepted for controlled internal/synthetic review with minor-to-moderate usability friction.

The strongest previously identified narrow frontend-only usability hardening candidate was addressed by Operational Workspace UX v3.1.

Remaining narrow frontend-only UX hardening candidate:

- tune notification lifetime shorter so success banners remain visible but less long-lived.

Further daily-use improvements beyond this point should be treated either as separate minor UX hardening or as product expansion decision, depending on scope.

The strongest future product-expansion candidates are search/filtering, import/export, barcode/QR, reporting/dashboard and asset catalogue enrichment, but only after separate explicit product decision.

### Operational Workspace UX v3.1 hardening closeout

Status: implemented and accepted as frontend-only usability hardening after feedback visibility correction.

Handled by v3.1:

- explicit success/error notification visibility after write operations;
- setup save confirmation;
- role assignment confirmation;
- setup row affordance;
- technical ID visual de-emphasis;
- audit/history wording hardening;
- location visibility using existing frontend data only.

Manual ground-truth verification confirmed:

- `Готово. Запись настройки сохранена.`
- `Готово. Роль назначена пользователю. Изменение сохранено.`
- local inline role assignment confirmation is visible.

Remaining deferred minor UX hardening candidate:

- tune notification lifetime shorter so success banners remain visible but less long-lived.

Remaining product expansion / parity candidates:

- broader daily-use layout simplification;
- advanced search/filtering;
- import/export;
- barcode/QR;
- reports/dashboard;
- Asset 360;
- richer asset catalogue;
- Hardware Inspector parity.

The remaining items are not authorized by v3.1 and require separate product decision where applicable.