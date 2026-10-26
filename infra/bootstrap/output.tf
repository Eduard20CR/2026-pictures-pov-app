output "state_bucket_name" {
  description = "Name of the S3 bucket that stores the Terraform state."
  value       = aws_s3_bucket.tfstate.bucket
}

output "github_actions_dev_role_arns" {
  description = "IAM roles assumed by GitHub Actions in dev, one per workflow."
  value = {
    infra = aws_iam_role.github_actions_dev_infra.arn
    api   = aws_iam_role.github_actions_dev_api.arn
    web   = aws_iam_role.github_actions_dev_web.arn
  }
}

output "github_actions_prod_role_arns" {
  description = "IAM roles assumed by GitHub Actions in prod, one per workflow."
  value = {
    infra = aws_iam_role.github_actions_prod_infra.arn
    api   = aws_iam_role.github_actions_prod_api.arn
    web   = aws_iam_role.github_actions_prod_web.arn
  }
}