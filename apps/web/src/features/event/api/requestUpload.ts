import { api } from '../../../api/client'
import type { UploadTicket } from '../../upload/types'
import { UploadError } from './UploadError'

export async function requestUpload(publicCode: string, file: File): Promise<UploadTicket> {
    const { data, error, response } = await api.POST('/events/{publicCode}/uploads', {
        params: { path: { publicCode } },
        body: { contentType: file.type, sizeBytes: file.size },
    })
    if (!response.ok || !data) {
        const code = (error as { code?: string } | undefined)?.code ?? `HTTP_${response.status}`
        throw new UploadError(code)
    }
    return data
}