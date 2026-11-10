import { useMutation } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { createEvent } from '../api/createEvent'
import type { CreateEventRequest } from '../types'

export function useCreateEvent() {
    const auth = useAuth()

    return useMutation({
        mutationFn: (body: CreateEventRequest) =>
            createEvent(auth.user?.access_token ?? '', body),
    })
}