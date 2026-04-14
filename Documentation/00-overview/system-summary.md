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

## Current Implementation Baseline

На текущем этапе проектный канон должен различать:
- scope / architecture / ADR baseline;
- current implementation baseline in repo.


### Current clarification
Для уже реализованных technical slices повторная постановка greenfield implementation task недопустима.

### Current known repo baseline
- Slice 1 (`Tenant + Org Reference Base` in `user-service`) уже реализован;
- Slice 2 (`User + Role Base` in `user-service`) уже реализован на core уровне и не должен переоткрываться как новый implementation slice;
- дальнейшие задачи по этой области должны трактоваться как:
  - review / hardening / gap-closure,
  - или следующий implementation step,
  - но не как повторная реализация уже существующего slice.
- Minimal Pilot UI Contour уже реализован и не должен переоткрываться как новый greenfield implementation contour;


### Rule
Перед подготовкой нового implementation packet для Codex
нужно сначала явно проверить текущий repo state
и зафиксировать, действительно ли target slice еще не реализован.

## Minimal Pilot UI Contour

После backend-first managed pilot baseline следующим contour проекта
является Minimal Pilot UI Contour.

Это не новый модуль и не новый bounded context.
Это минимальный user-facing completion layer,
нужный для practical operational use уже утвержденного Asset Accountability MVP
одним tenant.

## Minimal Pilot Access Contour

После backend + minimal pilot UI internally validated baseline
следующим contour проекта является Minimal Pilot Access Contour.

Это не новый модуль, не новый bounded context
и не новый auth platform contour.

### Purpose
Довести уже утвержденный MVP до practically usable tenant-facing access state
для реального первого tenant.

### Interpretation rule
Minimal Pilot Access Contour трактуется как минимальный completion layer,
который дает user-facing access path в уже утвержденный MVP contour,
но не открывает новый enterprise IAM scope.

## Minimal Pilot Access Contour — Architecture Clarification

Minimal Pilot Access Contour трактуется как минимальный tenant-facing access completion layer
поверх уже реализованного Minimal Pilot UI Contour и существующего backend contour текущего MVP.

### Interpretation rule
- это не новый модуль;
- это не новый bounded context;
- это не новый auth platform contour;
- это не новый backend service family.

### Consumption rule
Access contour должен опираться на уже существующие слои:
- existing UI contour как user-facing surface;
- `user-service` как owner tenant/user/role foundation;
- `asset-service` как owner asset-side MVP surfaces.

### Architectural rule
В рамках текущего contour не открываются:
- отдельный auth service;
- отдельный IAM backend;
- отдельный BFF;
- новый enterprise IAM scope;
- новые backend endpoints только ради access convenience.

## Minimal Pilot UI Contour — Architecture Clarification

Minimal Pilot UI Contour трактуется как минимальный user-facing completion layer
поверх уже утвержденного backend contour текущего MVP.

### Interpretation rule
- это не новый модуль;
- это не новый bounded context;
- это не новый integration contour;
- это не новый API/read-model contour.

### Consumption rule
UI contour должен опираться на уже существующие service boundaries:
- `asset-service` для asset registry / asset card / asset CRUD v1 / dedicated responsibility flows / history-audit visibility;
- `user-service` для roles / users / departments / branches / locations.

### Architectural rule
В рамках текущего contour не открываются:
- отдельный BFF;
- отдельный UI service;
- отдельная microfrontend family;
- новые backend endpoints только ради UI convenience.


## Wave 2 Sequencing Clarification

После закрытия Asset Accountability MVP
следующим рекомендуемым contour внутри Wave 2
является `Inventory / Warehouse`.

### Why this contour next
Этот contour является ближайшим product extension
к уже утвержденному asset accountability baseline.

Текущий MVP отвечает на вопросы:
- что у нас есть;
- где это находится;
- за кем это закреплено;
- кто это менял.

Следующий практический operational gap:
- что сейчас находится в storage / warehouse custody;
- где именно это хранится;
- что доступно для следующей выдачи;
- как актив переходит из хранения в выдачу и обратно.

### Interpretation rule
`Inventory / Warehouse` на первом шаге трактуется не как full WMS / ERP,
а как bounded storage-aware contour
для already tracked physical assets.

## Inventory / Warehouse — Architecture Clarification

После закрытия Asset Accountability MVP
следующим рекомендуемым Wave 2 contour
является `Inventory / Warehouse`.

### Interpretation rule
Этот contour трактуется узко:
как storage-aware operational layer
для already tracked physical assets.

Он не трактуется как:
- full WMS;
- ERP warehouse module;
- procurement / supplier / finance contour;
- reporting-first contour.

### Source-of-truth rule
- `asset-service` остается source of truth для asset identity и master record;
- existing responsibility flows остаются source of truth для responsibility lifecycle;
- `Inventory / Warehouse` добавляет отдельную operational dimension:
  storage custody и warehouse placement.