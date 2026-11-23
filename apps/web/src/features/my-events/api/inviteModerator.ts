import { api } from '../../../api/client'
import type { Invitation } from '../types'
import { InviteModeratorError } from './InviteModeratorError'

export async function inviteModerator(accessToken: string, eventId: string, email: string): Promise<Invitation> {
    const { data, error, response } = await api.POST('/me/events/{id}/invitations', {
        params: { path: { id: eventId } },
        headers: { Authorization: `Bearer ${accessToken}` },
        body: { email },
    })

    if (!response.ok || !data) {
        // A body from outside the API (e.g. a gateway error) has no code: fall back to the status
        throw new InviteModeratorError(error?.code ?? `HTTP_${response.status}`)
    }

    return data
}
