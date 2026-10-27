#!/usr/bin/env bash
# Exports the OpenAPI contract from the API and regenerates the frontend types in apps/web/src/api/.
# No database needed: the API opens no connection at startup.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PORT="${PORT:-8081}"
OUT="$REPO_ROOT/apps/web/src/api/openapi.yaml"

# 1. Build the jar
cd "$REPO_ROOT/apps/api"
./mvnw -B -q package -DskipTests

# 2. Start the API in the background and stop it when the script exits
java -jar target/api-0.0.1-SNAPSHOT.jar --server.port="$PORT" >/dev/null &
API_PID=$!
trap 'kill "$API_PID" 2>/dev/null' EXIT

# 3. Wait until the contract is served (max 60 s)
for _ in $(seq 60); do
  curl -fsS "http://localhost:$PORT/v3/api-docs.yaml" -o /dev/null 2>/dev/null && break
  sleep 1
done

# 4. Save the contract
mkdir -p "$(dirname "$OUT")"
curl -fsS "http://localhost:$PORT/v3/api-docs.yaml" -o "$OUT"

echo "Saved $OUT"

# 5. Generate the frontend types from the contract
cd "$REPO_ROOT/apps/web"
npm run api:types