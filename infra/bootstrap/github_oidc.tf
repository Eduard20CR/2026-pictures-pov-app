resource "aws_iam_openid_connect_provider" "github" {
  url            = "https://token.actions.githubusercontent.com"
  client_id_list = ["sts.amazonaws.com"]
}

data "aws_iam_policy_document" "github_trust_dev" {
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
      values   = ["repo:Eduard20CR/2026-pictures-pov-app:ref:refs/heads/main"]
    }
  }
}

resource "aws_iam_role" "github_actions_dev" {
  name               = "${var.project}-dev-github-actions"
  assume_role_policy = data.aws_iam_policy_document.github_trust_dev.json

  tags = {
    Environment = "dev"
  }
}

data "aws_iam_policy_document" "github_trust_prod" {
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
      values   = ["repo:Eduard20CR/2026-pictures-pov-app:environment:prod"]
    }
  }
}

resource "aws_iam_role" "github_actions_prod" {
  name               = "${var.project}-prod-github-actions"
  assume_role_policy = data.aws_iam_policy_document.github_trust_prod.json

  tags = {
    Environment = "prod"
  }
}