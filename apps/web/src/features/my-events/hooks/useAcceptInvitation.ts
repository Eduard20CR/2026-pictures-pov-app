import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { acceptInvitation } from '../api/acceptInvitation'

export function useAcceptInvitation() {
    const auth = useAuth()
    const queryClient = useQueryClient()
    const sub = auth.user?.profile.sub

    return useMutation({
        mutationFn: (id: string) => acceptInvitation(auth.user?.access_token ?? '', id),
        // Also on error: a removed invitation leaves the list, and the user sees the current state.
        onSettled: () => Promise.all([
            queryClient.invalidateQueries({ queryKey: ['my-invitations', sub] }),
            queryClient.invalidateQueries({ queryKey: ['my-events', sub] }),
        ]),
    })
}
