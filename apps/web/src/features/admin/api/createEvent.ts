import { api } from '../../../api/client'
import type { AdminEvent, CreateEventRequest } from '../types'
import { CreateEventError, type FieldErrors } from './CreateEventError'

type ApiError = {
    code?: string
    errors?: { field: string; message: string }[]
}

export async function createEvent(accessToken: string, body: CreateEventRequest): Promise<AdminEvent> {
    const { data, error, response } = await api.POST('/admin/events', {
        headers: { Authorization: `Bearer ${accessToken}` },
        body,
    })

    if (!response.ok || !data) {
        const apiError = error as ApiError | undefined
        const fieldErrors: FieldErrors = {}
        apiError?.errors?.forEach(({ field, message }) => { fieldErrors[field] = message })
        throw new CreateEventError(apiError?.code ?? `HTTP_${response.status}`, fieldErrors)
    }

    return data
}