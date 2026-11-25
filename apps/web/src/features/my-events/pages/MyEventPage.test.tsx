// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import MyEventPage from './MyEventPage'
import { useMyEvent } from '../hooks/useMyEvent'
import { useInviteModerator } from '../hooks/useInviteModerator'
import { InviteModeratorError } from '../api/InviteModeratorError'
import type { MyEvent } from '../types'
import { downloadEventQrPdf, downloadEventQrPng } from '../qr/eventQrCode'
import { useZipExport } from '../hooks/useZipExport'
import { useRequestZipExport } from '../hooks/useRequestZipExport'
import { ZipExportError } from '../api/ZipExportError'
import type { ZipExport } from '../types'

vi.mock('../hooks/useMyEvent')
vi.mock('../hooks/useInviteModerator')
vi.mock('../qr/eventQrCode')
vi.mock('../hooks/useZipExport')
vi.mock('../hooks/useRequestZipExport')

const mutate = vi.fn()

function givenInvite(state: Partial<ReturnType<typeof useInviteModerator>>) {
    vi.mocked(useInviteModerator).mockReturnValue(
        { mutate, isPending: false, isError: false, error: null, data: undefined, ...state } as unknown as ReturnType<typeof useInviteModerator>,
    )
}

const myEvent: MyEvent = { id: '1', publicCode: 'a', name: 'Boda Ana y Luis', eventDate: '2026-12-12', status: 'OPEN', role: 'OWNER' }

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

const requestZip = vi.fn()

function givenZip(zip: ZipExport | null) {
    vi.mocked(useZipExport).mockReturnValue(
        { data: zip, isPending: false, isError: false } as unknown as ReturnType<typeof useZipExport>,
    )
}

function givenZipRequest(state: Partial<ReturnType<typeof useRequestZipExport>>) {
    vi.mocked(useRequestZipExport).mockReturnValue(
        { mutate: requestZip, isPending: false, isError: false, error: null, ...state } as unknown as ReturnType<typeof useRequestZipExport>,
    )
}

beforeEach(() => {
    vi.clearAllMocks()
    givenInvite({})
    givenZip(null)
    givenZipRequest({})
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

test('as organizer I invite a moderator by email', () => {
    givenEvent(myEvent)

    renderPage()
    fireEvent.change(screen.getByLabelText('Correo del moderador'), { target: { value: 'beto@example.com' } })
    fireEvent.click(screen.getByRole('button', { name: 'Invitar moderador' }))

    expect(useInviteModerator).toHaveBeenCalledWith('1')
    expect(mutate).toHaveBeenCalledWith('beto@example.com', expect.anything())
})

test('says the invitation is pending once created', () => {
    givenEvent(myEvent)
    givenInvite({ data: { id: '9', email: 'beto@example.com', role: 'MODERATOR', status: 'PENDING' } })

    renderPage()

    expect(screen.getByRole('status').textContent).toContain('beto@example.com')
})

test('says the email already has an invitation', () => {
    givenEvent(myEvent)
    givenInvite({ isError: true, error: new InviteModeratorError('INVITATION_ALREADY_EXISTS') })

    renderPage()

    expect(screen.getByRole('alert').textContent).toBe('Ese correo ya tiene una invitación a este evento.')
})

test('a moderator does not see the invite form', () => {
    givenEvent({ ...myEvent, role: 'MODERATOR' })

    renderPage()

    expect(screen.queryByRole('button', { name: 'Invitar moderador' })).toBeNull()
})

test.each(['OWNER', 'MODERATOR'] as const)('as %s I copy the public event link', async (role) => {
    const writeText = vi.fn().mockResolvedValue(undefined)
    Object.defineProperty(navigator, 'clipboard', { value: { writeText }, configurable: true })
    givenEvent({ ...myEvent, role })

    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Copiar enlace' }))

    expect(writeText).toHaveBeenCalledWith(`${window.location.origin}/events/a`)
    await waitFor(() => expect(screen.getByRole('button', { name: 'Enlace copiado' })).toBeTruthy())
})

test.each(['OWNER', 'MODERATOR'] as const)('as %s I download the event QR code as PNG and PDF', (role) => {
    givenEvent({ ...myEvent, role })

    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Descargar QR (PNG)' }))
    fireEvent.click(screen.getByRole('button', { name: 'Descargar QR (PDF)' }))

    const link = `${window.location.origin}/events/a`
    expect(downloadEventQrPng).toHaveBeenCalledWith(link, 'qr-a.png')
    expect(downloadEventQrPdf).toHaveBeenCalledWith(link, 'Boda Ana y Luis', 'qr-a.pdf')
})

test.each(['OWNER', 'MODERATOR'] as const)('as %s I open the review of the event photos', (role) => {
    givenEvent({ ...myEvent, role })

    renderPage()

    expect(screen.getByRole('link', { name: 'Revisar fotos' }).getAttribute('href')).toBe('/my-events/1/review')
})

test('as organizer I request the ZIP of all the photos', () => {
    givenEvent(myEvent)

    renderPage()
    fireEvent.click(screen.getByRole('button', { name: 'Generar ZIP' }))

    expect(useZipExport).toHaveBeenCalledWith('1')
    expect(useRequestZipExport).toHaveBeenCalledWith('1')
    expect(requestZip).toHaveBeenCalled()
})

test('a ZIP in progress says so and cannot be requested again', () => {
    givenEvent(myEvent)
    givenZip({ status: 'IN_PROGRESS', requestedAt: '2026-11-25T10:00:00Z' })

    renderPage()

    expect(screen.getByText(/ZIP en preparación/)).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Generar ZIP' })).toBeNull()
})

test('an available ZIP is downloaded through its link', () => {
    givenEvent(myEvent)
    givenZip({ status: 'AVAILABLE', requestedAt: '2026-11-25T10:00:00Z', imageCount: 12, sizeBytes: 3_400_000, downloadUrl: 'https://s3/zips/9.zip?X-Amz-Signature=x' })

    renderPage()

    expect(screen.getByRole('link', { name: 'Descargar ZIP' }).getAttribute('href')).toBe('https://s3/zips/9.zip?X-Amz-Signature=x')
    expect(screen.getByText(/12 fotos/)).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Generar ZIP de nuevo' })).toBeTruthy()
})

test('a failed ZIP says so and can be requested again', () => {
    givenEvent(myEvent)
    givenZip({ status: 'FAILED', requestedAt: '2026-11-25T10:00:00Z' })

    renderPage()

    expect(screen.getByText(/No se pudo generar el ZIP/)).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Generar ZIP de nuevo' })).toBeTruthy()
})

test('says the event has no photos to put in the ZIP yet', () => {
    givenEvent(myEvent)
    givenZipRequest({ isError: true, error: new ZipExportError('NO_PHOTOS_TO_EXPORT') })

    renderPage()

    expect(screen.getByRole('alert').textContent).toBe('El evento aún no tiene fotos para descargar.')
})

test('a moderator does not see the ZIP and never asks for it', () => {
    givenEvent({ ...myEvent, role: 'MODERATOR' })

    renderPage()

    expect(screen.queryByRole('button', { name: 'Generar ZIP' })).toBeNull()
    expect(useZipExport).not.toHaveBeenCalled()
})
