variable "name" {
  type        = string
  description = "Name of the Lambda function."
}

variable "image_uri" {
  type        = string
  description = "ECR image URI, including the tag (repo:sha)."
}

variable "environment_variables" {
  type        = map(string)
  description = "Environment variables for the function."
  default     = {}
}