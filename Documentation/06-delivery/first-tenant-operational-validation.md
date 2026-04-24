# First-Tenant Operational Validation

Status: candidate
Owner: product

## Purpose

Этот document фиксирует
следующий stage
после closeout
backend-first internally validated baseline
и first minimal post-Wave-2 hardening / validation packet.

Этот stage нужен,
чтобы prove or falsify
practical tenant-facing usability
already approved baseline
через one real tenant operational loop.

## Interpretation Rule

First-Tenant Operational Validation:
- не является новым bounded context;
- не является новым Wave 2 contour;
- не является release-ready claim;
- не открывает broad product expansion by default.

## In Scope

- one real tenant operational loop;
- observed day-to-day usage validation
  для already approved product surfaces;
- evidence gathering
  on usability, operational friction and blocking gaps;
- concrete gap register
  inside already approved scope;
- outcome classification:
  - confirmed one-tenant validation;
  - partial validation only;
  - in-scope gap closure required;
  - escalation to separate expansion.

## Out of Scope

- новый bounded context;
- новый Wave 2 contour;
- broad feature expansion;
- release-ready claim without explicit evidence;
- BI / dashboard / analytics expansion;
- orchestration layer opening by default.

## Evidence Model

### Evidence unit
Валидный evidence unit
должен содержать одновременно:
- real tenant-scoped actor;
- real action
  в already approved surface;
- owner-side current-state result;
- reporting-side derived result,
  если он релевантен;
- observed usability outcome or friction.

### Reality evidence
Должны быть подтверждены:
- один real tenant;
- real tenant users;
- real role paths;
- real reference-data setup path.

### Persona evidence
Минимально должны быть observed:
- Tenant Admin path;
- Asset Operator path;
- Support Reviewer path.

### Consistency evidence
Для touched assets
должны сравниваться:
- owner-context dedicated current-state surfaces;
- reporting-side derived current visibility.

Strict synchronous equality
не требуется.

Требуется подтвердить:
- semantic alignment;
- bounded lag;
- preservation of owner authority;
- non-authoritative role of reporting.

## Minimal Validation Loop

### Core loop
Без core loop
stage exit невозможен.

Core loop minimally includes:
1. real tenant access and setup;
2. real asset create/update usage;
3. real responsibility loop
   through assign
   and at least one of transfer / return;
4. history / audit review;
5. reporting comparison
   for touched assets;
6. repeated observation
   beyond one scripted session.

### Contour-extension rule
Каждый contour,
который claim’ится
как one-tenant operationally validated,
должен иметь
at least one real observed tenant loop
inside its already approved scope.

Если contour не был реально observed,
он остается only internally validated.

## Gap-Closure vs Expansion

### In-scope gap closure
Работа остается внутри stage,
если она:
- устраняет concrete mismatch
  inside approved scope;
- hardens existing semantics;
- не требует нового owner;
- не требует новой standalone model family;
- не требует нового primary API family;
- не требует orchestration layer.

### Expansion
Работа требует separate product decision,
если она требует:
- нового owner;
- новой standalone model family;
- нового primary API family;
- нового write-owning context;
- orchestration layer;
- BI / dashboard / analytics family.

## Exit Outcomes

### Outcome A
Confirmed one-tenant operational validation
inside current approved scope.

### Outcome B
Partial validation only.

### Outcome C
In-scope gap closure required.

### Outcome D
STOP and escalate
to separate product expansion decision.

## Explicit Non-Goals

Этот stage
не должен использоваться
для:
- hidden Wave 2 reopening;
- hidden auth/IAM expansion;
- hidden BI expansion;
- hidden workflow/orchestration expansion;
- premature release-ready claim.

## Not Fixed Yet

Этот document
не канонизирует пока:
- exact observation-window length;
- exact repeat-count threshold;
- issue severity taxonomy;
- final sign-off artifact owner;
- exact manual-support allowance during validation.

Пока эти items не approved,
они остаются candidate-only operating details.

## Executability Gate Clarification

First-Tenant Operational Validation
является правильным следующим validation stage conceptually,
но он не executable без real tenant evidence.

### Entry gate

Stage может стартовать только если есть:
- один real tenant или first adopter;
- real tenant-scoped actors;
- real role paths;
- real reference-data setup path;
- real operational actions внутри already approved surfaces;
- usage loop, observed beyond a single scripted internal walkthrough.

### Non-substitution rule

Internal demos, scripted happy-path walkthroughs,
synthetic tests или additional internal hardening
не могут использоваться как основание для claim
First-Tenant Operational Validation exit.

### If the gate is not met

Если real tenant / usage loop отсутствует,
этот stage остается not-started / not-executable,
а проект остается в режиме:

**Project Stabilization / First-Adopter-Gated Hold**