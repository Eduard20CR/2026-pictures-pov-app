locals {
  environment = "dev"
  name_prefix = "${var.project}-${local.environment}"
}

module "api_ecr" {
  source = "./../../../modules/ecr_repo"

  name = "${local.name_prefix}-api"
}



module "api_lambda" {
  source    = "../../../modules/lambda_java"
  name      = "${local.name_prefix}-api"
  s3_bucket = aws_s3_bucket.artifacts.bucket
  s3_key    = var.api_s3_key
}



module "s3_to_store_app_artifact" {
  source = "../../../modules/s3_to_store_app_artifact"
  name      = "${local.name_prefix}"
}