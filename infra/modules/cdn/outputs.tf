output "distribution_id" {
  description = "CloudFront distribution ID (used for cache invalidations)"
  value       = aws_cloudfront_distribution.this.id
}

output "distribution_arn" {
  description = "CloudFront distribution ARN"
  value       = aws_cloudfront_distribution.this.arn
}

output "distribution_domain_name" {
  description = "Public URL of the site and the API (xxxx.cloudfront.net)"
  value       = aws_cloudfront_distribution.this.domain_name
}
