# Asset API

Status: draft
Owner: architecture
Last Updated: 2026-05-19

## Purpose
Минимальный API-контракт Foundation Slice для asset-service.

## Scope
Этот документ покрывает только Asset CRUD v1 для мастер-записи `Asset`.

## API Principles
1. Все API versioned.
2. asset-service владеет bounded context Asset Management.
3. tenant isolation обязателен.
4. Tenant определяется на API boundary из аутентифицированного контекста.
5. Сервис не принимает tenant как свободный бизнес-параметр.
6. Прямой доступ к БД user-service запрещен.
7. Валидация `assigned_user_id` и `location_id`, если они присутствуют в create-path, выполняется через API user-service.
8. Critical writes должны попадать в audit по принятому audit contract.
9. Generic CRUD v1 не должен ломать доменный инвариант истории.

## Service Ownership
asset-service в Foundation Slice v1 владеет:
- Asset

Следующие responsibility-changing operations существуют как отдельные command flows и не являются частью generic CRUD v1:
- assign
- transfer
- return
- retire

## Endpoints v1
- `POST /api/v1/assets`
- `GET /api/v1/assets`
- `GET /api/v1/assets/{id}`
- `PATCH /api/v1/assets/{id}`

## Responsibility Flow Command Layer (candidate for next slice)

Этот раздел фиксирует только минимальный command contract layer,
достаточный для следующего design / implementation шага.
Он не канонизирует полный public API surface, полный error model или richer payload schema.

### 1. Assign

#### Candidate endpoint
- `POST /api/v1/assets/{id}/assign`

#### Command intent
- установить текущую пользовательскую ответственность за Asset

#### Request minimum
- `target_user_id`

#### Response minimum
- `id`
- `assigned_user_id`

#### Success outcome
- создан append-only факт assignment;
- текущее состояние Asset отражает новую текущую пользовательскую ответственность;
- операция считается critical write и должна попадать в audit.

#### Not fixed yet
- должен ли assignment одновременно фиксировать операционную локацию;
- exact eligibility rules for assignment;
- richer request fields;
- richer response schema.

### 2. Transfer

#### Candidate endpoint
- `POST /api/v1/assets/{id}/transfer`

#### Command intent
- изменить текущую пользовательскую ответственность и/или текущую операционную локацию как одно непрерывное доменное изменение

#### Request minimum
- как минимум одно из:
  - `target_user_id`
  - `target_location_id`

#### Response minimum
- `id`
- `assigned_user_id`
- `location_id`

#### Success outcome
- произошло effective change хотя бы по одной из двух доменных осей;
- создан append-only факт transfer;
- текущее состояние Asset отражает новое текущее состояние;
- операция считается critical write и должна попадать в audit.

#### Boundary rule
- если у Asset нет текущей пользовательской ответственности, создание новой текущей пользовательской ответственности относится к `assign`, а не к `transfer`;
- `transfer` без текущего assigned user допустим только для location-only scenario.

#### Not fixed yet
- отдельные top-level endpoints для user-only / location-only / combined variants;
- richer eligibility rules;
- richer request fields;
- richer response schema.

### 3. Return

#### Candidate endpoint
- `POST /api/v1/assets/{id}/return`

#### Command intent
- завершить текущую пользовательскую ответственность без назначения нового пользователя в том же flow

#### Request minimum
- body minimum не фиксируется; asset identity в path достаточно для минимального command contract

#### Response minimum
- `id`

#### Success outcome
- создан append-only факт return;
- у Asset больше нет активной текущей пользовательской ответственности;
- операция считается critical write и должна попадать в audit.

#### Boundary rule
- `return` не канонизирует изменение `location_id`;
- `return` не требует `target location`.

#### Not fixed yet
- exact response representation отсутствия активной текущей ответственности;
- richer request fields;
- richer response schema.

### 4. Retire

#### Candidate endpoint
- `POST /api/v1/assets/{id}/retire`

#### Command intent
- вывести Asset из эксплуатации с сохранением audit и истории

#### Request minimum
- body minimum не фиксируется; asset identity в path достаточно для минимального command contract

#### Response minimum
- `id`

#### Success outcome
- создан append-only факт retirement;
- operational use Asset завершен;
- если существовала текущая активная пользовательская ответственность, после retirement она больше не считается активной;
- операция считается critical write и должна попадать в audit.

#### Boundary rule
- `retire` не является delete workflow;
- предварительный `return` перед `retire` не является обязательным precondition.

#### Not fixed yet
- eligibility rules for retirement;
- richer request fields;
- richer response schema.

## Exact Status Vocabulary (Pilot / MVP)

Для MVP / Pilot Readiness используется следующий exact status vocabulary:
- `ACTIVE`
- `RETIRED`

### Status Rules by Flow
- `POST /api/v1/assets` -> creates Asset with `status = ACTIVE`
- `PATCH /api/v1/assets/{id}` -> не используется как status-transition flow
- `POST /api/v1/assets/{id}/assign` -> status unchanged
- `POST /api/v1/assets/{id}/transfer` -> status unchanged
- `POST /api/v1/assets/{id}/return` -> post-return status = `ACTIVE`
- `POST /api/v1/assets/{id}/retire` -> post-retirement status = `RETIRED`

### Response / Read Model Semantics
- current status на asset card / read models должен отображаться именно как `Asset.status`;
- current assignee и current location должны отображаться отдельно от `status`;
- после `return` response / read model должны показывать отсутствие активной текущей пользовательской ответственности при сохранении `status = ACTIVE`;
- после `retire` response / read model должны показывать `status = RETIRED`.

## Write Operations Requiring Audit
Следующие write-операции считаются критичными и должны попадать в audit:
- create asset
- update asset
- assign asset
- transfer asset
- return asset
- retire asset

## Restrictions
- `DELETE /api/v1/assets/{id}` не включается в v1;
- generic PATCH не используется для responsibility-changing operations;
- `assigned_user_id` и `location_id` не меняются через generic PATCH.

## Not Fixed In This Document
Намеренно не фиксируются:
- full request body schema;
- full response body schema;
- error model;
- pagination/filter parameter names;
- delete semantics;
- final history storage schema;
- отдельная canonical entity/table `AssetReturn`;
- event contract;
- audit payload format beyond already accepted minimal audit envelope.

## Out of Scope
- delete workflow
- bulk command APIs
- attachment API
- barcode / QR workflows
- separate top-level command resources beyond asset-scoped command layer

## Open Questions
- eligibility rules for `assignment / return / retirement`;
- final physical history storage schema;
- нужно ли later расширять command responses beyond current minimum.


## Printable Label v1 Read Clarification

Printable asset label v1 не вводит отдельный public API endpoint.

### Canonical read source
Для one-by-one manual print path каноническим read source считается existing detail read surface:
- `GET /api/v1/assets/{id}`

### Minimum read contract for label v1
Printable label v1 использует только следующие уже существующие Asset master fields:
- `asset_type`
- `asset_tag`
- `serial_number`

### Human-readable top line
Для v1 верхняя human-readable строка label фиксируется как:
- `asset_type`

Derived display representation допускается только как presentation-level derivation
из уже существующего asset presentation, без нового persistent field
и без нового canonical API field.

### Boundary rules
- printable label v1 не требует dedicated `/label` или `/print` endpoint;
- printable label v1 не требует label-specific read model;
- `GET /api/v1/assets` не фиксируется как канонический label contract;
- label v1 не требует `status`, `assigned_user_id`, `location_id`, history или audit
  как обязательной части printable payload;
- label v1 не открывает scope в bulk print, printer integration, barcode / QR или scanning workflows.

## Asset Identity v1 API Extension

Asset Identity v1 adds two optional Asset master-record fields to the existing Asset CRUD surfaces:

- `display_name`
- `note`

### Create

`POST /api/v1/assets` may accept:

- `display_name`
- `note`

Rules:

- both fields are optional;
- omitted values are allowed;
- `null` is allowed;
- blank / whitespace-only values may normalize to `null`;
- existing required fields remain unchanged.

### Update

`PATCH /api/v1/assets/{id}` may update:

- `display_name`
- `note`

as ordinary Asset master-record fields.

This does not change the responsibility-flow boundary.

Generic PATCH must still not be used for:

- `assigned_user_id`;
- `location_id` where current responsibility/location boundary forbids it;
- responsibility state;
- status transition.

If true partial PATCH semantics are desired beyond these fields, that requires a separate API semantics decision.

### Read

Asset read surfaces should include:

- `display_name`
- `note`

Compatibility rules:

- existing assets may return `null`;
- existing clients may omit these fields in create/update requests;
- `display_name` can be used by UI as a human-readable title;
- `note` should be treated as optional plain text and may be detail-first if future API separates list/detail shapes.

### Validation

Recommended validation:

- `display_name`: optional, nullable, max length `160`;
- `note`: optional, nullable, plain text, max length `2000`.

### Audit

Changes to `display_name` and `note` are Asset master-record updates and should be covered by existing update-asset audit behavior.

No new audit event type is required by this API extension.