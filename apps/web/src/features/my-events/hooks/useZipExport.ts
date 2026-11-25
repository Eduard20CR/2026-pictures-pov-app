import { skipToken, useQuery } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { getZipExport } from '../api/getZipExport'

/** While the ZIP is in progress, how often the page asks again. */
const POLL_MS = 10_000

export function zipExportQueryKey(userId: string | undefined, eventId: string) {
    return ['my-events', userId, eventId, 'zip-export']
}

export function useZipExport(eventId: string) {
    const auth = useAuth()
    const accessToken = auth.user?.access_token

    return useQuery({
        // Keyed by user, so one user's cached ZIP is never shown to another.
        queryKey: zipExportQueryKey(auth.user?.profile.sub, eventId),
        queryFn: accessToken ? () => getZipExport(accessToken, eventId) : skipToken,
        refetchInterval: (query) => (query.state.data?.status === 'IN_PROGRESS' ? POLL_MS : false),
    })
}
