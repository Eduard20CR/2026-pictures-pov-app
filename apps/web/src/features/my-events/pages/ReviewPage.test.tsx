// @vitest-environment jsdom
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { useAuth } from 'react-oidc-context'
import { MemoryRouter, Route, Routes } from 'react-router'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import ReviewPage from './ReviewPage'
import { useMyEvent } from '../hooks/useMyEvent'
import { useReviewImages } from '../hooks/useReviewImages'
import { getImageDownloadUrl } from '../api/getImageDownloadUrl'
import type { MyEvent, ReviewImage } from '../types'

vi.mock('../hooks/useMyEvent')
vi.mock('../hooks/useReviewImages')
vi.mock('../api/getImageDownloadUrl')
vi.mock('react-oidc-context')

const fetchNextPage = vi.fn()

const myEvent: MyEvent = { id: '1', publicCode: 'a', name: 'Boda Ana y Luis', eventDate: '2026-12-12', status: 'OPEN', role: 'MODERATOR' }

function photo(id: string, overrides: Partial<ReviewImage> = {}): ReviewImage {
    return { id, thumbnailUrl: `https://s3.example/thumbs/${id}.webp?sig`, hidden: false, createdAt: '2026-12-12T18:00:00Z', ...overrides }
}

function givenEvent(event: MyEvent | null) {
    vi.mocked(useMyEvent).mockReturnValue(
        { data: event, isPending: false, isError: false } as unknown as ReturnType<typeof useMyEvent>,
    )
}

function givenPhotos(pages: ReviewImage[][], state: Partial<ReturnType<typeof useReviewImages>> = {}) {
    vi.mocked(useReviewImages).mockReturnValue({
        data: { pages: pages.map((items) => ({ items, nextCursor: null })), pageParams: [] },
        isPending: false, isError: false, hasNextPage: false, isFetchingNextPage: false, fetchNextPage,
        ...state,
    } as unknown as ReturnType<typeof useReviewImages>)
}

function renderPage() {
    const queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
    render(
        <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/my-events/1/review']}>
            <Routes>
                <Route path="/my-events/:id/review" element={<ReviewPage />} />
            </Routes>
        </MemoryRouter>
        </QueryClientProvider>,
    )
}

const assign = vi.fn()

beforeEach(() => {
    vi.clearAllMocks()
    vi.stubGlobal('location', { ...window.location, assign })
    vi.mocked(useAuth).mockReturnValue({ user: { access_token: 'token' } } as unknown as ReturnType<typeof useAuth>)
    givenEvent(myEvent)
    givenPhotos([])
})

afterEach(() => {
    cleanup()
    vi.unstubAllGlobals()
})

test('shows every photo of the event as a thumbnail, across pages', () => {
    givenPhotos([[photo('p1'), photo('p2')], [photo('p3')]])

    renderPage()

    expect(useReviewImages).toHaveBeenCalledWith('1')
    expect(screen.getByRole('heading', { name: 'Revisar fotos' })).toBeTruthy()
    const images = screen.getAllByRole('img')
    expect(images.map((img) => img.getAttribute('src'))).toEqual([
        'https://s3.example/thumbs/p1.webp?sig',
        'https://s3.example/thumbs/p2.webp?sig',
        'https://s3.example/thumbs/p3.webp?sig',
    ])
})

test('shows a placeholder while a photo has no thumbnail yet', () => {
    givenPhotos([[photo('p1', { thumbnailUrl: null })]])

    renderPage()

    expect(screen.queryByRole('img')).toBeNull()
    expect(screen.getByText('Procesando foto')).toBeTruthy()
})

test('says the event has no photos yet', () => {
    renderPage()

    expect(screen.getByText('Este evento aún no tiene fotos.')).toBeTruthy()
})

test('loads the next page of photos when I ask for more', () => {
    givenPhotos([[photo('p1')]], { hasNextPage: true })

    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Cargar más fotos' }))

    expect(fetchNextPage).toHaveBeenCalledOnce()
})

test('does not offer more photos on the last page', () => {
    givenPhotos([[photo('p1')]], { hasNextPage: false })

    renderPage()

    expect(screen.queryByRole('button', { name: 'Cargar más fotos' })).toBeNull()
})

test('marks hidden photos clearly, with text and not by color alone', () => {
    givenPhotos([[photo('visible'), photo('hidden', { hidden: true })]])

    renderPage()

    const [visible, hidden] = screen.getAllByRole('listitem')
    expect(within(hidden).getByText('Oculta')).toBeTruthy()
    expect(within(visible).queryByText('Oculta')).toBeNull()
})

test('says the event was not found when I cannot reach it', () => {
    givenEvent(null)
    givenPhotos([[photo('p1')]])

    renderPage()

    expect(screen.getByText('Evento no encontrado')).toBeTruthy()
    expect(screen.queryByRole('img')).toBeNull()
})

test('the organizer downloads a photo\'s original, hidden ones too', async () => {
    givenEvent({ ...myEvent, role: 'OWNER' })
    givenPhotos([[photo('p1', { hidden: true })]])
    vi.mocked(getImageDownloadUrl).mockResolvedValue('https://s3.example/originals/p1?sig')

    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Descargar foto 1' }))

    await waitFor(() => expect(assign).toHaveBeenCalledWith('https://s3.example/originals/p1?sig'))
    expect(getImageDownloadUrl).toHaveBeenCalledWith('token', '1', 'p1')
})

test('a moderator is not offered the download', () => {
    givenPhotos([[photo('p1')]])

    renderPage()

    expect(screen.queryByRole('button', { name: /Descargar/ })).toBeNull()
})

test('says the download failed so I can try again', async () => {
    givenEvent({ ...myEvent, role: 'OWNER' })
    givenPhotos([[photo('p1')]])
    vi.mocked(getImageDownloadUrl).mockRejectedValue(new Error('HTTP_500'))

    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Descargar foto 1' }))

    expect((await screen.findByRole('alert')).textContent).toBe('No se pudo descargar la foto. Intenta de nuevo.')
    expect(assign).not.toHaveBeenCalled()
})

test('a photo still processing cannot be downloaded yet', () => {
    givenEvent({ ...myEvent, role: 'OWNER' })
    givenPhotos([[photo('p1', { thumbnailUrl: null })]])

    renderPage()

    expect(screen.queryByRole('button', { name: /Descargar/ })).toBeNull()
})
