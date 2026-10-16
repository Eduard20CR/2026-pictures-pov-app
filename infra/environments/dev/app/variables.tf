variable "project" {
  description = "Project name, used as a prefix for resource names."
  type        = string
  default     = "pictures-pov"
}

variable "api_s3_key" {
  type        = string
  description = "S3 key (api/<commit SHA>.zip) of the API zip used when the Lambda is first created. Later deploys update the code from the CD workflow."
}