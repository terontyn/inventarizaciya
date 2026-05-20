# ADR-0005 Asset Delete Semantics

Status: accepted
Date: 2026-03-27

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
- Existing `/retire` backend route remains blocked / not product-exposed until separately approved for exposure.
- Archive / Hide remains deferred and requires separate semantics.
- Correction Delete remains future candidate only for mistaken records with strict no-history constraints.
- Correction Delete is not approved for implementation now.
- Physical hard delete is not part of MVP / pilot public API.

## Repo Reconciliation

- `ACTIVE` / `RETIRED` status vocabulary exists.
- Backend `POST /api/v1/assets/{id}/retire` route exists.
- Retire service path exists.
- Current authorization blocks Retire for default pilot roles.
- Frontend does not surface `/retire`.
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


This ADR acceptance does not authorize:

- unlocking `/retire`;
- exposing Retire in UI;
- adding Archive / Hide;
- adding Correction Delete;
- adding hard delete;
- adding DB migrations;
- changing role/IAM behavior.


## Follow-up
Обновить:
- Documentation/02-domain/asset-domain.md
- Documentation/04-api/asset-api.md
- Documentation/05-workflows/asset-retirement.md