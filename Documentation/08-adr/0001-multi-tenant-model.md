# ADR-0001 Tenancy Model

Status: proposed
Date: 2026-03-27

## Context
Проект поддерживает multi-tenant SaaS модель.
Варианты, которые уже обсуждались:
- shared database + tenant_id
- schema per tenant
- dedicated database per tenant

Foundation Slice нужно запускать быстро и без лишней сложности.

## Decision
Для Foundation Slice и MVP принять:
shared database + explicit tenant_id во всех tenant-scoped таблицах.

## Why
- это самый быстрый путь к MVP;
- он согласуется с минимальной моделью таблиц для user-service и asset-service;
- он позволяет сразу enforce tenant isolation на уровне API, сервиса и запросов;
- он не мешает позже перейти к более изолированным вариантам для enterprise-tier.

## Consequences
Плюсы:
- быстрее старт;
- проще DDL и API;
- проще testing и migration strategy.

Минусы:
- нужен строгий tenant enforcement;
- возрастает риск ошибок, если пропустить tenant filter;
- enterprise isolation позже может потребовать эволюции архитектуры.

## Follow-up
Обновить:
- /docs/00-overview/system-summary.md
- /docs/02-domain/user-domain.md
- /docs/02-domain/asset-domain.md
- /docs/03-data/user-er.md
- /docs/03-data/asset-er.md