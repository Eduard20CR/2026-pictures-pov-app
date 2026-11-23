// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import MyEventsPage from './MyEventsPage'
import { useMyEvents } from '../hooks/useMyEvents'
import { useMe } from '../../auth/hooks/useMe'
import type { MyEvent } from '../types'

vi.mock('../hooks/useMyEvents')
vi.mock('../../auth/hooks/useMe')
vi.mock('../../auth/hooks/useSignOut')

function givenEvents(events: MyEvent[]) {
    vi.mocked(useMyEvents).mockReturnValue(
        { data: events, isPending: false, isError: false } as unknown as ReturnType<typeof useMyEvents>,
    )
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

    render(<MyEventsPage />)

    const rows = screen.getAllByRole('row').slice(1)
    expect(rows.map((row) => row.textContent)).toEqual([
        'Boda Ana y Luis2026-12-12AbiertoOrganizador',
        'Navidad2026-11-01CerradoModerador',
    ])
})

test('says so when I have no events', () => {
    givenEvents([])

    render(<MyEventsPage />)

    expect(screen.getByText('Aún no tienes eventos.')).toBeTruthy()
    expect(screen.queryByRole('table')).toBeNull()
})
