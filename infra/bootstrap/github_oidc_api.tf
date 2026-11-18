
# DEV

resource "aws_iam_role" "github_actions_dev_api" {
  name               = "${var.project}-dev-gha-api"
  assume_role_policy = data.aws_iam_policy_document.github_trust_dev["api"].json

  tags = {
    Environment = "dev"
  }
}

data "aws_iam_policy_document" "github_actions_dev_api" {
  statement {
    sid       = "UploadArtifact"
    actions   = ["s3:PutObject", "s3:GetObject"]
    resources = ["arn:aws:s3:::${var.project}-dev-artifacts-${data.aws_caller_identity.current.account_id}/api/*"]
  }

  statement {
    sid = "DeployLambda"
    actions = [
      "lambda:UpdateFunctionCode",
      "lambda:GetFunctionConfiguration",
      "lambda:UpdateAlias",
    ]
    resources = [
      "arn:aws:lambda:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:function:${var.project}-dev-api",
      "arn:aws:lambda:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:function:${var.project}-dev-api:*",
    ]
  }
}

resource "aws_iam_role_policy" "github_actions_dev_api" {
  name   = "deploy-api"
  role   = aws_iam_role.github_actions_dev_api.id
  policy = data.aws_iam_policy_document.github_actions_dev_api.json
}
