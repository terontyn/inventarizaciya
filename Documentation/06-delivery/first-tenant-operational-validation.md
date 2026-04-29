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

## Managed Demo Non-Substitution Rule

Managed Pilot UI demo readiness является полезной подготовкой,
но не заменяет First-Tenant Operational Validation.

Managed internal demo может показать,
что existing MVP surfaces demonstrable,
но не может доказать:

- real tenant adoption;
- real day-to-day operational usage;
- repeated real user behavior;
- operational friction under real tenant conditions;
- First-Tenant Operational Validation exit.

Следовательно,
managed demo evidence может прикладываться как preparation context,
но не может использоваться как stage exit evidence
для First-Tenant Operational Validation.

## Preparation / Evidence Capture Guidance

This section prepares future First-Tenant Operational Validation evidence capture.

It is guidance only.

It does not start, execute, complete or claim First-Tenant Operational Validation.

This guidance prepares:

- first-adopter intake;
- tenant setup prerequisites;
- persona observation checklists;
- minimal validation loop capture;
- evidence unit structure;
- owner-side and reporting-side evidence capture;
- history / audit evidence capture;
- responsibility-flow evidence capture;
- reference-data setup evidence capture;
- gap register structure;
- candidate issue severity taxonomy;
- candidate manual-support allowance;
- candidate sign-off artifact;
- stop conditions.

### Non-claim rule

This preparation guidance does not start, execute, complete or claim First-Tenant Operational Validation.

It does not authorize:

- implementation work;
- Codex work;
- backend/API changes;
- BFF / UI backend;
- auth/IAM expansion;
- new bounded context;
- new Wave 2 surface;
- release-ready claim;
- First-Tenant Operational Validation start claim;
- First-Tenant Operational Validation exit claim.

This guidance may be used only as preparation context until the real tenant / first adopter entry gate is met.



### 1. Facts

Current project mode remains:

**Project Stabilization / First-Adopter-Gated Hold**

Current baseline remains internally validated only.

Confirmed current facts:

- `Operational Workspace UX v2 / Release UI Layout & Navigation Cutline` is implemented and accepted.
- UX v2 is accepted as frontend-only refine-in-place layout, navigation and readability refinement over existing accepted operational UI surfaces.
- No production backend/API behavior was changed.
- No new endpoint was introduced.
- No DTO/read-model change was introduced.
- No BFF / UI backend was introduced.
- No auth/IAM expansion was introduced.
- No new frontend framework/router/microfrontend/UI service was introduced.
- No new bounded context was opened.
- No new Wave 2 surface was opened.
- No dashboard / BI / analytics was opened.
- No Asset 360 was opened.
- No global search / Elasticsearch was opened.
- No saved views / persistent preferences were opened.
- No bulk actions were opened.
- Assign / transfer / return remain dedicated responsibility flows.
- Generic edit remains master-record-only.
- Current baseline does not prove release-ready tenant-facing product state.
- Current baseline does not prove real tenant validation.
- Current baseline does not prove real day-to-day operational validation.
- FTOV execution is not started.
- FTOV exit is not achieved.

The project may prepare FTOV evidence capture docs, but only as preparation inside hold.

### 2. Preparation verdict

Verdict:

**Docs-only FTOV preparation is justified now. FTOV execution is not justified now.**

This guidance may be maintained because current hold explicitly allows:

- first-adopter intake preparation;
- first-tenant evidence-capture preparation;
- entry / exit gate clarification;
- concrete gap register preparation;
- preservation of already closed contour boundaries.

This guidance must not be interpreted as:

- FTOV start;
- FTOV exit;
- implementation authorization;
- Codex authorization;
- backend/API authorization;
- production readiness claim;
- release-ready tenant-facing claim;
- new product scope.

### 3. Purpose

Purpose:

Prepare reusable evidence-capture guidance for future First-Tenant Operational Validation.

This guidance should make future FTOV execution easier to run once the entry gate is met, by defining:

- what real tenant evidence must be captured;
- which personas must be observed;
- which owner-side and reporting-side facts must be compared;
- how assign / transfer / return evidence should be recorded;
- how setup/reference-data evidence should be captured;
- how gaps should be registered;
- when validation must stop or escalate.

This guidance prepares future execution.

It does not execute FTOV.

### 4. Non-claim rule

This guidance creates no claim that:

- FTOV has started;
- FTOV has exited;
- real tenant validation has been achieved;
- real day-to-day operational validation has been achieved;
- release-ready tenant-facing state has been achieved;
- production tenant readiness has been proven;
- internal walkthrough evidence is sufficient;
- screenshots, video review or synthetic tests can substitute real tenant evidence.

All future evidence must be clearly labelled by type:

- real tenant evidence;
- managed demo evidence only;
- internal walkthrough evidence only;
- synthetic test evidence only;
- implementation acceptance evidence only.

Only real tenant evidence can support FTOV outcome classification.

### 5. Entry gate

FTOV may start only when all entry conditions are met.

Required entry gate:

1. One real tenant or first adopter exists.
2. Real tenant-scoped actors are identified.
3. Real role paths exist for:
   - Tenant Admin;
   - Asset Operator;
   - Support Reviewer.
4. Real reference-data setup path exists.
5. Real operational actions can be performed inside already approved surfaces.
6. Usage can be observed beyond a single scripted internal walkthrough.
7. The observation is tenant-facing and operational, not only internal demo.
8. Evidence capture is allowed by the tenant / first adopter.
9. Manual support boundaries are agreed before observation.
10. Known limitations are explicitly stated before observation.

Entry gate is not met by:

- internal demo;
- scripted happy-path walkthrough;
- synthetic tests;
- screenshots only;
- video review only;
- internal implementation acceptance;
- managed demo readiness;
- additional internal hardening without real tenant usage.

If the entry gate is not met, FTOV remains:

**not-started / not-executable**

and the project remains in:

**Project Stabilization / First-Adopter-Gated Hold**

### 6. First-adopter intake checklist

Before FTOV can start, capture the following intake facts.

#### Tenant identity

- Tenant name:
- Tenant contact / sponsor:
- Tenant operational context:
- Tenant data sensitivity notes:
- Allowed evidence capture methods:
- Allowed screenshot/video/log capture:
- Any redaction requirements:

#### Validation scope

- Which already approved surfaces will be observed:
  - asset registry;
  - asset card;
  - asset create/update;
  - assign;
  - transfer;
  - return;
  - history/audit review;
  - setup/reference data;
  - reporting-derived visibility, if relevant.
- Which surfaces are explicitly not being claimed as validated:
- Which Wave 2 derived/current-state visibility surfaces are in observation, if any:
- Which surfaces remain internally validated only:

#### Actors

- Tenant Admin actor:
- Asset Operator actor:
- Support Reviewer actor:
- Any observer/facilitator:
- Any internal support participant:

#### Operational data readiness

- Real or tenant-representative assets available:
- Real or tenant-representative users available:
- Real departments available:
- Real branches available:
- Real locations available:
- Any data anonymization needed:
- Any seed/setup data created manually:

#### Evidence rules

- Evidence owner:
- Evidence reviewer:
- Gap register owner:
- Sign-off artifact owner candidate:
- Tenant acknowledgement owner candidate:
- Observation dates:
- Observation sessions:
- Repeat observation plan:
- Manual-support allowance:

### 7. Tenant setup prerequisites

Minimum setup prerequisites before first operational observation:

1. Tenant context exists.
2. Controlled access path exists for the real tenant.
3. First Tenant Admin can access the already implemented tenant-facing path.
4. Tenant Admin can manage or validate setup/reference-data surfaces.
5. Minimum reference data exists before first asset create / assign / transfer / return.

Recommended setup order:

1. Roles.
2. Pilot users with role assignment.
3. Branches.
4. Locations.
5. Departments.

Minimum reference-data areas:

- roles;
- users;
- departments;
- branches;
- locations.

Ownership rule:

- Tenant Admin owns setup/reference data at pilot/first-tenant stage.
- Asset Operator does not manage reference data by default.
- Support Reviewer does not manage reference data.

Setup evidence should include:

- actor performing setup;
- setup step;
- created/confirmed reference value;
- reason the reference value is needed;
- resulting visible state;
- any missing prerequisite;
- any manual support used.

### 8. Persona observation checklist

#### 8.1 Tenant Admin

Observe whether Tenant Admin can:

- enter tenant context through the controlled access path;
- understand setup/admin navigation;
- create or confirm roles;
- create or confirm users;
- assign roles to users;
- create or confirm branches;
- create or confirm locations;
- create or confirm departments;
- understand missing prerequisite states;
- distinguish setup/admin area from asset operations;
- avoid needing unsupported IAM/self-service onboarding capabilities.

Capture:

- setup action performed;
- reference value created/confirmed;
- success/failure state;
- usability friction;
- manual support used;
- whether setup prerequisites were sufficient for later asset operations.

#### 8.2 Asset Operator

Observe whether Asset Operator can:

- enter asset registry;
- identify an asset by operational labels rather than raw UUID only;
- open asset card;
- create asset;
- update asset master record without changing responsibility through generic edit;
- assign asset through dedicated assign flow;
- transfer asset through dedicated transfer flow;
- return asset through dedicated return flow;
- understand current responsibility state;
- understand next valid action;
- find history/audit after action completion;
- return to registry/card after actions.

Capture:

- asset used;
- action performed;
- owner-side current-state result;
- history/audit evidence;
- reporting-side derived result, if relevant;
- usability outcome or friction;
- manual support used;
- whether the action remained inside dedicated responsibility flow.

#### 8.3 Support Reviewer

Observe whether Support Reviewer can:

- enter read-only operational review path;
- view asset registry;
- open asset card;
- inspect current state;
- inspect history;
- inspect audit;
- compare owner-side state with reporting-derived visibility, if relevant;
- avoid seeing or using write entry points that should not be available;
- understand what happened, who acted and when.

Capture:

- reviewed asset;
- reviewed event/action;
- visible current-state fact;
- visible history/audit fact;
- reporting comparison, if relevant;
- any unauthorized write affordance observed;
- any readability/friction issue.

### 9. Minimal validation loop template

A valid minimal FTOV loop must include real tenant usage.

#### Core loop

For at least one real tenant operational loop, capture:

1. Real tenant access and setup.
2. Real asset create or update usage.
3. Real responsibility loop through:
   - assign;
   - and at least one of:
     - transfer;
     - return.
4. History / audit review.
5. Reporting comparison for touched assets, if reporting is relevant.
6. Repeated observation beyond one scripted session.
7. Explicit outcome classification.

#### Loop record

- Loop ID:
- Tenant:
- Observation date/session:
- Persona(s):
- Asset(s):
- Setup prerequisites used:
- Action sequence:
- Owner-side current-state result:
- Reporting-side derived result, if relevant:
- History/audit result:
- Usability outcome:
- Manual support used:
- Gap register links:
- Outcome candidate:
  - confirmed one-tenant validation;
  - partial validation only;
  - in-scope gap closure required;
  - escalation to separate expansion.

#### Contour-extension rule

If a surface or contour is not observed with real tenant actors, it remains:

**internally validated only**

Do not claim it as one-tenant operationally validated.

### 10. Evidence unit template

Each evidence unit must contain all required fields.

#### Evidence unit

- Evidence ID:
- Date/time:
- Tenant:
- Tenant actor:
- Persona:
- Role path:
- Surface:
- Action:
- Asset/user/reference object touched:
- Precondition:
- Input used:
- Owner-side current-state result:
- Reporting-side derived result, if relevant:
- History result:
- Audit result:
- Usability outcome:
- Friction observed:
- Manual support used:
- Screenshot/video/log reference:
- Redaction status:
- Gap register link, if any:
- Outcome:
  - pass;
  - pass with friction;
  - partial;
  - blocked;
  - out-of-scope request;
  - expansion candidate.

#### Valid evidence rule

A valid FTOV evidence unit must include:

- real tenant-scoped actor;
- real action in an already approved surface;
- owner-side current-state result;
- reporting-side derived result, if relevant;
- observed usability outcome or friction.

Evidence units that do not include a real tenant actor must be labelled:

**not FTOV evidence**

### 11. Evidence checklist by surface / workflow

#### 11.1 Owner-side asset evidence checklist

For asset registry / asset card / asset create / asset update, capture:

- tenant-scoped asset visible in registry;
- asset card opens from registry;
- asset master fields are visible:
  - asset_tag;
  - serial_number;
  - asset_type;
  - status;
  - assigned_user_id, where relevant;
  - location_id, where relevant.
- create asset produces current owner-side asset state;
- update asset changes only master-record fields;
- generic edit does not replace assign / transfer / return;
- asset remains tenant-scoped;
- current-state meaning is understandable to the user;
- user can return to registry/card after action.

#### 11.2 Reporting-side evidence checklist, only where relevant

Reporting-side evidence is required only when reporting-derived visibility is part of the observed loop.

Capture:

- reporting view for touched asset;
- owner-side source-of-truth fact being compared;
- reporting-side derived fact;
- whether reporting is semantically aligned with owner-side current state;
- whether any lag is bounded and understandable;
- whether the user understands reporting is derived visibility;
- whether reporting stayed read-only;
- whether reporting was incorrectly treated as source of truth.

Important:

- strict synchronous equality is not required;
- bounded lag / eventual consistency may be acceptable;
- owner context remains authoritative for the corresponding fact slice;
- unresolved semantic mismatch must be registered as a gap.

#### 11.3 History / audit evidence checklist

For each critical action, capture:

- visible history entry;
- visible audit entry, if available in the approved surface;
- actor;
- tenant scope;
- affected entity;
- action type;
- timestamp;
- previous value, if represented;
- new value, if represented;
- correlation/reference detail, if represented;
- whether history is append-only;
- whether prior history remains visible;
- whether audit/history is readable for Support Reviewer.

Required actions to check where performed:

- asset create/update, if audit/history is expected;
- assign;
- transfer;
- return.

#### 11.4 Responsibility-flow evidence checklist — assign

Assign evidence must show:

- actor is real tenant-scoped Asset Operator or allowed role;
- Asset exists in tenant scope;
- target User exists in tenant scope;
- assign is performed through dedicated assign flow;
- assign is not performed through generic PATCH/master edit;
- current owner-side Asset responsibility updates to assigned user;
- history receives append-only assignment fact;
- durable audit evidence is visible or traceable;
- user sees success/failure state;
- user can return to asset card/registry;
- if Asset was in storage custody, inventory-side consumption may be eventually consistent and must not be treated as synchronous precondition;
- reporting comparison is captured if relevant.

#### 11.5 Responsibility-flow evidence checklist — transfer

Transfer evidence must show:

- actor is real tenant-scoped Asset Operator or allowed role;
- Asset exists in tenant scope;
- target User and/or target Location exists in tenant scope;
- transfer is performed through dedicated transfer flow;
- transfer is not performed through generic PATCH/master edit;
- transfer changes at least one effective domain axis:
  - user-only;
  - location-only;
  - combined user + location.
- if there is no current assigned user, new user responsibility is treated as assignment, not transfer;
- current owner-side Asset state updates correctly;
- history receives append-only transfer fact;
- audit evidence is visible or traceable;
- user sees success/failure state;
- user can return to asset card/registry;
- reporting comparison is captured if relevant.

#### 11.6 Responsibility-flow evidence checklist — return

Return evidence must show:

- actor is real tenant-scoped Asset Operator or allowed role;
- Asset exists in tenant scope;
- Asset has current user responsibility before return;
- return is performed through dedicated return flow;
- return is not performed through generic PATCH/master edit;
- after return, Asset has no active current user responsibility;
- return does not canonically change location_id;
- return does not create storage custody by itself;
- if storage intake is needed after return, it is separate inventory-side operation and not implied by return;
- history receives append-only return fact;
- audit evidence is visible or traceable;
- user sees success/failure state;
- user can return to asset card/registry;
- reporting comparison is captured if relevant.

#### 11.7 Reference-data setup evidence checklist

For each setup area, capture:

##### Roles

- role created or confirmed;
- permissions/role meaning understood at operational level;
- role used for at least one real actor;
- no new RBAC model required.

##### Users

- user created or confirmed;
- user belongs to tenant;
- user receives correct role assignment;
- user can participate in assigned persona path.

##### Departments

- department created or confirmed;
- department is used only as managerial dimension;
- department does not become separate org entity.

##### Branches

- branch created or confirmed;
- branch is used as physical dimension;
- branch supports later location setup.

##### Locations

- location created or confirmed;
- location belongs to branch;
- location is available for asset current operational location where needed.

Setup evidence should verify only the minimum reference values required for the observed flows.

Do not expand setup into:

- IAM platform;
- SSO / MFA / SCIM / IdP;
- self-service onboarding;
- import engine;
- master-data module.

### 12. Gap register template

Use the following gap register structure.

#### Gap record

- Gap ID:
- Date observed:
- Tenant:
- Persona:
- Actor:
- Surface/workflow:
- Evidence unit link:
- Description:
- Expected behavior:
- Observed behavior:
- Impact:
- Severity:
- Scope classification:
  - in-scope gap;
  - manual support issue;
  - documentation/training issue;
  - out-of-scope request;
  - separate product expansion candidate.
- Owner-side fact affected:
- Reporting-side fact affected, if relevant:
- Workaround:
- Manual support used:
- Decision:
  - no action;
  - monitor;
  - docs clarification;
  - targeted gap closure candidate;
  - stop validation;
  - escalate to product decision.
- Resolution owner candidate:
- Status:
  - open;
  - accepted limitation;
  - candidate fix;
  - closed;
  - escalated.
- Notes:

#### Gap closure boundary

A gap can remain inside FTOV / targeted gap closure only if it:

- is concrete;
- is observed;
- is inside already approved scope;
- can be solved without a new owner;
- can be solved without a new standalone model family;
- can be solved without a new primary API family;
- can be solved without orchestration layer;
- can be solved without BI / dashboard / analytics expansion.

If resolution requires any of the above, stop and escalate to separate product expansion decision.

### 13. Issue severity taxonomy

Status:

**candidate-only taxonomy**

This taxonomy is proposed for FTOV preparation. It is not yet canonical until reviewed and accepted.

#### S0 — Stop / invalid validation condition

Use when observation cannot continue or evidence cannot be trusted.

Examples:

- no real tenant actor;
- tenant isolation concern;
- evidence capture not allowed;
- core loop cannot be observed at all;
- critical data integrity concern;
- cross-tenant visibility concern;
- request requires new backend/API/BFF/IAM/bounded context before validation can proceed.

Effect:

- stop observation;
- do not claim FTOV progress;
- escalate.

#### S1 — Blocking in-scope gap

Use when an approved core action cannot be completed by real tenant actor without unacceptable intervention.

Examples:

- Asset Operator cannot complete assign;
- transfer cannot complete with valid tenant data;
- return cannot complete where preconditions are satisfied;
- history/audit for critical action is absent where required;
- Tenant Admin cannot complete minimum setup required for core loop.

Effect:

- FTOV exit blocked;
- register targeted gap closure candidate;
- no release-ready or FTOV exit claim.

#### S2 — Major friction / partial validation

Use when the action completes, but with significant friction or manual support.

Examples:

- user completes flow only after repeated facilitator guidance;
- navigation causes repeated wrong path;
- success/failure state is unclear;
- reporting lag is acceptable technically but not understandable operationally;
- history/audit is present but hard to interpret.

Effect:

- partial validation only unless resolved or accepted explicitly;
- register gap;
- repeat observation after mitigation if possible.

#### S3 — Minor usability issue

Use when the action completes and meaning is preserved, but UX polish is needed.

Examples:

- wording is unclear but not misleading;
- UUID is too prominent but asset remains identifiable;
- return path exists but is not obvious;
- review table is readable but could be clearer.

Effect:

- does not block FTOV by itself;
- register as improvement candidate;
- may remain known limitation.

#### S4 — Observation / request / future expansion candidate

Use when the finding is not an in-scope defect.

Examples:

- tenant asks for dashboard;
- tenant asks for bulk actions;
- tenant asks for global search;
- tenant asks for Asset 360;
- tenant asks for self-service onboarding;
- tenant asks for BI/KPI reporting;
- tenant asks for new workflow automation.

Effect:

- do not treat as FTOV blocker unless it prevents current approved scope from being usable;
- register as out-of-scope request or separate product expansion candidate.

### 14. Manual-support allowance

Status:

**candidate-only operating detail**

Manual support may be allowed during FTOV only if it is explicitly recorded and does not mask product gaps.

Allowed manual support:

- explaining the validation purpose;
- helping schedule observation;
- preparing controlled access context;
- controlled materialization of first Tenant Admin where already allowed outside repo;
- explaining already documented concepts;
- answering “where should I click?” during first exposure;
- recording evidence;
- clarifying known limitations;
- redacting sensitive evidence.

Not allowed manual support:

- performing core operational actions instead of tenant actor and counting them as tenant evidence;
- bypassing approved UI surfaces;
- changing backend/API/data manually to make a flow pass;
- hiding product friction from evidence;
- treating facilitator-driven walkthrough as day-to-day tenant usage;
- using manual support to claim release readiness;
- using manual support to claim FTOV exit.

Manual-support classification:

- **MS0: none** — actor completed independently.
- **MS1: light guidance** — facilitator explained where existing surface is.
- **MS2: significant guidance** — actor completed only after step-by-step help.
- **MS3: facilitator action** — facilitator performed action; not valid as tenant action evidence.
- **MS4: unsupported workaround** — evidence invalid for FTOV claim; register gap/stop condition.

Rule:

If a core evidence unit depends on MS3 or MS4, it cannot support FTOV exit.

### 15. Candidate sign-off artifact

Status:

**candidate-only artifact**

Suggested artifact name:

**FTOV Observation Summary & Outcome Record**

Suggested contents:

1. Tenant identity.
2. Observation window.
3. Actors/personas observed.
4. Surfaces observed.
5. Evidence unit index.
6. Minimal validation loop summary.
7. Owner-side evidence summary.
8. Reporting-side evidence summary, if relevant.
9. History/audit evidence summary.
10. Responsibility-flow evidence summary:
    - assign;
    - transfer;
    - return.
11. Reference-data setup evidence summary:
    - roles;
    - users;
    - departments;
    - branches;
    - locations.
12. Gap register summary.
13. Manual-support summary.
14. Stop conditions encountered, if any.
15. Outcome classification:
    - confirmed one-tenant validation;
    - partial validation only;
    - in-scope gap closure required;
    - STOP and escalate to separate product expansion decision.
16. Explicit non-claim statement.
17. Candidate sign-off parties:
    - product/project owner;
    - evidence reviewer;
    - tenant-side representative acknowledgement, if available.
18. Open follow-ups.
19. Decision on whether any docs should be updated.

Required non-claim text:

> This artifact records evidence from a first-tenant operational validation preparation/execution context only as explicitly stated. It does not by itself create a release-ready tenant-facing claim, does not expand product scope, does not authorize backend/API/BFF/IAM implementation, and does not substitute synthetic/internal/demo evidence for real tenant evidence.

### 16. Stop conditions

Stop preparation or future observation and return to product/architecture review if any of the following appears:

1. Future FTOV execution / observation is requested while no real tenant / first adopter exists.
2. Real tenant actors are not available.
3. Observation is only an internal demo.
4. Observation is only a scripted happy-path walkthrough.
5. Evidence depends on synthetic tests, screenshots or video review instead of real tenant action.
6. Core loop cannot include real asset create/update usage.
7. Core loop cannot include assign and at least one of transfer / return.
8. History/audit cannot be reviewed where required.
9. Reporting comparison is claimed but reporting was not actually observed.
10. Reporting is treated as source of truth instead of derived visibility.
11. Manual support becomes the main way the core flow works.
12. A critical gap requires new backend/API.
13. A critical gap requires new DTO/read model.
14. A critical gap requires BFF / UI backend.
15. A critical gap requires auth/IAM expansion.
16. A critical gap requires a new bounded context.
17. A critical gap requires a new Wave 2 surface.
18. A critical gap requires dashboard / BI / analytics.
19. A critical gap requires Asset 360.
20. A critical gap requires global search / Elasticsearch.
21. A critical gap requires saved views / persistent preferences.
22. A critical gap requires bulk actions.
23. A finding would require new owner, standalone model family, primary API family, orchestration layer or reporting-side write ownership.
24. This guidance starts making release-ready, FTOV start or FTOV exit claims.

### 17. What this guidance does not authorize

This guidance does not authorize:

- code work;
- Codex task;
- implementation packet;
- backend/API changes;
- new endpoints;
- DTO/read-model changes;
- BFF / UI backend;
- auth/IAM expansion;
- new frontend framework/router/microfrontend/UI service;
- new bounded context;
- new Wave 2 surface;
- dashboard / BI / analytics;
- Asset 360;
- global search / Elasticsearch;
- saved views / persistent preferences;
- bulk actions;
- new product scope;
- reporting-side write ownership;
- orchestration layer;
- new workflow engine;
- self-service onboarding;
- SSO / MFA / SCIM / IdP;
- import engine;
- barcode / QR / scanning / printer integration;
- release-ready claim;
- FTOV start claim;
- FTOV exit claim;
- substitution of real tenant evidence with synthetic/internal/demo evidence.

