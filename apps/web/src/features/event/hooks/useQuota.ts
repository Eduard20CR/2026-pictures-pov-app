import { useQuery } from '@tanstack/react-query'
import { getQuota } from '../api/getQuota'

export function useQuota(publicCode: string) {
    return useQuery({
        queryKey: ['events', publicCode, 'quota'],
        queryFn: () => getQuota(publicCode),
    })
}