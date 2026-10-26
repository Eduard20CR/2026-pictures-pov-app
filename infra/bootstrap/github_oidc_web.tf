resource "aws_iam_role" "github_actions_dev_web" {
  name               = "${var.project}-dev-gha-web"
  assume_role_policy = data.aws_iam_policy_document.github_trust_dev.json

  tags = {
    Environment = "dev"
  }
}

data "aws_iam_policy_document" "github_actions_dev_web" {
  statement {
    sid       = "ListSite"
    actions   = ["s3:ListBucket"] 
    resources = ["arn:aws:s3:::${var.project}-dev-web-${data.aws_caller_identity.current.account_id}"]
  }

  statement {
    sid       = "SyncSite"
    actions   = ["s3:PutObject", "s3:DeleteObject"] 
    resources = ["arn:aws:s3:::${var.project}-dev-web-${data.aws_caller_identity.current.account_id}/*"]
  }

  statement {
    sid       = "InvalidateCache"
    actions   = ["cloudfront:CreateInvalidation"]
    resources = ["arn:aws:cloudfront::${data.aws_caller_identity.current.account_id}:distribution/${var.dev_web_distribution_id}"]
  }
}

resource "aws_iam_role_policy" "github_actions_dev_web" {
  name   = "deploy-web"
  role   = aws_iam_role.github_actions_dev_web.id
  policy = data.aws_iam_policy_document.github_actions_dev_web.json
}


# PROD

resource "aws_iam_role" "github_actions_prod_web" {
  name               = "${var.project}-prod-gha-web"
  assume_role_policy = data.aws_iam_policy_document.github_trust_prod.json

  tags = {
    Environment = "prod"
  }
}

resource "aws_iam_role_policy_attachment" "github_actions_prod_web" {
  role       = aws_iam_role.github_actions_prod_web.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}


