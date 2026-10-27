import { useParams } from 'react-router'
import { usePublicEvent } from '../hooks/usePublicEvent'

function EventPage() {
    const { publicCode = '' } = useParams()
    const { data: event, isPending, error } = usePublicEvent(publicCode)

    if (isPending) return <p>Cargando...</p>
    if (error) return <p>No pudimos cargar el evento.</p>

    return (
        <main>
            <h1>{event.name}</h1>
            <p>{event.eventDate}</p>
            <p>Puedes subir hasta {event.imagesPerUser} fotos.</p>
        </main>
    )
}

export default EventPage