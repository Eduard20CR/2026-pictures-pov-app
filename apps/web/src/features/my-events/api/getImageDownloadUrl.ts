import { api } from '../../../api/client'

/** Temporary signed link that downloads a photo's original (RF-ORG-13, RN-08); organizer only. */
export async function getImageDownloadUrl(accessToken: string, eventId: string, imageId: string): Promise<string> {
    const { data, response } = await api.GET('/me/events/{id}/images/{imageId}/download', {
        params: { path: { id: eventId, imageId } },
        headers: { Authorization: `Bearer ${accessToken}` },
    })

    if (!response.ok || !data) {
        throw new Error(`HTTP_${response.status}`)
    }

    return data.url
}
