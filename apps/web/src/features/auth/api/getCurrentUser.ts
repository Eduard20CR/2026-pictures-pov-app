import { api } from '../../../api/client'
import type { CurrentUser } from '../types'

export async function getCurrentUser(accessToken: string): Promise<CurrentUser> {
    const { data, response } = await api.GET('/me', {
        headers: { Authorization: `Bearer ${accessToken}` },
    })
    if (!response.ok || !data) {
        throw new Error(`HTTP ${response.status}`)
    }
    return data
}