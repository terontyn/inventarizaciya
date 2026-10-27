# ADR-0006 Service Integration Pattern

Status: proposed
Date: 2026-03-27

## Context
Открытый вопрос:
- в Foundation Slice API-first
- позже API + events
- event bus choice отдельно, т.к. он еще открыт

Также архитектурный принцип проекта:
сервисы не читают БД друг друга и взаимодействуют через API и/или events.

## Decision
Для Foundation Slice:
- принять API-first integration pattern;
- synchronous validation между asset-service и user-service делать через versioned internal APIs;
- event-driven integration оставить для следующей волны;
- выбор event bus не блокирует MVP и выносится в отдельный ADR позже.

## Why
- это минимально сложный путь;
- подходит для старта с двумя сервисами;
- уменьшает premature platform complexity.

## Consequences
Плюсы:
- проще реализация;
- меньше инфраструктурной сложности;
- легче отлаживать MVP.

Минусы:
- часть событийной модели появится позже;
- надо аккуратно проектировать API boundaries.

## Follow-up
Обновить:
- /docs/01-architecture/bounded-contexts.md
- /docs/01-architecture/microservices.md
- /docs/04-api/user-api.md
- /docs/04-api/asset-api.md