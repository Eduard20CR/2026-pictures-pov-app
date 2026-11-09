import { skipToken, useQuery } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { getMe } from '../api/getMe'

export function useMe() {
    const auth = useAuth()
    const accessToken = auth.user?.access_token

    return useQuery({
        queryKey: ['me', auth.user?.profile.sub],
        // skipToken: no request until there is a token (signed out = no call).
        queryFn: accessToken ? () => getMe(accessToken) : skipToken,
    })
}