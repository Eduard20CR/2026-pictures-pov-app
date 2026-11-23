import { skipToken, useQuery } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { getCurrentUser } from '../api/getCurrentUser'

export function useCurrentUser() {
    const auth = useAuth()
    const accessToken = auth.user?.access_token

    return useQuery({
        queryKey: ['current-user', auth.user?.profile.sub],
        // skipToken: no request until there is a token (signed out = no call).
        queryFn: accessToken ? () => getCurrentUser(accessToken) : skipToken,
    })
}