# Asset Return Workflow

Status: draft
Owner: architecture
Last Updated: 2026-04-01

## Purpose
Описать доменный flow возврата актива с завершением текущей пользовательской ответственности в пределах одного tenant.

## Scope
Документ задает только доменные правила return flow.
Точный API-контракт и точные payload schema фиксируются отдельно.

## Core Rule
Return не является частью generic PATCH Asset.
Изменение текущей ответственности должно выполняться только через dedicated domain flow.

## Preconditions
- Asset существует.
- Asset принадлежит текущему tenant scope.
- У Asset существует текущая пользовательская ответственность, которую можно завершить.
- Операция проходит tenant-scoped validation.
- Return выполняется как dedicated domain flow, а не как generic PATCH.

## Flow
1. Определить tenant scope, actor и correlation id из request context.
2. Найти Asset в tenant scope.
3. Проверить, что у Asset есть текущая пользовательская ответственность.
4. Проверить, что операция выполняется как return flow, а не как generic PATCH.
5. Зафиксировать append-only доменный факт завершения текущей ответственности.
6. Обновить текущее состояние Asset как мастер-записи так, чтобы текущая пользовательская ответственность больше не считалась активной.
7. Создать durable local audit record в той же транзакции.
8. Передать audit в central audit storage асинхронно.

## Exceptions
- Asset not found.
- Tenant mismatch.
- Asset has no current assigned user responsibility to return.
- Responsibility change attempted via generic PATCH.
- Return is used where transfer semantics are required.
- Return is used as retire/delete substitute.

## Audit implications
- Return является critical write.
- Audit record должен быть создан durably в producer transaction.
- Canonical audit envelope должен содержать tenant scope, actor, entity, action, time, correlation id и change payload.

## History implications
- История ответственности append-only.
- Return не должен затирать предшествующую историю назначения и перемещений.
- Return должен добавлять новый доменный след о завершении текущей ответственности.
- Текущее состояние Asset не заменяет историю, а отражает только актуальное состояние ответственности.

## ADR-0008 Clarifications
- `Return` завершает только текущую пользовательскую ответственность.
- `Return` не канонизирует изменение `location_id`.
- `Return` не требует `target location`.
- После `Return` у Asset больше нет активной текущей пользовательской ответственности.
- `Return` обязан создавать отдельный append-only history fact, отличный от `Assignment`, `Transfer` и `Retirement`.
- Отдельная canonical entity `AssetReturn` в текущем каноне не вводится.

## Explicit non-generic-PATCH rule
- assigned_user_id нельзя считать обычным PATCH-полем.
- Return не должен маскироваться под generic PATCH Asset.
- Если изменение текущей ответственности требуется как результат возврата, это все равно не generic PATCH, а результат dedicated return flow.

## Open Decisions / STOP
- Не зафиксировано, какие именно доменные условия делают Asset eligible for `return` сверх наличия текущей ответственности.
- Конкретное значение `status` для post-return состояния не канонизировано.
- Финальная physical storage model history остается open decision.
- Public API для `return` не канонизирован в этом документе.


## Inventory / Warehouse Clarification
`Return` сам по себе
не создает storage custody
и не фиксирует warehouse placement.

Если после `Return`
Asset фактически принимается в warehouse / storage custody,
это должно трактоваться
как отдельная explicit inventory-side operation,
а не как неявная часть existing `return`.

Следовательно:
- post-return storage intake не маскируется под `return`;
- до explicit inventory intake
  Asset не должен считаться `in storage now`
  только на основании completed `return`.

  ## Inventory Readiness Clarification
Если после `Return`
Asset проходит explicit inventory intake
через `place-in-storage`,
inventory-side readiness
не должна считаться automatic.

Для readiness refinement
такой Asset должен входить в current storage custody
с initial `NOT_READY`,
пока отдельный explicit readiness-change workflow
не переведет его в `READY`.