# Backlog Epics

Status: approved
Owner: product
Last Updated: 2026-05-27

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

- continue with remaining owner-approved planning candidates after Unified Create Launcher closeout.

### Owner-approved future planning candidates after Asset Identity v1

Status: approved as future planning candidates only.

These items are approved for future product decision / planning work.

This section does not authorize implementation, Codex work, backend/API changes, database changes, frontend changes, Documentation canon changes, release-ready claim, FTOV start, FTOV exit, production readiness or real tenant validation.

#### 1. Unified Create Launcher

Status: implemented and accepted as bounded frontend-only UX/navigation shortcut.

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

Implementation status:

- completed and accepted;
- implemented as one compact `Создать` launcher;
- launcher entries:
  - `Актив / устройство`;
  - `Пользователь`;
  - `Подразделение`;
  - `Филиал`;
  - `Локация`;
- reuses existing creation forms/surfaces;
- asset creation remains master-record create only;
- setup creation remains inside existing Setup/Admin surfaces;
- static UI smoke coverage was updated.

Preserved:

- Assign / Transfer / Return remain dedicated;
- Retire remains dedicated where currently present;
- generic edit remains master-record-only;
- asset create does not become assign;
- asset create does not become create-and-assign;
- no backend/API/DB changes were opened;
- no auth/IAM changes were opened.

#### 2. Simplified Asset Actions UX

Status: implemented and accepted as bounded frontend-only selected-asset action orientation refinement.

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

Implementation status:

- completed and accepted;
- implemented as frontend-only refinement over existing selected Asset Actions tab;
- existing action surfaces were grouped more clearly;
- responsibility actions are grouped under `Ответственность`;
- master-record edit is grouped under `Описание актива`;
- user-facing labels were clarified:
  - `Выдать сотруднику`;
  - `Переместить`;
  - `Вернуть`;
  - `Редактировать описание актива`;
- Overview `Открыть действия` remains a lightweight hint only;
- existing forms and submit handlers remain in use;
- static UI smoke coverage was updated.

Preserved:

- Assign / Transfer / Return remain dedicated;
- Retire remains unchanged where currently present;
- no Retire action was added;
- no `/retire` surface was added;
- generic edit remains master-record-only;
- selected-asset grouping does not create a universal action endpoint;
- asset create does not become assign;
- asset create does not become create-and-assign;
- Unified `Создать` remains creation-only;
- Assign / Transfer / Return do not move into `Создать`;
- no backend/API/DB changes were opened;
- no auth/IAM changes were opened.


#### 3. Delete / Archive Semantics

Status: accepted as ADR/product semantics decision; bounded Retire exposure later implemented and accepted.

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


ADR/product semantics status:

- ADR-0005 Asset Delete Semantics is accepted;
- public hard delete is rejected for the current stage;
- `DELETE /api/v1/assets/{id}` remains unsupported;
- Retire is the canonical safe lifecycle removal semantics;
- `POST /api/v1/assets/{id}/retire` is now product-exposed as a bounded dedicated lifecycle command by the later Retire Exposure / Safe Lifecycle Removal closeout;
- Archive / Hide has since been accepted only as bounded persisted Archive visibility semantics for already `RETIRED` assets by the later Archive / Hide Lifecycle Visibility closeout;
- Correction Delete remains a future candidate only for mistaken records with strict no-history constraints;
- Correction Delete is not approved for implementation now.

This historical decision did not originally authorize Retire exposure.

It has since been superseded only for bounded Retire exposure by:

- `Retire Exposure / Safe Lifecycle Removal`.

The following remain not authorized:

- `DELETE /api/v1/assets/{id}`;
- hard delete;
- Archive / Hide beyond the accepted bounded Archive visibility command;
- Correction Delete;
- bulk Retire;
- bulk Archive;
- restore-to-ACTIVE;
- bulk Unarchive;
- broader lifecycle recovery expansion;
- Archive for ACTIVE assets;
- ordinary PATCH status transition;
- generic status editor;
- new role model;
- auth/IAM expansion.

Required next step:

- no further Delete / Archive Semantics implementation is opened by this historical ADR/product semantics decision;
- bounded Retire exposure has already been implemented and accepted separately;
- bounded Archive visibility has already been implemented and accepted separately;
- bounded Recovery / Unarchive visibility reversal has already been implemented and accepted separately;
- if owner wants Correction Delete, bulk Retire, bulk Archive, restore-to-ACTIVE, bulk Unarchive or Archive for ACTIVE assets, open a separate lifecycle decision gate;
- otherwise continue to the next owner-selected bounded feature gate or deferred integrated stabilization / verification gate.

#### 4. End-User Asset Information View

Status: first bounded implementation accepted as controlled read-only employee/device information sheet; employee self-view remains deferred.

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

Implementation status:

- first bounded implementation completed and accepted;
- implemented as `Информационный лист сотрудника`;
- implemented inside existing Operational Workspace;
- primary entry is selected employee / user detail context;
- implementation is frontend-only;
- sheet is read-only;
- uses existing loaded frontend state only;
- shows employee identity context and assigned devices;
- assigned devices show:
  - asset title / `display_name`;
  - `asset_tag`;
  - `asset_type`;
  - `serial_number`;
  - `status`;
  - location only if already resolvable;
- `note` is hidden by default;
- audit/history are not shown in the sheet;
- raw technical IDs are not primary content.

User-facing terminology:

- `Администратор`;
- `Оператор учёта`;
- `Наблюдатель / Аудитор`;
- `Сотрудник`.

Preserved:

- no backend/API/DB changes;
- no new endpoint;
- no auth/IAM expansion;
- no ordinary employee role;
- no employee self-view;
- no public full-name lookup;
- no Assign / Transfer / Return actions inside the sheet;
- no Edit / Delete / Archive / Retire actions inside the sheet;
- no QR/export/PDF/print;
- no Asset 360;
- no reporting/dashboard.

Deferred:

- authenticated `Сотрудник self-view`;
- server-side self-only scoping;
- backend scoped endpoint if later needed;
- auth/IAM/session decision;
- public lookup remains rejected.

Required next step:

- no further implementation is opened by this closeout;
- if owner wants ordinary employee self-view, open `Employee Self-View Access Decision Gate`;
- otherwise continue to the next owner-approved planning candidate.



#### 5. Search v1

Status: first bounded implementation accepted as frontend-only controlled quick find; Backend/API Search v1 remains deferred.

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
Implementation status:

- first bounded implementation completed and accepted;
- implemented as `Быстрый поиск`;
- implemented inside existing Operational Workspace;
- placed near the left tree / navigation area;
- visually separated from existing `Фильтр дерева`;
- implementation is frontend-only;
- uses already loaded authorized frontend state only;
- no backend/API/DB changes were made;
- no new endpoint was added;
- no auth/IAM change was made.

Data sources:

- `state.assets`;
- `state.references.users`;
- `state.references.departments`;
- `state.references.branches`;
- `state.references.locations`.

Searchable values:

- employee / user name;
- `asset_tag`;
- `display_name`;
- `serial_number`;
- `asset_type`.

Result groups:

- `Сотрудники`;
- `Активы / устройства`.

Result behavior:

- employee result opens existing selected user detail;
- asset result opens existing selected asset detail;
- result rows are read-only navigation rows;
- no action buttons are present in result rows.

Preserved distinction:

- existing `Фильтр дерева` remains a local visible/open tree filter;
- `Быстрый поиск` searches already loaded authorized workspace state;
- this is not backend search;
- this is not global search;
- this is not advanced filtering.

Preserved boundaries:

- no backend/API Search v1;
- no server-side filtering;
- no pagination/limits;
- no new endpoint;
- no DB migration;
- no auth/IAM expansion;
- no ordinary employee role;
- no employee self-search;
- no public search;
- no full-name-only lookup;
- no `note` search;
- no audit/history search;
- no technical attributes search;
- no QR/export/PDF/print;
- no Asset 360;
- no reporting/dashboard;
- no Elasticsearch/global search;
- no saved views;
- no advanced filters.

Deferred:

- Backend/API Search v1;
- server-side filtering;
- pagination/limits;
- larger tenant scale handling;
- ordinary employee self-search;
- search over future technical attributes;
- QR/export/PDF;
- Asset 360/reporting.



Required next step:

- no further Search v1 implementation is opened by this closeout;
- if owner wants server-side search, open `Backend/API Search v1 Planning Packet`;
- otherwise continue to the next owner-approved planning candidate.

#### 6. QR Lite / Derived Asset Identifier

Status: first bounded implementation accepted as frontend-only derived QR display; printable label/sheet, scanner workflow, public lookup and backend/API QR remain deferred.


Purpose:

- show a derived QR representation for a selected Asset inside the managed Operational Workspace;
- support physical-to-record recognition through existing `asset_tag`;
- preserve `asset_tag` as the canonical inventory/business identifier;
- keep printable label/sheet and information-sheet QR as separate deferred planning tracks.

Candidate interpretation:

- QR should be derived from existing approved asset identity;
- QR should not become a separate canonical asset identifier by default.

Boundary:

- no barcode/scanner platform is approved;
- no printer integration is approved;
- no bulk label printing is approved;
- no mobile scanning workflow is approved;
- no new `qr_code` / `barcode` canonical DB field is approved by this candidate note.
- no export/PDF/Excel/print workflow is approved by this candidate note;
- Controlled Employee Asset Information Sheet remains QR-free unless separately approved later.
Implementation status:

- first bounded implementation completed and accepted;
- implemented as `QR Lite` / `QR по инвентарному номеру`;
- implemented inside existing Operational Workspace;
- displayed only in selected Asset Technical details;
- implementation is frontend-only;
- QR payload is exactly `asset_tag`;
- QR is generated from existing selected Asset frontend state;
- QR is read-only;
- QR is derived presentation only;
- QR is not canonical Asset identity;
- QR does not replace `asset_tag`;
- `asset_tag` remains the canonical inventory/business identifier inside tenant.

Displayed near QR:

- asset display title;
- `asset_tag`;
- `serial_number`;
- `asset_type`.

These nearby values are display-only and are not encoded into QR.

QR generation:

- local frontend generation;
- embedded local QR generator;
- no external dependency;
- no remote QR service;
- no package/build pipeline change.

Preserved placement boundaries:

- Asset Overview remains QR-free;
- asset header/card identity area remains QR-free;
- Controlled Employee Asset Information Sheet remains QR-free;
- printable label/sheet was not opened;
- scanner/mobile/public route was not opened.

Preserved boundaries:

- no backend/API QR;
- no backend/API changes;
- no DB changes;
- no migrations;
- no new endpoint;
- no DTO/read-model changes;
- no auth/IAM expansion;
- no ordinary employee role;
- no employee self-service;
- no public QR lookup;
- no public asset page;
- no QR-based authentication;
- no canonical `qr_code`;
- no canonical `barcode`;
- no canonical `inventory_number`;
- no stored QR value;
- no stored barcode value;
- no backend QR generation;
- no remote QR service;
- no scanner/camera/mobile workflow;
- no printer integration;
- no bulk print;
- no export/PDF/Excel/print;
- no download QR action;
- no label designer;
- no Asset 360;
- no reporting/dashboard;
- no QR search;
- no backend search;
- no Elasticsearch/global search;
- no QR payload from anything except `asset_tag`;
- no note/history/audit payload;
- no assignment/user payload;
- no tenant id payload;
- no URL payload.

Deferred:

- QR in Asset Overview;
- QR in Controlled Employee Asset Information Sheet;
- printable label/sheet;
- scanner/camera/mobile workflow;
- backend/API QR;
- QR search;
- public lookup;
- employee self-service;
- printer integration;
- bulk print/export.

Required next step:

- no further QR Lite implementation is opened by this closeout;
- if owner wants printable labels / information sheets, open `Printable Asset Label / Information Sheet Planning Packet`;
- if owner wants scanner / camera / mobile workflow, open separate scanner workflow product decision;
- if owner wants backend/API QR or QR lookup, open `Backend/API QR Planning Packet`;
- otherwise continue to the next owner-approved planning candidate.

#### 7. Asset Type-Specific Technical Attributes

Status: first bounded implementation accepted as backend/API/DB controlled key-value support plus frontend integration inside selected Asset Technical details.

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

Implementation status:

- first bounded implementation completed and accepted;
- backend/API/DB implementation completed and accepted;
- frontend integration completed and accepted;
- implemented as `Технические атрибуты`;
- implemented inside existing Operational Workspace;
- frontend surface is selected Asset Technical details only;
- technical attributes are secondary operational context;
- technical attributes are not canonical Asset identity;
- technical attributes do not replace `asset_tag`;
- technical attributes do not replace `serial_number`;
- technical attributes do not replace responsibility/location/status semantics.

Backend/API/DB implementation:

- added `asset_technical_attributes` table;
- implemented controlled key/value storage;
- added server-side profile mapping from current free-text `asset_type`;
- supported profiles:
  - `PC_LAPTOP`;
  - `STATIONARY_PHONE`;
  - `PRINTER`;
  - `UNSUPPORTED_GENERIC`;
- extended asset detail response with nested `technical_attributes`;
- added separate update endpoint:
  - `PUT /api/v1/assets/{id}/technical-attributes`;
- added audit action:
  - `ASSET_TECHNICAL_ATTRIBUTES_UPDATED`;
- reused existing asset read/mutate authorization cutline.

Frontend implementation:

- added `Технические атрибуты` block only in selected Asset Technical details;
- added profile-based display and edit;
- edit uses only:
  - `PUT /api/v1/assets/{id}/technical-attributes`;
- read-only users see values without edit controls;
- unsupported/generic profile shows safe no-edit state.

Supported fields:

- `PC_LAPTOP`:
  - `hostname`;
  - `anydesk_id`;
  - `ip_address`;
  - `mac_address`;
- `STATIONARY_PHONE`:
  - `phone_ip`;
  - `extension_number`;
  - `sip_line_id`;
- `PRINTER`:
  - `printer_ip`;
  - `printer_network_name`;
  - `printer_placement_note`.

Preserved boundaries:

- no technical attributes in Asset Overview;
- no technical attributes in asset header;
- no technical attributes in generic master edit;
- no technical attributes in Employee Information Sheet;
- no technical attribute search in Quick Find;
- no QR Lite payload change;
- no import/export/PDF/Excel/print;
- no reporting/dashboard;
- no Asset 360;
- no discovery agent;
- no endpoint scanner;
- no network scanning;
- no remote control;
- no patch management;
- no Hardware Inspector parity;
- no Astra Configuration Manager parity;
- no uncontrolled custom fields;
- no tenant-defined field admin platform;
- no employee self-service;
- no public lookup;
- no auth/IAM expansion;
- no new role model.

Deferred:

- search over technical attributes;
- showing technical attributes in Employee Information Sheet;
- showing technical attributes in Asset Overview;
- QR payload changes;
- import/export of technical attributes;
- reporting/dashboard usage;
- discovery/agent/scanner/platform scope;
- stricter format validation for IP/MAC/SIP/AnyDesk;
- sensitive-value redaction policy changes.

Required next step:

- no further Asset Type-Specific Technical Attributes implementation is opened by this closeout;
- if owner wants search by technical attributes, open `Search over Technical Attributes Decision Gate`;
- if owner wants technical attributes in Employee Information Sheet, open `Employee Information Sheet Technical Attribute Exposure Decision Gate`;
- if owner wants import/export, reporting/dashboard, discovery/agent/scanner or Asset 360, open separate product decision first;
- otherwise continue to integrated stabilization / verification.

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


### Planning priority after Recovery / Unarchive Lifecycle Visibility

Unified Create Launcher, Simplified Asset Actions UX, ADR-0005 Asset Delete Semantics, Controlled Employee Asset Information Sheet, Operational Search v1 Frontend Quick Find, Asset QR Lite Frontend Derived Display, Asset Type-Specific Technical Attributes v1, Search over Technical Attributes, Employee Sheet Technical Attribute Exposure, Standalone Employee Information Sheet Interface, Quick Find / Technical Search Merge Semantics, Search Scope Expansion / printer_placement_note Technical Search, Retire Exposure / Safe Lifecycle Removal, Archive / Hide Lifecycle Visibility, and Recovery / Unarchive Lifecycle Visibility are implemented/accepted at their approved scope levels.

Standalone Employee Information Sheet Interface is accepted as:

- a frontend-only focused-interface correction;
- a separate top-level `Информационный лист` navigation item;
- employee-only loaded-state search;
- selected employee information plus assigned devices;
- low-sensitivity technical hints only.

Quick Find / Technical Search Merge Semantics is accepted as:

- a frontend-only visual grouping and semantic clarification;
- shared visual search area:
  - `Поиск`;
- two separate sections:
  - `Сотрудники и активы`;
  - `Технические данные`;
- no merged search logic;
- no backend/API changes;
- no global search;
- no advanced filters;
- no search scope expansion.

Search Scope Expansion / printer_placement_note Technical Search is accepted as:

- a bounded Technical Search-only searchable-key expansion;
- `printer_placement_note` searchable only through:
  - `GET /api/v1/assets/technical-attribute-search`;
- asset/device results only;
- matched field label:
  - `Примечание по размещению`;
- sensitive/masked result display;
- no raw placement note exposure in result rows;
- no Quick Find expansion;
- no Employee Sheet expansion;
- no Asset Overview exposure;
- no search over asset `note`;
- no backend/global search expansion.

Retire Exposure / Safe Lifecycle Removal is accepted as:

- a bounded safe lifecycle action;
- existing endpoint exposed:
  - `POST /api/v1/assets/{id}/retire`;
- dedicated lifecycle command only;
- selected Asset Actions tab only;
- user-facing action:
  - `Списать актив`;
- `status = RETIRED` after successful Retire;
- active `assigned_user_id` is cleared;
- current `location_id` is preserved;
- `RETIRE` responsibility fact is appended once;
- `RETIRE` audit event is emitted once;
- repeated Retire is guarded;
- no duplicate `RETIRE` fact/audit on repeated Retire;
- no delete / archive / hide / correction delete;
- no ordinary PATCH status transition;
- no generic status editor;
- no bulk retire;
- no recovery / unretire.

Archive / Hide Lifecycle Visibility is accepted as:

- a bounded persisted visibility capability for already retired assets;
- dedicated endpoint:
  - `POST /api/v1/assets/{id}/archive`;
- persisted visibility field:
  - `archived_at`;
- allowed only for `RETIRED` assets;
- rejected for `ACTIVE` assets;
- `status` remains `RETIRED`;
- Archive does not change `assigned_user_id`;
- Archive preserves `location_id`;
- Archive does not append responsibility facts;
- Archive emits one `ARCHIVE` audit event;
- repeated Archive is guarded;
- default asset list excludes archived assets;
- explicit archive discovery path:
  - `GET /api/v1/assets?visibility=archived`;
- selected Asset Actions lifecycle UI only;
- user-facing action:
  - `Архивировать актив`;
- explicit `Архив` discovery group/path;
- no delete / hard delete / physical erase;
- no Correction Delete;
- no status `ARCHIVED`;
- no generic PATCH archive;
- no generic status editor;
- no bulk archive;
- no recovery / unarchive.

Recovery / Unarchive Lifecycle Visibility is accepted as:

- a bounded visibility reversal capability for archived retired assets;
- dedicated endpoint:
  - `POST /api/v1/assets/{id}/unarchive`;
- uses existing persisted visibility field:
  - `archived_at`;
- allowed only for archived `RETIRED` assets;
- rejected for `ACTIVE` assets;
- rejected for non-archived assets;
- clears `archived_at`;
- `status` remains `RETIRED`;
- Unarchive does not restore `assigned_user_id`;
- Unarchive does not change `assigned_user_id`;
- Unarchive preserves `location_id`;
- Unarchive does not append responsibility facts;
- Unarchive emits one `UNARCHIVE` audit event;
- repeated Unarchive is guarded;
- default asset list includes the asset again after Unarchive;
- archive discovery list excludes the asset after Unarchive;
- selected Asset Actions lifecycle UI only;
- user-facing action:
  - `Разархивировать актив`;
- no restore-to-ACTIVE;
- no reactivation;
- no Retire rollback;
- no assignment restoration;
- no hard delete;
- no Correction Delete;
- no generic PATCH archive manipulation;
- no generic status editor;
- no bulk unarchive.

Employee Sheet Technical Attribute Exposure is accepted as:

- backend/API tenant-scoped Employee Sheet technical hints endpoint;
- frontend UI rendering inside existing `Информационный лист сотрудника`;
- low-sensitivity assigned-asset hints only;
- read-only selected employee/user detail enhancement.

Accepted Employee Sheet hints remain:

- `hostname`;
- `extension_number`;
- `printer_network_name`.

Owner decision:

- integrated `Post-Technical-Attributes Stabilization / Controlled End-to-End Recheck` remains deferred until after the current batch of new bounded features;
- this deferral is not validation evidence;
- it does not prove release readiness, FTOV start, FTOV exit, production readiness or real tenant validation.

The next stage should be the next owner-selected bounded feature gate or the deferred integrated stabilization / verification gate when the owner explicitly chooses it.

Possible optional branches:

- `Search Scope Expansion Decision Gate` for asset `note` only;
- separate product/architecture gates for global search, advanced filters, reporting/dashboard, discovery/scanner, employee self-service, export/PDF/print or Asset 360.

Do not open these branches implicitly.

Each requires explicit owner decision.


#### Search over Technical Attributes

Status: first bounded implementation accepted as backend/API technical-attribute search plus frontend UI through separate `Поиск по техническим данным`.

Intent:

- allow managed workspace users to find assets/devices by already accepted technical attributes;
- support support/operator lookup by technical clues;
- keep search bounded to approved technical fields;
- avoid global search, advanced filtering, reporting, discovery or public lookup scope.

Backend/API implementation:

- added endpoint:
  - `GET /api/v1/assets/technical-attribute-search`;
- added bounded tenant-scoped search over technical attributes;
- added search-scope index:
  - `idx_asset_technical_attributes_search_scope`;
- reused existing asset read authorization cutline;
- returned asset/device results only;
- returned one result per asset;
- returned matched field metadata;
- masked sensitive values in result rows;
- emitted no audit for read-only search.

Frontend implementation:

- added separate UI block:
  - `Поиск по техническим данным`;
- placed it near current `Быстрый поиск`;
- kept current `Быстрый поиск` unchanged;
- kept `Фильтр дерева` unchanged;
- result rows are read-only navigation rows;
- result click opens selected Asset detail / Technical details when safe;
- displayed backend-provided masked values as-is;
- did not unmask values in frontend.

Searchable fields:

- `hostname`;
- `anydesk_id`;
- `ip_address`;
- `mac_address`;
- `phone_ip`;
- `extension_number`;
- `sip_line_id`;
- `printer_ip`;
- `printer_network_name`.

Excluded by default:

- `printer_placement_note`;
- `note`;
- audit/history;
- QR payload;
- unsupported/generic arbitrary fields;
- tenant-defined custom keys.

Preserved boundaries:

- no merge into current `Быстрый поиск`;
- no backend/global search platform;
- no Elasticsearch;
- no advanced filters;
- no saved views;
- no reporting/dashboard;
- no Asset 360;
- no QR payload changes;
- no QR search;
- no Employee Information Sheet exposure;
- no Asset Overview exposure;
- no generic PATCH changes;
- no result-row Assign / Transfer / Return / Edit / Delete / Archive / Retire;
- no import/export/PDF/Excel/print;
- no discovery/scanner/network scan;
- no employee self-service;
- no public lookup;
- no auth/IAM expansion;
- no new role model.

Deferred:

- merging technical search into `Быстрый поиск`;
- search over `printer_placement_note`;
- search over `note`;
- global search / Elasticsearch;
- advanced filters / saved views;
- QR search / QR payload changes;
- Employee Information Sheet technical attribute exposure;
- Asset Overview technical attribute exposure;
- import/export;
- discovery/agent/scanner;
- reporting/dashboard / Asset 360.

Required next step:

- no further Search over Technical Attributes implementation is opened by this closeout;
- if owner wants to merge technical search into `Быстрый поиск`, open a separate UI semantics decision gate;
- if owner wants search over `note` or `printer_placement_note`, open a separate search-scope decision gate;
- if owner wants global search, advanced filters, saved views, reporting/dashboard, discovery/scanner or Asset 360, open separate product/architecture decision first;
- otherwise continue to the next owner-selected bounded feature gate.

#### Employee Sheet Technical Attribute Exposure

Status: first bounded implementation accepted as backend/API Employee Sheet technical hints endpoint plus frontend UI integration inside existing `Информационный лист сотрудника`.

Intent:

- allow managed workspace users to see low-sensitivity technical hints for devices assigned to the selected employee/user;
- improve Employee Sheet usefulness without exposing sensitive technical attributes;
- keep the sheet read-only and selected-user scoped;
- avoid employee self-service, public lookup, export/PDF/print or Asset 360 scope.

Backend/API implementation:

- added endpoint:
  - `GET /api/v1/assets/employee-sheet-technical-hints?user_id=<userId>`;
- returns assets assigned to selected `user_id` in the current tenant;
- returns minimal asset identity/context;
- returns only allowlisted low-sensitivity `technical_hints`;
- reuses existing asset read authorization;
- performs no user-service DB join;
- performs no user-service existence check in this accepted cut;
- returns empty `assets` for unknown/no-match `user_id`;
- emits no audit for read-only retrieval.

Frontend implementation:

- existing `Информационный лист сотрудника` renders hints under assigned-device rows/cards;
- calls:
  - `GET /api/v1/assets/employee-sheet-technical-hints?user_id=<selectedUserId>`;
- clears stale hints when selected user changes;
- merges hints by `asset_id`;
- displays only present hints;
- does not show noisy empty hint blocks;
- does not block Employee Sheet rendering if the endpoint fails;
- uses a hard frontend allowlist.

Accepted hints:

- `hostname` -> `Hostname`;
- `extension_number` -> `Внутренний номер`;
- `printer_network_name` -> `Сетевое имя принтера`.

Preserved boundaries:

- no full `technical_attributes` in Employee Sheet;
- no sensitive technical fields in Employee Sheet;
- no masked sensitive values in Employee Sheet;
- no `profile`;
- no `note`;
- no QR payload;
- no audit/history exposure;
- no action links;
- no technical attribute editing from Employee Sheet;
- no employee self-service;
- no public lookup;
- no full-name lookup;
- no export/PDF/Excel/print;
- no Asset 360;
- no Asset Overview exposure;
- no Quick Find changes;
- no Technical Search changes;
- no QR Lite changes;
- no generic PATCH changes;
- no Assign / Transfer / Return changes;
- no auth/IAM expansion;
- no new role model.

Explicitly excluded fields:

- `anydesk_id`;
- `ip_address`;
- `mac_address`;
- `phone_ip`;
- `sip_line_id`;
- `printer_ip`;
- `printer_placement_note`;
- arbitrary unsupported/custom technical fields.

Deferred:

- broader Employee Sheet expansion;
- employee self-service;
- public lookup remains rejected unless separately decided;
- export/PDF/print;
- QR in Employee Sheet;
- Asset Overview technical hints or full technical attributes;
- reporting/dashboard;
- discovery/agent/scanner/platform scope.

Required next step:

- no further Employee Sheet Technical Attribute Exposure implementation is opened by this closeout;
- if owner wants export/PDF/print, open a separate product decision gate;
- if owner wants employee self-service, open a separate auth/IAM and product decision gate;
- if owner wants Asset Overview technical hints, open a separate exposure decision gate;
- if owner wants Asset 360, reporting/dashboard, global search, discovery/scanner or Hardware Inspector parity, open separate product/architecture decision first;
- otherwise continue to the next owner-selected bounded feature gate or the deferred integrated stabilization / verification gate.

#### Standalone Employee Information Sheet Interface

Status: implemented and accepted as a bounded frontend-only focused-interface correction.

Intent:

- make `Информационный лист сотрудника` feel like a separate focused interface;
- avoid hiding the sheet inside general selected-user detail;
- provide a simple operator-facing path with only employee search and employee information;
- preserve all accepted Employee Sheet privacy and sensitivity boundaries.

Accepted implementation:

- added top-level left navigation item:
  - `Информационный лист`;
- added focused right-pane interface;
- added employee-only local search;
- search uses only already loaded authorized frontend state;
- search is not server-side search;
- search is not Quick Find;
- search is not Technical Search;
- selected employee stays inside the standalone sheet path;
- assigned devices render from loaded frontend asset state;
- low-sensitivity technical hints are loaded through the existing endpoint:
  - `GET /api/v1/assets/employee-sheet-technical-hints?user_id=<userId>`;
- stale hints are cleared when selected employee changes;
- stale hint responses are ignored;
- existing embedded Employee Sheet remains intact.

Accepted displayed content:

- selected employee identity/context;
- department if already resolvable;
- assigned devices;
- asset title / `display_name`;
- `asset_tag`;
- `asset_type`;
- `serial_number`;
- `status`;
- location if already resolvable;
- allowed low-sensitivity technical hints.

Accepted hints:

- `hostname` -> `Hostname`;
- `extension_number` -> `Внутренний номер`;
- `printer_network_name` -> `Сетевое имя принтера`.

Preserved boundaries:

- read-only only;
- managed workspace only;
- no public lookup;
- no employee self-service;
- no backend employee search;
- no full-name public lookup;
- no export/PDF/Excel/print;
- no QR in Employee Sheet;
- no audit/history in Employee Sheet;
- no action links;
- no Assign / Transfer / Return;
- no edit controls;
- no technical attribute editing;
- no full `technical_attributes`;
- no sensitive technical fields;
- no Asset 360.

Explicitly excluded fields:

- `anydesk_id`;
- `ip_address`;
- `mac_address`;
- `phone_ip`;
- `sip_line_id`;
- `printer_ip`;
- `printer_placement_note`;
- `note`;
- QR payload;
- audit/history;
- `profile`;
- arbitrary unsupported/custom fields.

Implementation scope:

- frontend-only;
- existing static Operational Workspace UI only;
- smoke test updated for static UI coverage;
- no backend/API changes;
- no user-service API changes;
- no DB/migration changes;
- no BFF/UI backend;
- no auth/IAM expansion;
- no new endpoint.

Required next step:

- no further Standalone Employee Information Sheet implementation is opened by this closeout;
- if owner wants server-side employee search, open separate backend/API decision gate;
- if owner wants public lookup or employee self-service, open separate auth/IAM and product decision gate;
- if owner wants export/PDF/print, open separate product decision gate;
- if owner wants Asset 360, reporting/dashboard, global search or Hardware Inspector parity, open separate product/architecture decision first;
- otherwise continue to the next owner-selected bounded feature gate or the deferred integrated stabilization / verification gate.

This backlog closeout does not prove release readiness, FTOV start, FTOV exit, production readiness, real tenant validation, or real day-to-day operational validation.

#### Quick Find / Technical Search Merge Semantics

Status: implemented and accepted as bounded frontend-only visual grouping and semantic clarification.

Intent:

- reduce user confusion between multiple search-like UI surfaces;
- clarify the difference between loaded-state workspace search and endpoint-based technical-attribute search;
- keep existing search semantics intact;
- avoid global search, advanced filters, backend unified search or product search expansion.

Accepted implementation:

- added shared visual search area:
  - `Поиск`;
- kept two separate sections:
  - `Сотрудники и активы`;
  - `Технические данные`;
- clarified Quick Find copy as loaded-state workspace search;
- clarified Technical Search copy as endpoint-based technical-attribute search with masking preserved;
- kept Quick Find and Technical Search logic separate;
- kept separate state, handlers, data sources and result renderers;
- kept standalone `Информационный лист` search unchanged;
- kept `Фильтр дерева` as local/open-tree filtering only.

Quick Find remains:

- frontend-only;
- loaded-state-only;
- no backend call;
- users + assets only;
- result groups:
  - `Сотрудники`;
  - `Активы / устройства`.

Quick Find searchable values remain:

- employee/user name;
- `asset_tag`;
- `display_name`;
- `serial_number`;
- `asset_type`.

Technical Search remains:

- separate backend/API technical-attribute search;
- existing endpoint:
  - `GET /api/v1/assets/technical-attribute-search`;
- technical attributes only;
- asset/device results only;
- backend masking/sensitivity behavior preserved;
- frontend does not unmask sensitive values;
- read-only navigation result rows.

Preserved boundaries:

- no merged search input;
- no merged result list;
- no backend/API changes;
- no new endpoint;
- no server-side unified search;
- no cross-service search orchestration;
- no global search;
- no Elasticsearch;
- no advanced filters;
- no saved views;
- no persistent preferences;
- no search over `note`;
- no search over `printer_placement_note`;
- no QR search;
- no audit/history search;
- no public lookup;
- no employee self-service;
- no Asset 360;
- no reporting/dashboard;
- no auth/IAM expansion;
- no new role model.

Required next step:

- no further Quick Find / Technical Search Merge Semantics implementation is opened by this closeout;
- if owner wants unified backend search, open separate backend/API decision gate;
- if owner wants search over `note` or `printer_placement_note`, open separate search-scope decision gate;
- if owner wants global search, Elasticsearch, advanced filters, saved views, reporting/dashboard, Asset 360, public lookup or employee self-service, open separate product/architecture decision first;
- otherwise continue to the next owner-selected bounded feature gate or the deferred integrated stabilization / verification gate.

This backlog closeout does not prove release readiness, FTOV start, FTOV exit, production readiness, real tenant validation, or real day-to-day operational validation.

#### Search Scope Expansion / printer_placement_note Technical Search

Status: implemented and accepted as bounded Technical Search-only searchable-key expansion with masked result display.

Intent:

- allow managed workspace users to find printer assets by placement note context;
- keep the expansion inside the existing Technical Search surface;
- avoid exposing raw free-text placement notes in result rows;
- avoid Quick Find, Employee Sheet, Asset Overview, global search or advanced search expansion.

Accepted implementation:

- added `printer_placement_note` to the existing Technical Search searchable-key allowlist;
- preserved existing endpoint:
  - `GET /api/v1/assets/technical-attribute-search`;
- added matched field label:
  - `Примечание по размещению`;
- classified `printer_placement_note` as sensitive/masked for Technical Search result rows;
- result rows return asset/device results only;
- raw `printer_placement_note` value is not exposed as `display_value`;
- existing response DTO shape remains unchanged;
- existing query parameters remain unchanged;
- existing tenant scoping remains unchanged;
- existing asset read authorization remains unchanged;
- no audit is emitted for read-only search;
- API tests were updated.

Accepted matched field behavior:

- `matched_field.key = printer_placement_note`;
- `matched_field.label = Примечание по размещению`;
- `matched_field.sensitive = true`;
- `matched_field.masked = true`;
- `matched_field.display_value` is masked.

Preserved boundaries:

- asset `note` remains excluded;
- Quick Find remains unchanged;
- Quick Find remains frontend-only and loaded-state-only;
- Quick Find does not search technical attributes;
- Quick Find does not search `printer_placement_note`;
- Employee Sheet remains unchanged;
- Employee Sheet hints remain only:
  - `hostname`;
  - `extension_number`;
  - `printer_network_name`;
- Employee Sheet does not show `printer_placement_note`;
- Asset Overview remains unchanged;
- Asset Overview does not show `printer_placement_note`;
- Asset Technical Details edit behavior remains unchanged;
- QR Lite remains unchanged;
- Assign / Transfer / Return remain unchanged.

Not opened:

- new endpoint;
- DB/migration changes;
- response DTO contract changes;
- backend unified search;
- global search;
- Elasticsearch;
- advanced filters;
- saved views;
- persistent preferences;
- search over asset `note`;
- audit/history search;
- QR search;
- public lookup;
- employee self-service;
- Asset 360;
- reporting/dashboard;
- auth/IAM expansion;
- new role model.

Required next step:

- no further Search Scope Expansion implementation is opened by this closeout;
- if owner wants search over asset `note`, open a separate search-scope decision gate;
- if owner wants Quick Find technical attribute search, open a separate UI/product decision gate;
- if owner wants Employee Sheet placement-note exposure, open a separate Employee Sheet exposure decision gate;
- if owner wants global search, Elasticsearch, advanced filters, saved views, reporting/dashboard, Asset 360, public lookup or employee self-service, open separate product/architecture decision first;
- otherwise continue to the next owner-selected bounded feature gate or the deferred integrated stabilization / verification gate.

This backlog closeout does not prove release readiness, FTOV start, FTOV exit, production readiness, real tenant validation, or real day-to-day operational validation.

#### Retire Exposure / Safe Lifecycle Removal

Status: implemented and accepted as bounded safe lifecycle command exposure plus selected-asset frontend action.

Intent:

- allow managed workspace users with existing mutate/lifecycle authority to safely remove an asset from active operation;
- preserve Asset record, responsibility history and audit evidence;
- avoid hard delete, archive / hide, correction delete, ordinary PATCH status transition or generic status editing;
- keep Retire visible only as a deliberate selected-asset lifecycle action.

Accepted implementation:

- existing endpoint exposed:
  - `POST /api/v1/assets/{id}/retire`;
- response remains:
  - `{ id }`;
- Retire uses existing asset mutate/lifecycle authorization cutline;
- read-only actors remain forbidden;
- Support Reviewer remains read-only / forbidden;
- no new role model was introduced;
- no auth/IAM expansion was introduced;
- UI action added only inside selected Asset Actions tab;
- action label:
  - `Списать актив`;
- confirmation copy explains that this is not deletion;
- API tests updated;
- static UI smoke coverage updated.

Accepted Retire behavior:

- active asset status becomes `RETIRED`;
- active `assigned_user_id` is cleared;
- current `location_id` is preserved;
- one `RETIRE` responsibility fact is appended;
- one `RETIRE` audit event is emitted;
- Asset record remains readable;
- responsibility history remains preserved;
- retired assets remain visible through existing retired/spisannye grouping.

Repeated Retire behavior:

- already `RETIRED` assets cannot be retired again;
- repeated Retire returns safe conflict/business-rule rejection;
- repeated Retire does not append duplicate `RETIRE` facts;
- repeated Retire does not emit duplicate `RETIRE` audit events.

Preserved boundaries:

- no `DELETE /api/v1/assets/{id}`;
- no hard delete;
- no archive / hide;
- no correction delete;
- no ordinary PATCH status transition;
- no generic status editor;
- no bulk retire;
- no recovery / unretire;
- no destructive erase;
- no new endpoint;
- no DB/migration change;
- no new role model;
- no auth/IAM expansion;
- no workflow engine;
- no approval workflow;
- no cross-service orchestration;
- no inventory/warehouse movement semantics;
- no employee self-service.

Preserved UI boundaries:

- Unified Create unchanged;
- Employee Sheet / `Информационный лист` unchanged;
- Quick Find unchanged;
- Technical Search unchanged;
- Asset Overview unchanged;
- generic master-record edit unchanged;
- Setup/Admin unchanged;
- asset list rows unchanged;
- tree rows unchanged.

Required next step:

- no further Retire Exposure implementation is opened by this closeout;
- if owner wants Archive / Hide, open a separate lifecycle visibility decision gate;
- if owner wants Correction Delete, open a separate strict correction-delete decision gate;
- if owner wants recovery / unretire, open a separate lifecycle decision gate;
- if owner wants bulk retire, open a separate bulk-action decision gate;
- if owner wants inventory/warehouse movement on Retire, open a separate cross-context lifecycle decision gate;
- otherwise continue to the next owner-selected bounded feature gate or the deferred integrated stabilization / verification gate.

This backlog closeout does not prove release readiness, FTOV start, FTOV exit, production readiness, real tenant validation, or real day-to-day operational validation.

#### Archive / Hide Lifecycle Visibility

Status: implemented and accepted as bounded persisted Archive visibility semantics for already retired assets.

Intent:

- reduce default working-list noise from old retired assets;
- preserve Asset record, responsibility history and audit evidence;
- keep Archive separate from Retire;
- avoid hard delete, physical erase, Correction Delete, generic PATCH archive or generic status editing;
- keep archived assets discoverable through an explicit archive path.

Accepted implementation:

- added DB-backed archive visibility field:
  - `archived_at`;
- added dedicated command endpoint:
  - `POST /api/v1/assets/{id}/archive`;
- added explicit archive discovery path:
  - `GET /api/v1/assets?visibility=archived`;
- added `ARCHIVE` audit action;
- reused existing asset mutate/lifecycle authorization cutline;
- read-only actors remain forbidden;
- Support Reviewer remains read-only / forbidden;
- no new role model was introduced;
- no auth/IAM expansion was introduced;
- UI action added only inside selected Asset Actions lifecycle block;
- action label:
  - `Архивировать актив`;
- confirmation copy explains that this is not deletion;
- explicit `Архив` discovery group/path added;
- API tests updated;
- static UI smoke coverage updated.

Accepted Archive behavior:

- only assets with `status = RETIRED` can be archived;
- `ACTIVE` assets cannot be archived;
- `archived_at` is set on successful Archive;
- `status` remains `RETIRED`;
- `assigned_user_id` is not changed by Archive;
- current `location_id` is preserved;
- no responsibility fact is appended for Archive;
- one `ARCHIVE` audit event is emitted;
- Asset record remains readable by id;
- responsibility history remains preserved;
- audit history remains preserved.

Repeated Archive behavior:

- already archived assets cannot be archived again;
- repeated Archive returns safe conflict/business-rule rejection;
- repeated Archive does not change `archived_at`;
- repeated Archive does not emit duplicate `ARCHIVE` audit events;
- repeated Archive does not append responsibility facts.

Accepted visibility behavior:

- default `GET /api/v1/assets` excludes archived assets;
- `GET /api/v1/assets?visibility=archived` returns archived assets only;
- archived assets remain readable by `GET /api/v1/assets/{id}`;
- Quick Find excludes archived assets by default;
- Technical Search excludes archived assets by default;
- Employee Sheet hints exclude archived assets;
- Employee Sheet does not expose Archive actions or archived assets.

Preserved boundaries:

- no `DELETE /api/v1/assets/{id}`;
- no hard delete;
- no physical erase;
- no Correction Delete;
- no new lifecycle status `ARCHIVED`;
- no Archive through generic PATCH;
- no generic status editor;
- no Archive for ACTIVE assets;
- no bulk archive;
- no bulk hide;
- no recovery / unarchive;
- no destructive erase;
- no workflow engine;
- no approval workflow;
- no cross-service orchestration;
- no inventory/warehouse movement semantics;
- no employee self-service;
- no public lookup;
- no Asset 360;
- no reporting/dashboard dependency.

Preserved UI boundaries:

- Unified Create unchanged;
- Employee Sheet / `Информационный лист` unchanged;
- Quick Find result rows unchanged;
- Technical Search result rows unchanged;
- Asset Overview unchanged as a primary action surface;
- generic master-record edit unchanged;
- Setup/Admin unchanged;
- asset list rows unchanged as direct action surfaces;
- tree rows unchanged as direct action surfaces.

Required next step:

- no further Archive / Hide implementation is opened by this closeout;
- bounded Recovery / Unarchive visibility reversal has already been implemented and accepted separately;
- if owner wants restore-to-ACTIVE, assignment restoration or bulk Unarchive, open a separate lifecycle decision gate;
- if owner wants Correction Delete, open a separate strict correction-delete decision gate;
- if owner wants bulk archive, open a separate bulk-action decision gate;
- if owner wants Archive for ACTIVE assets, open a separate lifecycle visibility decision gate;
- if owner wants archive-aware global search, advanced filters, reporting/dashboard, Asset 360, public lookup or employee self-service, open separate product/architecture decision first;
- otherwise continue to the next owner-selected bounded feature gate or the deferred integrated stabilization / verification gate.

This backlog closeout does not prove release readiness, FTOV start, FTOV exit, production readiness, real tenant validation, or real day-to-day operational validation.

#### Recovery / Unarchive Lifecycle Visibility

Status: implemented and accepted as bounded visibility reversal for archived retired assets.

Intent:

- allow archived retired assets to return from archive visibility to normal non-archived retired visibility;
- preserve Asset record, responsibility history and audit evidence;
- keep Unarchive separate from restore-to-ACTIVE;
- avoid reactivation, Retire rollback, assignment restoration, hard delete, Correction Delete, generic PATCH archive manipulation or generic status editing.

Accepted implementation:

- added dedicated command endpoint:
  - `POST /api/v1/assets/{id}/unarchive`;
- uses existing archive visibility field:
  - `archived_at`;
- no DB migration was added;
- added `UNARCHIVE` audit action;
- reused existing asset mutate/lifecycle authorization cutline;
- read-only actors remain forbidden;
- no new role model was introduced;
- no auth/IAM expansion was introduced;
- UI action added only inside selected Asset Actions lifecycle block;
- action label:
  - `Разархивировать актив`;
- confirmation copy explains that this is not restoration to operation;
- API tests updated;
- static UI smoke coverage updated.

Accepted Unarchive behavior:

- only archived assets with `status = RETIRED` can be unarchived;
- `ACTIVE` assets cannot be unarchived;
- non-archived assets cannot be unarchived;
- `archived_at` is cleared on successful Unarchive;
- `status` remains `RETIRED`;
- `assigned_user_id` is not restored;
- `assigned_user_id` is not changed;
- current `location_id` is preserved;
- no responsibility fact is appended for Unarchive;
- one `UNARCHIVE` audit event is emitted;
- Asset record remains readable by id;
- responsibility history remains preserved;
- audit history remains preserved.

Repeated Unarchive behavior:

- already non-archived assets cannot be unarchived again;
- repeated Unarchive returns safe conflict/business-rule rejection;
- repeated Unarchive does not emit duplicate `UNARCHIVE` audit events;
- repeated Unarchive does not append responsibility facts.

Accepted visibility behavior:

- default `GET /api/v1/assets` includes the asset again after Unarchive;
- `GET /api/v1/assets?visibility=archived` excludes the asset after Unarchive;
- archived and unarchived assets remain readable by `GET /api/v1/assets/{id}`;
- Quick Find can include the asset again through refreshed default loaded state;
- Technical Search can include the asset again after `archived_at` is null;
- Employee Sheet still does not include the asset because assignment is not restored;
- Employee Sheet does not expose Unarchive actions.

Preserved boundaries:

- no restore to `ACTIVE`;
- no reactivation;
- no Retire rollback;
- no previous assignee restoration;
- no assignment restoration;
- no location restoration;
- no inventory/warehouse movement;
- no `DELETE /api/v1/assets/{id}`;
- no hard delete;
- no Correction Delete;
- no bulk unarchive;
- no destructive erase;
- no generic PATCH archive manipulation;
- no generic status editor;
- no workflow engine;
- no approval workflow;
- no cross-service orchestration;
- no employee self-service;
- no public lookup;
- no Asset 360;
- no reporting/dashboard dependency.

Preserved UI boundaries:

- Unified Create unchanged;
- Employee Sheet / `Информационный лист` unchanged;
- Quick Find result rows unchanged;
- Technical Search result rows unchanged;
- Asset Overview unchanged as a primary action surface;
- generic master-record edit unchanged;
- Setup/Admin unchanged;
- asset list rows unchanged as direct action surfaces;
- tree rows unchanged as direct action surfaces.

Required next step:

- no further Recovery / Unarchive implementation is opened by this closeout;
- if owner wants restore-to-ACTIVE, open a separate lifecycle reactivation decision gate;
- if owner wants assignment restoration, open a separate responsibility lifecycle decision gate;
- if owner wants bulk unarchive, open a separate bulk-action decision gate;
- if owner wants Correction Delete, open a separate strict correction-delete decision gate;
- otherwise continue to the next owner-selected bounded feature gate or the deferred integrated stabilization / verification gate.

This backlog closeout does not prove release readiness, FTOV start, FTOV exit, production readiness, real tenant validation, or real day-to-day operational validation.