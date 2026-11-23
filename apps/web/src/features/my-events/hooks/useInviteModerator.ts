import { useMutation } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { inviteModerator } from '../api/inviteModerator'

export function useInviteModerator(eventId: string) {
    const auth = useAuth()

    return useMutation({
        mutationFn: (email: string) => inviteModerator(auth.user?.access_token ?? '', eventId, email),
    })
}
