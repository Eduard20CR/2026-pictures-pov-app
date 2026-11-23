import { api } from '../../../api/client'
import type { UploadQuota } from '../types'

export async function getUploadQuota(publicCode: string): Promise<UploadQuota> {
    const { data, response } = await api.GET('/events/{publicCode}/quota', {
        params: { path: { publicCode } },
    })
    if (!response.ok || !data) {
        throw new Error(`HTTP ${response.status}`)
    }
    return data
}