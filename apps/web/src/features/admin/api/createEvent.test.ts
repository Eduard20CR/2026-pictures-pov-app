import { beforeEach, expect, test, vi } from 'vitest'
import { api } from '../../../api/client'
import type { components } from '../../../api/schema'
import { createEvent } from './createEvent'

vi.mock('../../../api/client', () => ({ api: { POST: vi.fn() } }))

type ApiProblem = components['schemas']['ApiProblem']

const body = { name: '', eventDate: '2099-12-20', organizerEmail: 'ana@example.com' }

// Script for api.POST: what openapi-fetch returns when the request fails
function failWith(status: number, error?: Partial<ApiProblem>) {
    vi.mocked(api.POST).mockResolvedValue(
        { data: undefined, error, response: new Response(null, { status }) } as never,
    )
}

beforeEach(() => vi.resetAllMocks())

test('maps the invalid fields the API sends', async () => {
    failWith(400, { code: 'VALIDATION_FAILED', errors: [{ field: 'name', message: 'must not be blank' }] })

    await expect(createEvent('token', body)).rejects.toMatchObject({
        code: 'VALIDATION_FAILED',
        fieldErrors: { name: 'must not be blank' },
    })
})

test('throws HTTP_<status> when the API sends no code', async () => {
    failWith(502)

    await expect(createEvent('token', body)).rejects.toMatchObject({ code: 'HTTP_502', fieldErrors: {} })
})
