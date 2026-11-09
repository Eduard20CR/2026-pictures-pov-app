import { api } from '../../../api/client'
import type { Me } from '../types'

export async function getMe(accessToken: string): Promise<Me> {
    const { data, response } = await api.GET('/me', {
        headers: { Authorization: `Bearer ${accessToken}` },
    })
    if (!response.ok || !data) {
        throw new Error(`HTTP ${response.status}`)
    }
    return data
}