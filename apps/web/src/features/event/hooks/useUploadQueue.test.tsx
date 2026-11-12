// @vitest-environment jsdom
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, renderHook, waitFor } from '@testing-library/react'
import type { ReactNode } from 'react'
import { beforeEach, expect, test, vi } from 'vitest'
import { useUploadQueue } from './useUploadQueue'
import { requestUpload } from '../api/requestUpload'
import { uploadToS3 } from '../api/uploadToS3'

vi.mock('../api/requestUpload')
vi.mock('../api/uploadToS3')

function ticket(expiresAt: string) {
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

test('asks for a new ticket when the old one has expired', async () => {
    // Script: the first ticket is already expired and S3 fails once
    vi.mocked(requestUpload).mockResolvedValue(ticket('2000-01-01T00:00:00Z'))
    vi.mocked(uploadToS3).mockRejectedValueOnce(new Error('network'))
    const { result } = renderQueue()

    act(() => result.current.addFiles([photo]))
    await waitFor(() => expect(result.current.uploadItems[0].status).toBe('error'))

    act(() => result.current.retryItem(result.current.uploadItems[0]))
    await waitFor(() => expect(result.current.uploadItems[0].status).toBe('done'))

    expect(requestUpload).toHaveBeenCalledTimes(2)
})

test('shows UNKNOWN when the failure is not an UploadError', async () => {
    vi.mocked(requestUpload).mockRejectedValue(new TypeError('Failed to fetch'))
    const { result } = renderQueue()

    act(() => result.current.addFiles([photo]))

    await waitFor(() => expect(result.current.uploadItems[0].error).toBe('UNKNOWN'))
})
