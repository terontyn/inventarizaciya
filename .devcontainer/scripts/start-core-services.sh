#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

if [[ -n "${CODESPACE_NAME:-}" && -n "${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN:-}" ]]; then
  CODESPACE_ASSET_ORIGIN="https://${CODESPACE_NAME}-8081.${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN}"
  CODESPACE_USER_ORIGIN="https://${CODESPACE_NAME}-8080.${GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN}"
else
  CODESPACE_ASSET_ORIGIN=""
  CODESPACE_USER_ORIGIN=""
fi

export UI_ALLOWED_ORIGINS="${UI_ALLOWED_ORIGINS:-http://localhost:8081${CODESPACE_ASSET_ORIGIN:+,${CODESPACE_ASSET_ORIGIN}}}"

wait_for_postgres() {
  echo "Waiting for PostgreSQL at postgres:5432..."
  for _ in {1..60}; do
    if (echo > /dev/tcp/postgres/5432) >/dev/null 2>&1; then
      echo "PostgreSQL is ready."
      return 0
    fi
    sleep 1
  done

  echo "PostgreSQL did not become ready in time." >&2
  return 1
}

cleanup() {
  if [[ -n "${USER_SERVICE_PID:-}" ]]; then
    kill "$USER_SERVICE_PID" >/dev/null 2>&1 || true
  fi
  if [[ -n "${ASSET_SERVICE_PID:-}" ]]; then
    kill "$ASSET_SERVICE_PID" >/dev/null 2>&1 || true
  fi
}
trap cleanup INT TERM EXIT

wait_for_postgres

echo "Starting user-service on port 8080..."
SERVER_PORT=8080 \
USER_SERVICE_DB_URL="${USER_SERVICE_DB_URL:-jdbc:postgresql://postgres:5432/user_service}" \
USER_SERVICE_DB_USERNAME="${USER_SERVICE_DB_USERNAME:-postgres}" \
USER_SERVICE_DB_PASSWORD="${USER_SERVICE_DB_PASSWORD:-postgres}" \
UI_ALLOWED_ORIGINS="$UI_ALLOWED_ORIGINS" \
./gradlew --no-daemon :services:user-service:bootRun &
USER_SERVICE_PID=$!

echo "Starting asset-service and static UI on port 8081..."
SERVER_PORT=8081 \
ASSET_SERVICE_DB_URL="${ASSET_SERVICE_DB_URL:-jdbc:postgresql://postgres:5432/asset_service}" \
ASSET_SERVICE_DB_USERNAME="${ASSET_SERVICE_DB_USERNAME:-postgres}" \
ASSET_SERVICE_DB_PASSWORD="${ASSET_SERVICE_DB_PASSWORD:-postgres}" \
USER_SERVICE_BASE_URL="${USER_SERVICE_BASE_URL:-http://localhost:8080}" \
INVENTORY_SERVICE_BASE_URL="${INVENTORY_SERVICE_BASE_URL:-http://localhost:8082}" \
INVENTORY_ASSIGN_HANDOFF_ENABLED="${INVENTORY_ASSIGN_HANDOFF_ENABLED:-false}" \
./gradlew --no-daemon :services:asset-service:bootRun &
ASSET_SERVICE_PID=$!

echo
echo "Core services are starting."
echo "Asset UI: ${CODESPACE_ASSET_ORIGIN:-http://localhost:8081}"
echo "User-service URL for the UI form: ${CODESPACE_USER_ORIGIN:-http://localhost:8080}"
echo
echo "Press Ctrl+C to stop both services."

wait -n "$USER_SERVICE_PID" "$ASSET_SERVICE_PID"
