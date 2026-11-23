// @vitest-environment jsdom
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, renderHook, waitFor } from '@testing-library/react'
import type { ReactNode } from 'react'
import { beforeEach, expect, test, vi } from 'vitest'
import { useUploadQueue } from './useUploadQueue'
import { createPresignedUpload } from '../api/createPresignedUpload'
import { uploadPhotoToS3 } from '../api/uploadPhotoToS3'

vi.mock('../api/createPresignedUpload')
vi.mock('../api/uploadPhotoToS3')

function presignedUpload(expiresAt: string) {
    return { imageId: 'img-1', uploadUrl: 'https://s3.example/upload', expiresAt }
}

function renderQueue() {
    const queryClient = new QueryClient()
    const wrapper = ({ children }: { children: ReactNode }) => (
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    )
    return renderHook(() => useUploadQueue('abc'), { wrapper })
}

const photo = new File(['content'], 'a.jpg', { type: 'image/jpeg' })

beforeEach(() => vi.resetAllMocks())

test('asks for a new upload URL when the old one has expired', async () => {
    // Script: the first upload URL is already expired and S3 fails once
    vi.mocked(createPresignedUpload).mockResolvedValue(presignedUpload('2000-01-01T00:00:00Z'))
    vi.mocked(uploadPhotoToS3).mockRejectedValueOnce(new Error('network'))
    const { result } = renderQueue()

    act(() => result.current.addFiles([photo]))
    await waitFor(() => expect(result.current.uploadItems[0].status).toBe('error'))

    act(() => result.current.retryItem(result.current.uploadItems[0]))
    await waitFor(() => expect(result.current.uploadItems[0].status).toBe('done'))

    expect(createPresignedUpload).toHaveBeenCalledTimes(2)
})

test('shows UNKNOWN when the failure is not an UploadError', async () => {
    vi.mocked(createPresignedUpload).mockRejectedValue(new TypeError('Failed to fetch'))
    const { result } = renderQueue()

    act(() => result.current.addFiles([photo]))

    await waitFor(() => expect(result.current.uploadItems[0].error).toBe('UNKNOWN'))
})
