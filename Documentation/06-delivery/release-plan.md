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