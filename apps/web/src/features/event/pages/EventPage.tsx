import { useParams } from 'react-router'
import { usePublicEvent } from '../hooks/usePublicEvent'

function EventPage() {
    const { publicCode = '' } = useParams()
    const { data: event, isPending, error } = usePublicEvent(publicCode)

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

    return (
        <main className="mx-auto flex min-h-screen max-w-md flex-col gap-4 p-4">
            <h1 className="text-3xl font-bold" style={{ color: event.color }}>
                {event.name}
            </h1>
            <p>{event.eventDate}</p>
            <p>Puedes subir hasta {event.imagesPerUser} fotos.</p>
            <button
                type="button"
                disabled
                className="mt-auto w-full rounded-lg py-3 font-semibold text-white disabled:opacity-60"
                style={{ backgroundColor: event.color }}
            >
                Subir fotos
            </button>
        </main>
    )
}

export default EventPage