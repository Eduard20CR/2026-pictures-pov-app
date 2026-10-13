# Bootstrap

Creates the S3 bucket that stores the Terraform state of all the infrastructure. Decision: [ADR 0011](../../docs/adr/0011-terraform-state-amazon-s3.md).

## What it creates

| Resource | Setting |
| --- | --- |
| S3 bucket `<project>-tfstate-<region>-<account-id>` | `prevent_destroy` |
| Versioning | Enabled: previous states can be recovered |
| Encryption | SSE (AES256) |
| Public access block | All four settings on |

Locking uses the S3 native lock file (`use_lockfile`); no DynamoDB table.

## Why it is run manually the first time

The `s3` backend needs the bucket to exist before `terraform init`. The bucket does not exist yet, so the first run must keep the state locally. Once the bucket exists, the bootstrap state is moved into it.

## Steps

Prerequisites: Terraform ≥ 1.14 and AWS credentials for the target account.

### 1. First run, with local state

Comment out the `backend "s3"` block in `main.tf`, then:

```sh
cd infra/bootstrap
terraform init
terraform plan
terraform apply
```

Note the `state_bucket_name` output.

### 2. Move the bootstrap state to S3

Uncomment the `backend "s3"` block and set `bucket` to the output of step 1:

```hcl
backend "s3" {
  bucket       = "<state_bucket_name>"
  key          = "bootstrap/terraform.tfstate"
  region       = "us-east-1"
  encrypt      = true
  use_lockfile = true
}
```

Migrate the state and answer `yes` when asked to copy it:

```sh
terraform init -migrate-state
```

Delete the local `terraform.tfstate` and `terraform.tfstate.backup` only after checking that `bootstrap/terraform.tfstate` exists in the bucket. Never commit them.

### 3. Use the same bucket in every other configuration

Each environment (`infra/environments/<environment>/`) declares the same backend with its own `key`:

| Configuration | `key` |
| --- | --- |
| Bootstrap | `bootstrap/terraform.tfstate` |
| dev | `dev/terraform.tfstate` |
| prod | `prod/terraform.tfstate` |

Then run, from the environment folder:

```sh
terraform init
terraform plan
terraform apply
```

## Later changes

From step 2 onwards the bootstrap behaves like any other configuration: `terraform init`, `plan` and `apply`, with the state in S3.

## Precautions

- Never destroy the bucket while any configuration still uses it: all states would be lost.
- If the bucket is ever recreated, repeat steps 1 and 2.
