# ADR-0004 Asset CRUD vs History

Status: proposed
Date: 2026-03-27

## Context
В asset domain зафиксированы:
- Asset
- AssetAssignment
- AssetTransfer
- AssetRetirement

Также зафиксирован инвариант:
история назначения и перемещений не теряется.

При этом в Foundation Slice v1 нужен базовый Asset CRUD.

## Decision
Для Foundation Slice:
- CRUD v1 ограничивается мастер-записью Asset;
- generic PATCH не должен использоваться как универсальный механизм для операций ответственности;
- операции assign / transfer / return рассматриваются как отдельные domain actions;
- полноценные workflow/API для AssetAssignment / AssetTransfer / AssetRetirement идут следующим срезом.

## Why
- это защищает доменную историю;
- не смешивает реестр актива и операционные события жизненного цикла;
- снижает риск неправильного API-дизайна.

## Consequences
Плюсы:
- чище доменная модель;
- история не теряется;
- проще потом ввести отдельные workflows.

Минусы:
- MVP требует чуть более аккуратного API-дизайна;
- часть привычных update-сценариев нельзя свести к одному PATCH.

## Follow-up
Обновить:
- /docs/02-domain/asset-domain.md
- /docs/04-api/asset-api.md
- /docs/05-workflows/asset-assignment.md
- /docs/05-workflows/asset-transfer.md