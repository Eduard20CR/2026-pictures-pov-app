variable "project" {
  description = "Project name, used as a prefix for resource names."
  type        = string
  default     = "pictures-pov"
}

variable "dev_web_distribution_id" {
  description = "CloudFront distribution ID of the dev web app."
  type        = string
  default = "E32U5RYPHJVX1E"
}