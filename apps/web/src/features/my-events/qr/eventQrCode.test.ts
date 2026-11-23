import jsQR from 'jsqr'
import { expect, test } from 'vitest'
import { eventQrMatrix } from './eventQrCode'

// Rasterizes the matrix (1 px per module) on a white background and decodes it like a phone would.
function scan(matrix: boolean[][]): string | undefined {
    const quietZone = 4
    const size = matrix.length + quietZone * 2
    const pixels = new Uint8ClampedArray(size * size * 4).fill(255)
    matrix.forEach((row, y) =>
        row.forEach((dark, x) => {
            if (!dark) return
            const offset = ((y + quietZone) * size + x + quietZone) * 4
            pixels.fill(0, offset, offset + 3)
        }),
    )
    return jsQR(pixels, size, size)?.data
}

test('the QR code scans to the event link', () => {
    const link = 'https://fotos.example.com/events/Xy7kQ2pL'

    expect(scan(eventQrMatrix(link))).toBe(link)
})

test('the QR code is not mirrored, since phone scanners do not retry a mirrored code', () => {
    const matrix = eventQrMatrix('https://fotos.example.com/events/Xy7kQ2pL')
    const size = matrix.length

    // ISO/IEC 18004: the "dark module" is always at row size - 8, column 8.
    expect(matrix[size - 8][8]).toBe(true)
})
