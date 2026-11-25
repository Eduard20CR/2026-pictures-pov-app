variable "name" {
  type        = string
  description = "Name of the Lambda function and its queue. Also used as a prefix for its IAM role and dead-letter queue."
}

variable "s3_bucket" {
  type        = string
  description = "S3 bucket that holds the deployment zips."
}

variable "s3_key" {
  type        = string
  description = "S3 key of the zip used when the function is first created. Later deploys update the code from the CD workflow."
}

variable "ssm_parameter_path" {
  type        = string
  description = "SSM Parameter Store path (no trailing slash) the function may read, e.g. /pictures-pov/dev/db."
}

variable "environment_variables" {
  type        = map(string)
  description = "Environment variables for the function."
  default     = {}
}

variable "photos_bucket_arn" {
  type        = string
  description = "ARN of the photos bucket: the function reads originals/ and writes zips/."
}

variable "max_receive_count" {
  type        = number
  description = "Deliveries of a message before it goes to the dead-letter queue; the last one marks the export failed."
  default     = 3
}
