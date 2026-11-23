import { api } from '../../../api/client'
import type { MyEvent } from '../types'

export async function listMyEvents(accessToken: string): Promise<MyEvent[]> {
    const { data, response } = await api.GET('/me/events', {
        headers: { Authorization: `Bearer ${accessToken}` },
    })

    if (!response.ok || !data) {
        throw new Error(`HTTP_${response.status}`)
    }

    return data
}
