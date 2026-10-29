output "bucket_name" {
  description = "Name of the S3 bucket that holds the SPA files"
  # Read from the public access block so callers (the bucket policy in cdn) wait for it.
  value = aws_s3_bucket_public_access_block.this.bucket
}

output "bucket_arn" {
  description = "ARN of the SPA bucket (used in the CloudFront bucket policy)"
  value       = aws_s3_bucket.this.arn
}

output "bucket_regional_domain_name" {
  description = "Regional domain name of the bucket (CloudFront origin)"
  value       = aws_s3_bucket.this.bucket_regional_domain_name
}
