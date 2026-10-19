locals {
  name_prefix = "${var.project}-${var.environment}"
}

module "s3_to_store_app_artifact" {
  source = "../../../modules/s3_to_store_app_artifact"
  name      = "${local.name_prefix}"
}

module "api_lambda" {
  source    = "../../../modules/lambda_api"
  name      = "${local.name_prefix}-api"
  s3_bucket = module.s3_to_store_app_artifact.artifacts_bucket
  s3_key    = var.api_s3_key
  ssm_parameter_path = "/${var.project}/${var.environment}/db"
}

module "api_http" {
  source               = "../../../modules/api_gateway_for_lambda"
  name                 = "${local.name_prefix}-api"
  lambda_invoke_arn    = module.api_lambda.alias_invoke_arn
  lambda_function_name = module.api_lambda.function_name
  lambda_alias_name    = module.api_lambda.alias_name
}

