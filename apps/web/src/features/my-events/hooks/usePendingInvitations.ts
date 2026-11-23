import { skipToken, useQuery } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { listPendingInvitations } from '../api/listPendingInvitations'

export function usePendingInvitations() {
    const auth = useAuth()
    const accessToken = auth.user?.access_token

    return useQuery({
        // Keyed by user, so one user's cached invitations are never shown to another.
        queryKey: ['my-invitations', auth.user?.profile.sub],
        queryFn: accessToken ? () => listPendingInvitations(accessToken) : skipToken,
    })
}
