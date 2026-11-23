import { afterEach, expect, test, vi } from 'vitest'
import { uploadPhotoToS3 } from './uploadPhotoToS3'

const presignedUpload = { imageId: 'img-1', uploadUrl: 'https://s3.example/upload', expiresAt: '2026-11-18T12:00:00Z' }
const photo = new File(['content'], 'a.jpg', { type: 'image/jpeg' })

function s3Answers(status: number) {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status }))
    vi.stubGlobal('fetch', fetchMock)
    return fetchMock
}

afterEach(() => vi.unstubAllGlobals())

test('asks S3 to create the photo only if it does not exist yet', async () => {
    const fetchMock = s3Answers(200)

    await uploadPhotoToS3(presignedUpload, photo)

    expect(fetchMock).toHaveBeenCalledWith(presignedUpload.uploadUrl, expect.objectContaining({
        method: 'PUT',
        headers: expect.objectContaining({ 'If-None-Match': '*' }),
    }))
})

test('treats 412 as success: the photo from this URL already arrived', async () => {
    s3Answers(412)

    await expect(uploadPhotoToS3(presignedUpload, photo)).resolves.toBeUndefined()
})

test('fails on any other S3 error', async () => {
    s3Answers(403)

    await expect(uploadPhotoToS3(presignedUpload, photo)).rejects.toMatchObject({ code: 'S3_UPLOAD_FAILED' })
})
