# 0017. API served under /api on the CloudFront origin

- **Status:** Accepted
- **Date:** 2026-11-02

## Context

Guests are identified by a `guest_id` cookie that the API sets on the first visit and reads on every upload (#30). The SPA was served by CloudFront and the API by its own API Gateway URL: two different sites. Safari and other browsers block third-party cookies, so the guest cookie would not reach the API. Cross-site cookies also require `SameSite=None`, CORS and a CSRF token.

## Options

| Option | Pro | Con |
|---|---|---|
| A. Separate URLs, CORS and `SameSite=None` cookie | No infrastructure change | Safari blocks the cookie; needs CORS and CSRF tokens |
| B. API on a subdomain (`api.<domain>`) | Same site, so the cookie works | Needs a custom domain and certificate, which the project does not have yet |
| C. CloudFront routes `/api/*` to API Gateway | Same origin; works with the CloudFront default domain | API paths gain the `/api` prefix; one more hop |

## Decision

**Option C.** CloudFront has a second origin (API Gateway) behind an `/api/*` behavior, and the API uses `server.servlet.context-path=/api`. The browser sees one origin, so the guest cookie is first-party in every browser, and a future custom domain only needs an alias on the distribution.

## Consequences

- ✅ The guest cookie is first-party with `SameSite=Lax`; no CORS configuration in the API.
- ✅ CSRF protection is disabled in Spring Security, because `SameSite=Lax` stops other sites from sending the cookie. Revisit when organizer sign-in arrives.
- ✅ Locally, the Vite dev server proxies `/api` to the API, so development matches dev and prod.
- ⚠️ The `/api/*` behavior uses `Managed-CachingDisabled` and `Managed-AllViewerExceptHostHeader`; forwarding the `Host` header would break API Gateway.
- ⚠️ A CloudFront Function rewrites SPA routes to `index.html`, replacing the distribution-wide custom error pages that hid real API errors.
- ⚠️ The API Gateway URL is still reachable directly; restricting it to CloudFront is pending.
