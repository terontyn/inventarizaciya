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