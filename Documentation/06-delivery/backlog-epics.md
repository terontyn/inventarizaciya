# Backlog Epics

Status: approved
Owner: product
Last Updated: 2026-04-02

## Wave 1 Epics

### Epic 1. Tenant & Access Foundation
Цель: заложить tenant-aware основу первого релиза.

### Epic 2. User & Org Structure
Цель: создать справочник пользователей и оргструктуры, на который будут опираться активы.

### Epic 3. Asset Registry Core
Цель: дать базовый реестр и карточку актива как ядро первой бизнес-ценности.

### Epic 4. Audit Trail Foundation
Цель: обеспечить обязательный audit и историю изменений для критических операций.

### Epic 5. Asset Accountability Flows
Цель: реализовать выдачу, перемещение и возврат как ключевой операционный value loop.

### Epic 6. MVP Pilot Readiness
Цель: довести существующий Asset Accountability MVP до состояния first-tenant operational use без расширения scope.

Acceptance criteria:
- определены pilot personas:
  - Tenant Admin
  - Asset Operator
  - Support Reviewer
- утвержден minimum permission matrix для pilot-ready состояния;
- зафиксированы allowed / forbidden boundaries для pilot roles;
- users / roles / departments / branches / locations достаточны для первого tenant;
- asset registry и asset card usable для ежедневного базового учета;
- responsibility changes не происходят через generic PATCH;
- assign / transfer / return доступны как dedicated flows;
- history / audit видимы для operational support и review;
- определен минимальный admin / support layer для первого tenant.
- зафиксирован minimum reference-data ownership / setup model для первого pilot tenant;
- подтвержден minimum tenant-managed reference-data set:
  - roles
  - users
  - departments
  - branches
  - locations
- ownership rules зафиксированы:
  - Tenant Admin создает и поддерживает reference data;
  - Asset Operator использует только разрешенный reference read-only access;
  - Support Reviewer остается read-only;
- setup order before pilot start зафиксирован:
  - roles
  - pilot users with role assignment
  - branches
  - locations
  - departments
- reference-data setup model не вводит новый master-data module, import engine или extra asset-side catalogs.

Non-goals:
- inventory / warehouse;
- software licenses / SAM;
- maintenance & repair;
- reporting module;
- heavy BI;
- expanded integrations;
- custom workflows;
- retirement как обязательный pilot gate.