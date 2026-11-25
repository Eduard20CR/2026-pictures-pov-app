output "function_name" {
  value = aws_lambda_function.this.function_name
}

output "queue_url" {
  description = "URL of the queue the API sends ZIP requests to (#294)."
  value       = aws_sqs_queue.this.url
}

output "queue_arn" {
  description = "ARN of the queue, for the API's sqs:SendMessage permission (#294)."
  value       = aws_sqs_queue.this.arn
}
