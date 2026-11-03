import { api } from '../../../api/client'
import type { UploadTicket } from '../types'
import { UploadError } from './UploadError'

type ApiError = { code?: string }

export async function requestUpload(publicCode: string, file: File): Promise<UploadTicket> {
    const { data, error, response } = await api.POST('/events/{publicCode}/uploads', {
        params: { path: { publicCode } },
        body: { contentType: file.type, sizeBytes: file.size },
    })

    if (!response.ok || !data) {
        const apiError = error as ApiError | undefined
        const code = apiError?.code ?? `HTTP_${response.status}`
        throw new UploadError(code)
    }

    return data
}