# Asset Transfer Workflow

Status: draft
Owner: architecture
Last Updated: 2026-04-01

## Purpose
Описать доменный flow передачи актива другому пользователю и/или в другую операционную локацию в пределах одного tenant.

## Scope
Документ задает только доменные правила transfer flow.
Точный API-контракт и точные payload schema фиксируются отдельно.

## Boundary Reference
Канонические границы между `assign`, `transfer`, `return`, `retire` задаются в `/Documentation/02-domain/asset-domain.md`, section `Responsibility Flow Boundaries`.
Этот документ должен интерпретироваться в согласовании с этим section.

## Core Rule
Transfer не является частью generic PATCH Asset.
Изменение текущей ответственности и/или операционной локации должно выполняться только через dedicated domain flow.

## Boundary Clarifying Note
`Transfer` является одним каноническим responsibility flow.

В рамках этого flow допустимы три варианта доменного изменения:
- `user-only transfer`
- `location-only transfer`
- `combined user + location transfer`

`Transfer` используется, когда доменное намерение состоит в непрерывной передаче текущей ответственности и/или операционной локации как одного доменного изменения.

Если у Asset нет текущей пользовательской ответственности, операция, создающая новую текущую пользовательскую ответственность, должна рассматриваться как `assignment`, а не как `transfer`.

`Transfer` без текущего assigned user допустим только для `location-only` scenario.

## Preconditions
- Asset существует.
- Новый User и/или новая Location, если участвуют в операции, существуют.
- Все участвующие сущности принадлежат одному tenant.
- Cross-context validation выполняется через API user-service.
- Операция проходит tenant-scoped validation.
- Transfer должен менять хотя бы одну из доменных осей: текущую ответственность и/или текущую операционную локацию.

## Flow
1. Определить tenant scope, actor и correlation id из request context.
2. Найти Asset в tenant scope.
3. Валидировать target references через API user-service.
4. Проверить, что change responsibility/location выполняется dedicated flow, а не generic PATCH.
5. Создать append-only запись доменного изменения ответственности/локации.
6. Обновить текущее состояние Asset как мастер-записи.
7. Создать durable local audit record в той же транзакции.
8. Передать audit в central audit storage асинхронно.

## Exceptions
- Asset not found.
- Target User not found.
- Target Location not found.
- Tenant mismatch.
- No effective transfer change.
- Cross-context validation failed.
- Responsibility/location change attempted via generic PATCH.

## Audit implications
- Transfer является critical write.
- Audit record должен быть создан durably в producer transaction.
- Canonical audit envelope должен содержать tenant scope, actor, entity, action, time, correlation id и change payload.

## History implications
- История ответственности append-only.
- Transfer добавляет новую историческую запись, а не переписывает существующую.
- Предыдущее состояние ответственности и/или локации должно оставаться в истории.

## Explicit non-generic-PATCH rule
- assigned_user_id нельзя считать обычным PATCH-полем.
- location_id нельзя считать обычным PATCH-полем.
- Любое изменение этих полей должно быть следствием dedicated transfer flow.

## Open Decisions / STOP
- Не зафиксированы точные eligibility rules для `transfer` сверх tenant scope, existence checks и effective change requirement.
- Не зафиксированы точные operational distinctions для отдельных transfer scenarios beyond already accepted boundary canon.
- Public API для `transfer` не канонизирован в этом документе.