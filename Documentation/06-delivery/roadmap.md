# Roadmap

Status: approved
Owner: product
Last Updated: 2026-04-30

## Purpose

Этот roadmap является project-level direction gate.

Он фиксирует:

- какой baseline уже считается закрытым;
- в каком режиме проект находится сейчас;
- какие transition triggers разрешены;
- какие default moves запрещены;
- какие future paths существуют только как candidate paths без открытия scope.

Этот документ не является:

- backlog;
- release-plan timeline;
- implementation plan;
- Codex packet;
- engineering checklist;
- основанием для открытия нового scope по инерции.

## Roadmap vs Backlog vs Release Plan

### Roadmap

Roadmap отвечает на вопросы:

- где проект находится сейчас;
- какой baseline уже считается закрытым;
- какие gates должны быть выполнены перед следующим движением;
- что нельзя открывать по умолчанию.

### Backlog

Backlog содержит возможные epics и deferred product work.

Наличие backlog item не означает,
что он автоматически становится следующим workstream.

### Release Plan

Release plan фиксирует delivery interpretation,
sequencing и exit gates для уже approved scopes.

Наличие секции в release-plan
не переоткрывает уже закрытый slice автоматически.

## Current Project Mode

Текущий project mode:

**Project Stabilization / First-Adopter-Gated Hold**

Это означает:

- current baseline остается internally validated;
- real tenant validation еще не достигнута;
- real day-to-day operational validation еще не достигнута;
- release-ready tenant-facing claim не поддержан;
- First-Tenant Operational Validation является правильным следующим validation stage conceptually;
- First-Tenant Operational Validation не executable,
  пока нет real tenant / first adopter / real usage loop.

## Completed Baseline

### MVP / Pilot Baseline

Asset Accountability MVP baseline уже определен
и не должен переоткрываться по умолчанию.

Completed / accepted baseline включает:

- tenant-aware foundation;
- users / roles / departments / branches / locations;
- asset registry;
- asset card;
- dedicated assign / transfer / return flows;
- audit and history for critical actions;
- minimal admin / support layer для одного tenant.

### Minimal Pilot UI / Access

Minimal Pilot UI Contour закрыт на implementation + closeout уровне.

Minimal Pilot Access Contour закрыт
для текущей managed pilot interpretation.

Для текущего MVP controlled managed operational access outside repo допустим,
если он обеспечивает:

- authenticated context для real first tenant;
- controlled materialization первого Tenant Admin;
- tenant-facing usage уже approved MVP surfaces.

Это не открывает:

- новый auth platform;
- SSO / federation / MFA / SCIM / IdP scope;
- self-service onboarding scope;
- новый backend service family.

### Narrow Wave 2 Baseline

Current narrow Wave 2 map считается effectively complete.

Minimally closed contours:

- Inventory / Warehouse;
- Inventory Availability / Readiness Refinement;
- Maintenance;
- Software;
- Ticket;
- Reporting.

Completed Wave 2 baseline должен трактоваться узко:
как operational current-state coverage,
а не как full enterprise expansion.

### Post-Wave-2 Hardening / Validation

Первый minimal post-Wave-2 hardening / validation packet закрыт.

Confirmed baseline:

- reporting остается read-only;
- reporting остается derived visibility;
- reporting не является source of truth;
- owner contexts остаются authoritative для своих fact slices;
- bounded lag / eventual consistency остается accepted там, где уже approved;
- новые API / model / orchestration family не вводились.

## Current Hold

Проект сейчас находится в:

**Project Stabilization / First-Adopter-Gated Hold**

Этот hold является validation ceiling,
а не новым implementation workstream.

Разрешено внутри hold:

- documentation alignment вокруг текущего validation ceiling;
- first-adopter intake preparation;
- first-tenant evidence-capture preparation;
- entry / exit gate clarification;
- concrete gap register preparation;
- preservation of already closed contour boundaries.

Не разрешено по умолчанию:

- открывать новый bounded context;
- открывать новую Wave map;
- открывать еще один Wave 2 contour;
- открывать еще один generic validation packet;
- открывать generic engineering hardening без concrete mismatch;
- трактовать synthetic tests как real tenant validation;
- делать release-ready tenant-facing claim.

## Allowed Exit Triggers

Hold может завершиться только через explicit trigger.

### Trigger A — Real Tenant / First Adopter Exists

Если появляется real tenant или first adopter,
проект может стартовать:

**First-Tenant Operational Validation**

Entry gate:

- один real tenant или first adopter;
- real tenant-scoped actors;
- real role paths;
- real reference-data setup path;
- real operational actions inside already approved surfaces;
- usage observed beyond a single scripted internal walkthrough.

Этот stage является validation only.

Он не является:

- новым bounded context;
- новым Wave 2 contour;
- release-ready claim.

### Trigger B — Concrete In-Scope Defect or Mismatch

Если найден concrete in-scope defect или mismatch,
проект может открыть targeted gap closure.

Issue должен быть:

- concrete;
- observed;
- inside already approved scope;
- solvable without a new owner;
- solvable without a new standalone model family;
- solvable without a new primary API family;
- solvable without orchestration layer;
- solvable without BI / dashboard / analytics expansion.

### Trigger C — Separate Product Expansion Decision

Новый product expansion может стартовать
только после separate explicit approval.

Это нельзя выводить из:

- наличия backlog item;
- восстановления roadmap;
- старых planning packets;
- synthetic test results;
- developer convenience;
- интуиции “следующий логичный модуль”.

### Trigger D — Documentation Direction Gate

Roadmap restoration позволяет перейти
к direction-gate discussion.

Само по себе это не authorizes новый implementation workstream.

## Forbidden Default Moves

Следующие moves запрещены по умолчанию:

- reopening MVP / Pilot baseline;
- reopening Minimal Pilot UI Contour;
- reopening Minimal Pilot Access Contour;
- reopening first minimal post-Wave-2 hardening / validation packet;
- opening a new bounded context;
- opening a new Wave map;
- opening another Wave 2 contour;
- opening reporting / BI / dashboard / analytics expansion by inertia;
- opening orchestration layer;
- opening new API / model family just to continue activity;
- opening generic engineering / platform hardening without concrete mismatch;
- using synthetic tests as a substitute for real tenant validation;
- making a release-ready tenant-facing claim.

## Candidate Future Paths

Эти paths являются candidate-only.

Они не открывают scope сами по себе.

### Candidate Path 1 — First-Tenant Operational Validation

Это strongest next conceptual path.

Он может стартовать только когда выполнен real tenant / first adopter entry gate.

### Candidate Path 2 — Targeted Gap Closure

Этот path допустим только если concrete observed evidence показывает
in-scope defect или mismatch.

### Candidate Path 3 — Separate Product Expansion

Этот path требует separate explicit product approval.

Future expansion areas не должны открываться из roadmap по умолчанию.

### Candidate Path 4 — Documentation Stabilization

Этот path допустим внутри current hold.

Он может включать:

- roadmap restoration;
- release-plan alignment;
- system-summary alignment;
- first-adopter intake checklist;
- evidence-capture checklist;
- placeholder source-of-truth warnings.

Он не должен включать implementation work.

## Source-of-Truth Caution

Следующие документы нельзя использовать как current source-of-truth,
если они являются empty placeholders:

- `/Documentation/07-engineering/*`;
- `/Documentation/04-api/api-standards.md`;
- `/Documentation/03-data/database-strategy.md`;
- `/Documentation/01-architecture/integration-patterns.md`;
- `/Documentation/01-architecture/observability.md`;
- любые другие empty placeholder docs.

Empty placeholder не authorizes engineering direction,
implementation scope или architectural claims.

## Current Direction Gate Outcome

После roadmap restoration проект может перейти
к direction-gate discussion.

Однако текущий ответ на вопрос
“можно ли выбрать следующий implementation workstream?”:

**No, not by default.**

Следующий implementation workstream можно выбрать только если выполнен один из allowed exit triggers:

1. появился real tenant / first adopter;
2. найден concrete in-scope defect или mismatch;
3. отдельно approved separate product expansion decision.

До этого корректное состояние остается:

**Project Stabilization / First-Adopter-Gated Hold**

## Managed Pilot UI Stabilization Inside Current Hold

Внутри текущего режима **Project Stabilization / First-Adopter-Gated Hold**
managed Pilot UI stabilization разрешена только как bounded readiness activity
для уже существующего Minimal Pilot UI surface.

Эта activity может включать:
- подготовку managed demo checklist;
- подготовку evidence-capture template;
- подготовку role-path walkthrough;
- регистрацию concrete UI defect / mismatch;
- targeted gap closure внутри existing UI surface,
  если concrete defect реально observed.

Эта activity не:
- выводит проект из hold;
- стартует First-Tenant Operational Validation;
- открывает новый contour;
- открывает production UI / auth / onboarding scope;
- создает release-ready tenant-facing claim.

## Product Expansion Decision Gate — Tenant-Facing Operational UX

На уровне direction gate approved separate product expansion direction:

**Productized Tenant-Facing Operational UX Expansion**

Это docs-first product expansion direction,
а не implementation packet и не release-ready claim.

### Implementation closeout

The dedicated docs-first scope packet for this direction was approved as Productized Tenant-Facing Operational UX v1.

This specific approved expansion has now been implemented and accepted as frontend-only UX refinement of the existing static Pilot UI over already approved MVP and narrow Wave 2 surfaces.

This changes the state of this direction from approved direction awaiting scope / implementation to approved scope implemented and closed.

This implementation did not open:
- new bounded context;
- new Wave map;
- new Wave 2 contour;
- managed demo workstream;
- production release claim;
- auth / IAM platform;
- self-service onboarding;
- BFF or UI backend;
- BI / dashboard / analytics expansion;
- WMS / ITSM / SAM expansion;
- new backend endpoints solely for UI convenience.

No production backend/API changes were made; test-only smoke coverage was updated.

The project remains under the same real-tenant validation ceiling.

### Rationale

Это направление является strongest justified product move, потому что:
- current MVP и narrow Wave 2 baseline уже существуют;
- Pilot UI уже существует как managed pilot / internal pilot surface;
- managed demo сейчас не является следующим required step;
- First-Tenant Operational Validation не executable без real tenant / first adopter / real usage loop;
- concrete in-scope defect или mismatch сейчас не заявлен;
- richer domain expansions сейчас создают риск drift в WMS / ITSM / SAM / BI / IAM;
- tenant-facing operational UX остается ближайшим product investment
  к approved value proposition.

### Current post-closeout interpretation

This direction is no longer awaiting initial scope approval or implementation planning.

`Productized Tenant-Facing Operational UX v1` has been implemented and closed as frontend-only UX refinement.

Further work in this area is allowed only as:
- concrete defect fix;
- targeted UX gap closure inside accepted scope;
- documentation alignment.

Any new product expansion requires a separate explicit decision.


### Operational Workspace UX v2 — Closeout

`Operational Workspace UX v2 / Release UI Layout & Navigation Cutline`
has been implemented and accepted.

Canonical scope doc:

`/Documentation/06-delivery/operational-workspace-ux-v2-release-layout-navigation-cutline.md`

This work is closed as:

**frontend-only layout, navigation and readability refinement over existing accepted operational UI surfaces.**

### Implementation closeout interpretation

Operational Workspace UX v2 refined the existing static UI in place.

It improved:

- workspace structure;
- navigation hierarchy;
- section separation;
- registry / card / action / review / setup continuity;
- history / audit readability;
- RU-facing copy consistency;
- technical wording cleanup.

This implementation did not open:

- new bounded context;
- new Wave map;
- new Wave 2 contour or surface;
- backend/API changes;
- new endpoints;
- DTO / read-model expansion;
- BFF / UI backend;
- auth/IAM expansion;
- dashboard / BI / analytics;
- Asset 360;
- global search / Elasticsearch;
- saved views / persistent preferences;
- bulk actions.

No production backend/API changes were made.

Test-only smoke coverage was updated.

### Current post-closeout interpretation

This direction is no longer awaiting implementation planning or Codex authorization.

Operational Workspace UX v2 is now closed as accepted frontend-only UI refinement.

Further work in this area is allowed only as:

- concrete defect fix;
- targeted UX gap closure inside accepted scope;
- documentation alignment.

Any new product expansion requires a separate explicit product decision.

### Validation ceiling

This closeout does not change the current project mode:

**Project Stabilization / First-Adopter-Gated Hold**

This closeout does not prove:

- release-ready tenant-facing product state;
- real tenant validation;
- real day-to-day operational validation;
- First-Tenant Operational Validation exit.

Synthetic tests and internal/video review evidence remain internal implementation acceptance evidence only.
They do not substitute for real tenant validation.

## Operational Workspace UX Stabilization v3 — Targeted Gap Closure Closeout

Operational Workspace UX Stabilization v3 has been implemented and accepted as a targeted frontend-only gap closure inside the existing Operational Workspace UI surface.

This does not open a new roadmap direction.

It is classified as:

- concrete observed in-scope UX gap closure;
- frontend-only refine-in-place stabilization;
- preservation of existing Operational Workspace boundaries.

It does not change the current project mode:

**Project Stabilization / First-Adopter-Gated Hold**

It does not prove release readiness, FTOV start, FTOV exit, production readiness or real tenant validation.

Further work after this closeout should be either:

- controlled synthetic re-review using prepared seed/setup data;
- docs alignment;
- real FTOV only when real tenant / first adopter entry conditions exist;
- separate product expansion only by explicit decision.

### Post-v3 re-review outcome

Post-v3 controlled synthetic UI re-review completed with verdict:

**Passed with minor friction.**

This confirms that the targeted UX gap closure addressed the main observed issues around:

- sticky / anchored navigation;
- long-scroll orientation;
- loading states and double-submit prevention;
- UUID / technical value readability;
- asset card hierarchy;
- dedicated assign / transfer / return clarity;
- generic edit guardrail preservation;
- history / audit readability.

Remaining minor friction may be handled only as future targeted frontend UX hardening if explicitly chosen.

It does not create a default next implementation workstream.

It does not open:

- product expansion;
- Hardware Inspector parity;
- backend/API changes;
- BFF / UI backend;
- auth/IAM expansion;
- release-ready claim;
- FTOV start or exit claim.