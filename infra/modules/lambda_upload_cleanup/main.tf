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

data "aws_iam_policy_document" "photos_read" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${var.photos_bucket_arn}/originals/*"]
  }
  statement {
    actions   = ["s3:ListBucket"]
    resources = [var.photos_bucket_arn]
  }
}

resource "aws_iam_role_policy" "photos_read" {
  name   = "${var.name}-read-photos"
  role   = aws_iam_role.this.id
  policy = data.aws_iam_policy_document.photos_read.json
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

# SCHEDULE

data "aws_iam_policy_document" "scheduler_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["scheduler.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "scheduler" {
  name               = "${var.name}-scheduler"
  assume_role_policy = data.aws_iam_policy_document.scheduler_assume.json
}

data "aws_iam_policy_document" "scheduler_invoke" {
  statement {
    actions   = ["lambda:InvokeFunction"]
    resources = [aws_lambda_function.this.arn]
  }
}

resource "aws_iam_role_policy" "scheduler_invoke" {
  name   = "${var.name}-invoke-lambda"
  role   = aws_iam_role.scheduler.id
  policy = data.aws_iam_policy_document.scheduler_invoke.json
}

resource "aws_scheduler_schedule" "this" {
  name                = var.name
  schedule_expression = "rate(30 minutes)"
  state = "ENABLED"

  flexible_time_window {
    mode = "OFF"
  }

  target {
    arn      = aws_lambda_function.this.arn
    role_arn = aws_iam_role.scheduler.arn
  }
}