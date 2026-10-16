variable "name" {
  type        = string
  description = "Name of the HTTP API."
}

variable "lambda_invoke_arn" {
  type        = string
  description = "Invoke ARN of the Lambda alias that handles every request."
}

variable "lambda_function_name" {
  type        = string
  description = "Name of the Lambda function, used to grant API Gateway permission to invoke it."
}

variable "lambda_alias_name" {
  type        = string
  description = "Lambda alias that API Gateway invokes (for example, live)."
}