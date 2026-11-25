import { api } from '../../../api/client'
import type { ZipExport } from '../types'
import { ZipExportError } from './ZipExportError'

/** Starts the ZIP in the background, or returns the one in progress (ADR 0008). */
export async function requestZipExport(accessToken: string, eventId: string): Promise<ZipExport> {
    const { data, error, response } = await api.POST('/me/events/{id}/zip-export', {
        params: { path: { id: eventId } },
        headers: { Authorization: `Bearer ${accessToken}` },
    })

    if (!response.ok || !data) {
        // A body from outside the API (e.g. a gateway error) has no code: fall back to the status
        throw new ZipExportError(error?.code ?? `HTTP_${response.status}`)
    }

    return data
}
