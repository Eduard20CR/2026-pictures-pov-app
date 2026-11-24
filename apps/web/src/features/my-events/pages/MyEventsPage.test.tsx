// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import MyEventsPage from './MyEventsPage'
import { useMyEvents } from '../hooks/useMyEvents'
import { useCurrentUser } from '../../auth/hooks/useCurrentUser'
import { usePendingInvitations } from '../hooks/usePendingInvitations'
import { useAcceptInvitation } from '../hooks/useAcceptInvitation'
import type { MyEvent, PendingInvitation } from '../types'

vi.mock('../hooks/useMyEvents')
vi.mock('../hooks/usePendingInvitations')
vi.mock('../hooks/useAcceptInvitation')
vi.mock('../../auth/hooks/useCurrentUser')
// Factory: an automock would load the real module, which needs the Cognito env vars.
vi.mock('../../auth/hooks/useSignOut', () => ({ useSignOut: () => vi.fn() }))

function givenEvents(events: MyEvent[]) {
    vi.mocked(useMyEvents).mockReturnValue(
        { data: events, isPending: false, isError: false } as unknown as ReturnType<typeof useMyEvents>,
    )
}

function givenPendingInvitations(invitations: PendingInvitation[]) {
    vi.mocked(usePendingInvitations).mockReturnValue(
        { data: invitations, isPending: false, isError: false } as unknown as ReturnType<typeof usePendingInvitations>,
    )
}

const accept = vi.fn()

function renderPage() {
    render(<MemoryRouter><MyEventsPage /></MemoryRouter>)
}

beforeEach(() => {
    vi.clearAllMocks()
    givenPendingInvitations([])
    vi.mocked(useAcceptInvitation).mockReturnValue(
        { mutate: accept, isPending: false, isError: false } as unknown as ReturnType<typeof useAcceptInvitation>,
    )
    vi.mocked(useCurrentUser).mockReturnValue(
        { data: { id: 'u', email: 'ana@example.com' }, isPending: false, isError: false } as unknown as ReturnType<typeof useCurrentUser>,
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
        'Boda Ana y Luis2026-12-12AbiertoOrganizadorCopiar enlace',
        'Navidad2026-11-01CerradoModeradorCopiar enlace',
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

test('I copy the public link of an event from its row', async () => {
    const writeText = vi.fn().mockResolvedValue(undefined)
    Object.defineProperty(navigator, 'clipboard', { value: { writeText }, configurable: true })
    givenEvents([
        { id: '1', publicCode: 'a', name: 'Boda Ana y Luis', eventDate: '2026-12-12', status: 'OPEN', role: 'OWNER' },
        { id: '2', publicCode: 'b', name: 'Navidad', eventDate: '2026-11-01', status: 'CLOSED', role: 'MODERATOR' },
    ])

    renderPage()
    const navidad = screen.getByRole('row', { name: /Navidad/ })
    fireEvent.click(within(navidad).getByRole('button', { name: 'Copiar enlace' }))

    expect(writeText).toHaveBeenCalledWith(`${window.location.origin}/events/b`)
    await waitFor(() => expect(within(navidad).getByRole('button', { name: 'Enlace copiado' })).toBeTruthy())
})

test('shows my pending invitations with event name and role, above my events', () => {
    givenEvents([
        { id: '1', publicCode: 'a', name: 'Boda Ana y Luis', eventDate: '2026-12-12', status: 'OPEN', role: 'OWNER' },
    ])
    givenPendingInvitations([{ id: '9', eventId: '2', eventName: 'Navidad', role: 'MODERATOR' }])

    renderPage()

    const section = screen.getByRole('region', { name: 'Invitaciones pendientes' })
    expect(within(section).getByText('Navidad')).toBeTruthy()
    expect(within(section).getByText('Moderador')).toBeTruthy()
    expect(section.compareDocumentPosition(screen.getByRole('table')) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy()
})

test('accepting a pending invitation sends its id', () => {
    givenEvents([])
    givenPendingInvitations([{ id: '9', eventId: '2', eventName: 'Navidad', role: 'MODERATOR' }])

    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Aceptar' }))

    expect(accept).toHaveBeenCalledWith('9')
})

test('shows no pending section when I have no pending invitations', () => {
    givenEvents([])

    renderPage()

    expect(screen.queryByRole('region', { name: 'Invitaciones pendientes' })).toBeNull()
})
