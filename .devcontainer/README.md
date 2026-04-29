# GitHub Codespaces

This folder defines a Codespaces/devcontainer setup for the IT Asset Management SaaS monorepo.

## What Starts Automatically

- A Java 17 development container.
- A PostgreSQL 16 container.
- Databases for the implemented service modules:
  - `user_service`
  - `asset_service`
  - `inventory_service`
  - `maintenance_service`
  - `software_service`
  - `ticket_service`

The devcontainer runs `./gradlew testClasses` after creation so Gradle, the JDK, and the project graph are warmed up.

## Start The Pilot UI Services

Open a terminal in Codespaces and run:

```bash
.devcontainer/scripts/start-core-services.sh
```

This starts:

- `user-service` on port `8080`
- `asset-service` and the static UI on port `8081`

Open the forwarded `8081` port for the UI. In the UI context form, use the forwarded `8080` port URL as the user-service URL. In Codespaces this is usually shown in the **Ports** tab.

## Run Checks

```bash
./gradlew test
```

For a faster compile-only pass:

```bash
./gradlew testClasses
```

## Ports

- `8080`: user-service
- `8081`: asset-service UI
- `8082`: inventory-service
- `8083`: maintenance-service
- `8084`: software-service
- `8085`: ticket-service
- `8086`: reporting-service
- `5432`: PostgreSQL
