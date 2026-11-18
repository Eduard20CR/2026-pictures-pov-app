data "aws_caller_identity" "current" {}

resource "aws_s3_bucket" "artifacts" {
  bucket        = "${var.name}-artifacts-${data.aws_caller_identity.current.account_id}"
  force_destroy = true
}

resource "aws_s3_bucket_public_access_block" "artifacts" {
  bucket                  = aws_s3_bucket.artifacts.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Only the per-commit zips (<app>/builds/<sha>.zip) expire. <app>/latest.zip is what
# Terraform passes as s3_key, so it must stay to recreate a function after 30 days.
resource "aws_s3_bucket_lifecycle_configuration" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id

  dynamic "rule" {
    for_each = toset(var.apps)
    content {
      id     = "expire-${rule.value}-builds"
      status = "Enabled"
      filter {
        prefix = "${rule.value}/builds/"
      }
      expiration {
        days = 30
      }
    }
  }
}
