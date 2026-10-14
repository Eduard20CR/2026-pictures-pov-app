#!/usr/bin/env bash
# Runs the API tests locally against a throwaway Postgres in Docker (mirrors .github/workflows/api-ci.yml).
set -euo pipefail

CONTAINER=pov-db
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# Stop the container when the script exits, even if tests fail
trap 'docker stop "$CONTAINER" >/dev/null' EXIT

# 1. Start Postgres (reuse the container if it already exists)
if docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER"; then
  docker start "$CONTAINER" >/dev/null
else
  docker run -d --name "$CONTAINER" -p 5432:5432 \
    -e POSTGRES_DB=picturepov -e POSTGRES_USER=app -e POSTGRES_PASSWORD=app \
    postgres:17-alpine >/dev/null
fi

# 2. Wait until Postgres accepts connections
until docker exec "$CONTAINER" pg_isready -U app >/dev/null 2>&1; do sleep 1; done

# 3. Same variables as the workflow's env:
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/picturepov
export SPRING_DATASOURCE_USERNAME=app
export SPRING_DATASOURCE_PASSWORD=app

# 4. Run the tests
cd "$REPO_ROOT/apps/api"
./mvnw -B verify