locals {
  name_prefix        = "${var.project}-${var.environment}"
  ssm_parameter_path = "/${var.project}/${var.environment}/db"

  # P-10: the only place that sets how long an upload authorization lasts.
  upload_ttl_minutes = 15

  # P-13: the only place that sets how long a signed thumbnail link lasts.
  thumbnail_ttl_minutes = 15

  # P-12: the only place that sets how long a signed single-photo download link lasts.
  download_ttl_minutes = 15
}