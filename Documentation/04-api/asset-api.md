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

Accepted asset lifecycle command endpoints:

- `POST /api/v1/assets/{id}/assign`
- `POST /api/v1/assets/{id}/transfer`
- `POST /api/v1/assets/{id}/return`
- `POST /api/v1/assets/{id}/retire`
- `POST /api/v1/assets/{id}/archive`
- `POST /api/v1/assets/{id}/unarchive`

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

#### Accepted endpoint
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

#### Accepted exposure semantics

Retire is product-exposed as a bounded dedicated lifecycle command.

Retire is not:

- delete;
- hard delete;
- archive / hide;
- correction delete;
- ordinary PATCH;
- generic status edit.

Accepted Retire behavior:

- `status` becomes `RETIRED`;
- current `assigned_user_id` is cleared;
- current `location_id` is preserved;
- one `RETIRE` responsibility fact is appended;
- one `RETIRE` audit event is emitted;
- Asset record remains readable;
- responsibility history remains preserved.

Repeated Retire behavior:

- already `RETIRED` assets cannot be retired again;
- repeated Retire must not append duplicate `RETIRE` responsibility facts;
- repeated Retire must not emit duplicate `RETIRE` audit events;
- repeated Retire is rejected through the existing project business-error / conflict style.

Authorization:

- Retire uses the existing asset mutate/lifecycle authorization cutline;
- read-only actors remain forbidden;
- no new role model is introduced by Retire exposure;
- no auth/IAM expansion is introduced by Retire exposure.

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
- archive asset
- unarchive asset

## Restrictions
- `DELETE /api/v1/assets/{id}` не включается в v1;
- generic PATCH не используется для responsibility-changing operations;
- `assigned_user_id` и `location_id` не меняются через generic PATCH.

## Delete / Archive Semantics

Asset delete and archive semantics are governed by ADR-0005.

Canonical API rules:

- `DELETE /api/v1/assets/{id}` remains unsupported.
- Public hard delete is rejected for the current stage.
- Generic `PATCH /api/v1/assets/{id}` must not perform delete, archive, retire or status-transition behavior.
- Retire remains a dedicated command flow and not ordinary PATCH.
- `POST /api/v1/assets/{id}/retire` is product-exposed as a bounded dedicated lifecycle command.
- `POST /api/v1/assets/{id}/archive` is product-exposed as a bounded persisted visibility command for already `RETIRED` assets only.
- Archive is not delete, hard delete, physical erase, Correction Delete, Retire, ordinary PATCH or generic status edit.
- Correction Delete endpoint is not approved.
- Hard Delete endpoint is not approved.
- `POST /api/v1/assets/{id}/unarchive` is product-exposed as a bounded visibility reversal command for archived `RETIRED` assets only.
- Unarchive is not restore-to-ACTIVE, reactivation, Retire rollback or assignment restoration.
- Bulk archive is not approved.

Unarchive is the accepted visibility reversal command for archived retired assets.

Unarchive clears `archived_at` while preserving `status = RETIRED`, responsibility history and audit evidence.

Retire remains the canonical safe lifecycle removal command.

Archive is the accepted visibility command for already retired assets.

Retire preserves the Asset record, audit evidence and responsibility history.

Archive preserves the Asset record, audit evidence and responsibility history while hiding already retired assets from default working lists.

Any future Correction Delete, bulk Retire, bulk Archive, restore-to-ACTIVE, bulk Unarchive or broader lifecycle recovery expansion requires separate planning, access rules, audit semantics and tests.


## Not Fixed In This Document
Намеренно не фиксируются:
- full request body schema;
- full response body schema;
- error model;
- pagination/filter parameter names;
- Correction Delete endpoint semantics;
- restore-to-ACTIVE endpoint semantics;
- bulk unarchive endpoint semantics;
- bulk archive endpoint semantics;
- final history storage schema;
- отдельная canonical entity/table `AssetReturn`;
- event contract;
- audit payload format beyond already accepted minimal audit envelope.

## Out of Scope
- hard delete workflow
- bulk archive workflow
- restore-to-ACTIVE workflow
- bulk unarchive workflow
- archive for ACTIVE assets
- Correction Delete workflow
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

### 5. Archive

#### Accepted endpoint

- `POST /api/v1/assets/{id}/archive`

#### Command intent

- hide an already retired Asset from default working lists while preserving the Asset record, responsibility history and audit evidence.

#### Request minimum

- body minimum не фиксируется; asset identity в path достаточно для минимального command contract.

#### Response minimum

- `id`

#### Accepted Archive semantics

Archive is product-exposed as a bounded persisted visibility command for already `RETIRED` assets only.

Archive is not:

- delete;
- hard delete;
- physical erase;
- Correction Delete;
- Retire;
- ordinary PATCH;
- generic status edit.

Accepted Archive behavior:

- `archived_at` is set;
- `status` remains `RETIRED`;
- `assigned_user_id` is not changed by Archive;
- `location_id` is preserved;
- no responsibility fact is appended;
- one `ARCHIVE` audit event is emitted;
- Asset record remains readable by id;
- responsibility history remains preserved;
- audit history remains preserved.

Eligibility:

- Archive is allowed only for assets with `status = RETIRED`;
- Archive is rejected for `ACTIVE` assets;
- Archive is rejected for already archived assets.

Repeated Archive behavior:

- already archived assets cannot be archived again;
- repeated Archive must not change `archived_at`;
- repeated Archive must not emit duplicate `ARCHIVE` audit events;
- repeated Archive must not append responsibility facts;
- repeated Archive is rejected through the existing project business-error / conflict style.

Authorization:

- Archive uses the existing asset mutate/lifecycle authorization cutline;
- read-only actors remain forbidden;
- no new role model is introduced by Archive exposure;
- no auth/IAM expansion is introduced by Archive exposure.

#### Archive visibility read behavior

Default list:

- `GET /api/v1/assets` returns non-archived working assets only.

Archived discovery path:

- `GET /api/v1/assets?visibility=archived` returns archived assets only.

Detail:

- `GET /api/v1/assets/{id}` remains readable for archived assets by authorized users.

Read model:

- Asset read surfaces may include `archived_at`;
- `archived_at = null` means not archived;
- non-null `archived_at` means archived / hidden from default working lists.

#### Boundary rule

- `archive` не является delete workflow;
- `archive` не является status transition to `ARCHIVED`;
- `archive` не выполняется через generic PATCH;
- `archive` не доступен для `ACTIVE` assets.

### 6. Unarchive

#### Accepted endpoint

- `POST /api/v1/assets/{id}/unarchive`

#### Command intent

- return an archived retired Asset from archive visibility to normal non-archived retired visibility.

This is visibility reversal only.

It is not restoration to operation.

#### Request minimum

- body minimum не фиксируется; asset identity in path is sufficient for the minimum command contract.

#### Response minimum

- `id`

#### Accepted Unarchive semantics

Unarchive is product-exposed as a bounded visibility reversal command for archived `RETIRED` assets only.

Unarchive is not:

- restore-to-ACTIVE;
- reactivation;
- Retire rollback;
- assignment restoration;
- previous assignee restoration;
- location restoration;
- Correction Delete;
- hard delete;
- ordinary PATCH;
- generic status edit.

Accepted Unarchive behavior:

- `archived_at` is cleared;
- `status` remains `RETIRED`;
- `assigned_user_id` is not restored;
- `assigned_user_id` is not changed;
- `location_id` is preserved;
- no responsibility fact is appended;
- one `UNARCHIVE` audit event is emitted;
- Asset record remains readable by id;
- responsibility history remains preserved;
- audit history remains preserved.

Eligibility:

- Unarchive is allowed only for assets with `status = RETIRED`;
- Unarchive is allowed only when `archived_at IS NOT NULL`;
- Unarchive is rejected for `ACTIVE` assets;
- Unarchive is rejected for non-archived assets.

Repeated Unarchive behavior:

- already non-archived assets cannot be unarchived again;
- repeated Unarchive must not emit duplicate `UNARCHIVE` audit events;
- repeated Unarchive must not append responsibility facts;
- repeated Unarchive is rejected through the existing project business-error / conflict style.

Authorization:

- Unarchive uses the existing asset mutate/lifecycle authorization cutline;
- read-only actors remain forbidden;
- no new role model is introduced by Unarchive exposure;
- no auth/IAM expansion is introduced by Unarchive exposure.

#### Unarchive visibility read behavior

Default list after Unarchive:

- `GET /api/v1/assets` returns the asset again as a non-archived retired asset.

Archived discovery path after Unarchive:

- `GET /api/v1/assets?visibility=archived` no longer returns the asset.

Detail:

- `GET /api/v1/assets/{id}` remains readable before and after Unarchive.

Read model:

- `archived_at` becomes `null`;
- `status` remains `RETIRED`.

#### Boundary rule

- `unarchive` не является restore-to-ACTIVE workflow;
- `unarchive` не является rollback of Retire;
- `unarchive` не восстанавливает `assigned_user_id`;
- `unarchive` не выполняется через generic PATCH;
- `unarchive` не доступен для `ACTIVE` assets;
- `unarchive` не выполняет inventory/warehouse movement.

## Frontend Quick Find / Asset Note Search

Asset note search is accepted only as a frontend-only Quick Find scope expansion.

This does not add a backend/API search endpoint.

Accepted behavior:

- Quick Find may locally match `asset.note`;
- matching uses already loaded authorized frontend state only;
- matching uses default non-archived asset state only;
- archived assets are not searched through Quick Find;
- raw `note` is not displayed in search result rows;
- note snippets are not displayed;
- note highlighting is not added;
- the accepted marker for a note match is:
  - `Совпадение по комментарию`.

Preserved API boundaries:

- no new backend endpoint;
- no DB migration;
- no DTO/list expansion;
- no change to `GET /api/v1/assets`;
- no change to `GET /api/v1/assets/{id}`;
- no change to `GET /api/v1/assets/technical-attribute-search`;
- no change to `GET /api/v1/assets/employee-sheet-technical-hints`;
- no backend/global search;
- no advanced filters;
- no saved views;
- no public lookup;
- no employee self-service.

Technical Search remains technical-attribute-only.

Asset `note` is not part of Technical Search.

Employee Sheet does not search or expose asset `note`.

## Frontend Asset List Filters

Advanced Asset List Filters is accepted only as a frontend-only working asset list filtering refinement.

This does not add a backend/API filtering endpoint.

Accepted behavior:

- filters operate only on already loaded `state.assets`;
- filters do not operate on `state.archivedAssets`;
- archived assets remain discoverable only through the accepted archive visibility path;
- filters are session/in-memory UI state only;
- no saved views are introduced;
- no persistent preferences are introduced;
- no `localStorage` persistence is introduced.

Accepted first-cut filters:

- `Все рабочие`;
- `Назначенные`;
- `Неназначенные`;
- `Списанные`;
- `Тип`;
- `Локация`;
- `Сбросить фильтры`.

Preserved API boundaries:

- no new backend endpoint;
- no DB migration;
- no DTO/list expansion;
- no server-side filtering;
- no change to `GET /api/v1/assets`;
- no change to `GET /api/v1/assets?visibility=archived`;
- no change to `GET /api/v1/assets/{id}`;
- no change to `GET /api/v1/assets/technical-attribute-search`;
- no change to `GET /api/v1/assets/employee-sheet-technical-hints`;
- no backend/global search;
- no advanced filters;
- no saved views;
- no reporting/dashboard;
- no public lookup;
- no employee self-service.

Search boundaries:

- Quick Find remains separate loaded-state text search;
- Technical Search remains separate technical-attribute search;
- Asset note matching remains only the accepted Quick Find behavior;
- technical attributes are not added to asset list filters.

Action boundaries:

- filtered rows remain navigation-only;
- no lifecycle row actions are introduced;
- no bulk actions are introduced.