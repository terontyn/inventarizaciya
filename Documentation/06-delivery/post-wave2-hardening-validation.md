# Post-Wave-2 Hardening / Validation

Status: draft
Owner: product

## Purpose

Этот документ фиксирует
post-Wave-2 stage
после effective closeout
current narrow Wave 2 map.

Этот stage трактуется
как limited cross-cutting layer
для hardening and validation
уже approved narrow Wave 2 baseline,
а не как новый contour opening.

## Interpretation Rule

`cross-contour hardening / validation`:
- не является новым bounded context;
- не является новым large contour;
- не открывает автоматически новый Wave map;
- не открывает автоматически новый reporting expansion;
- остается cross-cutting limited stage
  поверх already approved contours.

## Stage Goals

Этот stage нужен, чтобы:
- подтвердить preservation of ownership boundaries;
- подтвердить cross-context consistency;
- подтвердить alignment между owner-side current-state visibility
  и reporting-side derived visibility;
- подтвердить acceptable lag / eventual-consistency behavior;
- подтвердить operational usability
  combined narrow Wave 2 stack
  как единого product baseline;
- разрешать только concrete gap closure
  внутри already approved scope,
  если validation показывает mismatch.

## What This Stage Means

Этот stage означает:
- hardening уже реализованных contours;
- validation combined operational behavior;
- gap closure only where validation reveals
  concrete mismatch
  inside already approved scope.

Этот stage не означает:
- opening new bounded context;
- opening new large contour;
- opening new BI/reporting capability family;
- automatic second reporting slice;
- automatic new API family;
- ownership transfer between existing contexts.

## Validation Focus

На этом stage
должны быть явно подтверждены:

- cross-context consistency;
- preservation of owner boundaries;
- current-state visibility alignment;
- lag / eventual-consistency acceptance;
- operational usability
  combined narrow stack.

## Minimal Workflow Interpretation

Post-Wave-2 `cross-contour hardening / validation`
должен трактоваться
как recurring limited validation/hardening loop
поверх already approved contours,
а не как отдельный workflow engine
или новая stage-wide model family.

### Loop meaning
Минимальный loop состоит из следующего:
1. взять owner-context dedicated current-state surfaces
   как primary comparison basis;
2. проверить reporting-side derived visibility
   against those owner-side surfaces;
3. оценить:
   - preservation of ownership boundaries;
   - semantic alignment;
   - acceptability of bounded lag;
   - operational meaning;
4. зафиксировать outcome:
   - confirmed alignment;
   - accepted lag;
   - concrete mismatch inside approved scope;
   - escalation to new expansion.

### Hardening interpretation
Hardening в этом stage
означает только targeted gap closure
внутри already approved scope,
если validation показывает concrete mismatch.

Этот loop не означает:
- новый orchestration workflow;
- новый approval chain;
- новую standalone model family;
- новый primary API family.

### Important interpretation
Цель stage
не в том,
чтобы устранить любую eventual consistency.

Цель stage
в том,
чтобы подтвердить,
что допустимая eventual consistency:
- понятна;
- ограничена;
- совместима с product semantics;
- не ломает operational meaning;
- не делает reporting source of truth.

## Cutline

Пока работа остается
внутри validation and hardening
already approved semantics,
она принадлежит этому stage.

Если работа начинает требовать:
- нового owner;
- новой standalone model family;
- нового primary API family;
- нового write-owning context;
- нового orchestration layer;
- нового BI/reporting capability family,

это уже не post-Wave-2 hardening / validation,
а новый product expansion
и требует отдельного product decision.

## Relation to Existing Contours

Этот stage:
- не меняет ownership `asset-service`;
- не меняет ownership `inventory-service`;
- не меняет ownership `maintenance-service`;
- не меняет ownership `software-service`;
- не меняет ownership `ticket-service`;
- не меняет role `reporting-service`
  как read-only derived visibility layer;
- не меняет ADR-0009.

## Gap-Closure vs New Expansion

### Gap-closure
Работа остается gap-closure,
если она:
- уточняет already approved semantics;
- устраняет mismatch
  внутри already approved boundaries;
- hardens validation path
  без появления нового owner
  или нового API family.

### New expansion
Работа становится new expansion,
если она:
- открывает новый owner;
- открывает новый large contour;
- открывает новую model family;
- открывает новую primary API family;
- открывает reporting-side write semantics;
- открывает BI / dashboard / analytics platform direction.

## Explicit Non-Goals

Этот stage не должен автоматически открывать:
- historical reporting;
- KPI / dashboard catalog;
- trend semantics;
- broader aggregation
  как отдельную reporting program;
- reporting-side write APIs;
- BI-first platform;
- free-form analytics / report builder;
- umbrella orchestration semantics;
- новый large contour beyond current Wave 2 map.

## Summary

После effective closeout
current narrow Wave 2 map
следующий justified move
следует трактовать
не как новый contour,
а как limited cross-cutting stage:

**cross-contour hardening / validation**

Это:
- не новый bounded context;
- не новый large contour;
- не новый BI/reporting expansion;
- а product-level stage
  для validation and hardening
  уже approved narrow Wave 2 baseline.

  ## Architecture-Level Interpretation

`cross-contour hardening / validation`
остается cross-cutting validation/hardening stage
поверх already approved owner contexts
и reporting-side derived visibility layer.

### Not a bounded context
Этот stage не является bounded context,
потому что он:
- не вводит нового domain owner;
- не вводит новой canonical model family;
- не вводит нового write-owning context;
- не вводит нового primary API family.

### Not a large contour
Этот stage не является large contour,
потому что он:
- не открывает новый product family;
- не открывает новый orchestration layer;
- не открывает новый BI / dashboard / analytics family;
- не превращает reporting в source-of-truth or write-owning layer.

## Validation Surfaces

### Owner-side dedicated surfaces
Validation должна опираться
на already approved owner-side current-state surfaces
как на primary comparison basis.

### Reporting-side derived surfaces
Reporting-side current visibility
должна проверяться
как derived layer,
а не как authoritative source.

### Alignment rule
Цель alignment check
не в strict synchronous equality
каждого response slice
во всех contexts.

Цель —
подтвердить,
что reporting-side derived visibility:
- остается semantically aligned
  with owner-side current-state facts;
- допускает only bounded lag;
- не ломает operational meaning;
- не создает reporting-side source-of-truth semantics.

## Eventual-Consistency Interpretation

Допустимая eventual consistency
на этом stage
не должна трактоваться как defect by default.

Она допустима,
если:
- понятна;
- bounded;
- соответствует already approved semantics;
- не ломает owner-boundary interpretation;
- не делает reporting authoritative source.

## Outcome Interpretation

### Confirmed alignment
Validation outcome трактуется
как confirmed alignment,
если owner-side current-state semantics
и reporting-side derived visibility
остаются semantically compatible
без нарушения boundary interpretation.

### Accepted lag
Validation outcome трактуется
как accepted lag,
если observed lag:
- bounded;
- understandable;
- compatible with already approved semantics;
- не ломает operational meaning;
- не делает reporting authoritative source.

### Concrete mismatch
Validation outcome трактуется
как concrete mismatch,
если observed behavior:
- не укладывается
  в already approved bounded lag semantics;
- ломает semantic alignment;
- размывает owner-boundary interpretation;
- или требует concrete gap closure
  внутри already approved scope.

### Escalation to new expansion
Validation outcome трактуется
как escalation,
если resolution начинает требовать:
- нового owner;
- новой standalone model family;
- нового primary API family;
- нового write-owning context;
- нового orchestration layer;
- нового BI / reporting capability family.

В этом случае работа уже не принадлежит
post-Wave-2 hardening / validation stage
и требует отдельного product decision.

### ADR-0009 note
Already accepted eventual consistency
around inventory-to-assign handoff
остается unchanged
и не требует нового orchestration scope.

## Hardening vs New Expansion

### Allowed hardening
Работа остается внутри данного stage,
если она:
- hardens already approved semantics;
- проверяет owner-boundary preservation;
- проверяет owner/reporting alignment;
- подтверждает acceptable lag behavior;
- закрывает concrete mismatch
  inside already approved scope.

### New expansion
Работа уже не относится к этому stage,
если она требует:
- нового owner;
- новой standalone model family;
- нового primary API family;
- нового write-owning context;
- нового orchestration layer;
- нового BI/reporting capability family.

## Data / API Interpretation for Validation Stage

На этом stage
existing reporting-side derived visibility
должна трактоваться
как sufficient validation target
for alignment checks
against owner-context dedicated surfaces.

### No new model-family rule
Этот stage
не требует:
- новой canonical read-model family;
- нового validation-specific model family;
- нового primary API family.

### Validation basis rule
Для data / API validation basis
достаточно already approved surfaces:

- owner-context dedicated current-state surfaces
- `AssetOperationalCurrentView`
- `GET /api/v1/reporting/assets`
- `GET /api/v1/reporting/assets/{asset_id}`

### Authoritative-source rule
При расхождении
между owner-context dedicated surface
и reporting-side derived visibility
authoritative source
остается owner context
для соответствующего fact slice.

### Consistency interpretation
Validation
не требует strict synchronous equality
across all current-state slices.

Достаточно подтвердить,
что:
- reporting-side derived visibility
  остается semantically aligned
  with owner-side current-state facts;
- bounded lag допустим,
  если он already compatible
  with approved semantics;
- reporting
  не становится source-of-truth layer.

### Gap-closure rule
Data / API change
на этом stage допустим
только как concrete gap closure
внутри already approved scope
без появления:
- нового owner;
- новой standalone model family;
- нового primary API family;
- reporting-side write semantics;
- orchestration semantics.


## Minimal Execution Packet Closeout Clarification

Первый minimal execution packet
для post-Wave-2 `cross-contour hardening / validation`
считается закрытым
на уровне implementation + verification.

### Current status
- narrow Wave 2 stack подтвержден
  как already implemented and wired;
- `cross-contour hardening / validation`
  подтвержден
  как hardening/validation stage,
  а не как новый contour opening;
- отдельный stage-level validation suite добавлен
  для:
  - confirmed alignment;
  - accepted lag around ADR-0009;
  - safe partial composition;
- reporting остался:
  - read-only;
  - derived-only;
  - non-authoritative;
- owner contexts остались authoritative
  для соответствующих fact slices;
- новых API/model/orchestration families
  не добавлялось;
- escalation items
  по итогам этого minimal packet
  не потребовались.

### Practical interpretation
Следовательно:
- этот minimal execution packet
  не должен снова открываться
  как новый implementation step;
- дальнейшая работа внутри post-Wave-2 stage
  допустима только как:
  - следующий concrete hardening packet,
  - конкретный gap closure,
  - или отдельный escalation case,
  - но не как повторная реализация
    уже закрытого validation packet.

## Transition to First-Tenant Operational Validation

После closeout
first minimal post-Wave-2 hardening / validation packet
следующий default move
не должен открываться
по инерции как еще один validation packet.

### Rule
Новый packet
внутри current stage
допустим только если
real tenant evidence
показал:
- concrete in-scope mismatch;
- или concrete blocking gap
  внутри already approved scope.

### Non-expansion cutline
Если resolution требует:
- нового owner;
- новой standalone model family;
- нового primary API family;
- orchestration layer;
- BI / dashboard / analytics family;

это уже не continuation of validation,
а separate product expansion decision.

### Important note
Real tenant evidence
не равно release-ready evidence.
Confirmed one-tenant validation
по-прежнему не означает
release-ready tenant-facing claim.

## Post-Minimal-Packet Transition Hold

После closeout первого minimal execution packet
post-Wave-2 hardening / validation
не должен продолжаться как автоматический repeated hardening.

### Rule

Новый packet внутри этого stage допустим только если есть:
- real tenant evidence, показывающий concrete in-scope mismatch;
- concrete blocking gap внутри already approved scope;
- или specific hardening target, justified by observed behavior.

### Not allowed

Отсутствие real tenant validation нельзя компенсировать через:
- synthetic tests, presented as operational validation;
- another generic validation packet;
- новые API или model families;
- orchestration layer opening;
- BI / dashboard / analytics expansion.

### Transition

Пока нет real tenant evidence или concrete mismatch,
правильное состояние проекта:

**Project Stabilization / First-Adopter-Gated Hold**