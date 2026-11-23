// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, expect, test, vi } from 'vitest'
import { PhotoPicker } from './PhotoPicker'

afterEach(cleanup)

function photo(name: string) {
    return new File(['contenido'], name, { type: 'image/jpeg' })
}

test('entrega las fotos si elige justo las que le quedan', () => {
    // Arrange: le quedan 2 fotos
    const onSelect = vi.fn()
    render(<PhotoPicker color="#000000" disabled={false} maxFiles={2} onSelect={onSelect} />)

    // Act: elige 2 fotos de la galería
    const gallery = screen.getByLabelText('Elegir de galería')
    fireEvent.change(gallery, { target: { files: [photo('a.jpg'), photo('b.jpg')] } })

    // Assert: se entregan las 2
    expect(onSelect).toHaveBeenCalledOnce()
    expect(onSelect.mock.calls[0][0]).toHaveLength(2)
    expect(screen.queryByRole('alert')).toBeNull()
})
test('avisa y no entrega nada si elige más fotos de las que le quedan', () => {
    // Arrange: le quedan 2 fotos
    const onSelect = vi.fn()
    render(<PhotoPicker color="#000000" disabled={false} maxFiles={2} onSelect={onSelect} />)

    // Act: elige 3 fotos de la galería
    const gallery = screen.getByLabelText('Elegir de galería')
    fireEvent.change(gallery, { target: { files: [photo('a.jpg'), photo('b.jpg'), photo('c.jpg')] } })

    // Assert: No se entrega nada y muestra mensaje de error.
    expect(onSelect).not.toHaveBeenCalledOnce()
    expect(screen.getByRole('alert').textContent).toBe('Solo te quedan 2 fotos. Elige de nuevo.')
})
