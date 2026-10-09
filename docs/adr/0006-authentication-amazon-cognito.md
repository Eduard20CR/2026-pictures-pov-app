# 0006. Authentication with Amazon Cognito

- **Status:** Accepted
- **Date:** 2026-10-09

## Context and problem statement

Administrators and organizers must sign in to the application; guests do not authenticate (RF-INV-01). The authentication system must support sign-in and sign-out, password recovery and multi-factor authentication for administrators (RF-AUT-01 to RF-AUT-04), and must distinguish administrators from organizers.

## Decision drivers

- Coverage of the authentication requirements without building them from scratch.
- Security of credential storage and handling.
- Integration with the rest of the AWS architecture.
- Cost compatible with RNF-COS-01.

## Considered options

1. Amazon Cognito.
2. A third-party identity provider (e.g. Auth0).
3. A custom implementation with Spring Security.

## Decision

**Option 1 is chosen: Amazon Cognito**, with a user pool and its managed sign-in.

- Users sign in with **email and password**.
- **Google** will likely be added as a second sign-in method, configured as a federated identity provider in the same user pool.
- Administrators are identified through a Cognito group.

The main reason is that Cognito covers the required features (password recovery, MFA, groups and federation with Google) as a managed AWS service, without the backend having to store or handle credentials.

## Pros and cons of the options

### Option 1: Amazon Cognito

- Pro: native to AWS and integrated with API Gateway; covers password recovery, MFA, groups and Google federation; no cost at the project's expected number of users.
- Con: customizing the managed sign-in is limited, and some user pool settings cannot be changed after creation.

### Option 2: Third-party identity provider

- Pro: flexible customization and a good developer experience.
- Con: adds an external service and account outside AWS, with paid plans as usage grows.

### Option 3: Custom implementation with Spring Security

- Pro: full control over the flow and the user interface.
- Con: requires building and securing password storage, recovery and MFA, increasing effort and security risk.

## Consequences

### Positive

- Authentication requirements are covered by a managed service, and credentials never reach the backend.
- The backend only validates the tokens issued by Cognito.
- Adding Google later requires configuration only, not changes to the sign-in model.

### Negative and risks

- **User pool settings.** Some attributes (such as using email as the sign-in identifier) cannot be changed after the user pool is created, so they must be defined correctly from the start.
- **Account linking.** If Google is added, a user who signs in with Google and with email and password using the same email must be linked to a single account, so that they see the same assigned events (RN-04).
- **Scope of RF-AUT-01.** RF-AUT-01 requires sign-in with Google as mandatory (M); the requirement must be aligned if Google is finally not implemented.

## Related decisions

- [0005. Backend compute on AWS Lambda](0005-compute-aws-lambda.md)
