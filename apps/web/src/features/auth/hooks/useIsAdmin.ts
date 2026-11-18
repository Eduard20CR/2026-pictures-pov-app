import { useAuth } from 'react-oidc-context'

// Cognito puts the user's groups in the ID token. This only drives the UI:
// the API checks the same group on every /admin request (RN-05).
export function useIsAdmin(): boolean {
    const groups = useAuth().user?.profile['cognito:groups']
    return Array.isArray(groups) && groups.includes('admins')
}
