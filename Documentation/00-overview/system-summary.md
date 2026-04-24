# System Summary

Status: approved
Owner: project
Last Updated: 2026-03-27

## Purpose
IT Asset Management SaaS для учета активов, оргструктуры, ответственности за технику и истории критических изменений.

## Current Project Stage
Проект находится на этапе Foundation Slice и MVP.

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
- asset delete semantics
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