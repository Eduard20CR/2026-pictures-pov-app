variable "name" {
  type        = string
  description = "Name of the Lambda function. Also used as a prefix for its IAM role."
}

variable "s3_bucket" {
  type        = string
  description = "S3 bucket that holds the deployment zips."
}

variable "s3_key" {
  type        = string
  description = "S3 key of the zip used when the function is first created. Later deploys update the code from the CD workflow."
}

variable "environment_variables" {
  type        = map(string)
  description = "Extra environment variables for the function."
  default     = {}
}

variable "ssm_parameter_path" {
  type        = string
  description = "SSM Parameter Store path (no trailing slash) the function may read, e.g. /pictures-pov/dev/db."
}

variable "photos_bucket_arn" {
  type        = string
  description = "ARN of the photos bucket. The function may sign uploads under originals/."
}