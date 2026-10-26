variable "project" {
  description = "Project name, used as a prefix for resource names."
  type        = string
  default     = "pictures-pov"
}

variable "environment" {
  description = "Project environment."
  type        = string
  default     = "dev"
}