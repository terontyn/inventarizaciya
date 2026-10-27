# Microservices

## user-service
Отвечает за tenant, users, roles, org structure.

## asset-service
Отвечает за карточки активов и их жизненный цикл.

## ticket-service
Отвечает за заявки и привязку к пользователям/активам.

## inventory-service
Отвечает за склады и инвентаризации.

## software-service
Отвечает за ПО и лицензии.

## maintenance-service
Отвечает за ремонты и обслуживание.


## Minimal Pilot UI Contour Note

Текущий MVP contour остается оперируемым через существующие backend services:
- `user-service`
- `asset-service`

### Current UI interpretation
Minimal Pilot UI Contour добавляет user-facing delivery layer,
но не требует открытия новой backend service boundary.

### Not justified in current contour
- отдельный BFF
- отдельный UI-only backend
- отдельная microfrontend family
- новые backend endpoints только для UI composition

### Practical rule
UI implementation должна повторно использовать уже существующие versioned service contracts
и не менять текущие domain/API boundaries.

## Minimal Pilot Access Contour Note

Текущий MVP contour остается оперируемым через существующие backend services:
- `user-service`
- `asset-service`

### Current access interpretation
Minimal Pilot Access Contour добавляет minimal tenant-facing access path,
но не требует открытия новой backend service boundary.

### Not justified in current contour
- отдельный auth service
- отдельный IAM backend
- отдельный BFF
- новая backend service family

### Practical rule
Access implementation должна повторно использовать уже существующие service boundaries
и уже реализованный Minimal Pilot UI Contour,
не меняя текущие domain/API boundaries.


## inventory-service

Отвечает за storage custody и warehouse placement
для already tracked physical assets.

### Narrow Wave 2 interpretation
`inventory-service` в первом contour
не трактуется как full warehouse platform.

Он не включает:
- procurement
- supplier management
- finance / depreciation
- advanced logistics
- reporting-first scope
- mandatory barcode / QR / scanning / printer integrations

### Boundary rule
`inventory-service` работает поверх asset registry,
но не становится owner asset master record.