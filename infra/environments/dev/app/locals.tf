locals {
  name_prefix        = "${var.project}-${var.environment}"
  ssm_parameter_path = "/${var.project}/${var.environment}/db"

  # P-10: the only place that sets how long an upload authorization lasts.
  upload_ttl_minutes = 15
}