import { skipToken, useQuery } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { listMyEvents } from '../api/listMyEvents'

export function useMyEvents() {
    const auth = useAuth()
    const accessToken = auth.user?.access_token

    return useQuery({
        // Keyed by user, so one user's cached list is never shown to another.
        queryKey: ['my-events', auth.user?.profile.sub],
        queryFn: accessToken ? () => listMyEvents(accessToken) : skipToken,
    })
}
