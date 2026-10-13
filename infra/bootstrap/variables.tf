variable "aws_region" {
  description = "AWS region where the bootstrap resources are created."
  type        = string
  default     = "us-east-1"
}

variable "project" {
  description = "Project name, used for naming and tagging resources."
  type        = string
  default     = "pictures-pov"
}
