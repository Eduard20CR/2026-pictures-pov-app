# 0018. Self sign-up with email and password only

- **Status:** Accepted
- **Date:** 2026-11-17

## Context

Events and moderator roles are granted to an email, even before that person has an account (RF-ADM-02, RF-ORG-22). The user pool allowed only administrator-created accounts, so invitees could not get in. [0006](0006-authentication-amazon-cognito.md) expected Google sign-in later (RF-AUT-01).

## Options

| Option | Pro | Con |
|---|---|---|
| A. Administrator creates every account | Only invited people have accounts | Manual work per invitee; temporary passwords expire; the API would need Cognito admin permissions to automate it |
| B. Self sign-up, email and password | Invitees join on their own; email verified by a code | Anyone can create an account |
| C. Option B plus Google | No new password for Google users | Two sign-in methods per email need account linking; one more secret to manage |

## Decision

**Option B.** Anyone creates an account and confirms their email with a code before getting a session. An account alone reaches no event, so open sign-up grants nothing (RN-04).

## Consequences

- ✅ No account linking: one email, one account, one sign-in method.
- ✅ Administrators no longer create accounts, except the first one (`scripts/create-first-admin.sh`).
- ⚠️ Cognito's default email sender has a low daily limit; production needs Amazon SES.
- ⚠️ Accounts without invitations accumulate; they are harmless and can be deleted (RF-ADM-11).
