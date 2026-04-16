# Asset Assignment Workflow

Status: draft
Owner: architecture
Last Updated: 2026-03-30

## Purpose
Описать доменный flow назначения актива пользователю в пределах одного tenant.

## Scope
Документ задает только доменные правила assignment flow.
Точный API-контракт и точные payload schema фиксируются отдельно.

## Core Rule
Assignment не является частью generic PATCH Asset.
Изменение текущей ответственности должно выполняться только через dedicated domain flow.

## Preconditions
- Asset существует.
- Target User существует.
- Asset и User принадлежат одному tenant.
- Cross-context validation выполняется через API user-service.
- Операция проходит tenant-scoped validation.
- Asset должен быть допустим для назначения.

## Flow
1. Определить tenant scope, actor и correlation id из request context.
2. Найти Asset в tenant scope.
3. Валидировать target User через API user-service.
4. Проверить, что изменение ответственности выполняется dedicated flow, а не generic PATCH.
5. Создать append-only запись доменного изменения ответственности.
6. Обновить текущее состояние Asset как мастер-записи в части текущей ответственности.
7. Создать durable local audit record в той же транзакции.
8. Передать audit в central audit storage асинхронно.

## Exceptions
- Asset not found.
- User not found.
- Tenant mismatch.
- Asset is not eligible for assignment.
- Cross-context validation failed.
- Responsibility change attempted via generic PATCH.

## Audit implications
- Assignment является critical write.
- Audit record должен быть создан durably в producer transaction.
- Canonical audit envelope должен содержать tenant scope, actor, entity, action, time, correlation id и change payload.

## History implications
- История ответственности append-only.
- Назначение не должно затирать предыдущую историю.
- Текущее состояние Asset не заменяет историю, а отражает только актуальную ответственность.

## Explicit non-generic-PATCH rule
- assigned_user_id нельзя считать обычным PATCH-полем.
- Если change responsibility требует изменение текущего состояния актива, это все равно не generic PATCH, а результат dedicated assignment flow.

## Open Decisions / STOP
- Не зафиксирован точный доменный смысл правила "asset available for assignment".
- Не зафиксировано, должен ли assignment всегда одновременно фиксировать операционную локацию.
- Не зафиксирована точная граница между first-time assignment и reassignment, если актив ранее не был назначен.

## ADR-0009 Clarification

Если Asset на момент assignment
currently находится в storage custody,
successful assign completion в `asset-service`
является canonical handoff point
для inventory-side consumption.

Для первого contour:
- `asset-service` инициирует internal async handoff signal;
- `inventory-service` consume’ит current storage custody representation;
- inventory-side consumption не является synchronous precondition
  для assign success;
- temporary eventual consistency
  между assign success и inventory-side closeout допустима.

Compensating rollback of assign
из-за временного inventory-side failure
в первом contour не требуется.