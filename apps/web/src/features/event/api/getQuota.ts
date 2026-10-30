import { api } from '../../../api/client'
import type { Quota } from '../../upload/types'

export async function getQuota(publicCode: string): Promise<Quota> {
    const { data, response } = await api.GET('/events/{publicCode}/quota', {
        params: { path: { publicCode } },
    })
    if (!response.ok || !data) {
        throw new Error(`HTTP ${response.status}`)
    }
    return data
}