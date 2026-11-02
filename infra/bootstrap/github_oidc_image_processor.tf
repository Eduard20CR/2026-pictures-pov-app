# DEV

resource "aws_iam_role" "github_actions_dev_image_processor" {
  name               = "${var.project}-dev-gha-image-processor"
  assume_role_policy = data.aws_iam_policy_document.github_trust_dev.json

  tags = {
    Environment = "dev"
  }
}

data "aws_iam_policy_document" "github_actions_dev_image_processor" {
  statement {
    sid       = "UploadArtifact"
    actions   = ["s3:PutObject", "s3:GetObject"]
    resources = ["arn:aws:s3:::${var.project}-dev-artifacts-${data.aws_caller_identity.current.account_id}/image-processor/*"]
  }

  statement {
    sid = "DeployLambda"
    actions = [
      "lambda:UpdateFunctionCode",
      "lambda:GetFunctionConfiguration",
    ]
    resources = [
      "arn:aws:lambda:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:function:${var.project}-dev-image-processor",
    ]
  }
}

resource "aws_iam_role_policy" "github_actions_dev_image_processor" {
  name   = "deploy-image-processor"
  role   = aws_iam_role.github_actions_dev_image_processor.id
  policy = data.aws_iam_policy_document.github_actions_dev_image_processor.json
}