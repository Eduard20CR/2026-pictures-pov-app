import { useQuery } from '@tanstack/react-query'
import { getGuestEvent } from '../api/getGuestEvent'

export function useGuestEvent(publicCode: string) {
    return useQuery({
        queryKey: ['events', publicCode],
        queryFn: () => getGuestEvent(publicCode),
    })
}