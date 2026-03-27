# ADR-0005 Asset Delete Semantics

Status: proposed
Date: 2026-03-27

## Context
Для Asset delete semantics пока открыты варианты:
- hard delete
- soft delete
- no delete / only retire

При этом доменное правило требует не терять историю, а критичные действия должны попадать в audit.

## Decision
Для Foundation Slice и MVP:
- не поддерживать DELETE /api/v1/assets/{id};
- использовать доменное состояние retirement вместо удаления;
- физическое удаление не включать в публичный API MVP.

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

## Follow-up
Обновить:
- /docs/02-domain/asset-domain.md
- /docs/04-api/asset-api.md
- /docs/05-workflows/asset-retirement.md