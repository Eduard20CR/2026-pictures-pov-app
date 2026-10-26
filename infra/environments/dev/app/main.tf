locals {
  name_prefix        = "${var.project}-${var.environment}"
  ssm_parameter_path = "/${var.project}/${var.environment}/db"
}

module "s3_artifacts_bucket" {
  source = "../../../modules/s3_artifacts_bucket"
  name   = local.name_prefix
}

module "api_lambda" {
  source             = "../../../modules/lambda_api"
  name               = "${local.name_prefix}-api"
  s3_bucket          = module.s3_artifacts_bucket.artifacts_bucket
  s3_key             = var.api_s3_key
  ssm_parameter_path = local.ssm_parameter_path
  environment_variables = {
    SSM_DB_PATH = local.ssm_parameter_path
  }
}

module "api_http" {
  source               = "../../../modules/api_gateway_for_lambda"
  name                 = "${local.name_prefix}-api"
  lambda_invoke_arn    = module.api_lambda.alias_invoke_arn
  lambda_function_name = module.api_lambda.function_name
  lambda_alias_name    = module.api_lambda.alias_name
}

module "s3_static_site" {
  source = "../../../modules/s3_static_site"
  name   = local.name_prefix 
}

