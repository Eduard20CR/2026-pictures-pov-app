output "function_name" {
  value = aws_lambda_function.this.function_name
}

output "alias_name" {
  value = aws_lambda_alias.live.name
}

output "alias_invoke_arn" {
  description = "Invoke ARN of the live alias, used by API Gateway."
  value       = aws_lambda_alias.live.invoke_arn
}