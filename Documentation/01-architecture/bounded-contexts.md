# Bounded Contexts

Status: approved
Owner: architecture
Last Updated: 2026-03-27

## Identity / Organization
Service owner: user-service

Entities:
- Tenant
- User
- Role
- Department
- Branch
- Location

Clarifications:
- отдельной сущности Org в Foundation Slice нет;
- Department — организационная / управленческая ось;
- Branch и Location — физическая ось;
- Location принадлежит Branch;
- Permissions описываются как часть role model, но direct user permissions в Foundation Slice не поддерживаются.

## Asset Management
Service owner: asset-service

Entities:
- Asset
- AssetAssignment
- AssetTransfer
- AssetRetirement

Clarifications:
- в Foundation Slice v1 публичный CRUD покрывает только мастер-запись Asset;
- AssetAssignment / AssetTransfer / AssetRetirement остаются частью asset-domain, но реализуются как отдельные flows, а не как generic PATCH.

## Cross-Context Rules
- между bounded contexts передаются только reference IDs;
- прямое чтение чужой БД запрещено;
- asset-service хранит tenant-scoped reference IDs на user/org entities;
- валидация assigned_user_id и location_id выполняется через API user-service;
- cross-context checks всегда выполняются в tenant scope.

## Minimal Pilot UI Contour Clarification

Minimal Pilot UI Contour не создает новый bounded context.

### Rule
UI остается consumption layer поверх уже существующих bounded contexts:
- Identity / Organization
- Asset Management

### Boundary implications
- domain ownership не меняется;
- cross-context interaction по-прежнему идет только через существующие service APIs;
- UI не создает отдельный orchestration context;
- UI convenience не оправдывает новый shared read model или новый backend aggregation contour.

## Minimal Pilot Access Contour Clarification

Minimal Pilot Access Contour не создает новый bounded context.

### Rule
Access contour остается completion layer поверх уже существующих bounded contexts:
- Identity / Organization
- Asset Management

### Boundary implications
- domain ownership не меняется;
- current role / permission model не переоткрывается как новый IAM model;
- contour не оправдывает новый orchestration context или новый identity platform context.


## Inventory / Warehouse

Service owner: inventory-service

Purpose:
- tenant-scoped operational control
  над already tracked physical assets,
  которые находятся в storage / warehouse custody,
  а не в current end-user assigned state.

Entities:
- Warehouse
- StoragePlace
- InventoryRecord

### Boundary rule
Этот context:
- не владеет asset identity;
- не переопределяет asset master record;
- не переопределяет responsibility lifecycle;
- не трактуется как full WMS / ERP warehouse module.

### Relation to existing contexts
- `Asset Management` остается owner asset identity / master record
  и responsibility lifecycle;
- `Inventory / Warehouse` владеет storage custody
  и warehouse placement как отдельной operational dimension.

### Clarifications
- `InventoryRecord` является inventory-side operational record,
  а не новым asset master record;
- `InventoryRecord` ссылается на Asset только по reference id;
- `StoragePlace` принадлежит `Warehouse`;
- inventory context отвечает на вопросы:
  - что сейчас находится в storage custody;
  - где именно это размещено;
  - что inventory-side видит как доступное для следующей выдачи;
- final responsibility transition в user assignment остается
  в existing assign flow.

### Cross-context implication
Переход из storage custody в user assignment
происходит через handoff в existing assign flow.

Переход из user return обратно в storage custody
не должен маскироваться под сам `return`,
а трактуется как отдельная inventory-side operation.

### ADR-0009 Cross-context handoff clarification

Переход из storage custody в user assignment
инициируется successful assign completion в `asset-service`.

При этом:
- `asset-service` не удаляет inventory data напрямую;
- `inventory-service` не управляет responsibility lifecycle;
- consumption current inventory-side storage custody representation
  выполняется самим `inventory-service`
  через internal-only asynchronous integration;
- direct DB coupling между сервисами остается запрещен.