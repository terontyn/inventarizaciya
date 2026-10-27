# Reporting Domain

Status: draft
Owner: architecture

## Purpose
Reporting domain добавляет narrow cross-context operational visibility layer
поверх уже существующих current-state contours,
не переопределяя ownership existing domains.

## Scope
Этот документ фиксирует только narrow first Reporting contour:
- asset-centric current-state operational visibility
- cross-context read aggregation
- reporting-side visibility semantics

## Core Rule
Reporting context отвечает на вопрос:
какова текущая operational picture
по already tracked assets
поверх existing current-state owner contexts.

## Ownership rule
- `asset-service` остается owner asset identity / master record / responsibility lifecycle / final assign decision logic
- `inventory-service` остается owner storage custody / placement / readiness semantics
- `maintenance-service` остается owner maintenance-side serviceability workflow
- `software-service` остается owner software-side records и current software allocations
- `ticket-service` остается owner ticket-side coordination lifecycle
- reporting context не становится owner source operational facts

## Relation to existing domains
- reporting uses existing current-state facts only as read inputs
- reporting visibility не переопределяет ownership existing domains
- reporting context не канонизирует write ownership
- reporting context не канонизирует orchestration ownership

## Out of Scope
- BI-first analytics platform
- heavy KPI / dashboard suite
- free-form reporting platform
- umbrella orchestration semantics
- historical analytics-first contour

## Minimal Reporting Read Model

Для narrow first Reporting contour
достаточно одного primary reporting-side read model:
- `AssetOperationalCurrentView`

Этот read model:
- остается asset-centric;
- остается current-state-first;
- не является source-of-truth record;
- не переопределяет ownership existing domains.

## Minimal reporting questions

Первый contour должен уметь отвечать на вопросы:
- где asset находится сейчас;
- ready он или not ready;
- есть ли current open maintenance work;
- есть ли current open tickets;
- какие current software relations существуют.

## Aggregation rule

Для first contour
достаточно одного aggregated asset-centric current view,
а не набора BI/dashboard surfaces
или free-form reporting model.

## Boundary rule

Reporting-side visibility:
- использует existing current-state facts только как read inputs;
- не канонизирует write ownership;
- не канонизирует orchestration ownership;
- не открывает historical BI-heavy contour.

## Reporting Workflow Semantics

### Derived-current-visibility rule
Для first contour
reporting-side visibility трактуется
только как derived current visibility
поверх owner contexts.

Reporting context:
- не создает новые canonical operational facts;
- не становится source-of-truth layer;
- не канонизирует write ownership.

### Refresh / update interpretation
Reporting-side current view
должен трактоваться
как internal recomposition
текущих owner-side facts.

Этот contour не требует отдельного reporting-side write workflow
и не открывает business mutation semantics
в reporting context.

### Partial-composition rule
First contour допускает partial current-state composition.

Следовательно:
- reporting view может содержать только те current slices,
  которые сейчас derived из owner contexts;
- отсутствие одного derived slice
  не требует artificial canonical fill-in
  из другого bounded context.

### Asset-centric rule
Первый contour остается asset-centric.

`AssetOperationalCurrentView`
является достаточным primary read model
для narrow first Reporting contour.

## Reporting Post-Closeout Clarification

Для current Wave 2 stage
первый narrow `Reporting` contour
считается достаточным
как minimal cross-context operational visibility layer.

### Current interpretation
На этом этапе reporting уже покрывает:
- asset-centric current-state reporting visibility;
- `AssetOperationalCurrentView`;
- cross-context derived read aggregation;
- read-only reporting semantics.

### Deferred by default
Следующие возможные расширения
не считаются default next step:
- historical reporting;
- KPI / dashboard catalog;
- trend semantics;
- broader read surfaces beyond asset-centric current view;
- richer aggregation semantics.

Эти темы остаются deferred
до отдельного product decision.