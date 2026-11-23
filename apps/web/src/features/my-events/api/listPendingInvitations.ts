import { api } from '../../../api/client'
import type { PendingInvitation } from '../types'

/** Empty when my email is not verified. */
export async function listPendingInvitations(accessToken: string): Promise<PendingInvitation[]> {
    const { data, response } = await api.GET('/me/invitations', {
        headers: { Authorization: `Bearer ${accessToken}` },
    })

    if (!response.ok || !data) {
        throw new Error(`HTTP_${response.status}`)
    }

    return data
}
