// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, expect, test, vi } from 'vitest'
import { PhotoPicker } from './PhotoPicker'

afterEach(cleanup)

function photo(name: string) {
    return new File(['contenido'], name, { type: 'image/jpeg' })
}

test('entrega como máximo las fotos que le quedan al invitado', () => {
    // Arrange: le quedan 2 fotos
    const onSelect = vi.fn()
    render(<PhotoPicker color="#000000" disabled={false} maxFiles={2} onSelect={onSelect} />)

    // Act: elige 3 fotos de la galería
    const gallery = screen.getByLabelText('Elegir de galería')
    fireEvent.change(gallery, { target: { files: [photo('a.jpg'), photo('b.jpg'), photo('c.jpg')] } })

    // Assert: solo se entregan las 2 primeras
    expect(onSelect).toHaveBeenCalledOnce()
    expect(onSelect.mock.calls[0][0]).toHaveLength(2)
})
