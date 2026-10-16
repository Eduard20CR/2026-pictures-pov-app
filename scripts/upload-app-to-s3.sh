#!/usr/bin/env bash
# Uploads apps/api/target/api.zip to the dev artifacts bucket under the current commit SHA.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ZIP="$REPO_ROOT/apps/api/target/api.zip"

# 1. The zip must exist (build it with scripts/api-package.sh)
if [ ! -f "$ZIP" ]; then
  echo "Missing $ZIP. Run scripts/api-package.sh first." >&2
  exit 1
fi

# 2. Bucket from Terraform, key from the commit SHA
BUCKET=$(terraform -chdir="$REPO_ROOT/infra/environments/dev/app" output -raw artifacts_bucket)
KEY="api/$(git -C "$REPO_ROOT" rev-parse --short HEAD).zip"

# 3. Upload and show the result
aws s3 cp "$ZIP" "s3://$BUCKET/$KEY"
aws s3 ls "s3://$BUCKET/api/"

echo "$KEY"