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