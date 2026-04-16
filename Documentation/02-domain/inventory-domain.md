# Inventory Domain

Status: draft
Owner: architecture

## Purpose
Inventory / Warehouse domain добавляет storage-aware operational layer
для already tracked physical assets,
не переопределяя asset identity, asset master record
или responsibility lifecycle.

## Scope
Этот документ фиксирует только первый narrow Wave 2 contour:
- storage custody
- warehouse placement
- minimal moves between storage places

## Core Entities
- Warehouse
- StoragePlace
- InventoryRecord

## Core Rules
- `Asset` остается master record в `asset-service`.
- `InventoryRecord` не является новым asset master record.
- `InventoryRecord` существует только как inventory-side current operational record.
- `InventoryRecord` ссылается на Asset только через `asset_id`.
- `StoragePlace` принадлежит `Warehouse`.

## Storage Custody Rule
Storage custody является отдельной operational dimension
и не должна маскироваться под responsibility lifecycle.

## Placement Rule
Warehouse placement фиксируется внутри inventory context
и не переопределяет existing org / responsibility model.

## Visibility Questions This Domain Must Answer
- что сейчас находится в storage custody;
- где именно это размещено;
- что inventory-side видит как доступное для следующей выдачи.

## Availability Clarification
`available for next assignment` в этом contour означает:
Asset сейчас находится в storage custody.

Это не переопределяет final assign eligibility,
потому что переход в user assignment
остается в existing assign flow.

## State Vocabulary Clarification
Первый contour не должен загрязнять `Asset.status`
warehouse/storage состояниями.

Минимально достаточно:
- хранить current storage custody отдельно от asset lifecycle vocabulary;
- трактовать наличие current `InventoryRecord`
  как признак того, что Asset сейчас находится в storage custody.

Если later понадобится richer inventory-side state vocabulary,
она должна вводиться отдельно внутри inventory context.

## Out of Scope
- full WMS
- procurement / supplier / finance / depreciation
- consumables / stock balances / SKU model
- reporting-first contour
- barcode / QR / scanning / printer integrations
- responsibility lifecycle ownership
- asset master record ownership

## Workflow Semantics Clarification

### Handoff to existing assign flow
Переход из current storage custody в current end-user assigned state
происходит через handoff в existing assign flow.

Для первого contour storage exit
не трактуется как отдельный inventory-side top-level workflow.
Current storage custody representation
должна считаться consumed
только при успешном assign handoff.

Следовательно:
- до успешного assign Asset остается в storage custody;
- после успешного assign Asset больше не считается `in storage now`;

## ADR-0009 Handoff Integration Clarification

Для первого contour
system of action в точке handoff
остается existing assign flow в `asset-service`.

Successful assign completion
должна считаться trigger point,
после которого current inventory-side storage custody representation
подлежит consumption.

Это consumption:
- не открывает отдельный public inventory exit workflow;
- выполняется через internal-only asynchronous integration;
- не является synchronous precondition для assign success.

Следовательно:
- первый contour допускает eventual consistency
  между successful assign и inventory-side storage custody consumption;
- если inventory-side consumption временно не выполнен,
  assign не откатывается только по этой причине;
- после успешной обработки handoff в inventory context
  Asset больше не должен иметь current `InventoryRecord`.

### Return-to-storage clarification
`Return` сам по себе не создает storage custody.

Если после existing return
Asset фактически принимается под warehouse / storage custody,
это должно фиксироваться как отдельная explicit inventory-side operation
через `place-in-storage`.

Следовательно:
- post-return storage intake не является неявной частью `return`;
- Asset может быть post-return,
  но еще не `in storage now`,
  пока explicit inventory intake не выполнен.

### Move semantics
Для первого contour intra-warehouse move
и inter-warehouse move
являются вариантами одного inventory-side move workflow.

Этот workflow:
- меняет только current storage placement;
- не переопределяет responsibility lifecycle;
- не трактуется как asset-domain `transfer`.

### Available for next assignment
В inventory context
`available for next assignment`
означает только следующее:

- Asset сейчас находится в storage custody,
  то есть имеет current `InventoryRecord`.

Это inventory-side readiness signal,
а не final assign eligibility.

Final decision о создании новой текущей пользовательской ответственности
остается в existing assign flow.

### Current-state vs history
Первый contour канонизирует только current storage custody representation.

Separate inventory-side history / closeout surface
beyond current-state representation
в этом contour не требуется.

Отдельный fact trail по storage entry / move / storage exit
для первого contour достаточно обеспечивать через mandatory audit.

Final physical current-vs-history model
остается отдельным open decision.