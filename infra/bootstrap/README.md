# Bootstrap

This configuration creates the Amazon S3 bucket that stores the Terraform state of the environments in `infra/environments/`. It is applied once, before any environment, and rarely changes afterwards. The decision is recorded in [ADR 0011](../../docs/adr/0011-terraform-state-amazon-s3.md).

## Why a separate configuration

An environment cannot create the bucket that holds its own state: the backend must exist before `terraform init` runs. The bootstrap breaks this dependency by creating the bucket first, with a configuration whose own state is kept locally.

## What it creates

- An S3 bucket for the Terraform state, with:
  - **Versioning** enabled, so every previous version of the state can be recovered after an erroneous apply or a corrupted file.
  - **Server-side encryption**, since the state may contain sensitive values.
  - **Public access blocked** on all four settings.

State locking uses the S3 native lock file (`use_lockfile`, Terraform 1.10 or later), so no DynamoDB table is required.

## Usage

Run it with credentials for the target AWS account (development or production). Each account has its own bucket.

```sh
cd infra/bootstrap
terraform init
terraform plan -var="aws_region=<region>"
terraform apply -var="aws_region=<region>"
```

The bucket name is printed in the outputs and is then referenced from the backend of each environment:

```hcl
terraform {
  backend "s3" {
    bucket       = "<state-bucket-name>"
    key          = "<environment>/terraform.tfstate"
    region       = "<region>"
    encrypt      = true
    use_lockfile = true
  }
}
```

## State of the bootstrap itself

The bootstrap state is stored locally in `terraform.tfstate`. It must not be committed to the repository; keep it in a safe place, since it is needed to modify or destroy the bucket later. If it is lost, the bucket can be brought back under management with `terraform import`.

## Precautions

- Do not destroy this configuration while any environment still uses the bucket: their state would be lost.
- The bucket should have `prevent_destroy` enabled to avoid accidental deletion.
