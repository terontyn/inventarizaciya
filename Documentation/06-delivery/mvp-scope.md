# MVP Scope

Status: approved
Owner: product
Last Updated: 2026-04-07

## MVP Name
Asset Accountability MVP

## Goal
Первый релиз должен отвечать без Excel на вопрос:
что у нас есть, где это находится, за кем закреплено и кто это менял.

## In Scope
- tenant-aware foundation для данных и доступа;
- users, roles, departments, branches, locations;
- реестр активов;
- карточка актива с текущим владельцем / локацией / статусом;
- assign / transfer / return;
- audit и история критических действий;
- минимально необходимый admin layer для поддержки этого контура.

## Pilot Readiness Interpretation

Pilot Readiness в рамках текущего MVP означает не новый модуль, а доведение уже утвержденного контура до operational use одним tenant.

Minimal admin layer для MVP включает только:
- tenant admin management of users / roles / departments / branches / locations;
- operator execution of asset CRUD v1 + assign / transfer / return;
- reviewer read-only visibility of registry / card / history / audit.

Pilot Readiness не расширяет scope в:
- inventory / warehouse;
- software licenses / SAM;
- maintenance & repair;
- reporting module;
- heavy BI;
- expanded integrations;
- custom workflows.

Наличие `retirement` в доменном каноне не делает его автоматически обязательным pilot gate, пока он не входит в explicit MVP loop.

## Pilot Reference-Data Setup Interpretation

Для первого pilot tenant minimum reference-data ownership / setup model ограничен текущим MVP contour и не вводит новый master-data module.

### Minimum tenant-managed areas
- roles
- users
- departments
- branches
- locations

### Ownership
- Tenant Admin владеет созданием и поддержкой roles / users / departments / branches / locations;
- Asset Operator не управляет reference data и использует только разрешенный reference read-only access;
- Support Reviewer остается read-only.

### Setup order before pilot start
1. roles
2. pilot users with role assignment
3. branches
4. locations
5. departments

### Clarifications
- `departments` входят в minimum tenant-managed reference-data set для MVP contour, но не являются hard precondition для каждого первого flow;
- отдельные asset-side reference catalogs не являются обязательной частью pilot setup model;
- exact Asset status vocabulary уже фиксирован каноном (`ACTIVE`, `RETIRED`) и не является tenant-managed reference dictionary.

## Out of Scope
- ticket / service desk;
- inventory / warehouse;
- software licenses / SAM;
- maintenance & repair;
- reporting как отдельный модуль;
- сложная аналитика и heavy BI;
- расширенные интеграции;
- кастомные workflow / automation features.

## Foundation Slice vs MVP
Foundation Slice — минимальный вертикальный путь.
MVP 6–8 недель — тот же путь, доведенный до pilot-ready состояния.

## Value
Один tenant должен перестать вести базовый учет техники в таблицах
и начать видеть:
- кто чем пользуется;
- где находится актив;
- кто и когда менял критичные данные.


## Printable Asset Label v1

В рамках MVP / Pilot допускается minimal printable asset label v1
как operational convenience поверх asset registry / asset card.

### Scope rule
- это не отдельный модуль;
- это не новый inventory contour;
- это не расширяет scope в barcode / QR / scanning / printer integration / bulk printing.

### Data rule
Label v1 использует только уже существующие Asset master fields:
- `asset_type`
- `asset_tag`
- `serial_number`

Для MVP / Pilot `asset_tag` используется как current printable inventory identifier.

## Printable Label v1 UI Anchor Clarification

Printable Asset Label v1 остается допустимым minimal operational convenience
внутри текущего MVP contour.

Однако practical implementation label v1 предполагает наличие existing asset detail / card UI anchor.

### Current clarification
- если asset detail / card UI anchor в текущем delivery state отсутствует,
  Printable Asset Label v1 остается docs-approved but implementation-deferred;
- это не отменяет самого product decision по label v1;
- это также не превращает label v1 в mandatory pilot gate.

### UI slice note
Если позже отдельно открывается minimal asset detail / card UI slice,
это трактуется как completion already-approved asset card scope,
а не как новый модуль или новый contour.


## Minimal Pilot UI Contour

Minimal Pilot UI Contour входит в completion path уже утвержденного MVP
и не считается новым модулем.

### Purpose
Довести уже утвержденный Asset Accountability MVP
до practically usable tenant-facing operational state.

### Mandatory surfaces
- asset registry UI
- asset detail / card UI
- create / update asset UI
- assign / transfer / return UI entry points
- history / audit read UI
- minimal admin / reference-data UI for:
  - roles
  - users
  - departments
  - branches
  - locations
- UI-level role-based visibility aligned with approved MVP personas

### Boundary rule
Этот contour:
- не открывает inventory / warehouse;
- не открывает reporting module;
- не открывает barcode / QR / scanning / printer integration / bulk print;
- не вводит новый auth platform;
- не меняет domain/API boundaries уже утвержденного MVP.


## Minimal Pilot Access Contour

Minimal Pilot Access Contour входит в completion path уже утвержденного MVP
и не считается новым модулем.

### Purpose
Дать реальному первому tenant минимально достаточный user-facing access path
в уже утвержденный MVP contour.

### Minimum cutline
- minimal login / access entry path
- minimal first-tenant access / bootstrap path after initial tenant creation
- tenant-scoped access for approved MVP personas:
  - Tenant Admin
  - Asset Operator
  - Support Reviewer
- role-aligned visibility into already approved UI surfaces

### Boundary rule
Этот contour:
- не открывает новый auth platform;
- не открывает SSO / federation / MFA / SCIM / IdP integration scope;
- не меняет существующие bounded contexts;
- не открывает inventory / warehouse / reporting / printing scope.