output "function_name" {
  value = aws_lambda_function.this.function_name
}

output "function_arn" {
  description = "ARN of the function, used by the photos bucket notification."
  value       = aws_lambda_function.this.arn
}