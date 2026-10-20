#!/usr/bin/env bash
# Runs Liquibase against the database configured in /db. Usage: ./db-liquibase.sh <command>
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT/db"

export LIQUIBASE_COMMAND_URL=jdbc:postgresql://localhost:5432/picturepov
export LIQUIBASE_COMMAND_USERNAME=app
export LIQUIBASE_COMMAND_PASSWORD=app

exec liquibase "$@"