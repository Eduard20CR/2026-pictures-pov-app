import { api } from '../../../api/client'
import type { AdminEventPage } from '../types'

export async function listEvents(accessToken: string, page: number): Promise<AdminEventPage> {
    const { data, response } = await api.GET('/admin/events', {
        headers: { Authorization: `Bearer ${accessToken}` },
        params: { query: { page, size: 20 } },
    })

    if (!response.ok || !data) {
        throw new Error(`HTTP_${response.status}`)
    }

    return data
}