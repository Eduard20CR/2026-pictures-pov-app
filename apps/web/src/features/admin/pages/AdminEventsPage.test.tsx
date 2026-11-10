// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import AdminEventsPage from './AdminEventsPage'
import { useAdminEvents } from '../hooks/useAdminEvents'

vi.mock('../hooks/useAdminEvents')

beforeEach(() => {
    vi.clearAllMocks()

    // Default: page 1 of 2, one event with owner and one without
    vi.mocked(useAdminEvents).mockReturnValue(
        {
            data: {
                items: [
                    { id: '1', publicCode: 'a', name: 'Navidad', eventDate: '2026-12-25', status: 'DRAFT', ownerEmail: 'ana@example.com' },
                    { id: '2', publicCode: 'b', name: 'Boda', eventDate: '2026-12-12', status: 'ACTIVE' },
                ],
                page: 0, size: 20, totalItems: 22, totalPages: 2,
            },
            isPending: false,
            isError: false,
        } as unknown as ReturnType<typeof useAdminEvents>,
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

test('shows each event with its owner, or "Sin dueño" when it has none', () => {
    renderPage()

    expect(screen.getByText('ana@example.com')).toBeTruthy()
    expect(screen.getByText('Sin dueño')).toBeTruthy()
})

test('"Siguiente" asks for the next page', () => {
    renderPage()

    expect(screen.getByRole('button', { name: 'Anterior' })).toHaveProperty('disabled', true)
    fireEvent.click(screen.getByRole('button', { name: 'Siguiente' }))

    expect(useAdminEvents).toHaveBeenLastCalledWith(1)
})
