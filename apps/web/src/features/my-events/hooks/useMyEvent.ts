import { skipToken, useQuery } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { getMyEvent } from '../api/getMyEvent'

export function useMyEvent(id: string) {
    const auth = useAuth()
    const accessToken = auth.user?.access_token

    return useQuery({
        // Keyed by user, so one user's cached event is never shown to another.
        queryKey: ['my-events', auth.user?.profile.sub, id],
        queryFn: accessToken ? () => getMyEvent(accessToken, id) : skipToken,
    })
}
