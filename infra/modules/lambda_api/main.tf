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
    actions = ["ssm:GetParametersByPath", "ssm:GetParameters", "ssm:GetParameter"]
    resources = [
      "arn:aws:ssm:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:parameter${var.ssm_parameter_path}",
      "arn:aws:ssm:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:parameter${var.ssm_parameter_path}/*",
    ]
  }
}

resource "aws_iam_role_policy" "ssm_read" {
  name   = "${var.name}-read-ssm-parameters"
  role   = aws_iam_role.this.id
  policy = data.aws_iam_policy_document.ssm_read.json
}

# POLICY

data "aws_iam_policy_document" "photos_upload" {
  statement {
    actions   = ["s3:PutObject"]
    resources = ["${var.photos_bucket_arn}/originals/*"]
  }
}

resource "aws_iam_role_policy" "photos_upload" {
  name   = "${var.name}-photos-upload"
  role   = aws_iam_role.this.id
  policy = data.aws_iam_policy_document.photos_upload.json
}

# LAMBDA

resource "aws_lambda_function" "this" {
  function_name = var.name
  role          = aws_iam_role.this.arn
  runtime       = "java21"
  handler       = "run.sh"
  s3_bucket     = var.s3_bucket
  s3_key        = var.s3_key
  architectures = ["arm64"]
  layers        = ["arn:aws:lambda:${data.aws_region.current.region}:753240598075:layer:LambdaAdapterLayerArm64:30"]
  memory_size   = 3008
  timeout       = 30
  publish       = true

  snap_start {
    apply_on = "PublishedVersions"
  }

  environment {
    variables = merge({
      AWS_LAMBDA_EXEC_WRAPPER      = "/opt/bootstrap"
      AWS_LWA_READINESS_CHECK_PATH = "/actuator/health/liveness"
      JAVA_TOOL_OPTIONS            = "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
    }, var.environment_variables)
  }

  lifecycle {
    ignore_changes = [s3_key]
  }
}

resource "aws_lambda_alias" "live" {
  name             = "live"
  function_name    = aws_lambda_function.this.function_name
  function_version = aws_lambda_function.this.version

  lifecycle {
    ignore_changes = [function_version]
  }
}