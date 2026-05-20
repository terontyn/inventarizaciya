# Backlog Epics

Status: approved
Owner: product
Last Updated: 2026-05-19

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

### Operational Workspace Tree-and-Detail UX closeout

Status: implemented and accepted as frontend-only information architecture / layout restructuring after feedback preservation correction.

Handled:

- replaced long-scroll primary orientation with tree-and-detail UI;
- introduced collapsed hierarchical left navigation;
- introduced selected-object right detail pane;
- introduced contextual asset tabs:
  - Overview;
  - Responsibility;
  - Actions;
  - History / Audit;
  - Technical details;
- separated Setup/Admin from daily asset operations;
- preserved assign / transfer / return as dedicated flows;
- preserved generic edit as master-record-only;
- preserved selected-asset contextual history/audit;
- preserved v3.1 feedback behavior after correction.

Accepted navigation model:

- `Оргструктура -> Department -> Users -> assigned assets`;
- `Локации -> Branch -> Location -> assets at location`;
- `Все активы -> Назначенные / Неназначенные / Retired`;
- `Настройка -> Роли / Пользователи / Подразделения / Филиалы / Локации`.

Remaining deferred minor UX hardening candidates:

- tune notification placement/lifetime if future reviews show friction;
- improve role visibility only if existing frontend data supports it;
- continue reducing context/technical value friction where possible frontend-only.

Remaining product expansion / parity candidates:

- advanced search/filtering;
- import/export;
- barcode/QR;
- reports/dashboard;
- Asset 360;
- richer asset catalogue;
- attachments/documents/photos/acts;
- Hardware Inspector parity.

The remaining items are not authorized by Tree-and-Detail UX closeout and require separate product decision where applicable.

### Operational Workspace Tree-and-Detail UX Density Reduction closeout

Status: implemented and accepted as frontend-only density reduction and progressive disclosure hardening inside the accepted Tree-and-Detail UI.

Handled:

- reduced left-tree visual density;
- improved tree row scan/click affordance;
- kept top-level groups compact and explicitly expandable;
- kept local counts as navigation hints only;
- added tiny local-only visible-tree filter without backend calls or persistence;
- improved Workspace / Overview first-step guidance;
- made forms less visually heavy through progressive disclosure;
- moved support/help copy into disclosures;
- de-emphasized technical IDs while keeping them copyable/accessible;
- made History / Audit summary-first;
- kept raw/correlation/service details collapsed by default but accessible;
- kept Setup/Admin separate from daily operations;
- preserved Assign / Transfer / Return as dedicated flows;
- preserved generic edit as master-record-only;
- preserved v3.1 / Tree-and-Detail feedback behavior.

Targeted Setup navigation verification confirmed:

- `Настройка` is visible;
- `Роли`, `Пользователи`, `Подразделения`, `Филиалы`, `Локации` appear after expansion;
- each setup item opens the correct right-pane setup surface;
- reload is not required.

Remaining deferred minor UX hardening candidates:

- tune Setup group collapse affordance if future reviews show friction;
- continue minor typography / spacing polish only if it remains frontend-only;
- further reduce context / technical value friction where possible without hiding support-critical data.

Remaining product expansion / parity candidates:

- advanced search/filtering;
- global search / Elasticsearch;
- saved views / persistent preferences;
- import/export;
- barcode/QR;
- reports/dashboard;
- Asset 360;
- richer asset catalogue;
- attachments/documents/photos/acts;
- Hardware Inspector parity.

The remaining items are not authorized by Density Reduction closeout and require separate product decision where applicable.

### Operational Workspace Tree-and-Detail UX Small Frontend Cleanup closeout

Status: implemented and accepted as small frontend-only cleanup inside the accepted Tree-and-Detail UI after Density Reduction.

Handled:

- reduced Setup/Admin duplicate navigation;
- converted setup step cards into concise setup-order guidance;
- kept setup tabs/tree as primary navigation;
- moved service IDs out of primary setup table columns;
- kept selected record service IDs accessible through technical disclosure and copy;
- replaced text copy controls with compact accessible clipboard icon buttons where appropriate;
- added compact connection status badge from existing frontend state;
- clarified local tree filter copy as local/open-tree only;
- made Assign / Transfer / Return visually primary;
- made master-record edit visually secondary while preserving master-record-only semantics;
- improved tree count wording;
- improved hover/focus states;
- improved selected tree row emphasis;
- improved narrow-width wrapping.

Conditional decisions:

- full user role column/chips skipped because current frontend state does not contain complete user -> roles mapping;
- client-side sorting skipped;
- client-side pagination skipped;
- broad feedback/toast rewrite skipped because no concrete current defect was found.

Preserved:

- accepted Tree-and-Detail navigation model;
- selected-object right detail pane;
- contextual asset tabs;
- Assign / Transfer / Return as dedicated flows;
- generic edit as master-record-only;
- selected-object contextual History / Audit;
- Technical details accessibility;
- Setup/Admin separation;
- v3.1 / Tree-and-Detail feedback behavior.

Controlled synthetic / internal live re-check confirmed:

- connection status badge is visible;
- local tree filter is clearly local-only;
- Setup/Admin duplication is reduced;
- service IDs are not primary table noise where human labels exist;
- IDs remain accessible and copyable;
- clipboard icon copy controls are usable;
- Actions tab hierarchy is clearer;
- zero-count wording is clearer;
- hover/focus states are improved;
- no forbidden product scope appeared.

Remaining deferred minor UX hardening candidates:

- improve disclosure arrow affordance if future reviews show friction;
- improve copy icon discoverability if future reviews show friction;
- tune left-tree scroll / page-scroll interaction if future reviews show friction;
- optionally normalize mixed RU/EN tab labels as future RU copy polish.

Remaining product expansion / parity candidates:

- global search;
- backend search;
- advanced search/filtering;
- saved views / preferences;
- server-side pagination;
- server-side sorting;
- import/export;
- barcode/QR;
- reports/dashboard;
- Asset 360;
- richer asset catalogue;
- attachments/documents/photos/acts;
- Hardware Inspector parity.

The remaining items are not authorized by Small Frontend Cleanup closeout and require separate product decision where applicable.

### Asset Identity v1 canon alignment

Status: approved as narrow Asset master-record canon extension.

Approved:

- `display_name`
- `note`

Purpose:

- improve human recognition of assets;
- support clearer creation/edit flows;
- provide a small plain-text operational note field.

Preserved:

- `asset_tag` remains canonical inventory/business identifier;
- `display_name` does not replace `asset_tag`;
- `note` remains plain text only;
- Assign / Transfer / Return remain dedicated;
- generic edit remains master-record-only;
- responsibility/location changes do not move into generic PATCH.

Not approved by this alignment:

- manufacturer/model;
- condition;
- purchase/warranty/cost;
- QR/barcode;
- import/export;
- global search;
- Asset 360;
- reporting/dashboard;
- attachments/documents/photos/acts;
- Hardware Inspector parity.

Implementation status:

- backend/API/DB implementation completed and accepted;
- implementation was bounded to `display_name` and `note`;
- migration added nullable `display_name` and nullable `note`;
- API/DTO surfaces now carry `display_name` and `note`;
- validation/normalization handles optional, nullable, blank-to-null semantics;
- existing asset create/update audit payloads include the new identity fields;
- Asset API, schema constraint and full asset-service tests passed.

Frontend usage status:

- frontend usage implementation completed and accepted;
- implementation was bounded to existing Operational Workspace static UI;
- create Asset form now includes optional `display_name` and `note`;
- master-record edit form now includes optional `display_name` and `note`;
- create/edit payloads carry `display_name` and `note`;
- UI title fallback uses:
  - `display_name`;
  - `asset_tag`;
  - `asset_type` / `serial_number`;
  - shortened Asset ID only as last resort;
- `asset_tag` remains visible as canonical inventory/business identifier;
- `note` is shown only as optional plain-text operational context in selected Asset detail / Overview;
- static UI smoke marker coverage was updated.

Preserved by frontend usage implementation:

- Assign / Transfer / Return remain dedicated;
- Retire remains dedicated where currently present;
- generic edit remains master-record-only;
- `assigned_user_id` does not move into generic edit;
- `location_id` does not move into generic edit where current boundary forbids it;
- ordinary PATCH does not become a status-transition path;
- `note` does not become rich text, attachment, document, act, comment thread or history stream.

Still not approved for implementation by Asset Identity v1:


- quick edit redesign;
- context connect screen redesign;
- QR/barcode;
- import/export;
- global/backend search;
- Asset 360;
- reporting/dashboard;
- manufacturer/model;
- condition;
- purchase/warranty/cost;
- attachments/documents/photos/acts;
- rich text notes;
- note threads;
- Hardware Inspector parity.

Next possible step:

- separate product decision for unified creation / quick edit / broader asset identity UX, only if explicitly approved.

### Owner-approved future planning candidates after Asset Identity v1

Status: approved as future planning candidates only.

These items are approved for future product decision / planning work.

This section does not authorize implementation, Codex work, backend/API changes, database changes, frontend changes, Documentation canon changes, release-ready claim, FTOV start, FTOV exit, production readiness or real tenant validation.

#### 1. Unified Create Launcher

Status: approved as future UX planning candidate.

Purpose:

- provide one clear `Создать` entry point instead of forcing the operator to search for creation forms across different menus;
- reduce confusion when creating common operational records.

Approved launcher entries for planning:

- `Актив / устройство`;
- `Пользователь`;
- `Подразделение`;
- `Филиал`;
- `Локация`.

Boundary:

- this is a UX/navigation simplification over existing creation surfaces;
- it must not create a new master-data module;
- it must not add new entity families;
- it must not add backend/API/DB behavior by default;
- it must not replace existing setup/admin ownership rules;
- it must not mix Asset creation with Assign / Transfer / Return;
- Assign / Transfer / Return remain dedicated responsibility flows.

Required next step:

- `Operational Workspace / Unified Create Launcher Direction Gate`.

#### 2. Simplified Asset Actions UX

Status: approved as future UX planning candidate.

Purpose:

- reduce confusion around current action placement;
- make common selected-asset actions easier to understand.

Candidate action grouping:

- edit asset description / master record;
- assign;
- transfer;
- return;
- retire / archive only if separately approved.

Boundary:

- visual grouping may be simplified;
- responsibility-changing operations must remain dedicated flows;
- generic edit remains master-record-only;
- `assigned_user_id` does not move into generic edit;
- `location_id` does not move into generic edit where current boundary forbids it;
- status transition does not move into ordinary PATCH.

Required next step:

- can be planned together with Unified Create Launcher only if scope remains frontend-only;
- otherwise split into separate UX direction gate.

#### 3. Delete / Archive Semantics

Status: approved as future product decision candidate.

Purpose:

- define safe removal / hiding semantics for wrong or obsolete asset records.

Candidate options to evaluate:

- retire;
- archive / hide from active lists;
- correction delete for mistakenly created records with no responsibility history;
- hard delete only if explicitly approved later.

Boundary:

- hard delete is not approved by this candidate note;
- audit/history preservation must be protected;
- deletion must not erase responsibility history by default;
- implementation requires separate product decision and likely backend/API/DB planning.

Required next step:

- `Asset Delete / Archive Semantics Decision Gate`.

#### 4. End-User Asset Information View

Status: approved as future product decision candidate.

Purpose:

- provide a simple read-only information screen for ordinary users or controlled lookup scenarios.

Candidate user-facing information:

- employee full name / identity context;
- assigned devices;
- asset title / `display_name`;
- inventory tag / `asset_tag`;
- serial number where appropriate;
- device type;
- QR if separately approved;
- simple operational note if appropriate.

Boundary:

- must be access-controlled;
- entering only full name must not expose other employees’ devices without a separate access/security decision;
- this is not self-service onboarding by default;
- this is not auth/IAM expansion by default;
- no assign / transfer / return actions for ordinary users by default.

Required next step:

- `End-User Asset Information View Direction Gate`.

#### 5. Search v1

Status: approved as future product decision candidate.

Purpose:

- allow users/operators to quickly find employees and devices without navigating long trees.

Candidate searchable values:

- employee name;
- asset tag / inventory number;
- `display_name`;
- serial number;
- device type;
- later technical attributes if separately approved.

Boundary:

- this is not the existing local tree filter;
- backend/API search is likely required;
- no Elasticsearch/global search platform is approved by this note;
- no saved views or advanced filtering are approved by this note.

Required next step:

- `Operational Search v1 Decision Gate`.

#### 6. QR Lite / Derived Asset Identifier

Status: approved as future product decision candidate.

Purpose:

- show or export a QR representation for an asset / information sheet.

Candidate interpretation:

- QR should be derived from existing approved asset identity;
- QR should not become a separate canonical asset identifier by default.

Boundary:

- no barcode/scanner platform is approved;
- no printer integration is approved;
- no bulk label printing is approved;
- no mobile scanning workflow is approved;
- no new `qr_code` / `barcode` canonical DB field is approved by this candidate note.

Required next step:

- `Asset QR Lite / Derived Identifier Decision Gate`.

#### 7. Asset Type-Specific Technical Attributes

Status: approved as future product decision candidate.

Purpose:

- support useful technical fields that differ by device type.

Candidate examples:

- PC / laptop:
  - hostname;
  - AnyDesk ID;
  - IP address;
  - MAC address.
- stationary phone:
  - phone IP;
  - extension number;
  - SIP / line identifier.
- printer:
  - IP address;
  - network name;
  - placement note.

Boundary:

- do not add many type-specific columns directly to `assets` by default;
- schema approach requires separate decision;
- no automatic discovery agent is approved;
- no Astra Configuration Manager / Hardware Inspector parity is approved;
- no import/export/search over attributes is approved by this candidate note.

Required next step:

- `Asset Type-Specific Technical Attributes v1 Decision Gate`.

#### 8. Configuration Manager-inspired candidates

Status: approved as analysis input only.

Useful ideas to evaluate for this project:

- clearer device identity;
- technical attributes by device type;
- employee/device information view;
- search by employee/device/inventory values;
- QR-lite for identification.

Not approved by this note:

- endpoint agent;
- automatic hardware/software discovery;
- remote control;
- patch management;
- OS/software deployment;
- full configuration-management platform;
- Hardware Inspector parity;
- Astra Configuration Manager parity.

### Planning priority after Asset Identity v1

Recommended owner-priority order:

1. Unified Create Launcher.
2. Simplified Asset Actions UX.
3. Delete / Archive Semantics.
4. End-User Asset Information View.
5. Search v1.
6. QR Lite.
7. Asset Type-Specific Technical Attributes.

The first recommended next stage is:

`Operational Workspace / Unified Create Launcher Direction Gate`