import type { UploadTicket } from '../types'
import { UploadError } from './UploadError'

// The signed URL is create-only: a reused ticket gets 412 and cannot replace the photo.
const PHOTO_ALREADY_UPLOADED = 412

export async function uploadToS3(ticket: UploadTicket, file: File): Promise<void> {
    const response = await fetch(ticket.uploadUrl, {
        method: 'PUT',
        headers: { 'Content-Type': file.type, 'If-None-Match': '*' },
        body: file,
    })
    // 412 on a retry: the first PUT arrived but its response was lost.
    if (!response.ok && response.status !== PHOTO_ALREADY_UPLOADED) {
        throw new UploadError('S3_UPLOAD_FAILED')
    }
}
