#!/usr/bin/env bash
# Creates the first administrator of an environment in Cognito (RF-ADM-12, AC 2).
# Safe to run more than once: it skips the user if it exists, and adding to a group twice is fine.
#
# How to run it (from the repository root):
#   1. Sign in to AWS:
#        aws login
#   2. Get the user pool ID of the environment:
#        terraform -chdir=infra/environments/dev/app output -raw cognito_user_pool_id
#   3. Run the script with the pool ID and the admin's email:
#        ./scripts/create-first-admin.sh <user-pool-id> <email>
#      Example:
#        ./scripts/create-first-admin.sh us-east-1_9zczJDp4m oscar@example.com
#   4. If the user is new, Cognito emails a temporary password.
#      The admin changes it at the first sign-in.
#
# Docs:
#   https://docs.aws.amazon.com/cli/latest/reference/cognito-idp/admin-create-user.html
#   https://docs.aws.amazon.com/cli/latest/reference/cognito-idp/admin-add-user-to-group.html
set -euo pipefail

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <user-pool-id> <email>" >&2
  exit 1
fi

POOL_ID="$1"
EMAIL="$2"
GROUP="admins"

# 1. Create the user only if it does not exist yet
if aws cognito-idp admin-get-user --user-pool-id "$POOL_ID" --username "$EMAIL" >/dev/null 2>&1; then
  echo "User $EMAIL already exists, skipping creation."
else
  aws cognito-idp admin-create-user \
    --user-pool-id "$POOL_ID" \
    --username "$EMAIL" \
    --user-attributes Name=email,Value="$EMAIL" Name=email_verified,Value=true \
    --desired-delivery-mediums EMAIL >/dev/null
  echo "User $EMAIL created. Cognito sent a temporary password by email."
fi

# 2. Add the user to the admins group
aws cognito-idp admin-add-user-to-group \
  --user-pool-id "$POOL_ID" \
  --username "$EMAIL" \
  --group-name "$GROUP"
echo "User $EMAIL is in the $GROUP group."
