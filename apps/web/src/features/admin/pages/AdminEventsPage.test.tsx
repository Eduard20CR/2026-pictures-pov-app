// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import AdminEventsPage from './AdminEventsPage'
import { useAllEvents } from '../hooks/useAllEvents'

vi.mock('../hooks/useAllEvents')

beforeEach(() => {
    vi.clearAllMocks()

    // Default: page 1 of 2, one event with an organizer and one without
    vi.mocked(useAllEvents).mockReturnValue(
        {
            data: {
                items: [
                    { id: '1', publicCode: 'a', name: 'Navidad', eventDate: '2026-12-25', status: 'OPEN', organizerEmail: 'ana@example.com' },
                    { id: '2', publicCode: 'b', name: 'Boda', eventDate: '2026-12-12', status: 'CLOSED' },
                ],
                page: 0, size: 20, totalItems: 22, totalPages: 2,
            },
            isPending: false,
            isError: false,
        } as unknown as ReturnType<typeof useAllEvents>,
    )
})

afterEach(cleanup)

function renderPage() {
    render(
        <MemoryRouter initialEntries={['/admin/events']}>
            <AdminEventsPage />
        </MemoryRouter>,
    )
}

test('shows each event with its organizer, or "Sin organizador" when it has none', () => {
    renderPage()

    expect(screen.getByRole('columnheader', { name: 'Organizador' })).toBeTruthy()
    expect(screen.getByText('ana@example.com')).toBeTruthy()
    expect(screen.getByText('Sin organizador')).toBeTruthy()
})

test('shows each status in Spanish', () => {
    renderPage()

    expect(screen.getByText('Abierto')).toBeTruthy()
    expect(screen.getByText('Cerrado')).toBeTruthy()
})

test('"Siguiente" asks for the next page', () => {
    renderPage()

    expect(screen.getByRole('button', { name: 'Anterior' })).toHaveProperty('disabled', true)
    fireEvent.click(screen.getByRole('button', { name: 'Siguiente' }))

    expect(useAllEvents).toHaveBeenLastCalledWith(1)
})
