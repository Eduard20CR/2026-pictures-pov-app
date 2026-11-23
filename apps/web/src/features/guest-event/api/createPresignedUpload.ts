import { api } from '../../../api/client'
import type { PresignedUpload } from '../types'
import { UploadError } from './UploadError'

export async function createPresignedUpload(publicCode: string, file: File): Promise<PresignedUpload> {
    const { data, error, response } = await api.POST('/events/{publicCode}/uploads', {
        params: { path: { publicCode } },
        body: { contentType: file.type, sizeBytes: file.size },
    })

    if (!response.ok || !data) {
        // A body from outside the API (e.g. a gateway error) has no code: fall back to the status
        throw new UploadError(error?.code ?? `HTTP_${response.status}`)
    }

    return data
}