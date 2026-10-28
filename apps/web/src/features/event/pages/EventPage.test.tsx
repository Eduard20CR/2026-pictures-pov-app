// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { afterEach, expect, test, vi } from 'vitest'
import EventPage from './EventPage'
import { usePublicEvent } from '../hooks/usePublicEvent'

vi.mock('../hooks/usePublicEvent')

afterEach(cleanup)

function renderPage() {
    render(
        <MemoryRouter initialEntries={['/events/abc']}>
            <Routes>
                <Route path="/events/:publicCode" element={<EventPage />} />
            </Routes>
        </MemoryRouter>,
    )
}

test('muestra "Evento no encontrado" cuando el evento no existe', () => {
    vi.mocked(usePublicEvent).mockReturnValue(
        { data: null, isPending: false, error: null } as unknown as ReturnType<typeof usePublicEvent>,
    )

    renderPage()

    expect(screen.getByText('Evento no encontrado')).toBeDefined()
})


test('muestra el evento cuando existe', () => {
    vi.mocked(usePublicEvent).mockReturnValue(
        {
            data: {
                publicCode: 'abc',
                name: 'Birthday',
                color: '#000000',
                eventDate: '2001-11-20',
                imagesPerUser: 10,
            },
            isPending: false,
            error: null,
        } as unknown as ReturnType<typeof usePublicEvent>,
    )

    renderPage()

    expect(screen.getByText('Birthday')).toBeDefined()
})


test('muestra "Cargando..." mientras carga', () => {
    vi.mocked(usePublicEvent).mockReturnValue(
        { data: undefined, isPending: true, error: null } as unknown as ReturnType<typeof usePublicEvent>,
    )

    renderPage()

    expect(screen.getByText('Cargando...')).toBeDefined()
})

test('muestra un mensaje de error cuando falla la carga', () => {
    vi.mocked(usePublicEvent).mockReturnValue(
        { data: undefined, isPending: false, error: new Error('HTTP 500') } as unknown as ReturnType<typeof usePublicEvent>,
    )

    renderPage()

    expect(screen.getByText(/No pudimos cargar el evento/)).toBeDefined()
})