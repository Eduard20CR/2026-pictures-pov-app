# DEV

resource "aws_iam_role" "github_actions_dev_zip_export" {
  name               = "${var.project}-dev-gha-zip-export"
  assume_role_policy = data.aws_iam_policy_document.github_trust_dev["zip_export"].json

  tags = {
    Environment = "dev"
  }
}

data "aws_iam_policy_document" "github_actions_dev_zip_export" {
  statement {
    sid       = "UploadArtifact"
    actions   = ["s3:PutObject", "s3:GetObject"]
    resources = ["arn:aws:s3:::${var.project}-dev-artifacts-${data.aws_caller_identity.current.account_id}/zip-export/*"]
  }

  statement {
    sid = "DeployLambda"
    actions = [
      "lambda:UpdateFunctionCode",
      "lambda:GetFunctionConfiguration",
    ]
    resources = [
      "arn:aws:lambda:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:function:${var.project}-dev-zip-export",
    ]
  }
}

resource "aws_iam_role_policy" "github_actions_dev_zip_export" {
  name   = "deploy-zip-export"
  role   = aws_iam_role.github_actions_dev_zip_export.id
  policy = data.aws_iam_policy_document.github_actions_dev_zip_export.json
}