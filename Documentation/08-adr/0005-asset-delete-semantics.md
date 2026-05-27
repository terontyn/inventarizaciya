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
- Archive / Hide remains deferred;
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
публичный hard delete не поддерживается, Retire является канонической безопасной lifecycle-семантикой, Archive / Hide и Correction Delete остаются отдельными deferred decisions.

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
- Archive / Hide remains deferred and requires separate semantics.
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
- Archive / Hide is not implemented.
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


This ADR status update authorizes only the bounded Retire exposure described above.

It does not authorize:

- `DELETE /api/v1/assets/{id}`;
- hard delete;
- Archive / Hide;
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