variable "name" {
  type        = string
  description = "Name prefix, e.g. pictures-pov-dev"
}

variable "allowed_origins" {
  type        = list(string)
  description = "Web origins allowed to upload with presigned URLs."
}