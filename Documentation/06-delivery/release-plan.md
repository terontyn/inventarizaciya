# Release Plan

Status: approved
Owner: product
Last Updated: 2026-04-07

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