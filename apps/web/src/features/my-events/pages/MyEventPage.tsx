import { Link, useParams } from 'react-router'
import { useMyEvent } from '../hooks/useMyEvent'
import { ROLE_LABELS, STATUS_LABELS } from '../labels'
import InviteModeratorForm from '../components/InviteModeratorForm'
import CopyEventLinkButton from '../components/CopyEventLinkButton'
import DownloadEventQrButtons from '../components/DownloadEventQrButtons'

// Private page: RequireAuth guarantees there is a session here.
export default function MyEventPage() {
    const { id = '' } = useParams()
    const event = useMyEvent(id)

    return (
        <main className="mx-auto flex w-full max-w-3xl flex-1 flex-col gap-4 p-4">
            <Link to="/my-events" className="self-start text-sm underline">
                Volver a Mis eventos
            </Link>

            {event.isPending && <p>Cargando evento...</p>}
            {event.isError && <p role="alert">No se pudo cargar el evento. Intenta de nuevo.</p>}
            {event.data === null && <h1 className="text-2xl font-bold">Evento no encontrado</h1>}
            {event.data && (
                <>
                    <h1 className="text-2xl font-bold">{event.data.name}</h1>
                    <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1">
                        <dt className="font-semibold">Fecha</dt>
                        <dd>{event.data.eventDate}</dd>
                        <dt className="font-semibold">Estado</dt>
                        <dd>{STATUS_LABELS[event.data.status]}</dd>
                        <dt className="font-semibold">Mi rol</dt>
                        <dd>{ROLE_LABELS[event.data.role]}</dd>
                    </dl>
                    <div className="flex flex-wrap items-center gap-2">
                        <Link to={`/my-events/${event.data.id}/review`} className="rounded border px-3 py-1 text-sm">
                            Revisar fotos
                        </Link>
                        <CopyEventLinkButton publicCode={event.data.publicCode} />
                        <DownloadEventQrButtons publicCode={event.data.publicCode} eventName={event.data.name} />
                    </div>
                    {event.data.role === 'OWNER' && <InviteModeratorForm eventId={event.data.id} />}
                </>
            )}
        </main>
    )
}
