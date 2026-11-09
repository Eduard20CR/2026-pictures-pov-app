import { Navigate } from 'react-router'
import { useAuth } from 'react-oidc-context'

// Cognito sends the browser back here with ?code=...; AuthProvider exchanges it for tokens.
export default function AuthCallbackPage() {
    const auth = useAuth()

    if (auth.error) {
        return <p role="alert">Sign-in failed: {auth.error.message}</p>
    }

    if (auth.isAuthenticated) {
        // replace: the URL with the one-time code must not stay in the browser history.
        return <Navigate to="/my-events" replace />
    }

    return <p>Signing you in…</p>
}