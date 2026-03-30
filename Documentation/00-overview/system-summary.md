# System Summary

Status: approved
Owner: project
Last Updated: 2026-03-27

## Purpose
IT Asset Management SaaS для учета активов, оргструктуры, ответственности за технику и истории критических изменений.

## Current Project Stage
Проект находится на этапе Foundation Slice и MVP.

## Foundation Slice
Foundation Slice = Tenant + User/Org + Asset + Audit.

На текущем этапе Foundation Slice реализуется через два сервиса:
- user-service
- asset-service

## MVP Definition
Первый релиз фиксируется как Asset Accountability MVP.

MVP должен позволять одному tenant:
- вести tenant-aware учет активов;
- хранить пользователей и оргструктуру;
- закреплять актив за пользователем и локацией;
- выполнять базовые операции ответственности: assign / transfer / return;
- хранить audit и историю критических действий.

## Wave 1 / Release 1
1. Tenant / Access foundation
2. User / Org
3. Asset core
4. Audit / History
5. Asset responsibility flows

## Wave 2
- Ticket
- Inventory
- Software
- Maintenance
- Reporting

## Accepted ADRs
- ADR-0001 Tenancy model: Foundation Slice uses shared DB/shared schema with mandatory tenant_id and tenant enforcement at API/repository boundaries.
- ADR-0002 Role / permission model: tenant-scoped RBAC; User ↔ Role is many-to-many; permissions are attached to roles only.
- ADR-0003 Org hierarchy: no separate Org entity; Department is managerial dimension; Branch / Location are physical dimensions; Location belongs to Branch.
- ADR-0004 Asset CRUD vs history: generic CRUD manages only Asset master record; responsibility-changing operations use dedicated flows.
- ADR-0007 Audit contract: critical writes produce durable local audit records and are delivered asynchronously to central audit storage.

## Architectural Rules
- каждый сервис владеет своей БД;
- прямой доступ к БД другого сервиса запрещен;
- взаимодействие между сервисами идет через API и/или events;
- tenant isolation обязателен;
- API должен быть versioned;
- критичные write-операции должны попадать в audit;
- важные архитектурные решения фиксируются в ADR.

## Open ADR Backlog
- asset delete semantics
- service integration pattern