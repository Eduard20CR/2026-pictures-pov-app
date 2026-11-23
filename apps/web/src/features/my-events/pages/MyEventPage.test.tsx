// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import MyEventPage from './MyEventPage'
import { useMyEvent } from '../hooks/useMyEvent'
import type { MyEvent } from '../types'

vi.mock('../hooks/useMyEvent')

function givenEvent(event: MyEvent | null) {
    vi.mocked(useMyEvent).mockReturnValue(
        { data: event, isPending: false, isError: false } as unknown as ReturnType<typeof useMyEvent>,
    )
}

function renderPage() {
    render(
        <MemoryRouter initialEntries={['/my-events/1']}>
            <Routes>
                <Route path="/my-events/:id" element={<MyEventPage />} />
            </Routes>
        </MemoryRouter>,
    )
}

beforeEach(() => {
    vi.clearAllMocks()
})

afterEach(cleanup)

test('shows the event name, date, status and my role in Spanish', () => {
    givenEvent({ id: '1', publicCode: 'a', name: 'Boda Ana y Luis', eventDate: '2026-12-12', status: 'CLOSED', role: 'MODERATOR' })

    renderPage()

    expect(useMyEvent).toHaveBeenCalledWith('1')
    expect(screen.getByRole('heading', { name: 'Boda Ana y Luis' })).toBeTruthy()
    expect(screen.getByText('2026-12-12')).toBeTruthy()
    expect(screen.getByText('Cerrado')).toBeTruthy()
    expect(screen.getByText('Moderador')).toBeTruthy()
})

test('says the event was not found when I cannot reach it', () => {
    givenEvent(null)

    renderPage()

    expect(screen.getByText('Evento no encontrado')).toBeTruthy()
})
