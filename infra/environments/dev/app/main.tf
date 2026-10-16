locals {
  environment = "dev"
  name_prefix = "${var.project}-${local.environment}"
}

module "api_ecr" {
  source = "./../../../modules/ecr_repo"

  name = "${local.name_prefix}-api"
}

module "api_lambda" {
  source    = "../../../modules/lambda_container"
  name      = "${local.name_prefix}-api"
  image_uri = "${module.api_ecr.repository_url}:${var.api_image_tag}"
}

module "s3_to_store_app_artifact" {
  source = "../../../modules/s3_to_store_app_artifact"
  name      = "${local.name_prefix}"
}