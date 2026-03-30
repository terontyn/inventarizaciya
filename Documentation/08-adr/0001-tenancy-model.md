# ADR-0001 Модель tenancy для Foundation Slice

Status: accepted
Date: 2026-03-27

## Context
Проект является multi-tenant SaaS.
Для Foundation Slice нужны простая модель изоляции tenant и предсказуемый путь к MVP.
Текущая архитектурная база:
- каждый сервис владеет своей БД;
- прямой доступ к БД другого сервиса запрещен;
- tenant isolation обязателен.

## Options
1. Shared DB + shared schema + tenant_id
2. Shared DB + schema per tenant
3. Dedicated DB per tenant
4. Гибрид сразу

## Decision
Для Foundation Slice и Release 1 принимается:
shared DB + shared schema + обязательный tenant_id во всех tenant-scoped таблицах.

Tenant определяется на API boundary из аутентифицированного контекста.
Сервисы не принимают tenant как свободный бизнес-параметр.
Все запросы и проверки выполняются только в tenant scope.
Все tenant-aware уникальности делаются composite, например:
(tenant_id, asset_tag)

## Consequences
Плюсы:
- быстрый старт MVP;
- простая и прозрачная DDL-модель;
- согласованность с текущими user-service и asset-service.

Минусы:
- repository/query layer обязан по умолчанию применять tenant filter;
- cross-context reference checks должны быть tenant-scoped;
- later для enterprise-tier может понадобиться отдельный deployment tier.

## Rejected For MVP
- schema per tenant как baseline
- dedicated DB per tenant как baseline
- гибридный tenancy tier с первого релиза