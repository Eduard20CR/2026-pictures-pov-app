import QRCode from 'qrcode'

// Printable QR code of the public event link, drawn in the browser (RF-ORG-03, RF-MOD-05).

const QUIET_ZONE = 4 // modules of white margin required by scanners
const PNG_MODULE_PX = 24

// Dark/light modules of the QR code, row by row; both formats are drawn from it.
export function eventQrMatrix(link: string): boolean[][] {
    const { modules } = QRCode.create(link, { errorCorrectionLevel: 'M' })
    // BitMatrix.get takes (row, col); swapping them mirrors the code and phones stop scanning it.
    return Array.from({ length: modules.size }, (_, y) =>
        Array.from({ length: modules.size }, (_, x) => modules.get(y, x) === 1),
    )
}

export async function downloadEventQrPng(link: string, fileName: string): Promise<void> {
    const matrix = eventQrMatrix(link)
    const size = (matrix.length + QUIET_ZONE * 2) * PNG_MODULE_PX
    const canvas = document.createElement('canvas')
    canvas.width = size
    canvas.height = size
    const context = canvas.getContext('2d')
    if (!context) throw new Error('CANVAS_UNAVAILABLE')
    context.fillStyle = '#fff'
    context.fillRect(0, 0, size, size)
    context.fillStyle = '#000'
    forEachDarkModule(matrix, (x, y) =>
        context.fillRect((x + QUIET_ZONE) * PNG_MODULE_PX, (y + QUIET_ZONE) * PNG_MODULE_PX, PNG_MODULE_PX, PNG_MODULE_PX),
    )
    const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, 'image/png'))
    if (!blob) throw new Error('PNG_UNAVAILABLE')
    saveFile(blob, fileName)
}

// A4 page with the event name, the QR code as vector squares (sharp at any print size) and the link.
export async function downloadEventQrPdf(link: string, eventName: string, fileName: string): Promise<void> {
    const { jsPDF } = await import('jspdf') // loaded on demand: only organizers and moderators need it
    const doc = new jsPDF({ unit: 'mm', format: 'a4' })
    const pageWidth = doc.internal.pageSize.getWidth()
    const matrix = eventQrMatrix(link)
    const qrSide = 140
    const moduleSide = qrSide / matrix.length
    const quietZone = QUIET_ZONE * moduleSide // text stays outside it
    const left = (pageWidth - qrSide) / 2
    const top = 45 + quietZone // below an event name of up to two lines

    doc.setFontSize(24)
    doc.text(eventName, pageWidth / 2, 30, { align: 'center', maxWidth: pageWidth - 40 })
    forEachDarkModule(matrix, (x, y) =>
        doc.rect(left + x * moduleSide, top + y * moduleSide, moduleSide, moduleSide, 'F'),
    )
    doc.setFontSize(14)
    doc.text('Escanea el código para subir tus fotos', pageWidth / 2, top + qrSide + quietZone + 5, { align: 'center' })
    doc.setFontSize(10)
    doc.text(link, pageWidth / 2, top + qrSide + quietZone + 13, { align: 'center' })
    doc.save(fileName)
}

function forEachDarkModule(matrix: boolean[][], draw: (x: number, y: number) => void) {
    matrix.forEach((row, y) => row.forEach((dark, x) => dark && draw(x, y)))
}

function saveFile(blob: Blob, fileName: string) {
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = fileName
    anchor.click()
    setTimeout(() => URL.revokeObjectURL(url)) // after the browser has started the download
}
