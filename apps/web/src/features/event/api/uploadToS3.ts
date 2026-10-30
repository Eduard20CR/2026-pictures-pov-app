import type { UploadTicket } from '../../upload/types'
import { UploadError } from './UploadError'

export async function uploadToS3(ticket: UploadTicket, file: File): Promise<void> {
    const response = await fetch(ticket.uploadUrl, {
        method: 'PUT',
        headers: { 'Content-Type': file.type },
        body: file,
    })
    if (!response.ok) {
        throw new UploadError('S3_UPLOAD_FAILED')
    }
}