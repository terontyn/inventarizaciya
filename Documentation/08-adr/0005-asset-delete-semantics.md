# ADR-0005 Asset Delete Semantics

Status: accepted
Date: 2026-03-27

## Implementation Status Update — Retire Exposure

Retire has now been exposed as a bounded dedicated lifecycle command:

- `POST /api/v1/assets/{id}/retire`

This updates the earlier implementation status where the backend route existed but was not product-exposed.

Retire exposure does not change the ADR decision:

- public hard delete remains rejected;
- `DELETE /api/v1/assets/{id}` remains unsupported;
- Retire remains the canonical safe lifecycle removal semantics;
- Archive / Hide has since been accepted only as bounded persisted Archive visibility semantics for already `RETIRED` assets by the later Archive / Hide Lifecycle Visibility status update;
- Correction Delete remains deferred;
- ordinary PATCH must not perform Retire;
- generic status editing is not approved.

Accepted Retire exposure:

- status becomes `RETIRED`;
- active `assigned_user_id` is cleared;
- current `location_id` is preserved;
- Asset record remains readable;
- responsibility history remains preserved;
- audit evidence remains preserved;
- repeated Retire is guarded and must not duplicate `RETIRE` facts or audit events.

Retire is exposed only as a dedicated command and selected-asset lifecycle action.

This does not authorize hard delete, archive / hide, correction delete, bulk retire, recovery / unretire, ordinary PATCH status transition or generic status editor.

## Context
Ранее для Asset delete semantics рассматривались варианты:

- hard delete;
- soft delete / archive;
- no delete / only retire;
- constrained correction delete for mistaken records.


По итогам product semantics gate и repo-domain verification решение принято:
публичный hard delete не поддерживается, Retire является канонической безопасной lifecycle-семантикой, Archive visibility has since been accepted only as bounded persisted visibility semantics for already `RETIRED` assets, and Correction Delete remains a separate deferred decision.

При этом доменное правило требует не терять историю, а критичные действия должны попадать в audit.

## Decision
Для Foundation Slice и MVP:
- не поддерживать DELETE /api/v1/assets/{id};
- использовать доменное состояние retirement вместо удаления;
- физическое удаление не включать в публичный API MVP.
- Public hard delete is rejected for current stage.
- `DELETE /api/v1/assets/{id}` remains unsupported.
- Retire is the canonical safe lifecycle removal semantics.
- `POST /api/v1/assets/{id}/retire` is now product-exposed as a bounded dedicated lifecycle command.
- Archive / Hide has now been implemented only as bounded persisted Archive visibility semantics for already `RETIRED` assets.
- Correction Delete remains future candidate only for mistaken records with strict no-history constraints.
- Correction Delete is not approved for implementation now.
- Physical hard delete is not part of MVP / pilot public API.
- Recovery / Unarchive has now been implemented only as bounded visibility reversal for archived `RETIRED` assets.

## Repo Reconciliation

- `ACTIVE` / `RETIRED` status vocabulary exists.
- Backend `POST /api/v1/assets/{id}/retire` route exists.
- Retire service path exists.
- Retire is exposed through the existing asset mutate/lifecycle authorization cutline.
- Read-only actors remain forbidden.
- Frontend exposes Retire only inside selected Asset Actions as `Списать актив`.
- Repeated Retire is guarded and does not duplicate `RETIRE` facts or audit events.
- Archive visibility is implemented through `POST /api/v1/assets/{id}/archive` for already `RETIRED` assets only.
- Archive uses persisted `archived_at`.
- Archive emits `ARCHIVE` audit and does not append responsibility facts.
- Default asset list excludes archived assets.
- Archived assets are discoverable through `GET /api/v1/assets?visibility=archived`.
- Correction Delete is not implemented.
- Public hard delete API is not implemented.
- Repository delete method is not present.

## Why

- это лучше соответствует history retention;
- не конфликтует с audit;
- снижает риск потери данных и спорных сценариев.

## Consequences
Плюсы:
- безопаснее для домена;
- проще для аудита;
- меньше риск случайной потери записи.

Минусы:
- cleanup и correction-сценарии нужно будет решать отдельно;
- нужна понятная модель статусов актива.


This Retire exposure status update authorizes only the bounded Retire exposure described above.
The later Archive / Hide Lifecycle Visibility status update below records the separately accepted bounded Archive visibility command.

It does not authorize:

- `DELETE /api/v1/assets/{id}`;
- hard delete;
- Archive / Hide beyond the accepted bounded Archive visibility command for already `RETIRED` assets;
- Correction Delete;
- bulk Retire;
- recovery / unretire;
- ordinary PATCH status transition;
- generic status editor;
- DB migrations;
- new role model;
- auth/IAM expansion.


## Follow-up
Обновить:
- Documentation/02-domain/asset-domain.md
- Documentation/04-api/asset-api.md
- Documentation/05-workflows/asset-retirement.md

## Implementation Status Update — Archive / Hide Lifecycle Visibility

Archive / Hide Lifecycle Visibility has now been implemented as bounded persisted Archive visibility semantics for already retired assets.

Accepted Archive endpoint:

- `POST /api/v1/assets/{id}/archive`

Accepted archive visibility field:

- `archived_at`

This updates the earlier status where Archive / Hide remained deferred.

Archive exposure does not change the ADR decision that public hard delete is rejected.

Accepted Archive semantics:

- Archive is visibility semantics only;
- Archive is allowed only for `RETIRED` assets;
- Archive is not allowed for `ACTIVE` assets;
- Archive does not replace Retire;
- Archive does not change `status`;
- `status` remains `RETIRED`;
- Archive does not change `assigned_user_id`;
- Archive preserves `location_id`;
- Archive does not append responsibility facts;
- Archive emits one `ARCHIVE` audit event;
- Archive preserves Asset record, responsibility history and audit evidence;
- repeated Archive is guarded and must not duplicate audit events.

Accepted visibility behavior:

- default asset list excludes archived assets;
- archived assets are discoverable through:
  - `GET /api/v1/assets?visibility=archived`;
- archived assets remain readable by id for authorized users.

Archive does not authorize:

- `DELETE /api/v1/assets/{id}`;
- hard delete;
- physical erase;
- Correction Delete;
- new lifecycle status `ARCHIVED`;
- Archive through generic PATCH;
- generic status editor;
- bulk archive;
- recovery / unarchive;
- workflow engine;
- approval workflow;
- new role model;
- auth/IAM expansion.

## Implementation Status Update — Recovery / Unarchive Lifecycle Visibility

Status: implemented and accepted as bounded visibility reversal for archived retired assets.

Intent:

- allow archived retired assets to return from archive visibility to normal non-archived retired visibility;
- preserve Asset record, responsibility history and audit evidence;
- keep Unarchive separate from restore-to-ACTIVE;
- avoid reactivation, Retire rollback, assignment restoration, hard delete, Correction Delete, generic PATCH archive manipulation or generic status editing.

Accepted implementation:

- added dedicated command endpoint:
  - `POST /api/v1/assets/{id}/unarchive`;
- uses existing archive visibility field:
  - `archived_at`;
- no DB migration was added;
- added `UNARCHIVE` audit action;
- reused existing asset mutate/lifecycle authorization cutline;
- read-only actors remain forbidden;
- no new role model was introduced;
- no auth/IAM expansion was introduced;
- UI action added only inside selected Asset Actions lifecycle block;
- action label:
  - `Разархивировать актив`;
- confirmation copy explains that this is not restoration to operation;
- API tests updated;
- static UI smoke coverage updated.

Accepted Unarchive behavior:

- only archived assets with `status = RETIRED` can be unarchived;
- `ACTIVE` assets cannot be unarchived;
- non-archived assets cannot be unarchived;
- `archived_at` is cleared on successful Unarchive;
- `status` remains `RETIRED`;
- `assigned_user_id` is not restored;
- `assigned_user_id` is not changed;
- current `location_id` is preserved;
- no responsibility fact is appended for Unarchive;
- one `UNARCHIVE` audit event is emitted;
- Asset record remains readable by id;
- responsibility history remains preserved;
- audit history remains preserved.

Repeated Unarchive behavior:

- already non-archived assets cannot be unarchived again;
- repeated Unarchive returns safe conflict/business-rule rejection;
- repeated Unarchive does not emit duplicate `UNARCHIVE` audit events;
- repeated Unarchive does not append responsibility facts.

Accepted visibility behavior:

- default `GET /api/v1/assets` includes the asset again after Unarchive;
- `GET /api/v1/assets?visibility=archived` excludes the asset after Unarchive;
- archived and unarchived assets remain readable by `GET /api/v1/assets/{id}`;
- Quick Find can include the asset again through refreshed default loaded state;
- Technical Search can include the asset again after `archived_at` is null;
- Employee Sheet still does not include the asset because assignment is not restored;
- Employee Sheet does not expose Unarchive actions.

Preserved boundaries:

- no restore to `ACTIVE`;
- no reactivation;
- no Retire rollback;
- no previous assignee restoration;
- no assignment restoration;
- no location restoration;
- no inventory/warehouse movement;
- no `DELETE /api/v1/assets/{id}`;
- no hard delete;
- no Correction Delete;
- no bulk unarchive;
- no destructive erase;
- no generic PATCH archive manipulation;
- no generic status editor;
- no workflow engine;
- no approval workflow;
- no cross-service orchestration;
- no employee self-service;
- no public lookup;
- no Asset 360;
- no reporting/dashboard dependency.

Preserved UI boundaries:

- Unified Create unchanged;
- Employee Sheet / `Информационный лист` unchanged;
- Quick Find result rows unchanged;
- Technical Search result rows unchanged;
- Asset Overview unchanged as a primary action surface;
- generic master-record edit unchanged;
- Setup/Admin unchanged;
- asset list rows unchanged as direct action surfaces;
- tree rows unchanged as direct action surfaces.

Required next step:

- no further Recovery / Unarchive implementation is opened by this closeout;
- if owner wants restore-to-ACTIVE, open a separate lifecycle reactivation decision gate;
- if owner wants assignment restoration, open a separate responsibility lifecycle decision gate;
- if owner wants bulk unarchive, open a separate bulk-action decision gate;
- if owner wants Correction Delete, open a separate strict correction-delete decision gate;
- otherwise continue to the next owner-selected bounded feature gate or the deferred integrated stabilization / verification gate.

This ADR status update does not prove release readiness, FTOV start, FTOV exit, production readiness, real tenant validation, or real day-to-day operational validation.