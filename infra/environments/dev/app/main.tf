locals {
  environment = "dev"
  name_prefix = "${var.project}-${local.environment}"
}

module "api_ecr" {
  source = "./../../../modules/ecr_repo"

  name = "${local.name_prefix}-api"
}

