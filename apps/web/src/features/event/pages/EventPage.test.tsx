// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import EventPage from './EventPage'
import { UploadError } from '../api/UploadError'
import { usePublicEvent } from '../hooks/usePublicEvent'
import { useQuota } from '../hooks/useQuota'
import { useUploadPhoto } from '../hooks/useUploadPhoto'

vi.mock('../hooks/usePublicEvent')
vi.mock('../hooks/useQuota')
vi.mock('../hooks/useUploadPhoto')

beforeEach(() => {
    vi.mocked(useQuota).mockReturnValue(
        { data: { limit: 10, used: 3, remaining: 7 } } as unknown as ReturnType<typeof useQuota>,
    )
    vi.mocked(useUploadPhoto).mockReturnValue(
        { mutateAsync: vi.fn() } as unknown as ReturnType<typeof useUploadPhoto>,
    )
})

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

function mockActiveEvent() {
    vi.mocked(usePublicEvent).mockReturnValue(
        {
            data: { publicCode: 'abc', name: 'Birthday', color: '#000000', eventDate: '2001-11-20', imagesPerUser: 10 },
            isPending: false,
            error: null,
        } as unknown as ReturnType<typeof usePublicEvent>,
    )
}

function photo(name: string) {
    return new File(['contenido'], name, { type: 'image/jpeg' })
}

test('muestra "Evento no encontrado" cuando el evento no existe', () => {
    vi.mocked(usePublicEvent).mockReturnValue(
        { data: null, isPending: false, error: null } as unknown as ReturnType<typeof usePublicEvent>,
    )

    renderPage()

    expect(screen.getByText('Evento no encontrado')).toBeDefined()
})

test('muestra "Este evento ya cerró" cuando el evento ya cerro', () => {
    vi.mocked(usePublicEvent).mockReturnValue(
        { data: "closed", isPending: false, error: null } as unknown as ReturnType<typeof usePublicEvent>,
    )

    renderPage()

    expect(screen.getByText('Este evento ya cerró')).toBeDefined()
})

test('muestra el evento y el cupo cuando existe', () => {
    mockActiveEvent()

    renderPage()

    expect(screen.getByText('Birthday')).toBeDefined()
    expect(screen.getByText('Te quedan 7 de 10 fotos.')).toBeDefined()
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

test('muestra error cuando la foto pesa más de 20 MB', async () => {
    // Arrange: hay un evento activo y la subida falla por tamaño
    mockActiveEvent()
    vi.mocked(useUploadPhoto).mockReturnValue(
        { mutateAsync: vi.fn().mockRejectedValue(new UploadError('FILE_TOO_LARGE')) } as unknown as ReturnType<typeof useUploadPhoto>,
    )

    // Act: el invitado elige una foto de la galería
    renderPage()
    const gallery = screen.getByLabelText('Elegir de galería')
    fireEvent.change(gallery, { target: { files: [photo('a.jpg')] } })

    // Assert: ve el mensaje de error
    expect(await screen.findByText('La foto pesa más de 20 MB.')).toBeDefined()
})

test('muestra “Foto subida.” cuando la subida de un file sale bien.', async () => {
    // Arrange: hay un evento activo y la subida falla por tamaño
    mockActiveEvent()
    vi.mocked(useUploadPhoto).mockReturnValue(
        { mutateAsync: vi.fn().mockResolvedValue(undefined) } as unknown as ReturnType<typeof useUploadPhoto>,
    )

    // Act: el invitado elige una foto de la galería
    renderPage()
    const gallery = screen.getByLabelText('Elegir de galería')
    fireEvent.change(gallery, { target: { files: [photo('a.jpg')] } })

    // Assert: ve el mensaje de error
    expect(await screen.findByText('Foto subida.')).toBeDefined()
})

test('desactiva los botones cuando ya no le quedan fotos', () => {
    // Arrange: hay un evento activo y el invitado ya usó todo su cupo
    mockActiveEvent()
    vi.mocked(useQuota).mockReturnValue(
        { data: { limit: 10, used: 10, remaining: 0 } } as unknown as ReturnType<typeof useQuota>,
    )

    // Act
    renderPage()

    // Assert: no puede elegir fotos ni de la cámara ni de la galería
    expect((screen.getByLabelText('Tomar foto') as HTMLInputElement).disabled).toBe(true)
    expect((screen.getByLabelText('Elegir de galería') as HTMLInputElement).disabled).toBe(true)
})