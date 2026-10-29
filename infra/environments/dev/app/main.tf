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
  s3_key             = "api/latest.zip"
  ssm_parameter_path = local.ssm_parameter_path
  photos_bucket_arn  = module.s3_photos_bucket.bucket_arn 
  environment_variables = {
    SSM_DB_PATH = local.ssm_parameter_path
    PHOTOS_BUCKET = module.s3_photos_bucket.bucket_name 
  }
}

module "api_http" {
  source               = "../../../modules/api_gateway_for_lambda"
  name                 = "${local.name_prefix}-api"
  lambda_invoke_arn    = module.api_lambda.alias_invoke_arn
  lambda_function_name = module.api_lambda.function_name
  lambda_alias_name    = module.api_lambda.alias_name
}

module "web_bucket" {
  source = "../../../modules/web_bucket"
  name   = local.name_prefix
}

module "cdn" {
  source                          = "../../../modules/cdn"
  name                            = local.name_prefix
  web_bucket_name                 = module.web_bucket.bucket_name
  web_bucket_arn                  = module.web_bucket.bucket_arn
  web_bucket_regional_domain_name = module.web_bucket.bucket_regional_domain_name
  api_domain_name                 = trimprefix(module.api_http.api_endpoint, "https://")
}


module "s3_photos_bucket" {
  source = "../../../modules/s3_photos_bucket"
  name   = local.name_prefix
  allowed_origins = [
    "https://${module.cdn.distribution_domain_name}",
    "http://localhost:5173",
  ]
}