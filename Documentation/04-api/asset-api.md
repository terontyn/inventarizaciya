# Asset API

Status: draft
Owner: architecture
Last Updated: 2026-05-25

## Purpose
Минимальный API-контракт Foundation Slice для asset-service.

## Scope
Этот документ покрывает Asset CRUD v1 для мастер-записи `Asset`, responsibility-flow command layer и принятые bounded API extensions внутри asset-service.

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

Core Asset endpoints:

- `POST /api/v1/assets`
- `GET /api/v1/assets`
- `GET /api/v1/assets/{id}`
- `PATCH /api/v1/assets/{id}`

Accepted bounded extension endpoints:

- `GET /api/v1/assets/technical-attribute-search`
- `GET /api/v1/assets/employee-sheet-technical-hints`

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

## Delete / Archive Semantics

Asset delete semantics are governed by ADR-0005.

Canonical API rules:

- `DELETE /api/v1/assets/{id}` remains unsupported.
- Public hard delete is rejected for the current stage.
- Generic `PATCH /api/v1/assets/{id}` must not perform delete, archive, retire or status-transition behavior.
- Retire, if product-exposed later, remains a dedicated command flow and not ordinary PATCH.
- Existing backend `/retire` route presence does not mean current product exposure.
- Existing `/retire` remains blocked / not product-exposed until separately approved for exposure.
- Archive / Hide endpoints are not approved.
- Correction Delete endpoint is not approved.
- Hard Delete endpoint is not approved.

Any future exposure of Retire, Archive / Hide or Correction Delete requires separate planning, access rules, audit semantics and tests.

## Not Fixed In This Document
Намеренно не фиксируются:
- full request body schema;
- full response body schema;
- error model;
- pagination/filter parameter names;
- Archive / Hide endpoint semantics;
- Correction Delete endpoint semantics;
- Retire exposure / access rules;
- final history storage schema;
- отдельная canonical entity/table `AssetReturn`;
- event contract;
- audit payload format beyond already accepted minimal audit envelope.

## Out of Scope
- hard delete workflow
- Archive / Hide workflow
- Correction Delete workflow
- Retire exposure unless separately approved
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

## Technical Attribute Search API Extension

This accepted API extension provides bounded tenant-scoped Technical Search for approved asset technical attributes.

### Endpoint

```http
GET /api/v1/assets/technical-attribute-search
```

### Purpose

Search managed workspace assets/devices by approved technical attributes.

This endpoint is separate from Quick Find, Employee Sheet, Asset Overview and global search.

### Request

Supported query parameters:

| Parameter | Required | Description |
|---|---:|---|
| `q` | yes | Search query. |
| `limit` | no | Bounded result limit. |

Unsupported query parameters are rejected.

Tenant id is derived from authenticated workspace context and is not accepted as a request parameter.

### Response behavior

The endpoint returns asset/device results only.

Each result may include matched technical field metadata:

- `key`;
- `label`;
- `display_value`;
- `sensitive`;
- `masked`.

Sensitive values are returned masked.

Frontend consumers must not unmask values.

### Searchable technical keys

Searchable keys:

- `hostname`;
- `anydesk_id`;
- `ip_address`;
- `mac_address`;
- `phone_ip`;
- `extension_number`;
- `sip_line_id`;
- `printer_ip`;
- `printer_network_name`;
- `printer_placement_note`.

### Explicitly excluded fields

Excluded by default:

- asset `note`;
- audit/history;
- QR payload;
- arbitrary unsupported/generic fields;
- tenant-defined custom keys;
- user-service data;
- reporting-service data.

### printer_placement_note search behavior

`printer_placement_note` is searchable only through the Technical Search endpoint.

It is treated as sensitive/masked in Technical Search result rows because it is free-text placement context and may contain physical, person-adjacent, security or operational details.

For matches on `printer_placement_note`, `matched_field` returns:

- `key`: `printer_placement_note`;
- `label`: `Примечание по размещению`;
- `sensitive`: `true`;
- `masked`: `true`;
- `display_value`: masked value only.

The full raw `printer_placement_note` value must not be exposed in Technical Search result rows.

This does not change the endpoint response shape, authorization model, tenant scoping or supported query parameters.

This does not add `printer_placement_note` to:

- Quick Find;
- Employee Sheet;
- Asset Overview;
- public lookup;
- global search.

### Boundary rules

This endpoint does not change:

- `GET /api/v1/assets`;
- `GET /api/v1/assets/{id}`;
- `PATCH /api/v1/assets/{id}`;
- `PUT /api/v1/assets/{id}/technical-attributes`;
- `GET /api/v1/assets/employee-sheet-technical-hints`;
- Assign / Transfer / Return;
- QR Lite;
- Asset Overview;
- Quick Find.

No audit event is emitted because this is read-only search.

## Employee Sheet Technical Hints API Extension

This accepted API extension adds a narrow read-only technical hints endpoint for the existing Employee Sheet.

It is intentionally narrower than full technical attributes, technical attribute search, Asset Overview, Asset 360 or employee self-service.

### Endpoint

```http
GET /api/v1/assets/employee-sheet-technical-hints?user_id=<userId>
```

### Purpose

Return low-sensitivity technical hints for assets assigned to the selected employee/user in the current tenant.

This endpoint supports the managed workspace `Информационный лист сотрудника`.

### Frontend consumption note

This endpoint is used by Employee Sheet frontend surfaces, including the standalone focused `Информационный лист` interface.

The standalone interface does not introduce a new API endpoint.

It reuses this endpoint only for the accepted low-sensitivity technical hints and remains loaded-state-only for employee search.

This does not change the endpoint contract, authorization model, response shape or excluded-field rules.

### Request

Query parameters:

| Parameter | Required | Description |
|---|---:|---|
| `user_id` | yes | Selected employee/user id. Must pass existing id/UUID validation style. |

Rules:

- `user_id` is required;
- blank `user_id` is rejected;
- malformed `user_id` is rejected;
- tenant id is derived from authenticated workspace context;
- tenant id is not accepted as request parameter;
- no full-name lookup is supported;
- no public lookup is supported;
- no user-service DB join is performed;
- no user-service existence check is performed in this accepted cut;
- unknown or no-match `user_id` returns an empty `assets` array.

### Authorization

Uses the existing asset read authorization boundary.

Expected access:

- Tenant Admin: allowed;
- Asset Operator: allowed;
- Support Reviewer: allowed read-only;
- ordinary Employee: not exposed as employee self-service;
- unauthenticated access: denied.

### Response

Example semantic response:

```json
{
  "user_id": "9a35f0d4-9e5c-4e3b-9f24-111111111111",
  "assets": [
    {
      "asset_id": "0a2f1f99-4d3d-4d4c-a1a8-222222222222",
      "asset_tag": "INV-001",
      "display_name": "Laptop Ivanov",
      "asset_type": "laptop",
      "serial_number": "SN-001",
      "status": "ACTIVE",
      "location_id": "64a78fb3-eeee-4444-a235-333333333333",
      "technical_hints": {
        "hostname": "host-01"
      }
    }
  ]
}
```

Top-level fields:

| Field | Description |
|---|---|
| `user_id` | Same selected employee/user id from the request. |
| `assets` | Assets assigned to this user inside the current tenant. |

Each asset row includes minimal asset identity/context:

| Field | Description |
|---|---|
| `asset_id` | Asset id. |
| `asset_tag` | Canonical inventory/business identifier. |
| `display_name` | Optional human-readable asset display name. |
| `asset_type` | Asset type. |
| `serial_number` | Serial number. |
| `status` | Asset status. |
| `location_id` | Current location id, if present. |
| `technical_hints` | Narrow map of allowed low-sensitivity hints. |

### Allowed technical hints

Only these `technical_hints` keys may be returned:

| Key | Employee Sheet label |
|---|---|
| `hostname` | Hostname |
| `extension_number` | Внутренний номер |
| `printer_network_name` | Сетевое имя принтера |

Missing, null, blank or whitespace-only hint values are omitted.

`technical_hints` may be an empty object when the assigned asset has no allowed hints.

### Explicitly excluded fields

This endpoint must not return:

- full `technical_attributes`;
- `profile`;
- `anydesk_id`;
- `ip_address`;
- `mac_address`;
- `phone_ip`;
- `sip_line_id`;
- `printer_ip`;
- `printer_placement_note`;
- `note`;
- QR payload;
- audit/history;
- action links;
- arbitrary unsupported/custom technical fields;
- masked sensitive technical values.

### Difference from Technical Attribute Search

This endpoint is narrower than:

```http
GET /api/v1/assets/technical-attribute-search
```

Technical Attribute Search is a search surface and may return matched-field metadata according to its own masking rules.

Employee Sheet technical hints are not search results. They are a read-only selected-employee sheet view and expose only the three accepted low-sensitivity hints without masked sensitive values.

### Boundary rules

This endpoint does not change:

- `GET /api/v1/assets`;
- `GET /api/v1/assets/{id}`;
- `PATCH /api/v1/assets/{id}`;
- `PUT /api/v1/assets/{id}/technical-attributes`;
- `GET /api/v1/assets/technical-attribute-search`;
- Assign / Transfer / Return;
- QR Lite;
- Asset Overview;
- Quick Find.

No audit event is emitted because this is read-only hint retrieval.