import { api } from '../../../api/client'
import type { ZipExport } from '../types'

/** The event's latest ZIP (RF-ORG-14); null on 204, when none was requested yet. Organizer only. */
export async function getZipExport(accessToken: string, eventId: string): Promise<ZipExport | null> {
    const { data, response } = await api.GET('/me/events/{id}/zip-export', {
        params: { path: { id: eventId } },
        headers: { Authorization: `Bearer ${accessToken}` },
    })

    if (response.status === 204) {
        return null
    }
    if (!response.ok || !data) {
        throw new Error(`HTTP_${response.status}`)
    }

    return data
}
