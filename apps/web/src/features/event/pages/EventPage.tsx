import { useParams } from 'react-router'
import { PhotoPicker } from '../components/PhotoPicker'
import { usePublicEvent } from '../hooks/usePublicEvent'
import { useQuota } from '../hooks/useQuota'
import { useUploadQueue } from '../hooks/useUploadQueue'
import { UploadList } from '../components/UploadList'



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
