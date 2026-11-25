locals {
  # GitHub OIDC immutable subject: <owner>@<owner_id>/<repo>@<repo_id>
  github_repo = "Eduard20CR@41557299/2026-pictures-pov-app@1407560871"
  # job_workflow_ref uses the repository name, not the immutable IDs.
  github_repo_path = "Eduard20CR/2026-pictures-pov-app"

  # Each dev role trusts only its own deploy workflow on main.
  dev_deploy_workflows = {
    api             = "api-deploy-dev.yaml"
    image_processor = "image-processor-deploy-dev.yaml"
    infra           = "infra-deploy-dev.yaml"
    upload_cleanup  = "upload-cleanup-deploy-dev.yaml"
    web             = "web-deploy-dev.yaml"
    zip_export      = "zip-export-deploy-dev.yaml"
  }
}

# GITHUB PROVIDES

resource "aws_iam_openid_connect_provider" "github" {
  url            = "https://token.actions.githubusercontent.com"
  client_id_list = ["sts.amazonaws.com"]
}

# DEV

data "aws_iam_policy_document" "github_trust_dev" {
  for_each = local.dev_deploy_workflows

  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${local.github_repo}:ref:refs/heads/main"]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:job_workflow_ref"
      values   = ["${local.github_repo_path}/.github/workflows/${each.value}@refs/heads/main"]
    }
  }
}

# Prod roles are added with prod (#229): least-privilege, trusted only from a
# protected GitHub `prod` environment limited to `main`.