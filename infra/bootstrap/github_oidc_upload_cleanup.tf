# DEV

resource "aws_iam_role" "github_actions_dev_upload_cleanup" {
  name               = "${var.project}-dev-gha-upload-cleanup"
  assume_role_policy = data.aws_iam_policy_document.github_trust_dev.json

  tags = {
    Environment = "dev"
  }
}

data "aws_iam_policy_document" "github_actions_dev_upload_cleanup" {
  statement {
    sid       = "UploadArtifact"
    actions   = ["s3:PutObject", "s3:GetObject"]
    resources = ["arn:aws:s3:::${var.project}-dev-artifacts-${data.aws_caller_identity.current.account_id}/upload-cleanup/*"]
  }

  statement {
    sid = "DeployLambda"
    actions = [
      "lambda:UpdateFunctionCode",
      "lambda:GetFunctionConfiguration",
    ]
    resources = [
      "arn:aws:lambda:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:function:${var.project}-dev-upload-cleanup",
    ]
  }
}

resource "aws_iam_role_policy" "github_actions_dev_upload_cleanup" {
  name   = "deploy-upload-cleanup"
  role   = aws_iam_role.github_actions_dev_upload_cleanup.id
  policy = data.aws_iam_policy_document.github_actions_dev_upload_cleanup.json
}