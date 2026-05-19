# Asset Domain

Status: approved
Owner: architecture
Last Updated: 2026-05-19

## Scope
Asset Domain реализуется внутри asset-service.

## Entities
- Asset
- AssetAssignment
- AssetTransfer
- AssetRetirement

## Confirmed Asset Fields
- asset_tag
- serial_number
- asset_type
- status
- assigned_user_id
- location_id

Asset Identity v1 extension fields:

- display_name
- note

## Invariants
- актив принадлежит tenant;
- asset_tag уникален внутри tenant;
- история назначения и перемещений не теряется.

## Confirmed Decisions
- Asset CRUD v1 управляет только мастер-записью Asset.
- Generic PATCH не должен менять assigned_user_id и location_id.
- Операции assign / transfer / return / retire являются отдельными domain flows.
- История ответственности является append-only.
- asset-service хранит только tenant-scoped reference IDs на user/org entities.
- Cross-context validation выполняется через user-service API, а не через прямой доступ к БД.

## Status Vocabulary (Pilot / MVP)

Для MVP / Pilot Readiness используется минимальный канонический словарь `Asset.status`:
- `ACTIVE`
- `RETIRED`

### Status Semantics
- `ACTIVE` означает, что Asset находится в текущем operational registry и не выведен из эксплуатации.
- `ACTIVE` не кодирует current responsibility state.
- Asset со `status = ACTIVE` может как иметь текущего assigned user, так и не иметь его.
- `RETIRED` означает, что Asset выведен из эксплуатации.
- `RETIRED` не является delete semantics.

### Flow Impact on Status
- create asset -> initial status = `ACTIVE`
- update asset master record -> не используется как status-transition flow
- assign -> status не меняется
- transfer -> status не меняется
- return -> status не меняется; post-return status = `ACTIVE`
- retire -> status становится `RETIRED`

## Asset Identity v1 Extension

Asset Identity v1 adds two optional Asset master-record fields:

- `display_name`
- `note`

### display_name

`display_name` is a human-readable Asset title used to make an Asset easier to recognize in UI, lists, cards and future presentation surfaces if separately approved.

Rules:

- optional;
- nullable;
- complements `asset_tag`;
- does not replace `asset_tag`;
- not unique;
- not a second canonical identifier family;
- no search index is implied by this field.

### note

`note` is an optional plain-text operational note for daily context.

Rules:

- optional;
- nullable;
- plain text only;
- not rich text;
- not an attachment;
- not a document;
- not an act;
- not a photo;
- not a comment thread;
- not a workflow state.

### Boundary

Asset Identity v1 does not change responsibility semantics.

The following remain unchanged:

- `asset_tag` remains the canonical inventory/business identifier inside tenant;
- `assigned_user_id` does not move into generic edit;
- `location_id` does not move into generic edit where responsibility/location boundary forbids it;
- Assign remains dedicated;
- Transfer remains dedicated;
- Return remains dedicated;
- Retire remains dedicated where currently present;
- generic edit remains master-record-only;
- status vocabulary remains `ACTIVE` / `RETIRED`.

## CRUD v1 Boundary
Foundation Slice v1 включает:
- POST /api/v1/assets
- GET /api/v1/assets
- GET /api/v1/assets/{id}
- PATCH /api/v1/assets/{id}

CRUD v1 не включает:
- dedicated command APIs для responsibility flows в этом документе;
- DELETE /api/v1/assets/{id}.

## Delete
Delete semantics пока не утверждена.
До ADR-0005 удаление остается open question.

## Open Questions
- final delete semantics: hard delete / soft delete / no delete / retire only;
- когда именно появляются отдельные command endpoints для AssetAssignment / AssetTransfer / AssetReturn / AssetRetirement;
- какая модель хранения history будет финальной после MVP.

## Related Workflow Docs
Детальные workflow для responsibility-changing operations описаны отдельно:
- /Documentation/05-workflows/asset-assignment.md
- /Documentation/05-workflows/asset-transfer.md
- /Documentation/05-workflows/asset-return.md
- /Documentation/05-workflows/asset-retirement.md

## Responsibility Flow Boundaries

### Assignment
Используется, когда доменное намерение — установить текущую пользовательскую ответственность за Asset.
Assignment не должен маскироваться под generic PATCH.

### Transfer
Используется, когда доменное намерение — изменить текущую пользовательскую ответственность и/или текущую операционную локацию как одно непрерывное доменное изменение.
Transfer не должен маскироваться под generic PATCH.

### Return
Используется, когда доменное намерение — завершить текущую пользовательскую ответственность без назначения нового пользователя в том же flow.
Return не должен маскироваться под generic PATCH.

### Retirement
Используется, когда доменное намерение — вывести Asset из эксплуатации с сохранением audit и истории.
Retirement не является delete workflow и не должен маскироваться под generic PATCH.

### Boundary Clarifications
- Return отличается от Transfer тем, что Return завершает текущую пользовательскую ответственность, а Transfer меняет ответственность и/или операционную локацию на новое текущее состояние.
- Assign следует использовать для установления текущей ответственности.
- Return + Assign следует использовать только тогда, когда бизнес-процесс требует двух отдельных доменных фактов в истории: сначала завершение текущей ответственности, затем новое назначение.
- Если бизнес-намерение состоит в непрерывной передаче актива другому пользователю и/или в другую локацию без промежуточной фиксации отдельного факта возврата, следует использовать Transfer.
- Return и Retirement не совпадают: Return завершает текущую пользовательскую ответственность, а Retirement завершает эксплуатационное использование Asset.
- Обязательность Return перед Retirement пока не канонизирована и остается open decision.
- `Return` завершает текущую пользовательскую ответственность и не канонизирует relocation.
- Relocation после `Return` должен трактоваться как отдельный доменный факт, а не как неявная часть `Return`.
- `Transfer` является единым каноническим flow с допустимыми вариантами: user-only, location-only, combined user + location.
- Если у Asset нет текущей пользовательской ответственности, создание новой текущей пользовательской ответственности относится к `Assignment`, а не к `Transfer`.
- `Retirement` не требует обязательного pre-return, но после `Retirement` текущая пользовательская ответственность, если она существовала, больше не считается активной.

### Non-generic-PATCH Rule
Во всех responsibility-changing flows изменение assigned_user_id и location_id не считается частью generic PATCH Asset.


## Printable Asset Label v1

Для MVP / Pilot допускается minimal printable asset label v1
как derived printable representation поверх текущего asset registry / asset card contour.

### Canonical rules
- label v1 использует только уже существующие Asset master fields:
  - `asset_type`
  - `asset_tag`
  - `serial_number`
- для MVP / Pilot `asset_tag` используется как current printable inventory identifier;
- label v1 не вводит новые canonical Asset master fields;
- label v1 не вводит отдельный `inventory_number`;
- label v1 не вводит `barcode` или `qr_code`;
- label v1 не меняет responsibility flow boundaries и не влияет на правила `assign / transfer / return / retire`.

### Boundary note
Printable label v1 является presentation-level convenience,
а не отдельным domain flow, не отдельной сущностью и не отдельным integration contour.