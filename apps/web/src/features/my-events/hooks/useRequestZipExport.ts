import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { requestZipExport } from '../api/requestZipExport'
import { zipExportQueryKey } from './useZipExport'

export function useRequestZipExport(eventId: string) {
    const auth = useAuth()
    const queryClient = useQueryClient()

    return useMutation({
        mutationFn: () => requestZipExport(auth.user?.access_token ?? '', eventId),
        // Shows the ZIP in progress at once; useZipExport then polls until it ends.
        onSuccess: (zip) => queryClient.setQueryData(zipExportQueryKey(auth.user?.profile.sub, eventId), zip),
    })
}
