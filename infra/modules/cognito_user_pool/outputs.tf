output "user_pool_id" {
  description = "ID of the Cognito user pool."
  value       = aws_cognito_user_pool.pool.id
}

output "user_pool_arn" {
  description = "ARN of the Cognito user pool."
  value       = aws_cognito_user_pool.pool.arn
}

output "user_pool_endpoint" {
  description = "Endpoint of the user pool, used to build the token issuer URL."
  value       = aws_cognito_user_pool.pool.endpoint
}

output "client_id" {
  description = "ID of the web app client."
  value       = aws_cognito_user_pool_client.web.id
}

output "login_domain" {
  description = "Domain of the managed login page."
  value       = "${aws_cognito_user_pool_domain.login.domain}.auth.${data.aws_region.current.region}.amazoncognito.com"
}