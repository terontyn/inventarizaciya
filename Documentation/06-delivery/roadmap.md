# Roadmap

Status: approved
Owner: product
Last Updated: 2026-04-24

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

### Scope interpretation

Это направление может определить richer tenant-facing operational UX
поверх уже approved MVP и narrow Wave 2 surfaces.

Оно может включать:
- role-aligned operational navigation;
- usability refinement для existing tenant-facing workflows;
- continuity между registry, asset card, responsibility flows, history / audit
  и relevant current-state visibility;
- UX-level friction reduction для будущего real tenant usage;
- preparation для будущего First-Tenant Operational Validation evidence capture.

Оно не разрешает по умолчанию:
- implementation work;
- новый bounded context;
- новую Wave map;
- managed demo workstream;
- production release claim;
- новый auth / IAM platform;
- self-service onboarding;
- SSO / federation / MFA / SCIM / IdP scope;
- BI / dashboard / analytics expansion;
- WMS / ITSM / SAM expansion;
- новые backend endpoints solely for UI convenience.

### Next required step

До implementation должен быть подготовлен и approved отдельный docs-first scope packet:

**Productized Tenant-Facing Operational UX Expansion Scope**

Этот packet должен определить:
- included personas;
- included existing surfaces;
- included user journeys;
- explicit non-goals;
- UX cutline;
- backend/API change constraints;
- acceptance criteria;
- stop conditions;
- docs deltas required before Codex implementation.