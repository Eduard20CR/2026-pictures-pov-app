variable "name" {
  type        = string
  description = "Prefix for the s3 buckets."
}

variable "apps" {
  type        = list(string)
  description = "Key prefix of each app's zips; their builds/ subfolder expires after 30 days."
}
