// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import MyEventsPage from './MyEventsPage'
import { useMyEvents } from '../hooks/useMyEvents'
import { useMe } from '../../auth/hooks/useMe'
import type { MyEvent } from '../types'

vi.mock('../hooks/useMyEvents')
vi.mock('../../auth/hooks/useMe')
// Factory: an automock would load the real module, which needs the Cognito env vars.
vi.mock('../../auth/hooks/useSignOut', () => ({ useSignOut: () => vi.fn() }))

function givenEvents(events: MyEvent[]) {
    vi.mocked(useMyEvents).mockReturnValue(
        { data: events, isPending: false, isError: false } as unknown as ReturnType<typeof useMyEvents>,
    )
}

function renderPage() {
    render(<MemoryRouter><MyEventsPage /></MemoryRouter>)
}

beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useMe).mockReturnValue(
        { data: { id: 'u', email: 'ana@example.com' }, isPending: false, isError: false } as unknown as ReturnType<typeof useMe>,
    )
})

afterEach(cleanup)

test('shows each event with its name, date, status and my role in Spanish', () => {
    givenEvents([
        { id: '1', publicCode: 'a', name: 'Boda Ana y Luis', eventDate: '2026-12-12', status: 'OPEN', role: 'OWNER' },
        { id: '2', publicCode: 'b', name: 'Navidad', eventDate: '2026-11-01', status: 'CLOSED', role: 'MODERATOR' },
    ])

    renderPage()

    const rows = screen.getAllByRole('row').slice(1)
    expect(rows.map((row) => row.textContent)).toEqual([
        'Boda Ana y Luis2026-12-12AbiertoOrganizador',
        'Navidad2026-11-01CerradoModerador',
    ])
})

test('says so when I have no events', () => {
    givenEvents([])

    renderPage()

    expect(screen.getByText('Aún no tienes eventos.')).toBeTruthy()
    expect(screen.queryByRole('table')).toBeNull()
})

test('each event links to its page', () => {
    givenEvents([
        { id: '1', publicCode: 'a', name: 'Boda Ana y Luis', eventDate: '2026-12-12', status: 'OPEN', role: 'OWNER' },
    ])

    renderPage()

    expect(screen.getByRole('link', { name: 'Boda Ana y Luis' }).getAttribute('href')).toBe('/my-events/1')
})
