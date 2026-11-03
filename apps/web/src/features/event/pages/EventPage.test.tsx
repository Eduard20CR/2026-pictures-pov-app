// @vitest-environment jsdom
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import EventPage from './EventPage'
import { requestUpload } from '../api/requestUpload'
import { UploadError } from '../api/UploadError'
import { uploadToS3 } from '../api/uploadToS3'
import { usePublicEvent } from '../hooks/usePublicEvent'
import { useQuota } from '../hooks/useQuota'

vi.mock('../hooks/usePublicEvent')
vi.mock('../hooks/useQuota')
vi.mock('../api/requestUpload')
vi.mock('../api/uploadToS3')

beforeEach(() => {
    // Default: the guest has quota left and every upload succeeds
    vi.mocked(useQuota).mockReturnValue(
        { data: { limit: 10, used: 3, remaining: 7 } } as unknown as ReturnType<typeof useQuota>,
    )
    vi.mocked(requestUpload).mockResolvedValue({
        imageId: 'img-1',
        uploadUrl: 'https://s3.example/upload',
        expiresAt: new Date(Date.now() + 5 * 60 * 1000).toISOString(),
    })
    vi.mocked(uploadToS3).mockResolvedValue(undefined)
})

afterEach(cleanup)

function renderPage() {
    const queryClient = new QueryClient()
    render(
        <QueryClientProvider client={queryClient}>
            <MemoryRouter initialEntries={['/events/abc']}>
                <Routes>
                    <Route path="/events/:publicCode" element={<EventPage />} />
                </Routes>
            </MemoryRouter>
        </QueryClientProvider>,
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
    return new File(['content'], name, { type: 'image/jpeg' })
}

test('shows "Evento no encontrado" when the event does not exist', () => {
    vi.mocked(usePublicEvent).mockReturnValue(
        { data: null, isPending: false, error: null } as unknown as ReturnType<typeof usePublicEvent>,
    )

    renderPage()

    expect(screen.getByText('Evento no encontrado')).toBeDefined()
})

test('shows "Este evento ya cerró" when the event is closed', () => {
    vi.mocked(usePublicEvent).mockReturnValue(
        { data: 'closed', isPending: false, error: null } as unknown as ReturnType<typeof usePublicEvent>,
    )

    renderPage()

    expect(screen.getByText('Este evento ya cerró')).toBeDefined()
})

test('shows the event and the quota when the event exists', () => {
    mockActiveEvent()

    renderPage()

    expect(screen.getByText('Birthday')).toBeDefined()
    expect(screen.getByText('Te quedan 7 de 10 fotos.')).toBeDefined()
})

test('shows "Cargando..." while loading', () => {
    vi.mocked(usePublicEvent).mockReturnValue(
        { data: undefined, isPending: true, error: null } as unknown as ReturnType<typeof usePublicEvent>,
    )

    renderPage()

    expect(screen.getByText('Cargando...')).toBeDefined()
})

test('shows an error message when loading fails', () => {
    vi.mocked(usePublicEvent).mockReturnValue(
        { data: undefined, isPending: false, error: new Error('HTTP 500') } as unknown as ReturnType<typeof usePublicEvent>,
    )

    renderPage()

    expect(screen.getByText(/No pudimos cargar el evento/)).toBeDefined()
})

test('shows an error when the photo is larger than 20 MB', async () => {
    // Arrange: an active event, and the backend rejects the upload because of its size
    mockActiveEvent()
    vi.mocked(requestUpload).mockRejectedValue(new UploadError('FILE_TOO_LARGE'))

    // Act: the guest picks a photo from the gallery
    renderPage()
    const gallery = screen.getByLabelText('Elegir de galería')
    fireEvent.change(gallery, { target: { files: [photo('a.jpg')] } })

    // Assert: the guest sees the error message under the photo
    expect(await screen.findByText('La foto pesa más de 20 MB.')).toBeDefined()
})

test('shows "Lista ✓" when the photo uploads successfully', async () => {
    // Arrange: an active event; beforeEach already makes every upload succeed
    mockActiveEvent()

    // Act: the guest picks a photo from the gallery
    renderPage()
    const gallery = screen.getByLabelText('Elegir de galería')
    fireEvent.change(gallery, { target: { files: [photo('a.jpg')] } })

    // Assert: the photo's row says it is done
    expect(await screen.findByText('Lista ✓')).toBeDefined()
})

test('disables the buttons when the guest has no photos left', () => {
    // Arrange: an active event, and the guest has used the whole quota
    mockActiveEvent()
    vi.mocked(useQuota).mockReturnValue(
        { data: { limit: 10, used: 10, remaining: 0 } } as unknown as ReturnType<typeof useQuota>,
    )

    // Act
    renderPage()

    // Assert: the guest cannot pick photos from the camera or the gallery
    expect((screen.getByLabelText('Tomar foto') as HTMLInputElement).disabled).toBe(true)
    expect((screen.getByLabelText('Elegir de galería') as HTMLInputElement).disabled).toBe(true)
})
