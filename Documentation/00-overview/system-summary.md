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

Цель Foundation Slice:
- не начинать asset-service в вакууме;
- получить минимальный архитектурный фундамент;
- дать первую бизнес-ценность уже на раннем этапе.

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

## Current Foundation Services
На текущем этапе Foundation Slice состоит из двух сервисов:
- user-service
- asset-service

## Architectural Rules
- каждый сервис владеет своей БД;
- прямой доступ к БД другого сервиса запрещен;
- взаимодействие между сервисами идет через API и/или events;
- tenant isolation обязателен;
- API должен быть versioned;
- критичные write-операции должны уходить в audit;
- важные архитектурные решения фиксируются в ADR.

## Open ADR Backlog
- tenancy model
- tenant resolution & enforcement
- role / permission model
- org hierarchy
- asset CRUD vs domain history
- asset delete semantics
- service integration pattern
- audit integration contract