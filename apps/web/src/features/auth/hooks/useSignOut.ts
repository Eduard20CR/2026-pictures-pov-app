import { useAuth } from 'react-oidc-context'
import { cognitoClientId } from '../authConfig'

export function useSignOut() {
    const auth = useAuth()

    // Official library sign out: clears the tokens and goes to Cognito /logout.
    // While it runs, RequireAuth waits instead of sending you back to sign in.
    return () =>
        auth.signoutRedirect({
            // Cognito asks for these two names, not the standard OIDC ones.
            extraQueryParams: {
                client_id: cognitoClientId,
                logout_uri: `${window.location.origin}/`,
            },
        })
}