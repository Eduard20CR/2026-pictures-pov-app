import { beforeEach, expect, test, vi } from 'vitest'
import { api } from '../../../api/client'
import type { components } from '../../../api/schema'
import { inviteModerator } from './inviteModerator'

vi.mock('../../../api/client', () => ({ api: { POST: vi.fn() } }))

type ApiProblem = components['schemas']['ApiProblem']

// Script for api.POST: what openapi-fetch returns when the request fails
function failWith(status: number, error?: Partial<ApiProblem>) {
    vi.mocked(api.POST).mockResolvedValue(
        { data: undefined, error, response: new Response(null, { status }) } as never,
    )
}

beforeEach(() => vi.resetAllMocks())

test('throws the code the API sends when the email is already invited', async () => {
    failWith(409, { code: 'INVITATION_ALREADY_EXISTS' })

    await expect(inviteModerator('token', '1', 'beto@example.com')).rejects.toMatchObject({
        code: 'INVITATION_ALREADY_EXISTS',
    })
})

test('throws HTTP_<status> when the API sends no code', async () => {
    failWith(502)

    await expect(inviteModerator('token', '1', 'beto@example.com')).rejects.toMatchObject({ code: 'HTTP_502' })
})
