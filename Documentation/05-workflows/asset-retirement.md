# Asset Retirement Workflow

Status: draft
Owner: architecture
Last Updated: 2026-04-01

## Purpose
Описать доменный flow вывода актива из эксплуатации с сохранением audit и истории.

## Scope
Документ задает только доменные правила retirement flow.
Точный API-контракт и точные payload schema фиксируются отдельно.

## Boundary Reference
Канонические границы между `assign`, `transfer`, `return`, `retire` задаются в `/Documentation/02-domain/asset-domain.md`, section `Responsibility Flow Boundaries`.
Этот документ должен интерпретироваться в согласовании с этим section.

## Core Rule
Retirement не является частью generic PATCH Asset.
Retirement также не должен трактоваться как delete workflow.

## Boundary Clarifying Note
`Retirement` не совпадает с `return`.

`Return` завершает текущую пользовательскую ответственность за Asset.
`Retirement` завершает эксплуатационное использование Asset с сохранением audit и истории.

Предварительный `return` перед `retirement` не является обязательным precondition в текущем каноне.

Если на момент `retirement` у Asset существует текущая активная пользовательская ответственность, после `retirement` она больше не считается активной.

`Retirement` не канонизирует обязательное изменение `location_id`.

Конкретное retirement-related значение `status` в текущем каноне не зафиксировано.

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
- Не зафиксированы точные доменные условия eligibility для `retirement`.
- Конкретное retirement-related значение `status` не канонизировано.
- Финальная physical history model для retirement-related facts остается open decision.
- Public API для `retirement` не канонизирован в этом документе.