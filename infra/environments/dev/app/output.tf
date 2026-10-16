output "artifacts_bucket" {
  value = module.s3_to_store_app_artifact.artifacts_bucket
}

output "api_function_name" {
  value = module.api_lambda.function_name
}

output "api_alias_name" {
  value = module.api_lambda.alias_name
}

output "api_endpoint" {
  value = module.api_http.api_endpoint
}