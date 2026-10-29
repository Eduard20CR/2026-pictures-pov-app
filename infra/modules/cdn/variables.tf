variable "name" {
  type        = string
  description = "Name prefix, e.g. pictures-pov-dev"
}

variable "web_bucket_name" {
  type        = string
  description = "Name of the S3 bucket that holds the SPA files"
}

variable "web_bucket_arn" {
  type        = string
  description = "ARN of the SPA bucket, used in its bucket policy"
}

variable "web_bucket_regional_domain_name" {
  type        = string
  description = "Regional domain name of the SPA bucket (default origin)"
}

variable "api_domain_name" {
  type        = string
  description = "API Gateway domain without https://, e.g. abc123.execute-api.us-east-1.amazonaws.com"
}
