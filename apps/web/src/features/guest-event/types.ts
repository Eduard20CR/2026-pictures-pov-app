import type { components } from '../../api/schema'

export type GuestEvent = components['schemas']['GuestEventResponse']

export type UploadQuota = components['schemas']['UploadQuotaResponse']
export type PresignedUpload = components['schemas']['PresignedUploadResponse']

export type UploadItem = {
    id: string
    file: File
    presignedUpload?: PresignedUpload
    status: 'waiting' | 'uploading' | 'done' | 'error'
    error?: string
}