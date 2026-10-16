#!/usr/bin/env bash
# Builds the Lambda zip for the API (run.sh + app.jar) at apps/api/target/api.zip.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT/apps/api"

# 1. Build the Spring Boot jar
./mvnw -B -q clean package -DskipTests

# 2. Package it with the startup script at the zip root
cp target/api-0.0.1-SNAPSHOT.jar target/app.jar
zip -j target/api.zip run.sh target/app.jar
rm target/app.jar

echo "Built $(pwd)/target/api.zip"