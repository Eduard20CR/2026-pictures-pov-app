import { api } from '../../../api/client'
import type { AdminEvent, CreateEventRequest } from '../types'
import { CreateEventError, type FieldErrors } from './CreateEventError'

export async function createEvent(accessToken: string, body: CreateEventRequest): Promise<AdminEvent> {
    const { data, error, response } = await api.POST('/admin/events', {
        headers: { Authorization: `Bearer ${accessToken}` },
        body,
    })

    if (!response.ok || !data) {
        // A body from outside the API (e.g. a gateway error) has no code: fall back to the status
        const fieldErrors: FieldErrors = {}
        error?.errors?.forEach(({ field, message }) => { fieldErrors[field] = message })
        throw new CreateEventError(error?.code ?? `HTTP_${response.status}`, fieldErrors)
    }

    return data
}