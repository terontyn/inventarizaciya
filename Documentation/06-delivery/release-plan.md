# Release Plan

Status: approved
Owner: product
Last Updated: 2026-05-20

## MVP Timeline
Срок MVP: 6–8 недель

## Weeks 1–2
Tenant + Access + User/Org Foundation

Результат:
- tenant-контекст;
- базовые роли;
- пользователи;
- отделы;
- филиалы;
- локации.

## Weeks 3–4
Asset Registry Core

Результат:
- можно завести актив;
- можно привязать актив к tenant и оргструктуре;
- доступны реестр и карточка актива.

## Weeks 5–6
Responsibility Flows + Audit

Результат:
- можно выдать актив сотруднику;
- можно переместить актив;
- можно вернуть актив;
- все критические действия попадают в историю и audit.

## Weeks 7–8
MVP Pilot Readiness

Результат:
- права, сценарии, справочники и UX доведены до состояния, когда один pilot tenant может реально начать учет в системе;
- утверждены pilot personas:
  - Tenant Admin
  - Asset Operator
  - Support Reviewer
- утвержден minimum pilot permission matrix;
- users / roles / departments / branches / locations доведены до operational use для первого tenant;
- asset registry и asset card usable для ежедневного базового учета;
- assign / transfer / return usable как dedicated responsibility flows;
- history / audit видимы для operational review;
- минимальный admin / support layer определен.

Exit gates before pilot sign-off:
- утвержден pilot permission matrix;
- exact status vocabulary для pilot UX и asset card зафиксирован: `ACTIVE`, `RETIRED`;
- зафиксирован minimum reference-data ownership / setup model для первого tenant:
  - minimum tenant-managed areas: roles / users / departments / branches / locations;
  - owner on pilot stage: Tenant Admin;
  - Asset Operator и Support Reviewer не управляют reference data;
  - setup order: roles -> pilot users with role assignment -> branches -> locations -> departments;
  - before first asset create / assign / transfer / return существуют только те reference values, которые реально требуются соответствующим flow.

Не входит в mandatory pilot gate:
- inventory / warehouse;
- software licenses / SAM;
- maintenance & repair;
- reporting module;
- heavy BI;
- expanded integrations;
- custom workflows;
- retirement как обязательный pilot gate.


## Printable Label v1 Clarification

На этапе MVP Pilot Readiness допускается minimal printable asset label v1
как дополнительное operational convenience поверх asset registry / asset card,
если это нужно для pilot usability.

### Boundary rule
- это не отдельный release track;
- это не mandatory pilot gate;
- это не открывает scope в barcode / QR / scanning / printer integration / bulk printing.

## Printable Label v1 Delivery Clarification

Printable Asset Label v1 не является mandatory pilot gate.

Если в текущем repo / delivery state отсутствует existing asset detail / card UI path,
implementation label v1 может быть отложен без пересмотра MVP contour
и без изменения backend-first managed pilot verdict.

Открытие minimal asset detail / card UI slice для будущей реализации label v1
допустимо как bounded completion already-approved MVP card usability,
но не является обязательным условием текущего pilot sign-off.


## Current Status Clarification

Текущий статус проекта фиксируется как:

**backend-first managed pilot = Ready with conditions satisfied**

### What this means
- текущий MVP / Pilot contour уже доведен до backend-first managed pilot-ready состояния;
- core docs / ADR / domain / data / API decisions для текущего contour зафиксированы;
- ранее пройденные CI и backend-only smoke остаются достаточной внутренней технической валидацией текущего backend-first baseline.

### Important limitation
При этом **real tenant / real operational pilot validation еще не достигнуты**.

Причина:
- текущая проверка остается в границах internal backend-first validation;
- отсутствие реального tenant, реального ежедневного usage loop и полноценного user-facing operational path не позволяет честно трактовать текущий статус как fully validated real pilot usage.

### Printable Label v1 status
- Printable Asset Label v1 остается approved in docs;
- Printable Asset Label v1 не является mandatory pilot gate;
- implementation Printable Asset Label v1 сейчас deferred,
  потому что approved implementation path зависит от existing asset detail / card UI anchor,
  а текущий repo verification подтверждает отсутствие такого UI anchor.

### Practical interpretation
Следовательно, текущий проектный baseline нужно трактовать как:

**backend-first internally validated MVP baseline**

а не как:
- fully proven real pilot usage;
- fully validated operational tenant rollout.

### What remains unchanged
Не меняется:
- текущий MVP contour;
- backend-first managed pilot verdict;
- approved docs decisions по Printable Asset Label v1;
- fact, что label v1 не блокирует текущий pilot sign-off.

### Follow-up trigger
К следующему уровню validation проект должен переходить только если:
- появится реальный tenant / реальный operational usage loop;
- или будет отдельно открыто решение на minimal asset detail / card UI slice;
- или будет запущен отдельный user-facing validation contour.

## First-Tenant Operational Validation — Delivery Clarification

После closeout
backend-first internally validated baseline
и first minimal post-Wave-2 hardening / validation packet
следующим stage
следует трактовать:

**First-Tenant Operational Validation**

### Delivery interpretation
Этот stage нужен,
чтобы prove or falsify
practical tenant-facing usability
already approved baseline
через one real tenant operational loop.

### Important rule
Этот stage:
- не является новым Wave 2 contour;
- не является новым bounded context;
- не является release-ready claim.

### Evidence rule
Full stage exit
не должен claim’иться
по одному internal demo
или по одному scripted happy-path walkthrough.

Нужен real tenant evidence loop
с:
- real tenant actors;
- real approved actions;
- owner-side current-state evidence;
- reporting-side derived evidence,
  если релевантно;
- explicit outcome classification.

### Outcome rule
Допустимы только outcomes:
- confirmed one-tenant validation;
- partial validation only;
- in-scope gap closure required;
- escalation to separate expansion.

## Implementation Progress Clarification

Для текущего delivery state нужно отдельно различать:
- approved scope;
- current implementation progress in repo.

### Current rule
Если implementation slice уже присутствует в repo,
он не должен снова открываться как новый ready-to-Codex delivery step.

### Current clarification
- Slice 1 (`Tenant + Org Reference Base` in `user-service`) считается уже реализованным baseline;
- Slice 2 (`User + Role Base` in `user-service`) считается уже реализованным baseline;
- дальнейшие задачи по `user-service` в этой зоне должны трактоваться не как greenfield implementation,
  а как:
  - verification,
  - hardening,
  - gap closure,
  - readiness improvement,
  - или переход к следующему slice.

### Practical consequence
Следующий implementation step должен определяться только после явной repo verification,
а не только по старому planning packet или прошлому candidate prompt.

## Slice 3 Closeout Clarification

Asset Registry Core (`Slice 3`) считается закрытым на уровне implementation + hardening.

### Current status
- базовый `asset-service` CRUD v1 уже реализован;
- hardening по tenant/authenticated context negative paths выполнен;
- explicit guard для history-sensitive `location_id` в generic PATCH подтвержден тестами;
- repo-level README / service README синхронизированы с текущим implementation state;
- новый product scope или новый API surface в рамках closeout не открывались.

### Practical interpretation
Следовательно:
- `Slice 3` не должен снова открываться как новый implementation step;
- дальнейшая работа по `asset-service` должна трактоваться либо как:
  - следующий slice,
  - либо отдельный hardening/gap-closure,
  - но не как повторная реализация `Asset Registry Core`.

### Remaining limitation
Локальная full verification может оставаться ограниченной средой выполнения,
если локальная машина не соответствует требуемому Java 17 baseline.

Это не меняет repo/CI verdict,
если CI path для `asset-service` уже настроен на Java 17 и остается рабочим.

## Backend Closeout Clarification

Текущий backend-first managed pilot baseline считается закрытым на уровне implementation + hardening.

### Current status
- `user-service` baseline для текущего MVP contour реализован и проверен;
- `asset-service` baseline для текущего MVP contour реализован и проверен;
- backend negative paths, pilot RBAC checks, managed setup path и service-readiness tests дожаты до внутренне согласованного состояния;
- unresolved code blockers для текущего backend contour не остаются.

### Important limitation
Это не означает, что достигнут:
- real tenant validation;
- real day-to-day operational validation;
- fully proven user-facing pilot usage.

Текущий результат следует трактовать как:

**backend-first internally validated managed pilot baseline**

### Practical rule
После этой точки текущий backend contour не должен снова открываться как новый implementation step,
если не найден конкретный repo gap или конкретный defect.

## Minimal Pilot UI Contour Clarification

Backend-first managed pilot baseline может считаться internally validated,
но real tenant operational use требует minimal user-facing contour.

Для practical pilot usability должны существовать следующие minimal UI surfaces:
- asset registry
- asset card
- create / update asset
- assign / transfer / return entry points
- history / audit read visibility
- minimal tenant admin / reference-data management surfaces

Этот contour не считается новым release track,
а трактуется как completion path already-approved MVP usability.

## Minimal Pilot UI Contour Closeout Clarification

Minimal Pilot UI Contour считается закрытым на уровне implementation + closeout.

### Current status
- minimal user-facing contour реализован поверх уже существующих backend services;
- registry / card / create / update / assign / transfer / return / history / audit / minimal admin surfaces присутствуют;
- browser-facing UI больше не зависит от internal-only contract;
- новый backend contour, BFF, UI-only backend или новый auth platform не открывались;
- UI scope не расширялся за пределы already-approved MVP completion path.

### Practical interpretation
Следовательно:
- Minimal Pilot UI Contour не должен снова открываться как новый implementation step;
- дальнейшая работа должна трактоваться либо как:
  - конкретный defect / gap closure,
  - следующий отдельный contour,
  - или next-level validation path,
  - но не как повторная реализация minimal pilot UI.

### Remaining limitation
Даже после UI closeout текущий проектный baseline не доказывает:
- real tenant validation;
- real day-to-day operational validation;
- self-service access / onboarding readiness.

Текущий результат следует трактовать как:

**backend + minimal pilot UI internally validated baseline**


## Minimal Pilot Access Contour Clarification

После backend + minimal pilot UI internally validated baseline
следующим completion path для real tenant operational usability
становится Minimal Pilot Access Contour.

### Why
Без minimal tenant-facing access path проект не может считаться
практически usable для реального первого tenant,
даже если backend baseline и minimal pilot UI contour already implemented.

### Boundary rule
- это не новый release track;
- это не новый auth platform program;
- это bounded completion layer внутри already-approved MVP.

## Minimal Pilot Access Contour — Delivery Clarification

Для текущего managed pilot Minimal Pilot Access Contour
может считаться выполненным без repo-owned production login path,
если существует controlled managed operational access path outside repo.

### Practical interpretation
Для первого tenant допустимо, что:
- production identity-establishment path находится вне repo;
- first Tenant Admin materialization остается controlled managed operational step;
- после этого already-implemented UI and backend contour
  используются как normal tenant-facing MVP path.

### Not required for current MVP
Не требуется как mandatory pilot gate:
- repo-owned auth/session/token mechanics;
- repo-owned self-service first-Tenant-Admin bootstrap;
- расширение проекта до нового auth/IAM contour.

## Minimal Pilot Access Contour Closeout Clarification

Для текущего MVP Minimal Pilot Access Contour считается закрытым
без обязательного нового in-repo implementation slice.

### Current interpretation
- controlled managed operational access path outside repo допустим;
- controlled materialization of the first Tenant Admin outside repo допустима;
- already-implemented UI and backend contour остаются достаточным in-repo baseline
  после появления authenticated context.

### Practical rule
Следовательно:
- новый greenfield access implementation step не требуется;
- новый ready-to-Codex implementation packet по access не открывается;
- дальнейшая работа допустима только как:
  - конкретный defect fix,
  - hardening,
  - gap closure,
  - но не как новый auth/access contour.

## Inventory / Warehouse Contour Decision

Decision: approved candidate

`Inventory / Warehouse` рекомендуется открыть
как следующий Wave 2 contour
после завершения текущего Asset Accountability MVP.

### Product intent
Дать tenant operational control
над активами, которые уже существуют в asset registry,
но в данный момент находятся в storage / warehouse custody,
а не закреплены за конечным пользователем.

### Non-expansion rule
Первый `Inventory / Warehouse` contour
не должен трактоваться как запуск большого складского продукта.

Он ограничивается only minimal storage-aware operations
для individually tracked assets
и не расширяется в procurement, supplier, finance,
consumables, advanced logistics или BI-first scope.

## Inventory / Warehouse Minimal Contour Closeout

Первый narrow `Inventory / Warehouse` contour считается реализованным
на уровне minimal Wave 2 implementation.

### Current status
- `inventory-service` добавлен как отдельная service boundary;
- storage custody и warehouse placement реализованы как отдельная operational dimension;
- реализованы minimal reads и commands первого contour;
- assign-to-inventory handoff реализован через internal-only asynchronous integration;
- direct DB coupling между сервисами не вводился;
- scope не расширялся в full WMS / ERP / procurement / reporting / barcode scope.

### Practical interpretation
Следовательно:
- первый minimal inventory contour не должен снова открываться как greenfield implementation step;
- дальнейшая работа по inventory должна трактоваться либо как:
  - hardening,
  - gap closure,
  - либо следующий inventory expansion slice,
  - но не как повторная реализация базового storage custody contour.


## Inventory Availability / Readiness Refinement Closeout Clarification

`Inventory Availability / Readiness Refinement`
считается закрытым
как completed bounded expansion slice
внутри `Inventory / Warehouse`.

### Current interpretation
После этого closeout inventory context уже покрывает:
- current storage custody;
- warehouse placement;
- warehouse anchoring to existing physical org model;
- inventory-side readiness semantics.

### Practical rule
Следовательно:
- readiness refinement не должен снова открываться
  как новый greenfield implementation slice;
- дальнейшее расширение inventory
  не считается default next step;
- move history beyond audit,
  richer current-vs-history representation
  и stocktaking / reconciliation
  остаются deferred
  до отдельного product decision.

## Wave 2 Sequencing Clarification After Inventory Readiness

После closeout `Inventory Availability / Readiness Refinement`
следующим рекомендуемым шагом Wave 2
является не еще один bounded inventory slice,
а переход к следующему большому contour.

### Recommended next contour
Следующим рекомендуемым contour
является `Maintenance`.

### Why not continue inventory now
После readiness refinement
inventory уже достиг bounded usable state
для своего narrow product purpose:
- what is in storage now;
- where it is placed now;
- whether it is inventory-side ready or not ready.

Следующие candidate expansions внутри inventory
уже в основном ведут в:
- move history platform;
- richer warehouse current-vs-history model;
- stocktaking / reconciliation / cycle counting.

Это означает заметно более warehouse-heavy expansion
и повышает риск drift в full WMS direction.

### Why Maintenance next
После inventory readiness
следующий practical operational gap
смещается из warehouse custody
в serviceability loop:

- почему asset currently not ready;
- как он проходит inspection / repair / servicing;
- как он возвращается
  в operationally usable state.

### Boundary rule
Этот sequencing decision:
- не расширяет inventory в full WMS;
- не меняет ADR-0009;
- не переносит readiness ownership из `inventory-service`;
- не переносит final assign decision logic из `asset-service`.


## Maintenance Minimal Contour Closeout Clarification

Первый narrow `Maintenance` contour
считается закрытым
как completed bounded Wave 2 slice.

### Current interpretation
После этого closeout maintenance context уже покрывает:
- minimal maintenance work representation;
- open maintenance work lifecycle;
- maintenance completion lifecycle;
- current open work visibility;
- maintenance-side serviceability outcome.

### Practical rule
Следовательно:
- первый narrow `Maintenance` contour
  не должен снова открываться
  как greenfield implementation step;
- дальнейшее расширение maintenance
  не считается default next move;
- richer outcome taxonomy,
  cancellation / reopen semantics,
  vendor workflow,
  cost linkage,
  maintenance history beyond audit
  и serviceability-to-readiness coordination refinement
  остаются deferred
  до отдельного product decision.

## Wave 2 Sequencing Clarification After Maintenance

После closeout первого narrow `Maintenance` contour
следующим рекомендуемым шагом Wave 2
является переход
к другому большому contour,
а не еще один default maintenance expansion slice.

### Recommended next contour
Следующим рекомендуемым contour
является `Software`.

### Why not continue Maintenance now
После minimal maintenance closeout
maintenance уже достиг bounded usable state
для своего narrow product purpose:
- why asset currently requires servicing;
- what maintenance work is open now;
- when maintenance-side serviceable outcome is reached.

Следующие candidate expansions внутри maintenance
уже в основном ведут в:
- workflow hardening / edge semantics;
- vendor / external servicing workflow;
- history-heavy refinement;
- cost / finance adjacency;
- cross-context coordination convenience.

Это означает,
что дальнейшее maintenance expansion
дает менее сильный next product leap
и повышает риск scope drift.

### Boundary rule
Этот sequencing decision:
- не превращает `Maintenance`
  в generic helpdesk / ITSM contour;
- не меняет inventory readiness ownership;
- не меняет final assign decision ownership;
- не меняет ADR-0009.


## Software Minimal Contour Closeout Clarification

Первый narrow `Software` contour
считается закрытым
как completed bounded Wave 2 slice.

### Current interpretation
После этого closeout software context уже покрывает:
- software record visibility;
- current software-to-user allocation visibility;
- current software-to-asset allocation visibility;
- explicit create / allocate / remove actions;
- current-state-only software relation model.

### Practical rule
Следовательно:
- первый narrow `Software` contour
  не должен снова открываться
  как greenfield implementation step;
- дальнейшее расширение software
  не считается default next move;
- richer software field model,
  key / type / status semantics,
  software history beyond current-state visibility,
  seat-like allocation refinement
  и coordination refinement with user / asset lifecycle
  остаются deferred
  до отдельного product decision.

## Wave 2 Sequencing Clarification After Software

После closeout первого narrow `Software` contour
следующим рекомендуемым шагом Wave 2
является переход
к другому большому contour,
а не еще один default software expansion slice.

### Recommended next contour
Следующим рекомендуемым contour
является `Ticket`.

### Why not continue Software now
После minimal software closeout
software уже достиг bounded usable state
для своего narrow product purpose:
- what software records tenant tracks now;
- what current software-to-user relations exist;
- what current software-to-asset relations exist.

Следующие candidate expansions внутри software
уже в основном ведут в:
- richer field model;
- key / type / status semantics;
- seat-like refinement;
- history-heavy refinement;
- lifecycle coordination convenience.

Это означает,
что дальнейшее software expansion
дает менее сильный next product leap
и повышает риск drift
в full SAM direction.

### Why not Reporting next
`Reporting` на этом этапе
не рекомендуется как next primary contour,
потому что он остается more derivative layer
поверх уже открытых operational contours,
а не следующим самостоятельным product move.

### Boundary rule
Этот sequencing decision:
- не открывает full SAM suite;
- не открывает compliance-heavy program;
- не открывает reporting / BI as primary goal;
- не меняет ownership existing contexts;
- не меняет ADR-0009.

## Ticket Minimal Contour Closeout

Первый narrow `Ticket` contour считается реализованным
на уровне minimal Wave 2 implementation.

### Current status
- `ticket-service` добавлен как отдельная service boundary;
- реализован minimal ticket current-state model:
  - `Ticket`
  - `OPEN`
  - `CLOSED`
- реализованы minimal read surfaces и lifecycle commands:
  - open ticket
  - close ticket
  - open ticket visibility
  - single ticket visibility
  - asset-linked current ticket visibility
- multiple current `OPEN` tickets на одном asset допустимы;
- independent-loop semantics подтверждены:
  - opening одного ticket не блокирует другой;
  - closing одного ticket не закрывает автоматически другие current `OPEN` tickets того же asset;
- audit для ticket open / close реализован;
- tenant isolation и negative-path hardening подтверждены тестами;
- ownership boundaries между `asset-service`, `inventory-service`, `maintenance-service`, `software-service` и `ticket-service` не менялись.

### Practical interpretation
Следовательно:
- первый minimal `Ticket` contour не должен снова открываться
  как greenfield implementation step;
- дальнейшая работа по ticket должна трактоваться либо как:
  - hardening,
  - gap closure,
  - либо следующий bounded ticket expansion slice,
  - но не как повторная реализация базового ticket contour.

## Wave 2 Sequencing Clarification After Ticket

После closeout первого narrow `Ticket` contour
следующим рекомендуемым шагом Wave 2
является переход
к другому большому contour,
а не еще один default ticket expansion slice.

### Recommended next contour
Следующим рекомендуемым contour
является `Reporting`.

### Practical rule
Следовательно:
- дальнейшее расширение ticket
  не считается default next move;
- requester / assignee refinement,
  queue / categorization refinement,
  richer ticket field model,
  cross-context action-link refinement
  и ticket history beyond current-state visibility
  остаются deferred
  до отдельного product decision.

### Reporting opening note
Следующий большой Wave 2 contour
должен трактоваться узко:
как current-state-first cross-context operational visibility layer,
а не как heavy BI / analytics platform.

## Reporting Minimal Contour Closeout

Первый narrow `Reporting` contour считается реализованным
на уровне minimal Wave 2 implementation.

### Current status
- `reporting-service` добавлен как отдельная service boundary;
- реализован primary reporting-side read model:
  - `AssetOperationalCurrentView`
- реализованы minimal read-only reporting surfaces:
  - `GET /api/v1/reporting/assets`
  - `GET /api/v1/reporting/assets/{asset_id}`
- reporting остается:
  - current-state-first
  - asset-centric
  - read-only
- reporting-side fields остаются derived visibility
  поверх existing owner contexts,
  а не новыми canonical operational facts;
- safe partial composition реализована;
- short-lived lag / eventual consistency допускаются;
- strict synchronous cross-context snapshot не обещается;
- owner context остается authoritative source
  при расхождении fact slice;
- reporting-side write APIs и write workflow не добавлялись;
- ownership existing contexts не менялся.

### Practical interpretation
Следовательно:
- первый minimal `Reporting` contour не должен снова открываться
  как greenfield implementation step;
- дальнейшая работа по reporting должна трактоваться либо как:
  - hardening,
  - gap closure,
  - либо следующий bounded reporting expansion slice,
  - но не как повторная реализация базового reporting contour.

## Wave 2 Completion Clarification After Reporting

После closeout первого narrow `Reporting` contour
current Wave 2 narrow map
следует трактовать
как effectively complete.

### Current interpretation
На этом этапе уже minimally closed:
- `Inventory / Warehouse`
- `Maintenance`
- `Software`
- `Ticket`
- `Reporting`

### Practical rule
Следовательно:
- следующим default move
  не должен становиться
  еще один bounded reporting slice;
- следующий новый large contour
  не должен auto-opened
  из текущего Wave 2 map;
- historical reporting,
  KPI / dashboard catalog,
  trend semantics
  и broader aggregation
  остаются deferred
  до отдельного product decision.


## Post-Wave-2 Hardening / Validation — Delivery Clarification

После effective closeout current narrow Wave 2 map
следующий delivery move
должен трактоваться
как combined hardening + validation stage,
а не как новый contour opening.

### Delivery interpretation
Этот stage допускает только:
- hardening уже реализованных contours;
- gap closure,
  если validation показывает конкретный mismatch
  внутри already approved scope;
- validation of combined operational behavior
  across existing contours.

### Not automatic
Этот stage не означает automatic opening of:
- new APIs;
- new bounded contexts;
- second reporting expansion;
- BI / dashboard scope;
- next large contour.

### Cutline
Пока работа остается
внутри validation and hardening
already approved semantics,
она принадлежит этому stage.

Если работа требует:
- нового owner,
- новой standalone model family,
- нового primary API family,
- нового orchestration layer,
она должна трактоваться
как новый product expansion
и требовать отдельного решения.

## Post-Wave-2 Hardening / Validation — Validation Focus

На этом stage
должны быть явно подтверждены
следующие области:

- cross-context consistency;
- preservation of owner boundaries;
- alignment between owner-side current-state reads
  и reporting-side derived visibility;
- acceptability of short-lived lag
  и eventual consistency;
- operational usability
  combined narrow Wave 2 stack
  как единого product baseline.

### Important note
Цель stage
не в том,
чтобы убрать любую eventual consistency.

Цель stage
в том,
чтобы подтвердить,
что допустимая eventual consistency:
- понятна;
- ограничена;
- не ломает operational meaning;
- не делает reporting source of truth.

## Post-Wave-2 Hardening / Validation — Minimal Packet Closeout

Первый minimal packet
для post-Wave-2 `cross-contour hardening / validation`
считается закрытым
на уровне implementation + verification.

### What was confirmed
По итогам этого packet подтверждено:
- reporting остается read-only derived visibility layer;
- owner-side current-state slices
  семантически мапятся
  в reporting-side current view;
- bounded lag around ADR-0009
  остается accepted
  и не трактуется как defect by default;
- safe partial composition
  остается допустимой
  без превращения reporting
  в source-of-truth layer;
- новых API/model/orchestration families
  не появилось.

### Delivery rule
Следовательно:
- этот packet не должен снова открываться
  как новый default execution step;
- следующий delivery move
  внутри post-Wave-2 stage
  допустим только если найден:
  - новый concrete mismatch
    внутри already approved scope;
  - или новый justified hardening target
    без выхода за текущий cutline;
- если resolution требует:
  - нового owner;
  - новой standalone model family;
  - нового primary API family;
  - нового orchestration layer;
  - нового BI/reporting capability family;
  такой move уже не принадлежит
  данному stage
  и требует отдельного product decision.

## Выбор следующего workstream после post-Wave-2 minimal packet

После effective closeout текущей narrow Wave 2 map
и closeout первого minimal post-Wave-2 hardening / validation packet
следующий justified move не должен автоматически открывать новый contour.

### Текущее решение

Текущий режим проекта:

**Project Stabilization / First-Adopter-Gated Hold**

### Интерпретация

Это означает:
- baseline остается только internally validated;
- real tenant / real operational validation еще не достигнуты;
- First-Tenant Operational Validation остается правильным следующим validation stage;
- First-Tenant Operational Validation не executable,
  пока нет real tenant, real tenant actors и real usage loop.

### Что разрешено внутри hold

Разрешено только:
- выравнивание docs вокруг текущего validation ceiling;
- подготовка first-tenant evidence capture;
- уточнение entry / exit gate;
- подготовка concrete gap register;
- сохранение границ уже закрытых contours.

### Что не разрешено по умолчанию

Этот hold не разрешает автоматически открывать:
- новый bounded context;
- новую Wave map;
- новый reporting / BI expansion;
- generic engineering hardening без concrete mismatch;
- synthetic validation как замену real tenant evidence;
- release-ready tenant-facing claim.

### Exit triggers

Hold завершается только если:
1. появляется real tenant / first adopter для operational validation;
2. найден concrete in-scope defect или mismatch;
3. отдельно approved новый product expansion decision.

## Pilot UI Stabilization / Managed Demo Readiness

После closeout Minimal Pilot UI Contour
существующий Pilot UI может быть стабилизирован
как managed demo / internal pilot surface.

### Interpretation

Это не новый contour и не новый release track.

Это readiness clarification
для controlled demonstration уже approved MVP surfaces.

### Managed demo readiness requires

Managed demo readiness требует:

- repeatable managed context setup;
- Tenant Admin setup path walkthrough;
- Asset Operator asset lifecycle walkthrough;
- Support Reviewer read-only walkthrough;
- visible asset registry / card / history / audit evidence;
- explicit known-limitation statement;
- evidence packet,
  который явно помечает это как managed demo evidence only.

### Non-claim rule

Managed demo readiness нельзя использовать как evidence для:

- production tenant-facing readiness;
- release-ready claim;
- First-Tenant Operational Validation exit;
- real tenant operational validation.


## Productized Tenant-Facing Operational UX v1 Closeout Clarification

Productized Tenant-Facing Operational UX v1 is accepted as implemented.

The implementation refined the existing static Pilot UI into a more productized tenant-facing operational UX over already approved MVP and narrow Wave 2 surfaces.

This closeout is accepted as frontend-only UX refinement.

No production backend/API changes were made; test-only smoke coverage was updated.

This is not:
- new backend/API delivery;
- new bounded context;
- new BFF or UI backend;
- new auth / IAM expansion;
- new Wave 2 contour or surface expansion;
- release-ready tenant-facing claim;
- First-Tenant Operational Validation claim.

Current project baseline remains internally validated only.
Real tenant validation and real day-to-day operational validation are still not proven.

## Operational Workspace UX v2 Closeout Clarification

`Operational Workspace UX v2 / Release UI Layout & Navigation Cutline`
is accepted as implemented.

Canonical scope doc:

`/Documentation/06-delivery/operational-workspace-ux-v2-release-layout-navigation-cutline.md`

### Delivery interpretation

The implementation is accepted as:

**frontend-only refine-in-place layout, navigation and readability refinement over existing accepted operational UI surfaces.**

The delivery changed the existing static UI and test-only smoke coverage.

It did not change production backend/API behavior.

### Implementation closeout facts

Codex implementation and correction pass were completed for the approved UX v2 scope.

Accepted implementation facts:

- existing static UI was refined in place;
- workspace layout and navigation hierarchy were improved;
- readability and section separation were improved;
- RU-facing copy and technical wording were cleaned up;
- assign / transfer / return remained dedicated responsibility flows;
- generic edit remained master-record-only;
- docs were not changed by Codex;
- focused smoke passed;
- full service test suite passed before correction pass;
- focused UI smoke passed after correction pass;
- Browser visual smoke could not run due Node OS access denied;
- video review accepted the UX result after correction pass.

Changed implementation files were reported as:

- `app.js`;
- `index.html`;
- `app.css`;
- `MinimalPilotUiSmokeTest.java`.

Correction pass changed:

- `app.js`;
- `index.html`;
- `MinimalPilotUiSmokeTest.java`.

### Boundary rule

This closeout does not open:

- backend/API changes;
- new endpoints;
- DTO / read-model expansion;
- BFF / UI backend;
- auth/IAM expansion;
- new frontend framework / router / microfrontend / UI service;
- new bounded context;
- new Wave 2 surface;
- dashboard / BI / analytics;
- Asset 360;
- global search / Elasticsearch;
- saved views / persistent preferences;
- bulk actions.

### Non-claim rule

This closeout does not change the current validation ceiling.

It does not prove:

- release-ready tenant-facing product state;
- production tenant readiness;
- real tenant validation;
- real day-to-day operational validation;
- First-Tenant Operational Validation exit.

The project remains in:

**Project Stabilization / First-Adopter-Gated Hold**

First-Tenant Operational Validation still requires real tenant / first adopter / real usage loop.

## Operational Workspace UX Stabilization v3 Closeout Clarification

Operational Workspace UX Stabilization v3 is accepted as implemented.

This was a targeted frontend-only gap closure inside the existing Operational Workspace UX surface, opened from controlled synthetic review findings.

### Delivery interpretation

The implementation is accepted as:

**frontend-only refine-in-place UX stabilization over existing accepted Operational Workspace UI surfaces.**

The delivery changed only existing static UI files under `asset-service`:

- `index.html`
- `app.css`
- `app.js`

It did not change production backend/API behavior.

### Implementation closeout facts

Accepted implementation facts:

- sticky / anchored navigation was added or improved;
- in-flight button states and double-submit prevention were added;
- create/update/assign/transfer/return/setup feedback was improved;
- empty states and prerequisite guidance were improved;
- UUID / long technical value readability was improved;
- asset card hierarchy was strengthened around asset tag, serial number, type, status and responsibility;
- assign / transfer / return remained dedicated responsibility flows;
- generic edit remained master-record-only;
- history/audit readability was improved with raw technical details collapsed and still available;
- role affordance was improved only within existing role context.

Checks reported:

- `:services:asset-service:test --tests com.itam.assetservice.MinimalPilotUiSmokeTest` passed;
- `:services:asset-service:test` passed;
- `git diff --check` passed with only Git LF-to-CRLF warnings;
- changed files were limited to existing static UI files.

### Post-v3 controlled synthetic re-review

Post-v3 controlled synthetic UI re-review has been completed.

Accepted re-review verdict:

**Passed with minor friction.**

Confirmed improvements:

- the UI opened and connected to the provided tenant context;
- the UI could reach `user-service`;
- no CORS/network issue was observed during the re-review;
- sticky / anchored top navigation worked and highlighted the active section;
- loading states and double-submit prevention were visible on key actions;
- UUIDs and technical identifiers were visually de-emphasized and copyable where needed;
- asset registry and asset card hierarchy were clearer;
- selected asset context was clearer;
- tenant setup tabs and editor states were clearer;
- assign / transfer / return remained dedicated responsibility flows;
- generic edit remained master-record-only;
- history/audit became more human-readable;
- raw technical details remained available but were less dominant;
- role/scope affordance labels were visible.

Remaining minor friction:

- explicit success/error notifications after mutating actions are still limited;
- role assignment confirmation can be clearer;
- context values remain truncated, though copy/tooltip affordances reduce the issue;
- some audit wording and correlation details remain technical;
- some navigation behavior near already-visible target sections may still feel subtle;
- further manual verification of setup selected-row affordance may be useful.

Current interpretation:

These remaining issues are minor frontend UX hardening candidates.

They do not block acceptance of UX Stabilization v3 as implemented and post-v3 synthetic re-reviewed.

They do not justify:

- backend/API changes;
- BFF / UI backend;
- auth/IAM expansion;
- Hardware Inspector parity;
- product expansion;
- release-ready claim;
- FTOV start or FTOV exit claim.

### Boundary rule

This closeout does not open:

- backend/API changes;
- new endpoints;
- DTO / read-model expansion;
- database / migration changes;
- BFF / UI backend;
- auth/IAM expansion;
- new role model;
- new bounded context;
- new Wave 2 surface;
- dashboard / BI / analytics;
- Asset 360;
- global search / Elasticsearch;
- saved views / persistent preferences;
- bulk actions;
- barcode / QR / scanning / printer integration;
- import/export engine;
- self-service onboarding;
- Hardware Inspector parity.

### Remaining validation limitation

The post-v3 controlled synthetic re-review had enough synthetic data to verify the updated UI behavior and core reviewed flows.

However, this remains controlled synthetic review evidence only.

It does not replace real tenant validation.

Future First-Tenant Operational Validation still requires:

- a real tenant / first adopter;
- real tenant-scoped actors;
- real reference-data setup path;
- real operational usage loop;
- observed day-to-day actions beyond scripted internal walkthrough.

Any further seed/setup work should be treated as preparation for better synthetic or managed demonstration evidence, not as FTOV evidence.

### Non-claim rule

This closeout does not change the current validation ceiling.

It does not prove:

- release-ready tenant-facing product state;
- production tenant readiness;
- real tenant validation;
- real day-to-day operational validation;
- First-Tenant Operational Validation start;
- First-Tenant Operational Validation exit.

The project remains in:

**Project Stabilization / First-Adopter-Gated Hold**

First-Tenant Operational Validation still requires real tenant / first adopter / real usage loop.


## Operational Workspace UX v3.1 Closeout Clarification

Operational Workspace UX v3.1 is accepted as implemented after frontend-only feedback visibility correction.

This was a narrow frontend-only usability hardening pass over the already accepted Operational Workspace UX v3 surface.

### Delivery interpretation

The implementation is accepted as:

**frontend-only refine-in-place usability hardening over existing Operational Workspace UI surfaces.**

Changed files were limited to existing static UI resources:

- `app.css`
- `app.js`

No production backend/API behavior was changed.

### Implemented hardening

v3.1 addressed:

- explicit success/error notification visibility after mutating actions;
- clearer feedback after asset create/edit;
- clearer feedback after assign / transfer / return;
- clearer feedback after setup create/update;
- clearer confirmation after role assignment;
- setup table row affordance preservation;
- technical-noise reduction preservation;
- audit/history wording preservation;
- location visibility using existing frontend data only.

### Feedback visibility correction

Post-v3.1 controlled synthetic re-review initially produced conflicting evidence around setup save and role assignment notifications.

Manual ground-truth verification confirmed:

- setup save shows a visible sticky success banner:
  `Готово. Запись настройки сохранена.`
- role assignment shows a visible sticky success banner:
  `Готово. Роль назначена пользователю. Изменение сохранено.`
- role assignment also shows local inline confirmation:
  `Последнее назначение в этой сессии подтверждено.`

The feedback visibility correction is accepted as passed.

### Remaining minor friction

Remaining minor frontend UX hardening candidate:

- notification lifetime may be shortened/tuned later so success banners remain noticeable but do not feel too long-lived.

This is not a blocker for v3.1 acceptance.

### Boundary rule

This closeout does not open:

- backend/API changes;
- new endpoints;
- DTO / read-model changes;
- database / migration changes;
- BFF / UI backend;
- auth/IAM expansion;
- new role model;
- new bounded context;
- new Wave 2 surface;
- dashboard / BI / analytics;
- Asset 360;
- global search / Elasticsearch;
- saved views / persistent preferences;
- bulk actions;
- barcode / QR / scanning / printer integration;
- import/export engine;
- Hardware Inspector parity.

### Non-claim rule

This closeout does not prove:

- release readiness;
- production tenant readiness;
- FTOV start;
- FTOV exit;
- real tenant validation;
- real day-to-day operational validation.

The project remains in:

**Project Stabilization / First-Adopter-Gated Hold**


## Operational Workspace Tree-and-Detail UX Closeout Clarification

Operational Workspace Tree-and-Detail UX is accepted as implemented after frontend-only feedback preservation correction.

This was a frontend-only information architecture / layout restructuring over the accepted Operational Workspace UI surface.

### Delivery interpretation

The implementation is accepted as:

**frontend-only tree-and-detail UX restructuring over existing Operational Workspace UI surfaces.**

The delivery replaced the long-scroll primary orientation model with:

- collapsed hierarchical left navigation;
- selected-object right detail pane;
- contextual asset tabs;
- separated Setup/Admin area.

### Implemented navigation model

Accepted navigation model:

- `Оргструктура -> Department -> Users -> assigned assets`;
- `Локации -> Branch -> Location -> assets at location`;
- `Все активы -> Назначенные / Неназначенные / Retired`;
- `Настройка -> Роли / Пользователи / Подразделения / Филиалы / Локации`.

The previous flat left-pane model is not the accepted primary navigation model.

### Implemented right-pane model

When an asset is selected, the right pane provides:

- Overview;
- Responsibility;
- Actions;
- History / Audit;
- Technical details.

Dedicated responsibility flows are preserved:

- assign;
- transfer;
- return.

Generic edit remains master-record-only.

### Feedback preservation correction

Post-implementation live review confirmed the tree-and-detail structure, but initially found that v3.1 feedback notifications could be missed after right-pane render / auto-navigation.

A small frontend-only correction preserved feedback inside the tree-and-detail layout.

Controlled synthetic / internal re-check confirmed:

- setup save feedback is visible;
- role assignment feedback is visible;
- assign feedback is visible in the asset card context;
- transfer feedback is visible in the asset card context;
- return feedback is visible in the asset card context.

Asset action feedback may be contextual in the right pane rather than always duplicated as a global banner.

### Boundary rule

This closeout does not open:

- backend/API changes;
- new endpoints;
- DTO / read-model changes;
- database / migration changes;
- BFF / UI backend;
- auth/IAM expansion;
- new role model;
- new bounded context;
- new Wave 2 surface;
- new frontend framework / router / microfrontend;
- server-computed tree;
- persistent saved tree state;
- saved views / persistent preferences;
- global search / Elasticsearch;
- advanced search/filtering as product feature;
- bulk actions;
- barcode / QR / scanning / printer integration;
- import/export engine;
- dashboard / BI / analytics;
- Asset 360;
- attachments / documents / photos / acts;
- full software/license desktop-like tabs;
- warehouse expansion;
- maintenance expansion;
- ticket/helpdesk expansion;
- Hardware Inspector parity;
- desktop app clone.

### Remaining minor friction

Remaining deferred candidates:

- full assigned-role visibility remains deferred unless already available from existing frontend data;
- further tuning of notification lifetime / placement may be considered later;
- broader Hardware Inspector-like feature parity remains separate product expansion.

### Non-claim rule

This closeout does not prove:

- release readiness;
- production tenant readiness;
- FTOV start;
- FTOV exit;
- real tenant validation;
- real day-to-day operational validation.

The project remains in:

**Project Stabilization / First-Adopter-Gated Hold**

## Operational Workspace Tree-and-Detail UX Density Reduction Closeout Clarification

Operational Workspace Tree-and-Detail UX Density Reduction is accepted as implemented after controlled synthetic / internal live re-check and targeted Setup navigation verification.

This was a small frontend-only density reduction and progressive disclosure hardening pass inside the already accepted Tree-and-Detail Operational Workspace UI.

### Delivery interpretation

The implementation is accepted as:

**frontend-only density reduction and progressive disclosure hardening over the accepted Tree-and-Detail UI.**

The delivery reduced visible density without changing backend/API behavior or product scope.

### Implemented hardening

The hardening addressed:

- left tree scan/click usability;
- collapsed / explicit expansion behavior;
- local client-side counts as navigation hints;
- first-step guidance in Workspace / Overview;
- form progressive disclosure;
- support/help copy moved into disclosures;
- History / Audit summary-first display;
- raw audit / correlation / service details collapsed by default;
- Technical details kept accessible but deeper by default;
- technical IDs remaining copyable but less dominant;
- Setup/Admin remaining separate from daily asset operations.

### Local tree filter decision

A tiny local-only visible-tree filter was implemented.

This filter:

- filters only already rendered/open tree rows in the browser;
- does not call backend;
- does not expand hidden branches;
- is not saved or persisted;
- is not product search;
- is not global search;
- is not advanced filtering.

### Live review result

Controlled synthetic / internal live re-check confirmed:

- Tree-and-Detail layout remains active;
- old long-scroll layout did not return;
- tree groups are not fully expanded by default;
- tree rows are easier to click;
- selected row is clearer;
- counts are helpful but not reporting-like;
- forms are less visually heavy;
- primary / optional disclosure works;
- technical IDs are de-emphasized but accessible;
- History / Audit is summary-first;
- raw audit details are collapsed but accessible;
- daily-user first-step guidance improved;
- Setup/Admin remains separate;
- Assign / Transfer / Return remain discoverable;
- feedback remains visible.

A targeted Setup navigation check confirmed:

- `Настройка` is visible in the left tree;
- setup sub-items appear immediately after expansion;
- `Роли`, `Пользователи`, `Подразделения`, `Филиалы`, `Локации` route to the correct right-pane setup surfaces;
- reload is not required.

### Remaining minor friction

Remaining deferred minor UX candidate:

- Setup group collapse affordance may be tuned later if future reviews show friction.

This is not a blocker for Density Reduction acceptance.

### Boundary rule

This closeout does not open:

- backend/API changes;
- new endpoints;
- DTO / read-model changes;
- database / migration changes;
- BFF / UI backend;
- auth/IAM expansion;
- new role model;
- new bounded context;
- new Wave 2 surface;
- new frontend framework / router / microfrontend;
- server-computed tree;
- backend-side grouping;
- backend-side search/filtering;
- persistent saved tree state;
- saved views / persistent preferences;
- global search / Elasticsearch;
- advanced search/filtering as product feature;
- bulk actions;
- barcode / QR / scanning / printer integration;
- import/export engine;
- dashboard / BI / analytics;
- Asset 360;
- attachments / documents / photos / acts;
- full software/license desktop-like tabs;
- warehouse expansion;
- maintenance expansion;
- ticket/helpdesk expansion;
- Hardware Inspector parity;
- desktop app clone.

### Non-claim rule

This closeout does not prove:

- release readiness;
- production tenant readiness;
- FTOV start;
- FTOV exit;
- real tenant validation;
- real day-to-day operational validation.

The project remains in:

**Project Stabilization / First-Adopter-Gated Hold**

## Operational Workspace Tree-and-Detail UX Small Frontend Cleanup Closeout Clarification

Operational Workspace Tree-and-Detail UX Small Frontend Cleanup is accepted as implemented after controlled synthetic / internal live re-check.

This was a small frontend-only cleanup pass inside the already accepted Tree-and-Detail UI after Density Reduction.

### Delivery interpretation

The implementation is accepted as:

**small frontend-only cleanup over the accepted Tree-and-Detail UI.**

The delivery reduced remaining UI noise without changing backend/API behavior or product scope.

### Implemented cleanup

The cleanup addressed:

- Setup/Admin duplicate navigation reduction;
- setup order guidance changed into a concise hint;
- setup tabs/tree retained as primary navigation;
- service IDs moved out of primary setup table columns;
- selected record service IDs kept available through technical disclosure and copy affordance;
- text `Копировать` controls replaced with compact accessible clipboard icon buttons where appropriate;
- compact connection status badge added from existing frontend state;
- local tree filter copy clarified as local/open-tree only;
- Assign / Transfer / Return made visually primary;
- master-record edit made visually secondary while remaining master-record-only;
- tree count wording improved;
- hover/focus states improved;
- selected tree row emphasis improved;
- narrow-width wrapping improved.

### Conditional decisions

The implementation intentionally did not add:

- full user role column/chips, because current frontend state does not contain complete user -> role mapping;
- client-side sorting;
- client-side pagination;
- broad feedback/toast rewrite, because no concrete current feedback defect was found.

Existing sticky/contextual feedback behavior was preserved.

### Live review result

Controlled synthetic / internal live re-check confirmed:

- Tree-and-Detail layout remains active;
- old long-scroll layout did not return;
- connection status badge is visible;
- local tree filter is clearly local-only;
- Setup/Admin duplication is reduced;
- setup sections remain accessible;
- service IDs are removed from primary table noise;
- IDs remain accessible and copyable;
- clipboard icon copy controls are usable;
- Actions tab hierarchy is clearer;
- Assign / Transfer / Return remain dedicated;
- master-record edit remains secondary;
- zero-count wording is clearer;
- hover/focus states are improved;
- responsive setup/open-section layout is acceptable;
- feedback remains visible;
- no forbidden product scope appeared.

### Remaining minor friction

Remaining deferred minor UX candidates:

- disclosure arrow affordance may be made more obvious later;
- copy icon discoverability may be improved later if reviews show friction;
- left-tree scroll / page-scroll interaction may be tuned later;
- mixed RU/EN tab names may be addressed in future RU copy polish.

These are not blockers for Small Frontend Cleanup acceptance.

### Boundary rule

This closeout does not open:

- backend/API changes;
- new endpoints;
- DTO / read-model changes;
- database / migration changes;
- BFF / UI backend;
- auth/IAM expansion;
- new role model;
- new bounded context;
- new Wave 2 surface;
- global search;
- backend search;
- advanced search/filtering;
- Elasticsearch;
- saved views / preferences;
- server-side pagination;
- server-side sorting;
- server-generated tree;
- import/export;
- barcode / QR / scanning / printer integration;
- dashboard / BI / analytics;
- Asset 360;
- attachments / documents / photos / acts;
- warehouse expansion;
- maintenance expansion;
- ticket/helpdesk expansion;
- Hardware Inspector parity;
- desktop app clone.

### Non-claim rule

This closeout does not prove:

- release readiness;
- production tenant readiness;
- FTOV start;
- FTOV exit;
- real tenant validation;
- real day-to-day operational validation.

The project remains in:

**Project Stabilization / First-Adopter-Gated Hold**

## Asset Identity v1 Canon Alignment Clarification

Asset Identity v1 is accepted as a narrow Asset master-record canon extension intended to improve Asset recognizability and creation/edit clarity.

Approved fields:

- `display_name`
- `note`

Delivery interpretation:

- this is not a new bounded context;
- this is not a new Wave 2 surface;
- this is not QR/barcode/label implementation;
- this is not import/export;
- this is not Asset 360;
- this is not reporting/dashboard;
- this is not release readiness or FTOV evidence.

Implementation is not authorized by this clarification.

Backend/API/DB implementation planning may proceed separately after docs alignment is accepted.

## Asset Identity v1 Backend/API/DB Implementation Closeout Clarification

Asset Identity v1 backend/API/DB implementation is accepted as completed.

The implementation delivered the narrow Asset master-record extension approved by canon alignment:

- `display_name`
- `note`

### Delivery interpretation

This implementation is accepted as:

**bounded backend/API/DB implementation for Asset Identity v1 fields only.**

Implemented scope:

- Flyway migration added nullable `display_name` and nullable `note` to `assets`;
- Asset domain/model mapping was extended;
- repository insert/update/read mapping was extended;
- create/update/read API DTOs were extended;
- validation and normalization were added;
- existing asset create/update audit payloads include the new identity fields;
- tests passed for Asset API, schema constraints and the full asset-service test suite.

### Preserved boundaries

The implementation preserved:

- `asset_tag` as canonical inventory/business identifier;
- Assign as dedicated flow;
- Transfer as dedicated flow;
- Return as dedicated flow;
- Retire as dedicated flow where currently present;
- generic edit as master-record-only;
- `assigned_user_id` and `location_id` as not updated through generic PATCH;
- status transition as not performed through ordinary PATCH.

### Not opened

This implementation did not open:

- frontend/static UI changes;
- QR/barcode/label implementation;
- import/export;
- global search;
- backend search;
- Asset 360;
- reporting/dashboard;
- manufacturer/model/cost/purchase/warranty/condition fields;
- attachments/documents/photos/acts;
- auth/IAM expansion;
- new bounded context;
- new Wave 2 surface;
- Hardware Inspector parity.

### Non-claim rule

This closeout does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

The project remains in:

**Project Stabilization / First-Adopter-Gated Hold**


## Asset Identity v1 Frontend Usage Implementation Closeout Clarification

Asset Identity v1 frontend usage is accepted as completed inside the existing Operational Workspace UI.

The implementation consumed the already implemented backend/API fields:

- `display_name`
- `note`

### Delivery interpretation

This implementation is accepted as:

**bounded frontend-only usage of Asset Identity v1 fields inside the existing Operational Workspace Tree-and-Detail UI.**

Implemented scope:

- create Asset form now exposes optional `display_name` and `note`;
- master-record edit form now exposes optional `display_name` and `note`;
- create/edit payloads include `display_name` and `note`;
- Asset title rendering uses a centralized fallback:
  - `display_name`;
  - `asset_tag`;
  - `asset_type` / `serial_number`;
  - shortened Asset ID only as last resort;
- `asset_tag` remains visible as canonical inventory/business identifier;
- `note` is displayed only as optional plain-text operational context in selected Asset detail / Overview;
- static UI smoke coverage was updated.

### Preserved boundaries

The implementation preserved:

- Assign as dedicated flow;
- Transfer as dedicated flow;
- Return as dedicated flow;
- Retire as dedicated flow where currently present;
- generic edit as master-record-only;
- `assigned_user_id` as not edited through generic edit;
- `location_id` as not edited through generic edit where current boundary forbids it;
- status transition as not performed through ordinary PATCH.

### Not opened

This implementation did not open:

- backend/API changes;
- DB changes;
- migrations;
- new endpoints;
- Documentation update as part of Codex implementation;
- BFF/UI backend;
- new frontend framework/router/microfrontend;
- unified `Создать` launcher;
- quick edit redesign;
- context connect screen redesign;
- QR/barcode/label implementation;
- import/export;
- global/backend search;
- Asset 360;
- reporting/dashboard;
- manufacturer/model/cost/purchase/warranty/condition fields;
- attachments/documents/photos/acts;
- rich text note editor;
- note thread/history stream;
- auth/IAM expansion;
- new bounded context;
- new Wave 2 surface;
- Hardware Inspector parity.

### Non-claim rule

This closeout does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

The project remains in:

**Project Stabilization / First-Adopter-Gated Hold**


## Owner-Approved Future Planning Candidates After Asset Identity v1

After Asset Identity v1 backend/API/DB and frontend usage closeout, the owner approved several future planning candidates.

These candidates are approved for product decision / planning only.

They do not authorize implementation, Codex, backend/API changes, DB changes, frontend changes, Documentation canon changes, release readiness, FTOV start, FTOV exit, production readiness or real tenant validation.

Approved future planning candidates:

1. Unified Create Launcher
   - one `Создать` entry point;
   - planned entries:
     - Asset / device;
     - User;
     - Department;
     - Branch;
     - Location;
   - must use existing creation semantics where possible;
   - must not become a new master-data module by default.

2. Simplified Asset Actions UX
   - simplify selected-asset action orientation;
   - preserve Assign / Transfer / Return as dedicated flows;
   - keep generic edit master-record-only.

3. Delete / Archive Semantics
   - define safe delete / archive / correction-delete behavior;
   - hard delete is not approved by default;
   - audit/history preservation must remain protected.

4. End-User Asset Information View
   - simple read-only information view for employee/device ownership;
   - must be access-controlled;
   - must not expose other employees’ assets based only on full name without separate approval.

5. Search v1
   - search by employee / asset tag / display name / serial number;
   - not the same as local tree filter;
   - requires separate backend/API decision if server-side search is needed.

6. QR Lite / Derived Asset Identifier
   - derived QR for asset identification / information sheet;
   - no scanner/printer/barcode platform by default.

7. Asset Type-Specific Technical Attributes
   - PC fields such as AnyDesk ID / hostname / IP / MAC;
   - phone fields such as IP / extension;
   - printer fields such as IP / network name;
   - requires separate schema/API/UI decision.

8. Configuration Manager-inspired analysis candidates
   - useful ideas may inform future planning;
   - no endpoint agent, auto-discovery, remote control, deployment, patch management or parity scope is approved.

Recommended next product decision:

`Operational Workspace / Unified Create Launcher Direction Gate`

Non-claim rule:

This planning-candidate update does not prove release readiness, FTOV start, FTOV exit, production readiness, real tenant validation or real day-to-day operational validation.

The project remains in:

**Project Stabilization / First-Adopter-Gated Hold**

## Unified Create Launcher Implementation Closeout Clarification

Operational Workspace / Unified Create Launcher is accepted as implemented.

### Delivery interpretation

This implementation is accepted as:

**bounded frontend-only UX/navigation shortcut over existing Operational Workspace creation surfaces.**

Implemented scope:

- one compact `Создать` launcher was added;
- launcher entries are:
  - `Актив / устройство`;
  - `Пользователь`;
  - `Подразделение`;
  - `Филиал`;
  - `Локация`;
- each entry routes or focuses an existing create surface;
- asset creation reuses the existing asset create form;
- user, department, branch and location creation reuse existing Setup/Admin create states;
- static UI smoke coverage was updated.

### Preserved boundaries

The implementation preserved:

- Assign as dedicated flow;
- Transfer as dedicated flow;
- Return as dedicated flow;
- Retire as dedicated flow where currently present;
- generic edit as master-record-only;
- asset create as not assign;
- asset create as not create-and-assign;
- Setup/Admin ownership for reference data.

### Not opened

This implementation did not open:

- backend/API changes;
- DB changes;
- migrations;
- new endpoints;
- DTO/read-model changes;
- BFF/UI backend;
- auth/IAM expansion;
- new role model;
- new permission model;
- new entity family;
- new master-data module;
- quick edit redesign;
- Delete / Archive implementation;
- End-User Asset Information View;
- Search v1 implementation;
- QR/barcode implementation;
- import/export;
- Asset 360;
- reporting/dashboard;
- Hardware Inspector parity.

### Non-claim rule

This closeout does not prove:

- release readiness;
- FTOV start;
- FTOV exit;
- production readiness;
- real tenant validation;
- real day-to-day operational validation.

The project remains in:

**Project Stabilization / First-Adopter-Gated Hold**