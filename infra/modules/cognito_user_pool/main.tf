data "aws_region" "current" {}

resource "aws_cognito_user_pool" "pool" {
  name = var.name

  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  # Ana@x.com and ana@x.com are one user (RN-04). Fixed at creation: changing it replaces the pool.
  username_configuration {
    case_sensitive = false
  }

  # Self sign-up: the user confirms the email code before getting a session.
  admin_create_user_config {
    allow_admin_create_user_only = false
  }

  # A changed email applies only after the new one is verified (RN-04).
  user_attribute_update_settings {
    attributes_require_verification_before_update = ["email"]
  }

  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }
}

resource "aws_cognito_user_pool_client" "web" {
  name         = "${var.name}-web"
  user_pool_id = aws_cognito_user_pool.pool.id

  generate_secret = false

  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = ["openid", "email", "profile"]
  supported_identity_providers         = ["COGNITO"]

  callback_urls = var.callback_urls
  logout_urls   = var.logout_urls

  prevent_user_existence_errors = "ENABLED"
}

resource "aws_cognito_user_pool_domain" "login" {
  domain                = var.name
  user_pool_id          = aws_cognito_user_pool.pool.id
  managed_login_version = 2
}

resource "aws_cognito_managed_login_branding" "web" {
  user_pool_id = aws_cognito_user_pool.pool.id
  client_id    = aws_cognito_user_pool_client.web.id

  use_cognito_provided_values = true
}

resource "aws_cognito_user_group" "admins" {
  name         = "admins"
  user_pool_id = aws_cognito_user_pool.pool.id
  description  = "Platform administrators: create events and invite owners."
}