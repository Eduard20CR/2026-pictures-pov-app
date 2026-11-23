import { useQuery } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { listMyEvents } from '../api/listMyEvents'

export function useMyEvents() {
    const auth = useAuth()

    return useQuery({
        queryKey: ['my-events'],
        queryFn: () => listMyEvents(auth.user?.access_token ?? ''),
    })
}
