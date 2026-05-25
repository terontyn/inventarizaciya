# System Summary

Status: approved
Owner: project
Last Updated: 2026-05-25

## Purpose
IT Asset Management SaaS для учета активов, оргструктуры, ответственности за технику и истории критических изменений.

## Current Project Stage

Foundation Slice and MVP baseline are already defined and internally implemented for the current project contour.

Current operating mode:

**Project Stabilization / First-Adopter-Gated Hold**

This means the current baseline is internally validated only and still does not prove:

- release-ready tenant-facing product state;
- First-Tenant Operational Validation start;
- First-Tenant Operational Validation exit;
- production tenant readiness;
- real tenant validation;
- real day-to-day operational validation.

## Foundation Slice
Foundation Slice = Tenant + User/Org + Asset + Audit.

На текущем этапе Foundation Slice реализуется через два сервиса:
- user-service
- asset-service

## MVP Definition
Первый релиз фиксируется как Asset Accountability MVP.

MVP должен позволять одному tenant:
- вести tenant-aware учет активов;
- хранить пользователей и оргструктуру;
- закреплять актив за пользователем и локацией;
- выполнять базовые операции ответственности: assign / transfer / return;
- хранить audit и историю критических действий.

## Wave 1 / Release 1
1. Tenant / Access foundation
2. User / Org
3. Asset core
4. Audit / History
5. Asset responsibility flows

## Wave 2
- Ticket
- Inventory
- Software
- Maintenance
- Reporting

## Accepted ADRs
- ADR-0001 Tenancy model: Foundation Slice uses shared DB/shared schema with mandatory tenant_id and tenant enforcement at API/repository boundaries.
- ADR-0002 Role / permission model: tenant-scoped RBAC; User ↔ Role is many-to-many; permissions are attached to roles only.
- ADR-0003 Org hierarchy: no separate Org entity; Department is managerial dimension; Branch / Location are physical dimensions; Location belongs to Branch.
- ADR-0004 Asset CRUD vs history: generic CRUD manages only Asset master record; responsibility-changing operations use dedicated flows.
- ADR-0005 Asset delete semantics: public hard delete is rejected for the current stage; `DELETE /api/v1/assets/{id}` remains unsupported; Retire is the canonical safe lifecycle removal semantics; Archive / Hide and Correction Delete remain deferred decisions.
- ADR-0007 Audit contract: critical writes produce durable local audit records and are delivered asynchronously to central audit storage.
- ADR-0009 Inventory-to-assign handoff semantics: successful assign in `asset-service` is the canonical trigger point for inventory-side storage custody consumption; handoff is internal-only asynchronous; assign success does not require synchronous inventory completion; eventual consistency is accepted for the first contour.

## Architectural Rules
- каждый сервис владеет своей БД;
- прямой доступ к БД другого сервиса запрещен;
- взаимодействие между сервисами идет через API и/или events;
- tenant isolation обязателен;
- API должен быть versioned;
- критичные write-операции должны попадать в audit;
- важные архитектурные решения фиксируются в ADR.

## Open ADR Backlog
- service integration pattern

## Current Implementation Baseline

На текущем этапе проектный канон должен различать:
- scope / architecture / ADR baseline;
- current implementation baseline in repo.


### Current clarification
Для уже реализованных technical slices повторная постановка greenfield implementation task недопустима.

### Current known repo baseline
- Slice 1 (`Tenant + Org Reference Base` in `user-service`) уже реализован;
- Slice 2 (`User + Role Base` in `user-service`) уже реализован на core уровне и не должен переоткрываться как новый implementation slice;
- дальнейшие задачи по этой области должны трактоваться как:
  - review / hardening / gap-closure,
  - или следующий implementation step,
  - но не как повторная реализация уже существующего slice.
- Minimal Pilot UI Contour уже реализован и не должен переоткрываться как новый greenfield implementation contour;


### Rule
Перед подготовкой нового implementation packet для Codex
нужно сначала явно проверить текущий repo state
и зафиксировать, действительно ли target slice еще не реализован.

## Minimal Pilot UI Contour

После backend-first managed pilot baseline следующим contour проекта
является Minimal Pilot UI Contour.

Это не новый модуль и не новый bounded context.
Это минимальный user-facing completion layer,
нужный для practical operational use уже утвержденного Asset Accountability MVP
одним tenant.

## Minimal Pilot Access Contour

После backend + minimal pilot UI internally validated baseline
следующим contour проекта является Minimal Pilot Access Contour.

Это не новый модуль, не новый bounded context
и не новый auth platform contour.

### Purpose
Довести уже утвержденный MVP до practically usable tenant-facing access state
для реального первого tenant.

### Interpretation rule
Minimal Pilot Access Contour трактуется как минимальный completion layer,
который дает user-facing access path в уже утвержденный MVP contour,
но не открывает новый enterprise IAM scope.

## Minimal Pilot Access Contour — Architecture Clarification

Minimal Pilot Access Contour трактуется как минимальный tenant-facing access completion layer
поверх уже реализованного Minimal Pilot UI Contour и существующего backend contour текущего MVP.

### Interpretation rule
- это не новый модуль;
- это не новый bounded context;
- это не новый auth platform contour;
- это не новый backend service family.

### Consumption rule
Access contour должен опираться на уже существующие слои:
- existing UI contour как user-facing surface;
- `user-service` как owner tenant/user/role foundation;
- `asset-service` как owner asset-side MVP surfaces.

### Architectural rule
В рамках текущего contour не открываются:
- отдельный auth service;
- отдельный IAM backend;
- отдельный BFF;
- новый enterprise IAM scope;
- новые backend endpoints только ради access convenience.

## Minimal Pilot UI Contour — Architecture Clarification

Minimal Pilot UI Contour трактуется как минимальный user-facing completion layer
поверх уже утвержденного backend contour текущего MVP.

### Interpretation rule
- это не новый модуль;
- это не новый bounded context;
- это не новый integration contour;
- это не новый API/read-model contour.

### Consumption rule
UI contour должен опираться на уже существующие service boundaries:
- `asset-service` для asset registry / asset card / asset CRUD v1 / dedicated responsibility flows / history-audit visibility;
- `user-service` для roles / users / departments / branches / locations.

### Architectural rule
В рамках текущего contour не открываются:
- отдельный BFF;
- отдельный UI service;
- отдельная microfrontend family;
- новые backend endpoints только ради UI convenience.


## Wave 2 Sequencing Clarification

После закрытия Asset Accountability MVP
следующим рекомендуемым contour внутри Wave 2
является `Inventory / Warehouse`.

### Why this contour next
Этот contour является ближайшим product extension
к уже утвержденному asset accountability baseline.

Текущий MVP отвечает на вопросы:
- что у нас есть;
- где это находится;
- за кем это закреплено;
- кто это менял.

Следующий практический operational gap:
- что сейчас находится в storage / warehouse custody;
- где именно это хранится;
- что доступно для следующей выдачи;
- как актив переходит из хранения в выдачу и обратно.

### Interpretation rule
`Inventory / Warehouse` на первом шаге трактуется не как full WMS / ERP,
а как bounded storage-aware contour
для already tracked physical assets.

 
## Inventory Availability / Readiness Refinement

После closeout первого narrow `Inventory / Warehouse` contour
следующим bounded expansion slice
внутри inventory context
рекомендуется `Inventory Availability / Readiness Refinement`.

### Purpose
Этот slice нужен,
чтобы различать:
- asset physically in storage;
- asset in storage and inventory-side ready for next assignment;
- asset in storage but inventory-side not ready.

### Boundary rule
Этот slice:
- остается внутри `Inventory / Warehouse`;
- не переопределяет asset identity;
- не переопределяет responsibility lifecycle;
- не переносит final assign eligibility в inventory context;
- не открывает full WMS / ERP scope.

## Wave 2 Sequencing Clarification After Inventory Readiness

После closeout `Inventory Availability / Readiness Refinement`
следующим рекомендуемым шагом Wave 2
является переход
к следующему большому contour,
а не еще один bounded inventory slice.

### Recommended next contour
Следующим рекомендуемым contour
является `Maintenance`.

## Maintenance — Wave 2 Architecture Clarification

После closeout `Inventory Availability / Readiness Refinement`
следующим рекомендуемым большим Wave 2 contour
является `Maintenance`.

### Purpose
`Maintenance` трактуется как serviceability layer
для already tracked physical assets.

Этот contour должен отвечать на вопросы:
- требует ли Asset inspection / repair / servicing;
- какой maintenance work сейчас открыт;
- когда Asset возвращается в serviceable operational state.

### Boundary rule
`Maintenance`:
- не переопределяет asset identity или asset master record;
- не переопределяет inventory custody ownership;
- не переопределяет inventory readiness ownership;
- не переопределяет responsibility lifecycle или final assign decision logic;
- не трактуется как generic service desk / full ITSM suite;
- не открывает procurement / supplier / finance / depreciation scope.

## Maintenance Minimal Contour Closeout

Первый narrow `Maintenance` contour реализован
как отдельный bounded Wave 2 serviceability layer
для already tracked physical assets.

Этот шаг:
- не изменил ownership boundaries;
- не перенес inventory readiness ownership в maintenance;
- не изменил ADR-0009;
- не открыл helpdesk / ITSM / spare-parts / finance scope.

## Wave 2 Sequencing Clarification After Maintenance

После closeout первого narrow `Maintenance` contour
следующим рекомендуемым шагом Wave 2
является переход
к другому большому contour,
а не еще один default maintenance expansion slice.

### Recommended next contour
Следующим рекомендуемым contour
является `Software`.


## Software — Wave 2 Architecture Clarification

После closeout первого narrow `Maintenance` contour
следующим рекомендуемым большим Wave 2 contour
является `Software`.

### Purpose
`Software` трактуется как software-aware operational layer
для already tracked users и already tracked assets.

Этот contour должен отвечать на вопросы:
- какие software records tenant сейчас ведет;
- какие current software-to-user relations существуют;
- какие current software-to-asset relations существуют.

### Boundary rule
`Software`:
- не переопределяет asset identity или asset master record;
- не переопределяет responsibility lifecycle или final assign decision logic;
- не переопределяет inventory custody / readiness ownership;
- не переопределяет maintenance ownership;
- не трактуется как full SAM suite;
- не открывает procurement / renewals / supplier / finance scope;
- не открывает compliance-heavy program как first-step contour.

## Software Minimal Contour Closeout

Первый narrow `Software` contour реализован
как отдельный bounded Wave 2 software-aware operational layer
для already tracked users и already tracked assets.

Этот шаг:
- не изменил ownership boundaries;
- не перенес ownership existing contexts в software;
- не изменил ADR-0009;
- не открыл full SAM / renewals / supplier / finance / compliance-heavy scope.

## Wave 2 Sequencing Clarification After Software

После closeout первого narrow `Software` contour
следующим рекомендуемым шагом Wave 2
является переход
к другому большому contour,
а не еще один default software expansion slice.

### Recommended next contour
Следующим рекомендуемым contour
является `Ticket`.

### Reporting note
`Reporting`
не рекомендуется
как следующий primary contour
на текущем этапе.

## Ticket — Wave 2 Architecture Clarification

После closeout первого narrow `Software` contour
следующим рекомендуемым большим Wave 2 contour
является `Ticket`.

### Purpose
`Ticket` трактуется как narrow operational issue / request coordination layer
для already tracked ITAM objects,
прежде всего вокруг tracked assets.

Этот contour должен отвечать на вопросы:
- какие operational issues / requests сейчас открыты;
- к каким tracked assets они относятся;
- каков current coordination state этих tickets.

### Boundary rule
`Ticket`:
- не переопределяет asset identity или asset master record;
- не переопределяет responsibility lifecycle или final assign decision logic;
- не переопределяет inventory custody / readiness ownership;
- не переопределяет maintenance workflow ownership;
- не переопределяет software ownership;
- не трактуется как generic enterprise helpdesk;
- не открывает full ITSM suite;
- не открывает SLA / queue / escalation platform;
- не трактуется как reporting-first contour.

## Ticket Minimal Contour Closeout

Первый narrow `Ticket` contour реализован
как отдельный bounded Wave 2 operational issue / request coordination layer
для already tracked assets.

Этот шаг:
- не изменил ownership boundaries;
- не перенес ownership existing contexts в ticket;
- не изменил ADR-0009;
- не открыл generic helpdesk / full ITSM / SLA / escalation / omnichannel scope.

## Wave 2 Sequencing Clarification After Ticket

После closeout первого narrow `Ticket` contour
следующим рекомендуемым шагом Wave 2
является переход
к другому большому contour,
а не еще один default ticket expansion slice.

### Recommended next contour
Следующим рекомендуемым contour
является `Reporting`.

### Why not continue Ticket now
После minimal ticket closeout
ticket уже достиг bounded usable state
для своего narrow product purpose:
- what current open tickets exist;
- which tracked assets they are linked to;
- whether a given ticket-side coordination loop is open or closed.

Следующие candidate expansions внутри ticket
уже в основном ведут в:
- requester / assignee refinement;
- queue / categorization refinement;
- richer ticket field model;
- cross-context action-link refinement;
- history-heavy refinement.

Это означает,
что дальнейшее ticket expansion
дает менее сильный next product leap
и повышает риск drift
в generic helpdesk / ITSM direction.

### Why Reporting now
После inventory, maintenance, software и ticket closeout
в проекте уже существуют несколько narrow current-state operational contours.

Следующий practical product gap
смещается в cross-context visibility:
- where assets are now;
- whether they are ready or not ready;
- whether maintenance is open;
- whether tickets are open;
- what software is currently related.

### Boundary rule
Этот sequencing decision:
- не открывает generic enterprise helpdesk;
- не открывает full ITSM suite;
- не открывает reporting / BI heavy platform;
- не меняет ownership existing contexts;
- не меняет ADR-0009.

## Reporting — Wave 2 Architecture Clarification

После closeout первого narrow `Ticket` contour
следующим рекомендуемым большим Wave 2 contour
является `Reporting`.

### Purpose
`Reporting` трактуется узко:
как cross-context operational visibility layer
поверх уже существующих current-state contours.

Этот contour должен отвечать на вопросы:
- какие assets сейчас in storage / ready / not ready;
- какие assets currently under maintenance;
- какие assets currently have open tickets;
- какие current software relations сейчас существуют
  для already tracked users и assets.

### Boundary rule
`Reporting`:
- не переопределяет ownership existing domains;
- не становится BI-first analytics platform;
- не открывает heavy KPI / dashboard suite by default;
- не открывает free-form reporting platform as first step;
- не открывает umbrella orchestration semantics
  поверх inventory / maintenance / software / ticket.

### Minimal cutline
Первый `Reporting` contour
должен оставаться current-state-first
и asset-centric,
а не historical BI-heavy contour.

## Reporting Minimal Contour Closeout

Первый narrow `Reporting` contour реализован
как отдельный bounded Wave 2 cross-context operational visibility layer
поверх already existing current-state owner contours.

Этот шаг:
- не изменил ownership boundaries;
- не перенес ownership existing contexts в reporting;
- не изменил ADR-0009;
- не открыл BI-first / KPI-heavy / free-form analytics scope;
- не открыл reporting-side write ownership или write workflow.

## Wave 2 Completion Clarification

После closeout первого narrow `Reporting` contour
current Wave 2 map
следует трактовать
как effectively complete
в его narrow operational interpretation.

### Why
На этом этапе уже открыты и minimally closed:
- `Inventory / Warehouse`
- `Maintenance`
- `Software`
- `Ticket`
- `Reporting`

Следовательно,
current Wave 2
уже покрывает narrow set of:
- storage / readiness visibility;
- serviceability visibility;
- software allocation visibility;
- ticket coordination visibility;
- cross-context operational reporting visibility.

### Practical rule
Это означает,
что следующим default move
не должен становиться
еще один bounded reporting slice
или новый large contour
внутри current Wave 2 map.

## Post-Wave-2 Transition Clarification

После effective closeout current Wave 2 map
следующим правильным шагом
следует считать
не opening another default contour,
а переход
к следующему product stage.

### Immediate recommended move
Ближайший justified move:
**cross-contour hardening / validation**
поверх уже открытого narrow Wave 2 stack.

### Why not Reporting expansion now
Следующие candidate expansions внутри reporting
в основном ведут в:
- historical reporting;
- KPI / dashboard catalog;
- trend semantics;
- broader aggregation semantics.

Это означает drift
в сторону BI / analytics platform,
что не соответствует
narrow first-step reporting interpretation.

### Boundary rule
Этот post-Wave-2 move:
- не открывает BI-first platform;
- не открывает free-form analytics;
- не переносит ownership existing contexts;
- не открывает umbrella orchestration semantics.


## Post-Wave-2 Hardening / Validation — Architecture Invariants

Post-Wave-2 `cross-contour hardening / validation`
должен трактоваться
как limited cross-cutting architecture stage
поверх already approved narrow Wave 2 stack.

### Architecture interpretation
Этот stage:
- не является новым bounded context;
- не является новым large contour;
- не открывает автоматически новый Wave map;
- не открывает новый integration layer;
- не открывает новый orchestration layer;
- не открывает новый primary API family;
- не открывает reporting-side write ownership;
- не открывает BI-first / KPI-heavy / free-form analytics platform.

### Core validation purpose
На этом stage
должны быть подтверждены:
- preservation of ownership boundaries;
- cross-context consistency;
- alignment between owner-side current-state visibility
  and reporting-side derived visibility;
- acceptability of bounded lag / eventual consistency;
- operational usability
  combined narrow Wave 2 stack
  as a single product baseline.

### Authoritative-source rule
При расхождении
между owner-context dedicated surface
и reporting-side derived visibility
authoritative source
остается owner context
для соответствующего fact slice.

### Expansion cutline
Если работа начинает требовать:
- нового owner;
- новой standalone model family;
- нового write-owning context;
- нового orchestration layer;
- нового BI/reporting capability family,

это уже не данный stage,
а новый product expansion.

## Current Project Stage Clarification

Current narrow Wave 2 map should be treated as effectively complete
in its narrow operational interpretation.

The first minimal post-Wave-2 hardening / validation packet
is closed at implementation + verification level.

### Honest baseline interpretation
The current baseline should be treated as:
- internally validated managed pilot-like product baseline;

and not as:
- release-ready tenant-facing product;
- fully proven real pilot usage;
- fully validated operational tenant rollout.

### Recommended next stage
The next recommended stage is:

**First-Tenant Operational Validation**

This stage must be treated as a dedicated user-facing validation contour
for one real tenant,
not as:
- a new bounded context;
- another Wave 2 contour;
- a release-ready claim.

## Pilot UI Stabilization / Managed Demo Readiness Clarification

Текущий repo baseline подтверждает,
что Pilot UI уже существует как managed pilot / internal pilot surface.

Этот UI surface включает:
- managed pilot context entry;
- asset registry;
- asset card;
- asset create / update master-record path;
- dedicated assign / transfer / return entry points;
- history / audit visibility;
- minimal admin/setup layer для roles, users, departments, branches и locations.

Это не означает,
что продукт является release-ready.

Текущий UI следует трактовать как:

**managed demo / internal pilot surface поверх уже approved MVP contour**

а не как:
- production tenant-facing UI;
- self-service onboarding;
- auth/IAM platform;
- First-Tenant Operational Validation evidence;
- release-ready tenant-facing baseline.

## Productized Tenant-Facing Operational UX Expansion — Direction Gate

After Product Expansion Decision Gate,
the strongest approved separate product expansion direction is:

**Productized Tenant-Facing Operational UX Expansion**

This direction recognizes that:
- Pilot UI already exists as managed pilot / internal pilot surface;
- managed demo is not the next required step;
- First-Tenant Operational Validation remains not executable
  without real tenant / first adopter / real usage loop;
- release-ready tenant-facing claim is not supported.

The expansion direction is limited to defining richer tenant-facing operational UX
over already approved MVP and narrow Wave 2 surfaces.

It is not:
- a new bounded context;
- a new Wave map;
- an IAM / auth platform expansion;
- self-service onboarding;
- BI / dashboard / analytics expansion;
- WMS / ITSM / SAM expansion;
- a release-ready claim.

This direction has since been scoped, implemented, and accepted as:

**Productized Tenant-Facing Operational UX v1**

It is now closed as frontend-only UX refinement of the existing static Pilot UI.

Further work in this area must be treated as targeted defect / gap closure unless separately approved.


## Current Baseline Update — Productized Tenant-Facing Operational UX v1

The current project baseline should now be interpreted as:

internally validated backend + minimal pilot UI/access baseline with Productized Tenant-Facing Operational UX v1 implemented.

Productized Tenant-Facing Operational UX v1 was implemented as frontend-only refinement of the existing static Pilot UI over already approved MVP and narrow Wave 2 surfaces.

Implementation closeout precision:
- production backend/API behavior was not changed;
- no new backend endpoint or API family was introduced;
- no new bounded context was opened;
- no BFF, UI backend, auth service, or IAM platform was introduced;
- no new Wave map or Wave 2 surface expansion was opened;
- test-only smoke coverage was updated through MinimalPilotUiSmokeTest.java.

This baseline remains internally validated only.

It is not:
- release-ready tenant-facing product;
- First-Tenant Operational Validation exit;
- fully proven real tenant usage;
- fully validated day-to-day operational tenant rollout.

Future First-Tenant Operational Validation still requires real tenant / first adopter / real usage loop.

## Current Baseline Update — Operational Workspace UX v2 Closeout

`Operational Workspace UX v2 / Release UI Layout & Navigation Cutline`
has now been implemented and accepted.

The implementation is accepted as:

**frontend-only layout, navigation and readability refinement of the existing static operational UI.**

This update supersedes the earlier interpretation
that Operational Workspace UX v2 was approved as scope only
with implementation not yet approved.

### Implementation closeout precision

Operational Workspace UX v2 was implemented as refine-in-place UI work over already accepted operational surfaces.

It changed the release-facing workspace structure, navigation clarity, readability and user-facing wording,
without changing production backend/API behavior.

Implementation closeout facts:

- existing static UI was refined in place;
- layout / navigation / readability were improved;
- RU-facing copy and technical wording were cleaned up;
- test-only smoke coverage was updated through `MinimalPilotUiSmokeTest.java`;
- no production backend/API changes were made;
- no new backend endpoint was introduced;
- no DTO or read model was changed;
- no BFF or UI backend was introduced;
- no auth/IAM expansion was introduced;
- no new frontend framework, router, microfrontend or UI service was introduced;
- no new bounded context was opened;
- no new Wave 2 surface was opened.

### Boundary preservation

Operational Workspace UX v2 did not open:

- dashboard / BI / analytics;
- Asset 360;
- global search / Elasticsearch;
- saved views / persistent preferences;
- bulk actions;
- backend/API expansion;
- BFF / UI backend;
- auth/IAM expansion;
- new bounded context;
- new Wave 2 surface.

Responsibility-changing operations remain dedicated flows:

- assign;
- transfer;
- return.

Generic asset edit remains limited to master-record changes
and does not replace responsibility-changing flows.

### Current baseline after closeout

The current project baseline should now be interpreted as:

**internally validated backend + minimal pilot UI/access baseline with Productized Tenant-Facing Operational UX v1 implemented, plus Operational Workspace UX v2 implemented and accepted as frontend-only refine-in-place layout/navigation/readability refinement.**

This baseline remains internally validated only.

It is not:

- release-ready tenant-facing product;
- First-Tenant Operational Validation exit;
- production tenant readiness proof;
- fully proven real tenant usage;
- fully validated day-to-day operational tenant rollout.

Future First-Tenant Operational Validation still requires:

- real tenant / first adopter;
- real tenant actors;
- real usage loop;
- observed operational actions beyond scripted internal walkthrough.

## Current Baseline Update — Operational Workspace UX Stabilization v3 Closeout

Operational Workspace UX Stabilization v3 has been implemented and accepted as a targeted frontend-only refine-in-place gap closure over the existing Operational Workspace UI.

The stabilization addressed controlled synthetic review findings around:

- navigation / anchors / long-scroll orientation;
- action feedback and loading states;
- empty states and prerequisite guidance;
- UUID / technical value readability;
- asset registry and asset card hierarchy;
- setup table / tab affordance;
- assign / transfer / return clarity;
- generic edit guardrail preservation;
- history / audit readability;
- role affordance within existing role context.

Post-v3 controlled synthetic re-review status:

**Passed with minor friction.**

The post-v3 re-review confirmed that the v3 stabilization improved:

- sticky / anchored navigation;
- active section indication;
- loading states and double-submit prevention;
- UUID / technical value readability;
- asset registry and asset card hierarchy;
- tenant setup guidance and editor context;
- assign / transfer / return separation;
- generic edit guardrail preservation;
- history / audit readability;
- role and scope affordance labels.

Remaining minor UX friction is limited to:

- explicit success/error notifications after mutating actions;
- clearer role assignment confirmation;
- residual audit wording / technical correlation details;
- minor navigation subtlety near already-visible target sections;
- continued truncation of context values, mitigated by copy/tooltip affordances.

These are minor frontend UX hardening candidates only.

They do not change the validation ceiling and do not reopen product scope.

Implementation closeout precision:

- only existing static UI files were changed:
  - `index.html`
  - `app.css`
  - `app.js`
- no production backend/API behavior was changed;
- no new backend endpoint was introduced;
- no DTO or read model was changed;
- no database migration was changed;
- no BFF or UI backend was introduced;
- no auth/IAM expansion was introduced;
- no new bounded context was opened;
- no new Wave 2 surface was opened;
- no product parity scope was opened.

This baseline remains internally validated only.

It is not:

- release-ready tenant-facing product;
- First-Tenant Operational Validation start;
- First-Tenant Operational Validation exit;
- production tenant readiness proof;
- fully proven real tenant usage;
- fully validated day-to-day operational tenant rollout.

Future First-Tenant Operational Validation still requires:

- real tenant / first adopter;
- real tenant actors;
- real usage loop;
- observed operational actions beyond scripted internal walkthrough.

## Current Baseline Update — Operational Workspace UX v3.1 Closeout

Operational Workspace UX v3.1 has been implemented and accepted as a narrow frontend-only usability hardening pass over the accepted Operational Workspace UX v3 surface.

v3.1 addressed:

- visible success/error feedback after mutating actions;
- clearer role assignment confirmation;
- setup table affordance preservation;
- technical-noise reduction preservation;
- audit/history wording hardening;
- location visibility using existing frontend data only.

Implementation precision:

- changed files were limited to existing static UI resources:
  - `app.css`
  - `app.js`
- no production backend/API behavior was changed;
- no new endpoint was introduced;
- no DTO/read-model change was introduced;
- no database/migration change was introduced;
- no BFF/UI backend was introduced;
- no auth/IAM expansion was introduced;
- no product parity scope was opened.

Manual ground-truth verification confirmed that setup save and role assignment notifications are visible in the live UI.

Remaining minor UX friction:

- notification lifetime may be tuned shorter later.

This remains controlled synthetic / internal evidence only.

It is not:

- release-ready tenant-facing product;
- FTOV start;
- FTOV exit;
- production tenant readiness proof;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Operational Workspace Tree-and-Detail UX Closeout

Operational Workspace Tree-and-Detail UX has been implemented and accepted as a frontend-only information architecture / layout restructuring over the accepted Operational Workspace UI surface.

The accepted UI model now uses:

- collapsed hierarchical left navigation;
- selected-object right detail pane;
- contextual asset tabs;
- separated Setup/Admin area.

Accepted navigation model:

- `Оргструктура -> Department -> Users -> assigned assets`;
- `Локации -> Branch -> Location -> assets at location`;
- `Все активы -> Назначенные / Неназначенные / Retired`;
- `Настройка -> Роли / Пользователи / Подразделения / Филиалы / Локации`.

The implementation preserved:

- dedicated assign / transfer / return flows;
- generic edit as master-record-only;
- selected-asset contextual History / Audit;
- technical details as secondary/copyable;
- v3.1 success/error feedback behavior.

Post-implementation controlled synthetic / internal review confirmed:

- old long-scroll primary UI is replaced by tree-and-detail layout;
- left navigation tree is visible and functional;
- right detail pane opens selected-node detail;
- asset tabs are present and usable;
- setup/admin is separated from daily asset operations;
- feedback preservation correction passed.

This remains controlled synthetic / internal evidence only.

It is not:

- release-ready tenant-facing product;
- FTOV start;
- FTOV exit;
- production tenant readiness proof;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Operational Workspace Tree-and-Detail UX Density Reduction Closeout

Operational Workspace Tree-and-Detail UX Density Reduction has been implemented and accepted as a small frontend-only density reduction and progressive disclosure hardening pass over the accepted Tree-and-Detail UI.

The hardening reduced visual density while preserving the accepted Tree-and-Detail model:

- `Оргструктура -> Department -> Users -> assigned assets`;
- `Локации -> Branch -> Location -> assets at location`;
- `Все активы -> Назначенные / Неназначенные / Retired`;
- `Настройка -> Роли / Пользователи / Подразделения / Филиалы / Локации`.

The implementation preserved:

- selected-object right detail pane;
- contextual asset tabs;
- dedicated assign / transfer / return flows;
- generic edit as master-record-only;
- selected-asset contextual History / Audit;
- Technical details accessibility;
- Setup/Admin separation;
- v3.1 / Tree-and-Detail feedback behavior.

Density reduction addressed:

- tree scan/click usability;
- form progressive disclosure;
- technical details de-emphasis;
- audit raw-details collapse;
- first-step daily-user guidance;
- empty-state guidance;
- Setup/Admin de-emphasis.

A tiny local-only visible-tree filter was added without backend calls, persistence, saved views, global search or advanced filtering.

Controlled synthetic / internal live re-check confirmed that density reduction passed with minor friction.

Targeted Setup navigation verification confirmed that setup sub-items appear and route correctly.

This remains controlled synthetic / internal evidence only.

It is not:

- release-ready tenant-facing product;
- FTOV start;
- FTOV exit;
- production tenant readiness proof;
- real tenant validation;
- real day-to-day operational validation.


## Current Baseline Update — Operational Workspace Tree-and-Detail UX Small Frontend Cleanup Closeout

Operational Workspace Tree-and-Detail UX Small Frontend Cleanup has been implemented and accepted as a small frontend-only cleanup pass over the accepted Tree-and-Detail UI after Density Reduction.

The cleanup preserved the accepted Tree-and-Detail model:

- `Оргструктура -> Department -> Users -> assigned assets`;
- `Локации -> Branch -> Location -> assets at location`;
- `Все активы -> Назначенные / Неназначенные / Retired`;
- `Настройка -> Роли / Пользователи / Подразделения / Филиалы / Локации`.

The implementation preserved:

- selected-object right detail pane;
- contextual asset tabs;
- dedicated assign / transfer / return flows;
- generic edit as master-record-only;
- selected-object contextual History / Audit;
- Technical details accessibility;
- Setup/Admin separation;
- v3.1 / Tree-and-Detail feedback behavior.

Small cleanup addressed:

- Setup/Admin duplicate navigation reduction;
- setup-order guidance simplification;
- service ID primary table de-emphasis;
- compact accessible clipboard copy controls;
- connection status badge;
- local tree filter clarity;
- Actions tab hierarchy;
- zero-count wording;
- hover/focus states;
- selected tree row emphasis;
- responsive wrapping.

The implementation intentionally skipped:

- full user role column/chips because complete user -> roles mapping is not available in current frontend state;
- client-side sorting;
- client-side pagination;
- broad feedback/toast rewrite.

Controlled synthetic / internal live re-check confirmed that the small cleanup passed with minor friction.

This remains controlled synthetic / internal evidence only.

It is not:

- release-ready tenant-facing product;
- FTOV start;
- FTOV exit;
- production tenant readiness proof;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Asset Identity v1 Canon Alignment

Asset Identity v1 canon alignment has been accepted as a narrow Asset master-record extension.

Approved fields:

- `display_name`
- `note`

Interpretation:

- `display_name` is an optional nullable human-readable Asset title;
- `note` is an optional nullable plain-text operational note;
- `asset_tag` remains the canonical inventory/business identifier;
- Assign / Transfer / Return remain dedicated responsibility flows;
- generic edit remains master-record-only.

This canon alignment does not open:

- QR/barcode;
- import/export;
- global search;
- Asset 360;
- reporting/dashboard;
- attachments/documents/photos/acts;
- Hardware Inspector parity.

This does not prove release readiness, FTOV start, FTOV exit, production readiness, real tenant validation or real day-to-day operational validation.

## Current Baseline Update — Asset Identity v1 Backend/API/DB Implementation Closeout

Asset Identity v1 backend/API/DB implementation has been completed and accepted as a bounded Asset master-record extension.

Implemented fields:

- `display_name`
- `note`

Implementation interpretation:

- `display_name` is an optional nullable human-readable Asset title;
- `note` is an optional nullable plain-text operational note;
- `asset_tag` remains the canonical inventory/business identifier;
- Assign / Transfer / Return remain dedicated responsibility flows;
- Retire remains dedicated where currently present;
- generic edit remains master-record-only;
- `assigned_user_id` and `location_id` do not move into generic PATCH;
- ordinary PATCH remains outside status-transition semantics.

Implementation scope:

- backend/API/DB only;
- Flyway migration added nullable `display_name` and nullable `note`;
- Asset model, repository, DTO/API response, validation/normalization and audit payload were updated;
- tests passed for Asset API, schema constraints and the full asset-service test suite.

This implementation did not open:

- frontend/static UI changes;
- Documentation changes as part of Codex implementation;
- QR/barcode;
- import/export;
- global search;
- Asset 360;
- reporting/dashboard;
- manufacturer/model/cost fields;
- attachments/documents/photos/acts;
- Hardware Inspector parity.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Asset Identity v1 Frontend Usage Implementation Closeout

Asset Identity v1 frontend usage has been implemented and accepted inside the existing Operational Workspace Tree-and-Detail UI.

Implemented UI usage:

- `display_name` is used as the human-readable Asset title where available;
- `asset_tag` remains visible as the canonical inventory/business identifier;
- fallback title logic is centralized:
  - `display_name`;
  - `asset_tag`;
  - `asset_type` / `serial_number`;
  - shortened Asset ID only as last resort;
- `note` is available as optional plain-text operational context in the selected Asset Overview;
- create Asset form includes optional `display_name` and `note`;
- master-record edit form includes optional `display_name` and `note`;
- create/edit payloads send `display_name` and `note` as Asset master-record fields.

Preserved boundaries:

- Assign remains dedicated;
- Transfer remains dedicated;
- Return remains dedicated;
- Retire remains dedicated where currently present;
- generic edit remains master-record-only;
- `assigned_user_id` does not move into generic edit;
- `location_id` does not move into generic edit where current boundary forbids it;
- ordinary PATCH remains outside status-transition semantics.

Implementation scope:

- frontend-only;
- existing static Operational Workspace UI only;
- no backend/API/DB changes;
- no Documentation changes as part of Codex implementation;
- no new endpoint;
- no BFF/UI backend;
- no new frontend framework/router/microfrontend.

This implementation did not open:

- unified `Создать` launcher;
- quick edit redesign;
- context connect screen redesign;
- QR/barcode;
- import/export;
- global/backend search;
- Asset 360;
- reporting/dashboard;
- manufacturer/model/cost fields;
- attachments/documents/photos/acts;
- rich text notes;
- note threads;
- Hardware Inspector parity.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Owner-Approved Future Planning Candidates

After Asset Identity v1 closeout, the owner approved several future planning candidates.

These are planning candidates only.

They do not authorize implementation, Codex, backend/API changes, DB changes, frontend changes, new endpoints, new bounded contexts, new Wave 2 surfaces, release readiness, FTOV start, FTOV exit, production readiness or real tenant validation.

Approved future planning candidates:

- Unified Create Launcher:
  - one `Создать` entry point for:
    - Asset / device;
    - User;
    - Department;
    - Branch;
    - Location.
- Simplified Asset Actions UX:
  - clearer selected-asset action orientation;
  - Assign / Transfer / Return remain dedicated.
- Delete / Archive Semantics:
  - separate safe decision for retire / archive / correction delete;
  - hard delete not approved by default.
- End-User Asset Information View:
  - simple read-only employee/device information surface;
  - access-control decision required.
- Search v1:
  - employee / asset tag / display name / serial search;
  - not local tree filter.
- QR Lite:
  - derived QR only;
  - no scanner/printer/barcode platform by default.
- Asset Type-Specific Technical Attributes:
  - examples include AnyDesk ID, phone IP, extension, printer IP;
  - schema/API/UI decision required.
- Configuration Manager-inspired analysis:
  - useful for future planning only;
  - no agent/discovery/remote-control/deployment/patch-management/parity scope approved.

Current recommended next decision:

`Post-Technical-Attributes Stabilization / Controlled End-to-End Recheck`

Reason:

All currently owner-prioritized post-Asset-Identity operational usability candidates have now been implemented or closed at their approved bounded scope levels.

The next default move should not be another product expansion.

Recommended next work should be a stabilization / verification gate that checks the integrated Operational Workspace after:

- Unified Create Launcher;
- Simplified Asset Actions UX;
- ADR-0005 Asset Delete Semantics;
- Controlled Employee Asset Information Sheet;
- Operational Search v1 Frontend Quick Find;
- Asset QR Lite;
- Asset Type-Specific Technical Attributes v1.

Optional branches:

If the owner explicitly wants search by technical attributes, open:

`Search over Technical Attributes Decision Gate`

This optional branch must remain separate from current frontend quick find, backend search, advanced filtering, saved views, reporting/dashboard and Elasticsearch/global search.

If the owner explicitly wants technical attributes visible to employees, open:

`Employee Information Sheet Technical Attribute Exposure Decision Gate`

This optional branch must remain separate from employee self-service, public lookup, QR payloads, export/PDF and Asset 360.

If the owner explicitly wants import/export, reporting, discovery/agent/scanner or Asset 360, open a separate product decision gate first.

## Current Baseline Update — Unified Create Launcher Closeout

Operational Workspace / Unified Create Launcher has been implemented and accepted as a bounded frontend-only UX/navigation shortcut over existing creation surfaces.

Implemented launcher:

- `Актив / устройство`;
- `Пользователь`;
- `Подразделение`;
- `Филиал`;
- `Локация`.

Implementation interpretation:

- one visible `Создать` entry point was added to the existing Operational Workspace shell;
- launcher routes/focuses existing create forms and setup surfaces;
- it does not create a new object-builder module;
- it does not create a new master-data module;
- it does not add new entity families;
- it does not change backend/API/DB behavior.

Preserved boundaries:

- Assign remains dedicated;
- Transfer remains dedicated;
- Return remains dedicated;
- Retire remains dedicated where currently present;
- asset create does not become assign;
- asset create does not become create-and-assign;
- generic edit remains master-record-only;
- Setup/Admin remains owner for users, departments, branches and locations.

This implementation did not open:

- backend/API changes;
- DB changes;
- new endpoints;
- DTO/read-model changes;
- BFF/UI backend;
- auth/IAM expansion;
- quick edit redesign;
- Delete / Archive;
- End-User Asset Information View;
- Search v1;
- QR/barcode;
- import/export;
- Asset 360;
- reporting/dashboard;
- Hardware Inspector parity.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Simplified Asset Actions UX Closeout

Operational Workspace / Simplified Asset Actions UX has been implemented and accepted as a bounded frontend-only refinement over existing selected-asset action surfaces.

Implemented UX refinement:

- selected Asset Actions tab now has clearer grouping;
- responsibility actions are grouped under `Ответственность`;
- master-record editing is grouped under `Описание актива`;
- user-facing action labels were clarified:
  - `Выдать сотруднику`;
  - `Переместить`;
  - `Вернуть`;
  - `Редактировать описание актива`;
- existing Overview `Открыть действия` behavior remains a lightweight hint and does not host duplicated action forms;
- state-aware guidance uses only existing selected Asset state.

Implementation interpretation:

- this is visual/action-orientation refinement only;
- existing forms and submit handlers remain in use;
- existing Assign / Transfer / Return endpoints and flows remain dedicated;
- generic edit remains master-record-only;
- no universal action endpoint was created.

Preserved boundaries:

- Assign remains dedicated;
- Transfer remains dedicated;
- Return remains dedicated;
- Retire remains unchanged where currently present;
- no Retire action was added;
- no `/retire` surface was added;
- asset create does not become assign;
- asset create does not become create-and-assign;
- Unified `Создать` remains creation-only;
- Assign / Transfer / Return do not move into `Создать`;
- `assigned_user_id` does not move into generic edit;
- `location_id` does not move into generic edit where current boundary forbids it;
- status transition does not move into ordinary PATCH.

This implementation did not open:

- backend/API changes;
- DB changes;
- migrations;
- new endpoints;
- DTO/read-model changes;
- BFF/UI backend;
- auth/IAM expansion;
- Delete / Archive;
- hard delete;
- archive implementation;
- correction delete;
- Retire action;
- QR/barcode;
- import/export;
- Search v1;
- Asset 360;
- reporting/dashboard;
- Asset Type-Specific Technical Attributes;
- Hardware Inspector parity.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — ADR-0005 Asset Delete Semantics Acceptance

ADR-0005 Asset Delete Semantics is accepted.

Canonical asset delete semantics are now:

- public hard delete is rejected for the current stage;
- `DELETE /api/v1/assets/{id}` remains unsupported;
- Retire is the canonical safe lifecycle removal semantics;
- Retire preserves the Asset record, audit and responsibility history;
- Retire is not hard delete;
- Retire is not Archive / Hide;
- Retire is not Correction Delete;
- Retire is not ordinary generic PATCH;
- existing backend `/retire` route remains blocked / not product-exposed until separately approved for exposure;
- Archive / Hide remains deferred visibility semantics;
- Correction Delete remains a future candidate only for mistaken records with strict no-history constraints;
- Correction Delete is not approved for implementation now.

This ADR acceptance does not authorize:

- unlocking `/retire`;
- exposing Retire in UI;
- adding Archive / Hide;
- adding Correction Delete;
- adding hard delete;
- adding DB migrations;
- changing role/IAM behavior.

This remains a docs/ADR semantics acceptance only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Controlled Employee Asset Information Sheet Closeout

Controlled Employee Asset Information Sheet has been implemented and accepted as the first safe bounded implementation of the End-User Asset Information View direction.

User-facing feature name:

`Информационный лист сотрудника`

Implementation interpretation:

- implemented inside the existing Operational Workspace;
- available from selected employee / user detail context;
- read-only only;
- uses existing loaded frontend state;
- shows employee identity context and assigned devices;
- remains inside managed authorized workspace;
- does not create public lookup;
- does not create employee self-service.

Displayed information:

- employee name;
- department when already resolvable;
- assigned devices;
- asset title / `display_name`;
- inventory tag / `asset_tag`;
- asset type;
- serial number;
- status;
- location only when already resolvable from existing frontend state.

Hidden or excluded by default:

- `note`;
- audit/history;
- raw technical UUIDs as primary content;
- service/correlation IDs;
- QR;
- export/PDF/Excel/print actions.

User-facing role labels introduced for product/UX copy:

- `Администратор`;
- `Оператор учёта`;
- `Наблюдатель / Аудитор`;
- `Сотрудник`.

Technical role constants remain unchanged.

Preserved boundaries:

- no backend/API changes;
- no DB changes;
- no migrations;
- no new endpoint;
- no auth/IAM expansion;
- no new role model;
- no ordinary employee role;
- no employee self-view;
- no public full-name lookup;
- no Assign / Transfer / Return actions inside the sheet;
- no Edit / Delete / Archive / Retire actions inside the sheet;
- no QR/export/PDF/print implementation;
- no Asset 360;
- no reporting/dashboard.

Deferred:

- authenticated employee self-view;
- public lookup remains rejected;
- backend scoped endpoint for self-view;
- auth/IAM expansion;
- QR/export/PDF;
- broader employee portal / personal cabinet.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Operational Search v1 Frontend Quick Find Closeout

Operational Search v1 first bounded implementation has been completed and accepted as frontend-only controlled quick find.

User-facing feature name:

`Быстрый поиск`

Implementation interpretation:

- implemented inside the existing Operational Workspace;
- placed near the left tree / navigation area;
- visually separated from the existing `Фильтр дерева`;
- uses already loaded authorized frontend state only;
- searches only loaded workspace data;
- does not call backend search;
- does not add backend/API/DB behavior;
- does not create a public search surface;
- does not create employee self-search.

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
- no result-row action buttons are added.

Preserved distinction:

- `Фильтр дерева` remains local visible/open tree filtering;
- `Быстрый поиск` searches already loaded authorized workspace state;
- neither feature is backend search.

Excluded from this implementation:

- backend/API Search v1;
- new endpoint;
- DB changes;
- migrations;
- server-side filtering;
- pagination/limits;
- auth/IAM expansion;
- ordinary employee role;
- employee self-search;
- public search;
- full-name-only public lookup;
- `note` search;
- audit/history search;
- AnyDesk/IP/MAC/extension search;
- QR/barcode search;
- export/PDF/Excel/print;
- Asset 360;
- reporting/dashboard;
- Elasticsearch/global search;
- saved views;
- advanced filters.

Deferred:

- Backend/API Search v1;
- server-side filtering;
- pagination/limits;
- larger-tenant scale handling;
- ordinary employee self-search;
- technical attributes search;
- QR/export/PDF;
- Asset 360/reporting.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Asset QR Lite Frontend Derived Display Closeout

Asset QR Lite first bounded implementation has been completed and accepted as frontend-only derived QR display.

User-facing feature names:

- `QR Lite`;
- `QR по инвентарному номеру`.

Implementation interpretation:

- implemented inside the existing Operational Workspace;
- displayed only inside selected Asset Technical details;
- generated from existing selected Asset frontend state;
- QR payload is exactly `asset_tag`;
- QR is derived presentation only;
- QR is not canonical Asset identity;
- QR does not replace `asset_tag`;
- `asset_tag` remains the canonical inventory/business identifier inside tenant;
- QR block is read-only;
- QR is visually secondary and support-oriented.

Displayed near QR:

- asset display title;
- `asset_tag`;
- `serial_number`;
- `asset_type`.

These nearby values are display-only and are not encoded into QR.

QR generation:

- generated locally in frontend;
- embedded local QR generator was used;
- no external dependency was added;
- no remote QR service was used;
- no package/build pipeline change was made.

Preserved placement boundaries:

- Asset Overview remains QR-free;
- asset header/card identity area remains QR-free;
- Controlled Employee Asset Information Sheet remains QR-free;
- printable label/sheet was not opened;
- scanner/mobile/public route was not opened.

Excluded from this implementation:

- backend/API QR;
- backend/API changes;
- DB changes;
- migrations;
- new endpoint;
- DTO/read-model changes;
- auth/IAM expansion;
- new role model;
- ordinary employee role;
- employee self-service;
- public QR lookup;
- public asset page;
- QR-based authentication;
- canonical `qr_code`;
- canonical `barcode`;
- canonical `inventory_number`;
- stored QR value;
- stored barcode value;
- backend QR generation;
- remote QR service;
- scanner/camera/mobile workflow;
- printer integration;
- bulk print;
- export/PDF/Excel/print;
- download QR action;
- label designer;
- Asset 360;
- reporting/dashboard;
- QR search;
- backend search;
- Elasticsearch/global search;
- QR payload from anything except `asset_tag`;
- note/history/audit payload;
- assignment/user payload;
- tenant id payload;
- URL payload.

Deferred:

- QR in Asset Overview;
- QR in Controlled Employee Asset Information Sheet;
- printable label/sheet;
- scanner/camera/mobile workflow;
- backend/API QR;
- QR search;
- public lookup;
- employee self-service.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Asset Type-Specific Technical Attributes v1 Closeout

Asset Type-Specific Technical Attributes v1 has been completed and accepted at its approved bounded scope.

Implementation is accepted as two bounded layers:

1. backend/API/DB controlled key-value support;
2. frontend integration inside selected Asset Technical details.

### Backend/API/DB implementation

Accepted backend/API/DB scope:

- added `asset_technical_attributes` as asset-service-owned tenant-scoped controlled key/value storage;
- added server-side technical attribute profiles:
  - `PC_LAPTOP`;
  - `STATIONARY_PHONE`;
  - `PRINTER`;
  - `UNSUPPORTED_GENERIC`;
- added allowlisted technical attribute keys:
  - `hostname`;
  - `anydesk_id`;
  - `ip_address`;
  - `mac_address`;
  - `phone_ip`;
  - `extension_number`;
  - `sip_line_id`;
  - `printer_ip`;
  - `printer_network_name`;
  - `printer_placement_note`;
- extended `GET /api/v1/assets/{id}` with nested `technical_attributes`;
- added separate update endpoint:
  - `PUT /api/v1/assets/{id}/technical-attributes`;
- added audit action:
  - `ASSET_TECHNICAL_ATTRIBUTES_UPDATED`;
- preserved existing asset read/mutate authorization cutline.

Preserved backend/API/DB boundaries:

- `GET /api/v1/assets` list endpoint remains lightweight;
- `POST /api/v1/assets` remains master-record create only;
- generic `PATCH /api/v1/assets/{id}` remains master-record-only;
- Assign / Transfer / Return remain dedicated flows;
- technical attributes do not move responsibility or location into generic PATCH;
- technical attributes do not become canonical Asset identity;
- technical attributes do not replace `asset_tag`;
- technical attributes do not replace `serial_number`;
- technical attributes do not affect status/lifecycle semantics.

### Frontend integration

Accepted frontend integration scope:

- added `Технические атрибуты` only inside selected Asset Technical details;
- added profile-based read display for:
  - PC/laptop fields;
  - stationary phone fields;
  - printer fields;
  - unsupported/generic safe empty state;
- added bounded in-place edit through:
  - `PUT /api/v1/assets/{id}/technical-attributes`;
- kept active Technical details context after save;
- read-only users see technical attributes without edit controls.

Displayed profile fields:

- `PC_LAPTOP`:
  - `Hostname` / `Имя компьютера`;
  - `IP-адрес`;
  - `MAC-адрес`;
  - `AnyDesk ID`;
- `STATIONARY_PHONE`:
  - `IP телефона`;
  - `Внутренний номер`;
  - `SIP / линия`;
- `PRINTER`:
  - `Сетевое имя принтера`;
  - `IP принтера`;
  - `Примечание по размещению`;
- `UNSUPPORTED_GENERIC`:
  - `Для этого типа актива технические атрибуты v1 не настроены`.

Preserved frontend boundaries:

- Asset Overview remains unchanged;
- asset header remains unchanged;
- generic master edit remains unchanged;
- Actions tab remains unchanged;
- Quick Find remains unchanged;
- QR Lite remains unchanged;
- Employee Information Sheet remains unchanged;
- no technical attribute search was added;
- no QR payload change was added;
- no technical attributes are exposed to ordinary employee/self-service/public surfaces.

### Excluded from this implementation

This closeout did not open:

- discovery agent;
- endpoint scanner;
- network scanning;
- remote control;
- patch management;
- OS/software deployment;
- Hardware Inspector parity;
- Astra Configuration Manager parity;
- uncontrolled custom fields;
- tenant-defined field admin platform;
- import/export;
- PDF/Excel/print;
- bulk edit;
- search over technical attributes;
- QR payload changes;
- Employee Information Sheet exposure;
- Asset Overview exposure;
- reporting/dashboard;
- Asset 360;
- public lookup;
- employee self-service;
- auth/IAM expansion;
- new role model.

### Deferred

Deferred to separate future decisions:

- search over technical attributes;
- technical attributes in Employee Information Sheet;
- technical attributes in Asset Overview;
- QR payload changes;
- import/export of technical attributes;
- reporting/dashboard usage of technical attributes;
- discovery/agent/scanner/platform scope;
- richer validation for IP/MAC/SIP/AnyDesk formats;
- redaction policy changes for sensitive technical values.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Search over Technical Attributes Closeout

Search over Technical Attributes has been completed and accepted at its approved bounded scope.

Implementation is accepted as two bounded layers:

1. backend/API tenant-scoped technical-attribute search support;
2. frontend UI integration through a separate `Поиск по техническим данным` block.

### Backend/API implementation

Accepted backend/API scope:

- added separate bounded endpoint:
  - `GET /api/v1/assets/technical-attribute-search`;
- added tenant-scoped technical-attribute search over approved keys only;
- added search-scope DB index:
  - `idx_asset_technical_attributes_search_scope`;
- returned asset/device results only;
- returned one result per asset;
- returned matched field metadata:
  - matched key;
  - matched label;
  - matched display value;
  - sensitivity flag;
  - masked flag;
- applied result-row masking for sensitive fields;
- reused existing asset read authorization cutline;
- emitted no audit because search is read-only.

Searchable technical keys:

- `hostname`;
- `anydesk_id`;
- `ip_address`;
- `mac_address`;
- `phone_ip`;
- `extension_number`;
- `sip_line_id`;
- `printer_ip`;
- `printer_network_name`.

Excluded from search by default:

- `printer_placement_note`;
- `note`;
- audit/history;
- QR payload;
- unsupported/generic arbitrary fields;
- tenant-defined custom keys;
- user-service data;
- reporting-service data.

### Frontend UI implementation

Accepted frontend scope:

- added separate UI block:
  - `Поиск по техническим данным`;
- placed the block near existing `Быстрый поиск`;
- kept existing `Быстрый поиск` visibly and behaviorally separate;
- kept `Фильтр дерева` unchanged;
- used only:
  - `GET /api/v1/assets/technical-attribute-search`;
- supported search by button and Enter;
- added local validation for query length;
- displayed results under:
  - `Активы / устройства`;
- displayed matched field label and backend-provided display value;
- respected backend masking;
- did not unmask sensitive values in frontend;
- used read-only navigation result rows;
- result click opens selected Asset detail and Technical details tab when safe.

### Preserved boundaries

This closeout preserves:

- current `Быстрый поиск` as frontend-only loaded-state search;
- `Фильтр дерева` as local/open-tree filtering;
- `GET /api/v1/assets` list endpoint unchanged;
- asset detail endpoint unchanged except already accepted technical attributes;
- create Asset unchanged;
- generic PATCH unchanged;
- technical attributes edit endpoint unchanged;
- Assign / Transfer / Return unchanged;
- QR Lite unchanged;
- Employee Information Sheet unchanged;
- Asset Overview unchanged;
- no result-row mutation actions.

### Not opened

This closeout did not open:

- global search;
- Elasticsearch;
- external search engine;
- advanced filters;
- saved views;
- persistent search preferences;
- reporting/dashboard;
- Asset 360;
- public lookup;
- employee self-service;
- auth/IAM expansion;
- new role model;
- QR payload changes;
- QR search;
- Employee Information Sheet exposure;
- Asset Overview exposure;
- import/export;
- PDF/Excel/print;
- discovery agent;
- scanner/camera workflow;
- network scanning;
- remote control;
- Hardware Inspector parity;
- Astra Configuration Manager parity;
- search over `printer_placement_note`;
- search over `note`;
- audit/history search.

### Deferred

Deferred to separate future decisions:

- merging technical search into `Быстрый поиск`;
- search over `printer_placement_note`;
- search over `note`;
- global search / Elasticsearch;
- advanced filters / saved views;
- QR search or QR payload changes;
- Employee Information Sheet technical attribute exposure;
- Asset Overview technical attribute exposure;
- import/export of technical search results;
- reporting/dashboard usage;
- discovery/agent/scanner/platform scope.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Employee Sheet Technical Attribute Exposure Closeout

Employee Sheet Technical Attribute Exposure has been completed and accepted at its approved bounded scope.

This closeout supersedes the earlier deferred-note status for Employee Information Sheet technical attribute exposure. The accepted implementation is intentionally narrower than full technical attribute exposure.

Implementation is accepted as two bounded layers:

1. backend/API scoped Employee Sheet technical hints endpoint;
2. frontend integration inside existing `Информационный лист сотрудника`.

### Backend/API implementation

Accepted backend/API scope:

- added separate read-only endpoint:
  - `GET /api/v1/assets/employee-sheet-technical-hints?user_id=<userId>`;
- endpoint is tenant-scoped;
- endpoint uses selected employee/user id only;
- endpoint returns only assets assigned to the selected `user_id` in the current tenant;
- endpoint uses existing asset read authorization;
- no user-service DB join is performed;
- no user-service existence check is performed in this first accepted cut;
- unknown or no-match `user_id` returns an empty `assets` array;
- no audit is emitted because the endpoint is read-only.

Each returned asset row includes minimal asset identity/context:

- `asset_id`;
- `asset_tag`;
- `display_name`;
- `asset_type`;
- `serial_number`;
- `status`;
- `location_id`;
- `technical_hints`.

Allowed technical hints are limited to:

- `hostname`;
- `extension_number`;
- `printer_network_name`.

The endpoint does not return:

- full `technical_attributes`;
- `profile`;
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
- action links;
- arbitrary unsupported/custom technical fields;
- masked sensitive technical values.

### Frontend integration

Accepted frontend scope:

- existing `Информационный лист сотрудника` renders low-sensitivity technical hints for assigned devices;
- frontend calls:
  - `GET /api/v1/assets/employee-sheet-technical-hints?user_id=<selectedUserId>`;
- hints are loaded when selected employee/user detail opens or changes;
- stale hints are cleared when selected user changes;
- hints are merged by `asset_id`;
- hints are displayed as secondary read-only information under existing assigned-device rows/cards;
- only present hint values are shown;
- missing hints do not create noisy empty blocks;
- endpoint failure does not block Employee Sheet rendering;
- frontend uses a hard allowlist for rendered keys.

User-facing labels:

- `hostname` -> `Hostname`;
- `extension_number` -> `Внутренний номер`;
- `printer_network_name` -> `Сетевое имя принтера`.

### Preserved boundaries

This closeout preserves:

- current Employee Sheet as selected employee/user detail only;
- read-only Employee Sheet behavior;
- managed-workspace-only access;
- no employee self-service;
- no public lookup;
- no full-name lookup;
- no export/PDF/Excel/print;
- no Asset 360;
- no action links from Employee Sheet hints;
- no technical attribute editing from Employee Sheet;
- `GET /api/v1/assets` list endpoint unchanged;
- `GET /api/v1/assets/{id}` detail endpoint unchanged;
- `GET /api/v1/assets/technical-attribute-search` unchanged;
- `PUT /api/v1/assets/{id}/technical-attributes` unchanged;
- Quick Find unchanged;
- Technical Search unchanged;
- Asset Overview unchanged;
- QR Lite unchanged;
- generic asset edit unchanged;
- Assign / Transfer / Return unchanged.

### Not opened

This closeout did not open:

- full technical attributes in Employee Sheet;
- sensitive technical attributes in Employee Sheet;
- masked sensitive values in Employee Sheet;
- Employee self-service;
- public lookup;
- full-name lookup;
- export/PDF/Excel/print;
- QR in Employee Sheet;
- Asset Overview technical attribute exposure;
- Asset 360;
- reporting/dashboard;
- global search;
- Elasticsearch;
- advanced filters;
- saved views;
- import/export;
- discovery agent;
- scanner/camera workflow;
- network scanning;
- remote control;
- Hardware Inspector parity;
- Astra Configuration Manager parity;
- auth/IAM expansion;
- new role model.

### Deferred

Deferred to separate future decisions:

- broader Employee Sheet / Asset 360 expansion;
- employee self-service;
- public lookup remains rejected unless separately decided;
- export/PDF/print of Employee Sheet;
- QR in Employee Sheet;
- Asset Overview technical hints or full technical attributes;
- reporting/dashboard usage;
- discovery/agent/scanner/platform scope.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Standalone Employee Information Sheet Interface Closeout

Standalone Employee Information Sheet Interface has been implemented and accepted as a bounded frontend-only UX correction over the already accepted Employee Sheet capability.

Owner correction:

- `Информационный лист сотрудника` should not feel like a sub-block hidden inside general selected-user detail;
- it should be available as a separate focused interface;
- the interface should contain only employee search and the selected employee information sheet.

Accepted implementation:

- added a separate top-level left navigation item:
  - `Информационный лист`;
- opening this item shows a focused right-pane interface;
- the interface provides employee-only search;
- search uses already loaded authorized frontend state only;
- search is limited to loaded users / employee names;
- selected employee rendering stays inside the standalone sheet path;
- assigned devices are shown from already loaded frontend asset state;
- low-sensitivity technical hints are loaded through the existing accepted endpoint:
  - `GET /api/v1/assets/employee-sheet-technical-hints?user_id=<userId>`;
- stale hints are cleared when selected employee changes;
- stale hint responses are ignored;
- existing embedded Employee Sheet inside selected user detail remains intact.

The standalone interface shows only:

- employee search;
- selected employee identity/context;
- assigned devices;
- allowed low-sensitivity technical hints.

Allowed technical hints remain limited to:

- `hostname` -> `Hostname`;
- `extension_number` -> `Внутренний номер`;
- `printer_network_name` -> `Сетевое имя принтера`.

The standalone interface does not show:

- asset registry;
- Setup/Admin;
- role binding editor;
- general user-management editor;
- action tabs;
- Assign / Transfer / Return;
- edit buttons;
- technical attribute editing;
- full `technical_attributes`;
- `profile`;
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
- action links;
- arbitrary unsupported/custom technical fields.

Implementation scope:

- frontend-only;
- existing static Operational Workspace UI only;
- smoke test updated for static UI coverage;
- no backend/API changes;
- no DB/migration changes;
- no user-service API changes;
- no BFF/UI backend;
- no auth/IAM expansion;
- no new endpoint;
- no Documentation change during implementation.

Preserved surfaces:

- existing selected-user detail remains usable;
- existing embedded Employee Sheet remains intact;
- Quick Find remains unchanged;
- Technical Search remains unchanged;
- Asset Overview remains unchanged;
- Asset Technical details remain unchanged;
- QR Lite remains unchanged;
- Assign / Transfer / Return remain unchanged;
- Setup/Admin remains unchanged.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

## Current Baseline Update — Quick Find / Technical Search Merge Semantics Closeout

Quick Find / Technical Search Merge Semantics has been implemented and accepted as a bounded frontend-only UX semantics clarification.

The implementation did not merge search logic and did not introduce a new search backend.

Accepted implementation:

- added a shared visual search area in the left workspace/navigation region:
  - `Поиск`;
- kept two separate sections inside this area:
  - `Сотрудники и активы`;
  - `Технические данные`;
- clarified that `Быстрый поиск` searches already loaded workspace data only;
- clarified that `Поиск по техническим данным` uses the existing technical-attribute search endpoint and preserves masking/sensitivity behavior;
- kept Quick Find and Technical Search state, handlers, result rendering and data sources separate;
- kept standalone `Информационный лист` employee-only search unchanged;
- kept `Фильтр дерева` as local/open-tree filtering only.

Preserved Quick Find semantics:

- frontend-only;
- loaded-state-only;
- no backend call;
- searches loaded users and loaded assets;
- searches:
  - employee/user name;
  - `asset_tag`;
  - `display_name`;
  - `serial_number`;
  - `asset_type`;
- result groups remain:
  - `Сотрудники`;
  - `Активы / устройства`;
- result rows remain read-only navigation rows.

Preserved Technical Search semantics:

- existing endpoint unchanged:
  - `GET /api/v1/assets/technical-attribute-search`;
- technical attributes only;
- result group remains:
  - `Активы / устройства`;
- backend masking/sensitivity behavior is preserved;
- frontend does not unmask sensitive values;
- result rows remain read-only navigation rows;
- result click still opens selected Asset detail / Technical details when safe.

Preserved standalone `Информационный лист` semantics:

- separate top-level interface remains unchanged;
- employee-only search remains loaded-state-only;
- asset results are not shown in Employee Sheet search;
- selected employee stays inside the standalone sheet path;
- assigned devices and low-sensitivity hints remain unchanged.

This implementation did not open:

- global search;
- Elasticsearch;
- advanced filters;
- saved views;
- persistent preferences;
- backend unified search;
- new backend endpoint;
- cross-service search orchestration;
- search over `note`;
- search over `printer_placement_note`;
- QR search;
- audit/history search;
- public lookup;
- employee self-service;
- Asset 360;
- reporting/dashboard;
- auth/IAM expansion;
- new role model.

Implementation scope:

- frontend-only;
- existing static Operational Workspace UI only;
- smoke test updated for static UI coverage;
- no backend/API changes;
- no DB/migration changes;
- no Documentation change during implementation;
- no build/package changes.

This remains internally validated implementation evidence only.

It does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.