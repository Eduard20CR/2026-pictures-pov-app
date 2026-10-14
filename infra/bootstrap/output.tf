output "state_bucket_name" {
  description = "Name of the S3 bucket that stores the Terraform state."
  value       = aws_s3_bucket.tfstate.bucket
}

output "github_actions_dev_role_arn" {
  description = "IAM role assumed by GitHub Actions to deploy to dev."
  value       = aws_iam_role.github_actions_dev.arn
}

output "github_actions_prod_role_arn" {
  description = "IAM role assumed by GitHub Actions to deploy to prod."
  value       = aws_iam_role.github_actions_prod.arn
}