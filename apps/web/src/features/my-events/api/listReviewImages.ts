import { api } from '../../../api/client'
import type { ReviewImagePage } from '../types'

/** One page of the review, newest first; cursor is null for the first page. */
export async function listReviewImages(accessToken: string, eventId: string, cursor: string | null): Promise<ReviewImagePage> {
    const { data, response } = await api.GET('/me/events/{id}/images', {
        params: { path: { id: eventId }, query: cursor ? { cursor } : {} },
        headers: { Authorization: `Bearer ${accessToken}` },
    })

    if (!response.ok || !data) {
        throw new Error(`HTTP_${response.status}`)
    }

    return data
}
