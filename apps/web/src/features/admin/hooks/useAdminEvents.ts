import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { listEvents } from '../api/listEvents'

export function useAdminEvents(page: number) {
    const auth = useAuth()

    return useQuery({
        queryKey: ['admin-events', page],
        queryFn: () => listEvents(auth.user?.access_token ?? '', page),
        placeholderData: keepPreviousData,
    })
}