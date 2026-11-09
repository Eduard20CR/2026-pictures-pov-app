variable "name" {
  type        = string
  description = "Name prefix, e.g. pictures-pov-dev"
}

variable "callback_urls" {
  description = "URLs where Cognito sends the user after sign-in."
  type        = list(string)
}

variable "logout_urls" {
  description = "URLs where Cognito sends the user after sign-out."
  type        = list(string)
}