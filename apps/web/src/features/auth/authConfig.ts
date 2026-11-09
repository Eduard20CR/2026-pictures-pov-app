import type { AuthProviderProps } from 'react-oidc-context'

// Fails at startup with a clear message instead of a confusing login error later.
function requireEnv(name: string, value: string | undefined): string {
    if (!value) {
        throw new Error(`Missing environment variable ${name}`)
    }
    return value
}

export const cognitoDomain = requireEnv('VITE_COGNITO_DOMAIN', import.meta.env.VITE_COGNITO_DOMAIN)
export const cognitoClientId = requireEnv('VITE_COGNITO_CLIENT_ID', import.meta.env.VITE_COGNITO_CLIENT_ID)

export const oidcConfig: AuthProviderProps = {
    authority: requireEnv('VITE_COGNITO_AUTHORITY', import.meta.env.VITE_COGNITO_AUTHORITY),
    client_id: cognitoClientId,
    redirect_uri: `${window.location.origin}/auth/callback`,
    response_type: 'code',
    scope: 'openid email profile',
    // Cognito does not publish its logout URL, so we tell the library where it is.
    metadataSeed: {
        end_session_endpoint: `${cognitoDomain}/logout`,
    },
}