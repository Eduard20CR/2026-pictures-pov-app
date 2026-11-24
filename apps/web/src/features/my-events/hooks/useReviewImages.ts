import { skipToken, useInfiniteQuery } from '@tanstack/react-query'
import { useAuth } from 'react-oidc-context'
import { listReviewImages } from '../api/listReviewImages'

export function useReviewImages(eventId: string) {
    const auth = useAuth()
    const accessToken = auth.user?.access_token

    return useInfiniteQuery({
        // Keyed by user, so one user's cached photos are never shown to another.
        queryKey: ['my-events', auth.user?.profile.sub, eventId, 'review'],
        queryFn: accessToken ? ({ pageParam }) => listReviewImages(accessToken, eventId, pageParam) : skipToken,
        initialPageParam: null as string | null,
        getNextPageParam: (lastPage) => lastPage.nextCursor,
        // Signed thumbnail links expire (P-12): never reuse a page older than a few minutes.
        staleTime: 5 * 60 * 1000,
        gcTime: 5 * 60 * 1000,
    })
}
