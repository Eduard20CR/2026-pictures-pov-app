locals {
  name_prefix        = "${var.project}-${var.environment}"
  ssm_parameter_path = "/${var.project}/${var.environment}/db"
}