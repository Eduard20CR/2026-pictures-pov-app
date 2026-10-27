import { useQuery } from '@tanstack/react-query'
import { getPublicEvent } from '../api/getPublicEvent'

export function usePublicEvent(publicCode: string) {
    return useQuery({
        queryKey: ['events', publicCode],
        queryFn: () => getPublicEvent(publicCode),
    })
}