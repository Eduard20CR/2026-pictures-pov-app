import type { components } from '../../api/schema'

export type PublicEvent = components['schemas']['PublicEventResponse']

export type Quota = components['schemas']['QuotaResponse']
export type UploadTicket = components['schemas']['UploadResponse']

export type UploadItem = {
    id: string
    file: File
    ticket?: UploadTicket
    status: 'waiting' | 'uploading' | 'done' | 'error'
    progress: number
    error?: string
}