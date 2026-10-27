# Учёт IT-оборудования

Система учёта оборудования, организационной структуры и ответственности за активы. Реализованы реестр и карточка актива, выдача, передача и возврат, история изменений, складской учёт, обслуживание, учёт ПО, тикеты и операционная сводка.

**Стек:** Java 17, Spring Boot, Gradle, PostgreSQL, JDBC, Flyway, JUnit, Docker Compose.

## Архитектура

Семь сервисов разделяют пользователей и роли (`user-service`), активы (`asset-service`), склад (`inventory-service`), обслуживание (`maintenance-service`), ПО (`software-service`), тикеты (`ticket-service`) и отчёты (`reporting-service`). Минимальный веб-интерфейс доступен из `asset-service` на `/`.

## Знакомство с проектом

- [Реестр и операции с оборудованием](services/asset-service/README.md).
- [Пользователи и организационная структура](services/user-service/README.md).
- [Складской учёт](services/inventory-service/README.md).
- Локальная конфигурация окружения: `ops/docker/docker-compose.local.yml`.
- Проверка сервиса: `./gradlew :services:asset-service:test` (Windows: `gradlew.bat :services:asset-service:test`).

Текущая публичная версия содержит ограниченный пилотный интерфейс. Расширенные QR-сценарии, SIM-карты и картриджи не входят в эту основную ветку. Контекст пользователя в пилоте не заменяет готовую систему аутентификации; Kubernetes и мониторинг представлены заготовками. Это не подтверждение готовности к работе с реальными данными организации.

## Подробности реализации


This repository is the working monorepo for the IT Asset Management SaaS project.
Canonical product, architecture, domain, data, API, and ADR documents stay in the
root `Documentation/` directory and remain the source of truth.

## Current Status

- `services/user-service/` already contains the Slice 1 and Slice 2 baseline for tenant, org reference, user, role, and role-binding paths.
- `services/user-service/` is the managed-pilot setup surface for roles, users, departments, branches, locations, and tenant-scoped role assignment.
- `services/asset-service/` already contains Asset CRUD v1, dedicated `assign` / `transfer` / `return` flows, and `/history` + `/audit` read endpoints.
- `services/asset-service/` now also serves the minimal pilot UI contour at `/`, reusing only the current `asset-service` and `user-service` contracts.
- `services/asset-service/` keeps `retire` behind the default pilot cutline, so the endpoint remains forbidden for the approved pilot role set.
- `services/inventory-service/` now contains the first narrow Inventory / Warehouse contour for warehouse read, storage placement read, current storage custody, `place-in-storage`, `move`, and internal assign handoff consumption.
- `services/maintenance-service/` now contains the first narrow Maintenance contour for current open work visibility, asset-scoped maintenance current view, and explicit `open` / `complete` lifecycle commands.
- `services/software-service/` now contains the first narrow Software contour for current software record visibility plus explicit user/asset allocation and removal commands.
- `services/ticket-service/` now contains the first narrow Ticket contour for asset-linked open ticket visibility and explicit `open` / `close` lifecycle commands.
- `services/reporting-service/` now contains the first narrow Reporting contour for asset-centric read-only operational current visibility derived from owner-context surfaces.
- `Documentation/` is still the canonical source of truth for scope and architecture decisions.

## Repository Layout

- `Documentation/` keeps approved and draft project documentation in the root.
- `services/user-service/` owns the Identity / Organization bounded context implementation path.
- `services/asset-service/` owns the Asset Management bounded context implementation path.
- `services/inventory-service/` owns the Inventory / Warehouse bounded context implementation path.
- `services/maintenance-service/` owns the Maintenance bounded context implementation path.
- `services/software-service/` owns the Software bounded context implementation path.
- `services/ticket-service/` owns the Ticket bounded context implementation path.
- `services/reporting-service/` owns the Reporting bounded context implementation path.
- `ops/` contains local docker, Kubernetes placeholders, and monitoring scaffolding.
- `scripts/` contains development, CI, and local database bootstrap helpers.
- `.github/workflows/` contains repository checks and service build/test workflows.

## Notes

- The repository is no longer bootstrap-only.
- Multiple backend service modules are implemented and should be treated as active service modules rather than placeholders.
- Service boundaries stay explicit; there is no shared business/domain module.
