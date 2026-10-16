output "api_ecr_repository_url" {
  value = module.api_ecr.repository_url
}

output "api_function_name" {
  value = module.api_lambda.function_name
}