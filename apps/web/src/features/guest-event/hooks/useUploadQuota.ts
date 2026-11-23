import { useQuery } from '@tanstack/react-query'
import { getUploadQuota } from '../api/getUploadQuota'

export function useUploadQuota(publicCode: string) {
    return useQuery({
        queryKey: ['events', publicCode, 'quota'],
        queryFn: () => getUploadQuota(publicCode),
    })
}