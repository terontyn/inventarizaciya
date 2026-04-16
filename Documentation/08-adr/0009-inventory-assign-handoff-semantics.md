# ADR-0009 Inventory-to-Assign Handoff Semantics

Status: approved
Date: 2026-04-15

## Context

В первом Inventory / Warehouse contour:
- `asset-service` остается owner responsibility lifecycle;
- `inventory-service` остается owner current storage custody representation;
- переход из storage custody в end-user assignment происходит через handoff в existing assign flow;
- current inventory-side storage custody representation должна считаться consumed только при successful assign handoff;
- отдельный public inventory exit API в первом contour не открывается.

При этом exact technical orchestration mechanism для этого handoff
до сих пор не канонизирован.

## Decision

Для первого Inventory / Warehouse contour принимается следующее:

1. System of action в точке handoff = existing assign flow в `asset-service`.
2. Successful assign completion является единственным trigger,
   после которого current inventory-side storage custody representation
   может считаться подлежащей consumption.
3. Consumption current `InventoryRecord`
   выполняется не через public API,
   а через internal-only asynchronous integration contract.
4. `asset-service` инициирует handoff emission,
   `inventory-service` выполняет consumption своей current-state representation.
5. Assign success не зависит от synchronous availability или synchronous completion
   inventory-side consumption.
6. Первый contour допускает eventual consistency
   между successful assign и inventory-side custody consumption.
7. Compensating rollback of assign
   из-за inventory-side consumption failure
   в первом contour не требуется.
8. Inventory-side consumer должен трактовать повторную обработку
   как idempotent no-op,
   если current `InventoryRecord` уже отсутствует.

## Consequences

### Positive
- сохраняется service ownership split;
- не открывается новый public inventory exit API;
- не требуется новый orchestration platform scope;
- не нарушается запрет на direct DB coupling;
- решение достаточно узкое для первого contour.

### Negative
- inventory-side reads становятся eventually consistent;
- возможен временный интервал,
  в котором Asset уже assigned,
  но inventory context еще кратковременно показывает его как `in storage now`;
- нужны retry / idempotency semantics в internal integration path.

## Out of Scope

Этот ADR не канонизирует:
- public handoff endpoint;
- transport technology / broker choice;
- project-wide integration standard;
- saga / workflow engine;
- compensation framework;
- richer inventory-side history model.