# static_site was split into web_bucket and cdn (2026-10-29).
# These blocks keep the existing resources (and the CloudFront URL) instead of recreating them.
# They can be removed once dev and every other environment have applied them.

moved {
  from = module.static_site.aws_s3_bucket.site
  to   = module.web_bucket.aws_s3_bucket.this
}

moved {
  from = module.static_site.aws_s3_bucket_public_access_block.site
  to   = module.web_bucket.aws_s3_bucket_public_access_block.this
}

moved {
  from = module.static_site.aws_cloudfront_origin_access_control.site
  to   = module.cdn.aws_cloudfront_origin_access_control.web
}

moved {
  from = module.static_site.aws_cloudfront_distribution.site
  to   = module.cdn.aws_cloudfront_distribution.this
}

moved {
  from = module.static_site.aws_s3_bucket_policy.site
  to   = module.cdn.aws_s3_bucket_policy.web_bucket
}
