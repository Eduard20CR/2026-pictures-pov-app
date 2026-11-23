// @vitest-environment jsdom
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import GuestEventPage from './GuestEventPage'
import { createPresignedUpload } from '../api/createPresignedUpload'
import { UploadError } from '../api/UploadError'
import { uploadPhotoToS3 } from '../api/uploadPhotoToS3'
import { useGuestEvent } from '../hooks/useGuestEvent'
import { useUploadQuota } from '../hooks/useUploadQuota'

vi.mock('../hooks/useGuestEvent')
vi.mock('../hooks/useUploadQuota')
vi.mock('../api/createPresignedUpload')
vi.mock('../api/uploadPhotoToS3')

beforeEach(() => {
    vi.clearAllMocks()

    // Default: the guest has quota left and every upload succeeds
    vi.mocked(useUploadQuota).mockReturnValue(
        { data: { limit: 10, used: 3, remaining: 7 } } as unknown as ReturnType<typeof useUploadQuota>,
    )
    vi.mocked(createPresignedUpload).mockResolvedValue({
        imageId: 'img-1',
        uploadUrl: 'https://s3.example/upload',
        expiresAt: new Date(Date.now() + 5 * 60 * 1000).toISOString(),
    })
    vi.mocked(uploadPhotoToS3).mockResolvedValue(undefined)
})

afterEach(cleanup)

function renderPage() {
    const queryClient = new QueryClient()
    render(
        <QueryClientProvider client={queryClient}>
            <MemoryRouter initialEntries={['/events/abc']}>
                <Routes>
                    <Route path="/events/:publicCode" element={<GuestEventPage />} />
                </Routes>
            </MemoryRouter>
        </QueryClientProvider>,
    )
}

function mockActiveEvent() {
    vi.mocked(useGuestEvent).mockReturnValue(
        {
            data: { publicCode: 'abc', name: 'Birthday', color: '#000000', eventDate: '2001-11-20', imagesPerUser: 10 },
            isPending: false,
            error: null,
        } as unknown as ReturnType<typeof useGuestEvent>,
    )
}

function photo(name: string) {
    return new File(['content'], name, { type: 'image/jpeg' })
}

test('shows "Evento no encontrado" when the event does not exist', () => {
    vi.mocked(useGuestEvent).mockReturnValue(
        { data: null, isPending: false, error: null } as unknown as ReturnType<typeof useGuestEvent>,
    )

    renderPage()

    expect(screen.getByText('Evento no encontrado')).toBeDefined()
})

test('shows "Este evento ya cerró" when the event is closed', () => {
    vi.mocked(useGuestEvent).mockReturnValue(
        { data: 'closed', isPending: false, error: null } as unknown as ReturnType<typeof useGuestEvent>,
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
    vi.mocked(useGuestEvent).mockReturnValue(
        { data: undefined, isPending: true, error: null } as unknown as ReturnType<typeof useGuestEvent>,
    )

    renderPage()

    expect(screen.getByText('Cargando...')).toBeDefined()
})

test('shows an error message when loading fails', () => {
    vi.mocked(useGuestEvent).mockReturnValue(
        { data: undefined, isPending: false, error: new Error('HTTP 500') } as unknown as ReturnType<typeof useGuestEvent>,
    )

    renderPage()

    expect(screen.getByText(/No pudimos cargar el evento/)).toBeDefined()
})

test('shows an error when the photo is larger than 20 MB', async () => {
    // Arrange: an active event, and the backend rejects the upload because of its size
    mockActiveEvent()
    vi.mocked(createPresignedUpload).mockRejectedValue(new UploadError('FILE_TOO_LARGE'))

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
    vi.mocked(useUploadQuota).mockReturnValue(
        { data: { limit: 10, used: 10, remaining: 0 } } as unknown as ReturnType<typeof useUploadQuota>,
    )

    // Act
    renderPage()

    // Assert: the guest cannot pick photos from the camera or the gallery
    expect((screen.getByLabelText('Tomar foto') as HTMLInputElement).disabled).toBe(true)
    expect((screen.getByLabelText('Elegir de galería') as HTMLInputElement).disabled).toBe(true)
})


test('retries a failed upload reusing the same upload URL', async () => {
    // Arrange: an active event; the first upload to S3 fails, the second succeeds
    mockActiveEvent()
    vi.mocked(uploadPhotoToS3).mockRejectedValueOnce(new UploadError('S3_UPLOAD_FAILED'))

    // Act: the guest picks a photo, waits for the retry button and taps it
    renderPage()
    const gallery = screen.getByLabelText('Elegir de galería')
    fireEvent.change(gallery, { target: { files: [photo('a.jpg')] } })
    fireEvent.click(await screen.findByText('Reintentar'))

    // Assert: the photo is done, with one upload URL (one quota slot) and two uploads
    expect(await screen.findByText('Lista ✓')).toBeDefined()
    expect(createPresignedUpload).toHaveBeenCalledTimes(1)
    expect(uploadPhotoToS3).toHaveBeenCalledTimes(2)
})

test('shows the status of each photo when several are uploaded', async () => {
    // Arrange: an active event; the first upload to S3 fails, the second succeeds
    mockActiveEvent()
    vi.mocked(uploadPhotoToS3).mockRejectedValueOnce(new UploadError('S3_UPLOAD_FAILED'))

    // Act: the guest picks two photos at once
    renderPage()
    const gallery = screen.getByLabelText('Elegir de galería')
    fireEvent.change(gallery, { target: { files: [photo('a.jpg'), photo('b.jpg')] } })

    // Assert: each photo shows its own status
    expect(await screen.findByText('Falló')).toBeDefined()
    expect(await screen.findByText('Lista ✓')).toBeDefined()
    expect(createPresignedUpload).toHaveBeenCalledTimes(2)
    expect(uploadPhotoToS3).toHaveBeenCalledTimes(2)
})