import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { listAllEvents } from '../api/listAllEvents'

export function useAllEvents(page: number) {
    const auth = useAuth()

    return useQuery({
        queryKey: ['admin-events', page],
        queryFn: () => listAllEvents(auth.user?.access_token ?? '', page),
        placeholderData: keepPreviousData,
    })
}