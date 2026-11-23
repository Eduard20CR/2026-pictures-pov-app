import { api } from '../../../api/client'

export async function acceptInvitation(accessToken: string, id: string): Promise<void> {
    const { response } = await api.PATCH('/me/invitations/{id}', {
        params: { path: { id } },
        headers: { Authorization: `Bearer ${accessToken}` },
        body: { status: 'ACCEPTED' },
    })

    if (!response.ok) {
        throw new Error(`HTTP_${response.status}`)
    }
}
