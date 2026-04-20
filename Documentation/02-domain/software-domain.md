# Software Domain

Status: draft
Owner: architecture

## Purpose
Software domain добавляет software-aware operational layer
для already tracked tenants, users и assets,
не переопределяя asset identity,
asset master record,
inventory ownership
или maintenance ownership.

## Scope
Этот документ фиксирует только narrow first Software contour:
- software record
- software-to-user allocation visibility
- software-to-asset allocation visibility

## Core Rule
Software context отвечает на вопрос:
какие software records существуют сейчас
и с какими user / asset они currently related.

## Ownership rule
- `asset-service` остается owner asset identity / master record / responsibility lifecycle / final assign decision logic
- `inventory-service` остается owner storage custody / placement / readiness semantics
- `maintenance-service` остается owner maintenance-side serviceability workflow
- software context не становится owner procurement / renewals / supplier / finance / compliance program

## Relation to existing domains
- software allocations используют existing tracked `user_id` и `asset_id` только как reference ids
- software visibility не переопределяет ownership user или asset
- software context не канонизирует inventory readiness
- software context не канонизирует maintenance workflow

## Out of Scope
- full SAM suite
- procurement / renewals / supplier / finance
- compliance-heavy program
- reporting-first contour
- generic helpdesk / ITSM platform

## Minimal Software Record Model

Для narrow first Software contour
достаточно следующего минимального набора:
- `SoftwareRecord`
- current software-to-user allocation visibility
- current software-to-asset allocation visibility

### SoftwareRecord interpretation
`SoftwareRecord` является software-side tracked record,
а не procurement / renewal / compliance record.

### Minimal business representation
Для first contour
достаточно narrow record representation:
- `name`

Этот contour намеренно не канонизирует:
- procurement / renewals meaning
- supplier ownership
- finance meaning
- compliance-heavy license model

## Allocation semantics

### Current-only rule
First contour канонизирует только current allocation visibility.

### Many-to-many rule
Software context должен уметь показывать:
- current many-to-many software-to-user relations;
- current many-to-many software-to-asset relations.

### Boundary rule
Эти relations:
- не переопределяют ownership user;
- не переопределяют ownership asset;
- не переопределяют responsibility lifecycle;
- не канонизируют inventory readiness;
- не канонизируют maintenance workflow.

## Software Workflow Semantics

### Dedicated write-action rule
Для first contour
software mutations фиксируются как explicit operator actions:
- create software record
- allocate to user
- allocate to asset
- remove from user
- remove from asset

Software contour не канонизирует automatic allocation / removal
как side effect других bounded contexts.

### Create record rule
Create software record
является explicit operator action.

Successful create означает только следующее:
- существует tenant-scoped `SoftwareRecord`;
- record represented as current software-side tracked record.

### Allocation rule
Allocate to user / allocate to asset
являются explicit operator actions.

Минимальные preconditions:
- tenant scope корректен;
- target `SoftwareRecord` существует;
- target reference id существует
  как already tracked user или asset;
- duplicate current relation отсутствует.

### Removal rule
Remove from user / remove from asset
являются explicit operator actions.

Минимальные preconditions:
- tenant scope корректен;
- target `SoftwareRecord` существует;
- target reference id существует
  как already tracked user или asset;
- corresponding current relation существует.

### Many-to-many interpretation
First contour канонизирует:
- один `SoftwareRecord` может иметь many current user relations;
- один `SoftwareRecord` может иметь many current asset relations;
- один user может иметь many current software relations;
- один asset может иметь many current software relations.

### Boundary to other contours
Software allocation visibility:
- не переопределяет ownership user;
- не переопределяет ownership asset;
- не переопределяет responsibility lifecycle;
- не канонизирует inventory readiness;
- не канонизирует maintenance workflow.

Следовательно:
- allocation to user не является responsibility fact;
- allocation to asset не требует inventory-owned readiness semantics;
- allocation to asset не требует maintenance-owned workflow semantics
  в first contour canon.