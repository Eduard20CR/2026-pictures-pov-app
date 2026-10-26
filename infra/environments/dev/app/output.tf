output "artifacts_bucket" {
  value = module.s3_artifacts_bucket.artifacts_bucket
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

output "web_bucket_name" {
  value = module.static_site.bucket_name
}

output "web_url" {
  value = "https://${module.static_site.distribution_domain_name}"
}

output "web_distribution_id" {
  value = module.static_site.distribution_id
}