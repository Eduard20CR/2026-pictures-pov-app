// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import CreateEventPage from './CreateEventPage'
import { CreateEventError } from '../api/CreateEventError'
import { useCreateEvent } from '../hooks/useCreateEvent'

vi.mock('../hooks/useCreateEvent')

const mutate = vi.fn()

// Builds the fake result of useCreateEvent; each test overrides only what it needs
function mockCreateEvent(overrides: object = {}) {
    vi.mocked(useCreateEvent).mockReturnValue(
        { mutate, isPending: false, isError: false, error: null, data: undefined, ...overrides } as unknown as ReturnType<typeof useCreateEvent>,
    )
}

beforeEach(() => {
    vi.clearAllMocks()
    mockCreateEvent()
})

afterEach(cleanup)

test('sends the form values when the admin submits', () => {
    render(<CreateEventPage />)

    fireEvent.change(screen.getByLabelText('Nombre'), { target: { value: 'Boda' } })
    fireEvent.change(screen.getByLabelText('Fecha'), { target: { value: '2099-12-20' } })
    fireEvent.change(screen.getByLabelText('Correo del dueño'), { target: { value: 'ana@example.com' } })
    fireEvent.click(screen.getByRole('button', { name: 'Crear evento' }))

    expect(mutate).toHaveBeenCalledWith({ name: 'Boda', eventDate: '2099-12-20', ownerEmail: 'ana@example.com' })
})

test('shows the error next to each invalid field', () => {
    mockCreateEvent({ isError: true, error: new CreateEventError('VALIDATION_FAILED', { name: 'must not be blank' }) })

    render(<CreateEventPage />)

    expect(screen.getByText('must not be blank')).toBeTruthy()
})

test('shows the public code after the event is created', () => {
    mockCreateEvent({ data: { publicCode: 'n7o3KnlYQn3zovbx0hVaOA' } })

    render(<CreateEventPage />)

    expect(screen.getByRole('status').textContent).toContain('n7o3KnlYQn3zovbx0hVaOA')
})
