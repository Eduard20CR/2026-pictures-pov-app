import { useMutation } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { getImageDownloadUrl } from '../api/getImageDownloadUrl'

/** Downloads a photo's original: the signed link serves it as an attachment, so the page stays. */
export function useDownloadImage(eventId: string) {
    const auth = useAuth()

    return useMutation({
        mutationFn: (imageId: string) => getImageDownloadUrl(auth.user?.access_token ?? '', eventId, imageId),
        onSuccess: (url) => window.location.assign(url),
    })
}
