import { useParams } from 'react-router'
import { PhotoPicker } from '../components/PhotoPicker'
import { usePublicEvent } from '../hooks/usePublicEvent'
import { useQuota } from '../hooks/useQuota'
import { useUploadQueue } from '../hooks/useUploadQueue'
import { UploadList } from '../components/UploadList'
import { EventStatusScreen } from '../components/EventStatusScreen'

function EventPage() {
    const { publicCode = '' } = useParams()
    const { data: event, isPending, error } = usePublicEvent(publicCode)
    const { data: quota } = useQuota(publicCode)
    const { uploadItems, addFiles, retryItem, removeItem } = useUploadQueue(publicCode)

    if (isPending) {
        return <p className="p-4 text-center">Cargando...</p>
    }

    if (error) {
        return <p className="p-4 text-center">No pudimos cargar el evento. Intenta de nuevo.</p>
    }

    if (event === null) {
        return (
            <EventStatusScreen
                title="Evento no encontrado"
                text="Revisa el enlace o vuelve a escanear el código QR."
            />
        )
    }

    if (event === 'closed') {
        return (
            <EventStatusScreen
                title="Este evento ya cerró"
                text="Ya no se reciben fotos. ¡Gracias por participar!"
            />
        )
    }

    const remaining = quota?.remaining ?? 0
    const isUploading = uploadItems.some(
        (item) => item.status === 'waiting' || item.status === 'uploading',
    )

    return (
        <main className="mx-auto flex w-full max-w-md flex-1 flex-col gap-4 p-4">
            <h1 className="text-3xl font-bold" style={{ color: event.color }}>
                {event.name}
            </h1>
            <p>{event.eventDate}</p>
            {quota && (
                <p>
                    Te quedan {quota.remaining} de {quota.limit} fotos.
                </p>
            )}

            <UploadList items={uploadItems} onRetry={retryItem} onRemove={removeItem} />

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
