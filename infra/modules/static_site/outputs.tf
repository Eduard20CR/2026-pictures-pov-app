output "bucket_name" {
  description = "Name of the S3 bucket that holds the SPA files"
  value       = aws_s3_bucket.site.id
}

output "bucket_arn" {
  description = "ARN of the SPA bucket (used in the CloudFront bucket policy)"
  value       = aws_s3_bucket.site.arn
}

output "bucket_regional_domain_name" {
  description = "Regional domain name of the bucket (CloudFront origin)"
  value       = aws_s3_bucket.site.bucket_regional_domain_name
}

output "distribution_id" {
  description = "CloudFront distribution ID (used for cache invalidations)"
  value       = aws_cloudfront_distribution.site.id
}

output "distribution_arn" {
  description = "CloudFront distribution ARN (used in the bucket policy)"
  value       = aws_cloudfront_distribution.site.arn
}

output "distribution_domain_name" {
  description = "Public URL of the site (xxxx.cloudfront.net)"
  value       = aws_cloudfront_distribution.site.domain_name
}