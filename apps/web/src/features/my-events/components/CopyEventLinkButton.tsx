import { useState } from 'react'
import { eventLink } from '../eventLink'

type CopyState = 'idle' | 'copied' | 'failed'

const LABELS: Record<CopyState, string> = {
    idle: 'Copiar enlace',
    copied: 'Enlace copiado',
    failed: 'No se pudo copiar',
}

// Copies the public guest link of the event (RF-ORG-02, RF-MOD-05).
export default function CopyEventLinkButton({ publicCode }: { publicCode: string }) {
    const [state, setState] = useState<CopyState>('idle')

    function handleCopy() {
        const link = eventLink(publicCode)
        // navigator.clipboard is undefined outside a secure context (e.g. plain HTTP on a LAN IP).
        const copied = navigator.clipboard?.writeText(link) ?? Promise.reject(new Error('CLIPBOARD_UNAVAILABLE'))
        copied.then(
            () => setState('copied'),
            () => setState('failed'),
        )
    }

    return (
        <button type="button" onClick={handleCopy} className="rounded border px-3 py-1 text-sm">
            {LABELS[state]}
        </button>
    )
}
