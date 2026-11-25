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

locals {
  # Lambda's maximum (ADR 0008): 2,000 photos must fit (RNF-REN-06, #296).
  timeout_seconds = 900
}

# QUEUES

resource "aws_sqs_queue" "dlq" {
  name                      = "${var.name}-dlq"
  message_retention_seconds = 1209600 # 14 days, the maximum, to inspect failures.
}

resource "aws_sqs_queue" "this" {
  name = var.name
  # At least the function timeout, so a running export is not delivered twice.
  visibility_timeout_seconds = local.timeout_seconds + 60

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.dlq.arn
    maxReceiveCount     = var.max_receive_count
  })
}

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

data "aws_iam_policy_document" "this" {
  statement {
    sid     = "ReadDbParameters"
    actions = ["ssm:GetParametersByPath"]
    resources = [
      "arn:aws:ssm:${data.aws_region.current.region}:${data.aws_caller_identity.current.account_id}:parameter${var.ssm_parameter_path}",
    ]
  }

  statement {
    sid       = "ReadOriginals"
    actions   = ["s3:GetObject"]
    resources = ["${var.photos_bucket_arn}/originals/*"]
  }

  statement {
    sid       = "WriteZips"
    actions   = ["s3:PutObject", "s3:AbortMultipartUpload"]
    resources = ["${var.photos_bucket_arn}/zips/*"]
  }

  statement {
    sid       = "ConsumeQueue"
    actions   = ["sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"]
    resources = [aws_sqs_queue.this.arn]
  }
}

resource "aws_iam_role_policy" "this" {
  name   = "${var.name}-export-zip"
  role   = aws_iam_role.this.id
  policy = data.aws_iam_policy_document.this.json
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
  # More memory also gives more network throughput; the ZIP itself is streamed.
  memory_size = 1024
  timeout     = local.timeout_seconds

  environment {
    variables = merge(var.environment_variables, {
      MAX_RECEIVE_COUNT = tostring(var.max_receive_count)
    })
  }

  lifecycle {
    ignore_changes = [s3_key]
  }
}

# One export per invocation: a failure retries only its own message.
resource "aws_lambda_event_source_mapping" "this" {
  event_source_arn = aws_sqs_queue.this.arn
  function_name    = aws_lambda_function.this.arn
  batch_size       = 1

  depends_on = [aws_iam_role_policy.this]
}
