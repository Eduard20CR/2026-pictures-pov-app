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
    SSM_DB_PATH   = local.ssm_parameter_path
    PHOTOS_BUCKET = module.s3_photos_bucket.bucket_name
  }
}

module "image_processor_lambda" {
  source             = "../../../modules/lambda_image_processor"
  name               = "${local.name_prefix}-image-processor"
  s3_bucket          = module.s3_artifacts_bucket.artifacts_bucket
  s3_key             = "image-processor/latest.zip"
  ssm_parameter_path = local.ssm_parameter_path
  photos_bucket_arn  = module.s3_photos_bucket.bucket_arn
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

resource "aws_s3_bucket_notification" "photos_to_image_processor" {
  bucket = module.s3_photos_bucket.bucket_name

  lambda_function {
    lambda_function_arn = module.image_processor_lambda.function_arn
    events              = ["s3:ObjectCreated:*"]
    filter_prefix       = "originals/"
  }

  depends_on = [module.image_processor_lambda]
}

module "upload_cleanup_lambda" {
  source             = "../../../modules/lambda_upload_cleanup"
  name               = "${local.name_prefix}-upload-cleanup"
  s3_bucket          = module.s3_artifacts_bucket.artifacts_bucket
  s3_key             = "upload-cleanup/latest.zip"
  ssm_parameter_path = local.ssm_parameter_path
  photos_bucket_arn  = module.s3_photos_bucket.bucket_arn
  environment_variables = {
    SSM_DB_PATH   = local.ssm_parameter_path
    PHOTOS_BUCKET = module.s3_photos_bucket.bucket_name
  }
}

module "cognito" {
  source = "../../../modules/cognito_user_pool"
  name   = local.name_prefix

  callback_urls = [
    "https://${module.cdn.distribution_domain_name}/auth/callback",
    "http://localhost:5173/auth/callback",
  ]
  logout_urls = [
    "https://${module.cdn.distribution_domain_name}/",
    "http://localhost:5173/",
  ]
}