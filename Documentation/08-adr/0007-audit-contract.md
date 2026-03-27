# ADR-0007 Audit Contract

Status: proposed
Date: 2026-03-27

## Context
Все критичные write-операции для Tenant, User, Role, Department, Branch, Location и Asset
должны уходить в audit.
Открытый вопрос:
- direct audit API
- domain events
- outbox later / not later

## Decision
Для Foundation Slice:
- использовать direct audit API как стартовый контракт;
- каждое критичное write-действие обязано отправлять audit event;
- event/outbox-based delivery отложить до следующего этапа после MVP.

## Minimal Audit Payload
Каждое audit-событие должно включать минимум:
- tenant_id
- entity_type
- entity_id
- action
- actor_id
- occurred_at

## Why
- быстрее старт;
- проще контролировать обязательность аудита в MVP;
- не требует сразу поднимать полноценную event pipeline.

## Consequences
Плюсы:
- простой и понятный контракт;
- быстрый старт audit trail.

Минусы:
- позже возможен рефакторинг под outbox/events;
- нужен контроль отказов и retry strategy.

## Follow-up
Обновить:
- /docs/01-architecture/observability.md
- /docs/04-api/user-api.md
- /docs/04-api/asset-api.md
- /docs/05-workflows/asset-assignment.md