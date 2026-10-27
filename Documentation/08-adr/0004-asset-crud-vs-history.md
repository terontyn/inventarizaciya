# ADR-0004 Граница Asset CRUD и истории ответственности

Status: accepted
Date: 2026-03-27

## Context
Asset domain уже содержит правило:
история назначения и перемещений не теряется.
При этом Foundation Slice требует базовый CRUD для Asset.
Нужно не сломать доменную историю generic PATCH-операциями.

## Options
1. Generic CRUD может менять все поля Asset
2. Generic CRUD управляет только мастер-записью, а ответственность и локация меняются через отдельные flows
3. Полный event sourcing уже в Foundation Slice

## Decision
Для Foundation Slice принимается:
- generic Asset CRUD управляет только мастер-записью Asset;
- assigned_user_id и location_id исключаются из generic PATCH;
- операции assign / transfer / return / retire реализуются как отдельные domain flows;
- история ответственности является append-only;
- full event sourcing не входит в MVP.

## Consequences
Плюсы:
- чище граница aggregate;
- история ответственности не теряется;
- меньше риск тихой порчи доменной истории.

Минусы:
- API становится немного богаче;
- часть сценариев нельзя решать одним универсальным PATCH.

## Rejected For MVP
- generic PATCH для responsibility-changing fields
- full event sourcing