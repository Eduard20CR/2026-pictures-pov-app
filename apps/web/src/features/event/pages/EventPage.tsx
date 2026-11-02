import { useState } from 'react'
import { useParams } from 'react-router'
import { UploadError } from '../api/UploadError'
import { PhotoPicker } from '../components/PhotoPicker'
import { usePublicEvent } from '../hooks/usePublicEvent'
import { useQuota } from '../hooks/useQuota'
import { useUploadPhoto } from '../hooks/useUploadPhoto'

const ERROR_MESSAGES: Record<string, string> = {
    UPLOAD_LIMIT_REACHED: 'Ya subiste todas las fotos permitidas.',
    UNSUPPORTED_CONTENT_TYPE: 'Solo se aceptan fotos JPEG, PNG, WebP o HEIC.',
    FILE_TOO_LARGE: 'La foto pesa más de 20 MB.',
    EVENT_NOT_FOUND: 'Este evento ya no recibe fotos.',
}

function errorMessage(reason: unknown): string {
    if (reason instanceof UploadError && ERROR_MESSAGES[reason.code]) {
        return ERROR_MESSAGES[reason.code]
    }
    return 'No pudimos subir la foto. Intenta de nuevo.'
}

function EventPage() {
    const { publicCode = '' } = useParams()
    const { data: event, isPending, error } = usePublicEvent(publicCode)
    const { data: quota } = useQuota(publicCode)
    const uploadPhoto = useUploadPhoto(publicCode)
    const [uploading, setUploading] = useState(false)
    const [message, setMessage] = useState<string | null>(null)

    async function handleSelect(files: File[]) {
        setUploading(true)
        setMessage(null)
        const results = await Promise.allSettled(files.map((file) => uploadPhoto.mutateAsync(file)))
        setUploading(false)

        const failed = results.filter((result) => result.status === 'rejected')
        if (failed.length === 0) {
            setMessage(files.length === 1 ? 'Foto subida.' : `${files.length} fotos subidas.`)
            return
        }
        const uploaded = files.length - failed.length
        const prefix = uploaded > 0 ? `Se subieron ${uploaded} de ${files.length}. ` : ''
        setMessage(prefix + errorMessage((failed[0] as PromiseRejectedResult).reason))
    }

    if (isPending) {
        return <p className="p-4 text-center">Cargando...</p>
    }

    if (error) {
        return <p className="p-4 text-center">No pudimos cargar el evento. Intenta de nuevo.</p>
    }

    if (event === null) {
        return (
            <main className="flex min-h-screen flex-col items-center justify-center gap-2 p-4 text-center">
                <h1 className="text-2xl font-bold">Evento no encontrado</h1>
                <p>Revisa el enlace o vuelve a escanear el código QR.</p>
            </main>
        )
    }

    const remaining = quota?.remaining ?? 0

    return (
        <main className="mx-auto flex min-h-screen max-w-md flex-col gap-4 p-4">
            <h1 className="text-3xl font-bold" style={{ color: event.color }}>
                {event.name}
            </h1>
            <p>{event.eventDate}</p>
            {quota && (
                <p>
                    Te quedan {quota.remaining} de {quota.limit} fotos.
                </p>
            )}
            {uploading && <p>Subiendo...</p>}
            {message && <p role="status">{message}</p>}
            <PhotoPicker
                color={event.color}
                disabled={uploading || remaining === 0}
                maxFiles={remaining}
                onSelect={handleSelect}
            />
        </main>
    )
}

export default EventPage
