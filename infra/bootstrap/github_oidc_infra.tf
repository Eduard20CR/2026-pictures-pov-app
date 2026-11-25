resource "aws_iam_role" "github_actions_dev_infra" {
  name               = "${var.project}-dev-gha-infra"
  assume_role_policy = data.aws_iam_policy_document.github_trust_dev["infra"].json

  tags = {
    Environment = "dev"
  }
}

data "aws_iam_policy_document" "github_actions_dev_infra" {
  statement {
    # No SSM access: dev infra must never read secrets (#229).
    sid       = "AppServices"
    actions   = ["lambda:*", "apigateway:*", "cloudfront:*", "logs:*", "scheduler:*", "cognito-idp:*"]
    resources = ["*"]
  }

  statement {
    sid       = "DevQueues"
    actions   = ["sqs:*"]
    resources = ["arn:aws:sqs:*:${data.aws_caller_identity.current.account_id}:${var.project}-dev-*"]
  }

  statement {
    sid       = "DevBuckets"
    actions   = ["s3:*"]
    resources = ["arn:aws:s3:::${var.project}-dev-*", "arn:aws:s3:::${var.project}-dev-*/*"]
  }

  statement {
    sid       = "StateList"
    actions   = ["s3:ListBucket"]
    resources = [aws_s3_bucket.tfstate.arn]
  }

  statement {
    sid       = "StateDevOnly"
    actions   = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"]
    resources = ["${aws_s3_bucket.tfstate.arn}/dev/*"]
  }

  statement {
    sid       = "DevRoles"
    actions   = ["iam:*"]
    resources = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${var.project}-dev-*"]
  }

  statement {
    sid       = "ProtectPipelineRoles"
    effect    = "Deny"
    actions   = ["iam:*"]
    resources = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${var.project}-dev-gha-*"]
  }
}

resource "aws_iam_role_policy" "github_actions_dev_infra" {
  name   = "deploy-infra"
  role   = aws_iam_role.github_actions_dev_infra.id
  policy = data.aws_iam_policy_document.github_actions_dev_infra.json
}
