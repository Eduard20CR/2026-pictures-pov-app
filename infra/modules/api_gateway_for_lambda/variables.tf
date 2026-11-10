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

variable "throttling_rate_limit" {
  type        = number
  description = "Steady-state requests per second allowed for the whole stage (all clients combined). Requests above the limit get 429 before reaching Lambda."
}

variable "throttling_burst_limit" {
  type        = number
  description = "Maximum concurrent requests (token bucket size) the stage accepts in a short spike, above the steady rate."
}