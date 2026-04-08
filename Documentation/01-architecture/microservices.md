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