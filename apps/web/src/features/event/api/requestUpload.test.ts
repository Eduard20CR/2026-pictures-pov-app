import { beforeEach, expect, test, vi } from 'vitest'
import { api } from '../../../api/client'
import { requestUpload } from './requestUpload'

vi.mock('../../../api/client', () => ({ api: { POST: vi.fn() } }))

const photo = new File(['content'], 'a.jpg', { type: 'image/jpeg' })

// Script for api.POST: what openapi-fetch returns when the request fails
function failWith(status: number, error?: { code: string }) {
    vi.mocked(api.POST).mockResolvedValue(
        { data: undefined, error, response: new Response(null, { status }) } as never,
    )
}

beforeEach(() => vi.resetAllMocks())

test('throws the code the API sends', async () => {
    failWith(409, { code: 'UPLOAD_LIMIT_REACHED' })

    await expect(requestUpload('abc', photo)).rejects.toMatchObject({ code: 'UPLOAD_LIMIT_REACHED' })
})

test('throws HTTP_<status> when the API sends no code', async () => {
    failWith(502)

    await expect(requestUpload('abc', photo)).rejects.toMatchObject({ code: 'HTTP_502' })
})
