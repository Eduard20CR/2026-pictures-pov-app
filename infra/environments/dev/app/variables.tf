variable "project" {
  description = "Project name, used as a prefix for resource names."
  type        = string
  default     = "pictures-pov"
}

variable "aws_region" {
  description = "AWS region where the state bucket is created."
  type        = string
  default     = "us-east-1"
}

variable "api_image_tag" {
  type        = string
  description = "Tag (commit SHA) of the API image in ECR used when the Lambda is first created. Later deploys update the image from the CD workflow."
  default     = "latest"
}