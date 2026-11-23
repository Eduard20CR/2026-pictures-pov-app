import { useState } from 'react'
import { eventLink } from '../eventLink'
import { downloadEventQrPdf, downloadEventQrPng } from '../qr/eventQrCode'

// Printable QR code of the event link, for organizers and moderators alike (RF-ORG-03, RF-MOD-05).
export default function DownloadEventQrButtons({ publicCode, eventName }: { publicCode: string; eventName: string }) {
    const [failed, setFailed] = useState(false)
    const fileBase = `qr-${publicCode}`

    async function download(save: (link: string) => Promise<void>) {
        setFailed(false)
        try {
            await save(eventLink(publicCode))
        } catch {
            setFailed(true)
        }
    }

    return (
        <>
            <button
                type="button"
                onClick={() => download((link) => downloadEventQrPng(link, `${fileBase}.png`))}
                className="rounded border px-3 py-1 text-sm"
            >
                Descargar QR (PNG)
            </button>
            <button
                type="button"
                onClick={() => download((link) => downloadEventQrPdf(link, eventName, `${fileBase}.pdf`))}
                className="rounded border px-3 py-1 text-sm"
            >
                Descargar QR (PDF)
            </button>
            {failed && <p role="alert" className="text-sm">No se pudo generar el código QR. Intenta de nuevo.</p>}
        </>
    )
}
