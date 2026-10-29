variable "name" {
  type        = string
  description = "Name prefix, e.g. pictures-pov-dev"
}

variable "api_domain_name" {
  type        = string
  description = "API Gateway domain without https://, e.g. abc123.execute-api.us-east-1.amazonaws.com"
}