# 0006. Authentication with Amazon Cognito

- **Status:** Accepted
- **Date:** 2026-10-09

## Context

Administrators and organizers sign in; guests do not (RF-INV-01). We need sign-in/out, password recovery, MFA for administrators (RF-AUT-01 to RF-AUT-04) and a way to tell administrators from organizers.

## Options

| Option | Pro | Con |
|---|---|---|
| Amazon Cognito | Native to AWS; recovery, MFA, groups and Google built in; free at our scale | Limited UI customization; some settings fixed at creation |
| Third party (e.g. Auth0) | Flexible; good developer experience | External account; paid as usage grows |
| Custom with Spring Security | Full control | We build and secure passwords, recovery and MFA ourselves |

## Decision

**Amazon Cognito** with a user pool and managed sign-in: email and password, likely Google later as a federated provider, and a Cognito group for administrators. Credentials never reach the backend, which only validates tokens.

## Consequences

- ✅ Requirements covered by a managed service; adding Google is configuration only.
- ⚠️ Some settings (e.g. email as identifier) cannot change after creation: define them right.
- ⚠️ With Google, the same email must link to one account so users keep their events (RN-04).
- ⚠️ RF-AUT-01 makes Google mandatory (M): align it if Google is not built.
