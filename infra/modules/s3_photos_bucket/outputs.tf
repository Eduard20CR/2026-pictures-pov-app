output "bucket_name" {
  description = "Name of the photos bucket"
  value       = aws_s3_bucket.photos.id
}

output "bucket_arn" {
  description = "ARN of the photos bucket (used in IAM policies)"
  value       = aws_s3_bucket.photos.arn
}