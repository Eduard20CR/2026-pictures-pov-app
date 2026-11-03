data "aws_iam_policy_document" "assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

# ROL

resource "aws_iam_role" "this" {
  name               = "${var.name}-lambda"
  assume_role_policy = data.aws_iam_policy_document.assume.json
}

resource "aws_iam_role_policy_attachment" "logs" {
  role       = aws_iam_role.this.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# POLICY

data "aws_iam_policy_document" "ssm_read" {
  statement {
    actions = ["ssm:GetParametersByPath"]
    resources = [
      "arn:aws:ssm:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:parameter${var.ssm_parameter_path}",
    ]
  }
}

resource "aws_iam_role_policy" "ssm_read" {
  name   = "${var.name}-read-ssm-parameters"
  role   = aws_iam_role.this.id
  policy = data.aws_iam_policy_document.ssm_read.json
}

data "aws_iam_policy_document" "photos_read_delete" {
  statement {
    actions   = ["s3:GetObject", "s3:DeleteObject"]
    resources = ["${var.photos_bucket_arn}/originals/*"]
  }
}

resource "aws_iam_role_policy" "photos_read_delete" {
  name   = "${var.name}-read-delete-photos"
  role   = aws_iam_role.this.id
  policy = data.aws_iam_policy_document.photos_read_delete.json
}

# LAMBDA

resource "aws_lambda_function" "this" {
  function_name = var.name
  role          = aws_iam_role.this.arn
  runtime       = "nodejs24.x"
  handler       = "index.handler"
  s3_bucket     = var.s3_bucket
  s3_key        = var.s3_key
  architectures = ["arm64"]
  memory_size   = 256
  timeout       = 30

  environment {
    variables = var.environment_variables
  }

  lifecycle {
    ignore_changes = [s3_key]
  }
}

# PERMISSION

resource "aws_lambda_permission" "photos_bucket" {
  statement_id   = "AllowPhotosBucketInvoke"
  action         = "lambda:InvokeFunction"
  function_name  = aws_lambda_function.this.function_name
  principal      = "s3.amazonaws.com"
  source_arn     = var.photos_bucket_arn
  source_account = data.aws_caller_identity.current.account_id
}