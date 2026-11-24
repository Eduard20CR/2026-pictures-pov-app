#!/usr/bin/env bash
# Rebuilds the database from scratch: drops every table (data, users and Liquibase history)
# and runs all migrations again, including the dev seed. Does not touch S3 or Cognito.
#
# Usage (from the repository root):
#   ./scripts/reset-db.sh       # local database (same defaults as liquibase-db.sh)
#   LIQUIBASE_COMMAND_URL='jdbc:postgresql://host/db?sslmode=require' \
#   LIQUIBASE_COMMAND_USERNAME=... LIQUIBASE_COMMAND_PASSWORD=... ./scripts/reset-db.sh
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TARGET="${LIQUIBASE_COMMAND_URL:-jdbc:postgresql://localhost:5432/picturepov}"

read -r -p "Drop EVERYTHING in ${TARGET%%\?*}? Type 'reset' to confirm: " ANSWER
if [ "$ANSWER" != "reset" ]; then
  echo "Cancelled."
  exit 1
fi

"$REPO_ROOT/scripts/liquibase-db.sh" drop-all --force=true
"$REPO_ROOT/scripts/liquibase-db.sh" update --context-filter=dev

echo "Done: database rebuilt from scratch."
