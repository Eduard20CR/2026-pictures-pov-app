import { useParams } from 'react-router'
import { PhotoPicker } from '../components/PhotoPicker'
import { usePublicEvent } from '../hooks/usePublicEvent'
import { useQuota } from '../hooks/useQuota'
import { useUploadQueue } from '../hooks/useUploadQueue'
import type { UploadItem } from '../types'

const ERROR_MESSAGES: Record<string, string> = {
    UPLOAD_LIMIT_REACHED: 'Ya subiste todas las fotos permitidas.',
    UNSUPPORTED_CONTENT_TYPE: 'Solo se aceptan fotos JPEG, PNG, WebP o HEIC.',
    FILE_TOO_LARGE: 'La foto pesa más de 20 MB.',
    EVENT_NOT_FOUND: 'Este evento ya no está disponible.',
    EVENT_CLOSED: 'Este evento ya no recibe fotos.',
}

const STATUS_LABELS: Record<UploadItem['status'], string> = {
    waiting: 'En espera',
    uploading: 'Subiendo...',
    done: 'Lista ✓',
    error: 'Falló',
}

function errorMessage(code: string): string {
    return ERROR_MESSAGES[code] ?? 'No pudimos subir la foto. Intenta de nuevo.'
}

function EventPage() {
    const { publicCode = '' } = useParams()
    const { data: event, isPending, error } = usePublicEvent(publicCode)
    const { data: quota } = useQuota(publicCode)
    const { uploadItems, addFiles } = useUploadQueue(publicCode)

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

    if (event === 'closed') {
        return (
            <main className="flex min-h-screen flex-col items-center justify-center gap-2 p-4 text-center">
                <h1 className="text-2xl font-bold">Este evento ya cerró</h1>
                <p>Ya no se reciben fotos. ¡Gracias por participar!</p>
            </main>
        )
    }

    const remaining = quota?.remaining ?? 0
    const isUploading = uploadItems.some(
        (item) => item.status === 'waiting' || item.status === 'uploading',
    )

    return (
        <main className="mx-auto flex min-h-dvh max-w-md flex-col gap-4 p-4">
            <h1 className="text-3xl font-bold" style={{ color: event.color }}>
                {event.name}
            </h1>
            <p>{event.eventDate}</p>
            {quota && (
                <p>
                    Te quedan {quota.remaining} de {quota.limit} fotos.
                </p>
            )}
            {isUploading && <p>Subiendo...</p>}

            <ul className="flex flex-col gap-2">
                {uploadItems.map((item) => (
                    <li key={item.id} className="flex flex-col">
                        <div className="flex justify-between">
                            <span className="truncate">{item.file.name}</span>
                            <span>{STATUS_LABELS[item.status]}</span>
                        </div>
                        {item.error && (
                            <p role="alert" className="text-sm text-red-600">
                                {errorMessage(item.error)}
                            </p>
                        )}
                    </li>
                ))}
            </ul>

            <PhotoPicker
                color={event.color}
                disabled={isUploading || remaining === 0}
                maxFiles={remaining}
                onSelect={addFiles}
            />
        </main>
    )
}

export default EventPage
