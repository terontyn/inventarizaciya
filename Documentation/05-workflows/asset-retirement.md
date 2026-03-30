# Asset Retirement Workflow

Status: draft
Owner: architecture
Last Updated: 2026-03-30

## Purpose
Описать доменный flow вывода актива из эксплуатации с сохранением audit и истории.

## Scope
Документ задает только доменные правила retirement flow.
Точный API-контракт и точные payload schema фиксируются отдельно.

## Core Rule
Retirement не является частью generic PATCH Asset.
Retirement также не должен трактоваться как delete workflow.

## Preconditions
- Asset существует.
- Asset принадлежит текущему tenant scope.
- Retirement запускается только как dedicated domain flow.
- Должны выполняться доменные условия вывода из эксплуатации.

## Flow
1. Определить tenant scope, actor и correlation id из request context.
2. Найти Asset в tenant scope.
3. Проверить, что операция выполняется как retirement flow, а не generic PATCH и не delete.
4. Проверить доменные условия вывода из эксплуатации.
5. Зафиксировать отдельное доменное изменение retirement с сохранением истории.
6. Обновить текущее состояние Asset в рамках утвержденной retirement model.
7. Создать durable local audit record в той же транзакции.
8. Передать audit в central audit storage асинхронно.

## Exceptions
- Asset not found.
- Tenant mismatch.
- Retirement conditions not satisfied.
- Retirement attempted via generic PATCH.
- Retirement attempted as delete semantics substitute.

## Audit implications
- Retirement является critical write.
- Audit record должен быть создан durably в producer transaction.
- Canonical audit envelope должен содержать tenant scope, actor, entity, action, time, correlation id и change payload.

## History implications
- Retirement не должен стирать историю назначения и перемещений.
- История остается append-only.
- Retirement должен добавлять новый доменный след, а не переписывать предшествующие записи.

## Explicit non-generic-PATCH rule
- Retirement не должен маскироваться под обычное изменение полей Asset.
- Любое изменение текущего состояния актива в связи с retirement должно быть следствием dedicated retirement flow.

## Open Decisions / STOP
- Не зафиксированы точные доменные условия вывода из эксплуатации.
- Не зафиксировано, должен ли актив быть предварительно возвращен перед retirement.
- Не зафиксировано, как retirement влияет на текущую ответственность и location_id.
- Не зафиксировано точное retirement-related state value.
- Delete semantics остаётся open question и не покрывается этим документом.