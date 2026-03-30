# ADR-0007 Контракт аудита для критичных write-операций

Status: accepted
Date: 2026-03-27

## Context
Критичные write-операции в Foundation Slice обязаны попадать в audit.
Нужно выбрать модель, которая не ломает write-path синхронной сетевой зависимостью.

## Options
1. Синхронный direct call в audit-service на каждый write
2. Fire-and-forget domain events без гарантированной доставки
3. Durable local audit record / outbox в той же транзакции + асинхронная доставка в central audit log

## Decision
Для Foundation Slice принимается вариант 3:
- критичное write-действие обязано оставить durable local audit record в той же транзакции;
- доставка в central audit storage выполняется асинхронно;
- бизнес-write не должен зависеть от доступности внешнего audit-service в момент commit.

## Minimal Audit Envelope
Канонический audit envelope должен содержать минимум:
- tenant scope
- actor
- entity
- action
- time
- correlation id
- change payload

## Consequences
Плюсы:
- audit не теряется;
- write-path надежнее;
- решение совместимо и с API-first сейчас, и с API + events позже.

Минусы:
- нужен local outbox / relay;
- нужны retry, monitoring и dead-letter strategy;
- audit становится eventual consistent.

## Rejected For MVP
- обязательный synchronous remote audit call
- fire-and-forget без durability