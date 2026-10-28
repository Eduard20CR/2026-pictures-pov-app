import { api } from '../../../api/client'
import type { PublicEvent } from '../types'

export async function getPublicEvent(publicCode: string): Promise<PublicEvent | null> {
    const { data, response } = await api.GET('/events/{publicCode}', {
        params: { path: { publicCode } },
    })
    if (response.status === 404) {
        return null
    }
    if (!response.ok || !data) {
        throw new Error(`HTTP ${response.status}`)
    }
    return data
}