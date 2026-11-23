import { api } from '../../../api/client'
import type { MyEvent } from '../types'

/** null on 404: no Accepted invitation, or the event does not exist or was deleted. */
export async function getMyEvent(accessToken: string, id: string): Promise<MyEvent | null> {
    const { data, response } = await api.GET('/me/events/{id}', {
        params: { path: { id } },
        headers: { Authorization: `Bearer ${accessToken}` },
    })

    if (response.status === 404) {
        return null
    }
    if (!response.ok || !data) {
        throw new Error(`HTTP_${response.status}`)
    }

    return data
}
