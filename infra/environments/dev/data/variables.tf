variable "project" {
  description = "Project name, used as a prefix for resource names."
  type        = string
  default     = "pictures-pov"
}

variable "environment" {
  type        = string
  default     = "dev"
  description = "Project environment."
}


variable "aws_region" {
  description = "AWS region where the state bucket is created."
  type        = string
  default     = "us-east-1"
}