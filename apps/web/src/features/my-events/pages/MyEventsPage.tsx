import { useMe } from '../../auth/hooks/useMe'
import { useSignOut } from '../../auth/hooks/useSignOut'
import { useMyEvents } from '../hooks/useMyEvents'
import type { MyEvent } from '../types'

const STATUS_LABELS: Record<MyEvent['status'], string> = {
    OPEN: 'Abierto',
    CLOSED: 'Cerrado',
    ARCHIVED: 'Archivado',
}

const ROLE_LABELS: Record<MyEvent['role'], string> = {
    OWNER: 'Organizador',
    MODERATOR: 'Moderador',
}

// Private page: RequireAuth guarantees there is a session here.
// Events where I hold an Accepted invitation, newest event date first, no pagination.
export default function MyEventsPage() {
    const me = useMe()
    const events = useMyEvents()
    const signOut = useSignOut()

    return (
        <main className="mx-auto flex w-full max-w-3xl flex-1 flex-col gap-4 p-4">
            <h1 className="text-2xl font-bold">Mis eventos</h1>

            {events.isPending && <p>Cargando eventos...</p>}
            {events.isError && <p role="alert">No se pudieron cargar tus eventos. Intenta de nuevo.</p>}
            {events.data && (events.data.length === 0 ? (
                <p>Aún no tienes eventos.</p>
            ) : (
                <table className="w-full text-left">
                    <thead>
                        <tr className="border-b">
                            <th className="py-2">Nombre</th>
                            <th className="py-2">Fecha</th>
                            <th className="py-2">Estado</th>
                            <th className="py-2">Mi rol</th>
                        </tr>
                    </thead>
                    <tbody>
                        {events.data.map((event) => (
                            <tr key={event.id} className="border-b">
                                <td className="py-2">{event.name}</td>
                                <td className="py-2">{event.eventDate}</td>
                                <td className="py-2">{STATUS_LABELS[event.status]}</td>
                                <td className="py-2">{ROLE_LABELS[event.role]}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            ))}

            {me.data && <p className="text-sm">Sesión iniciada como {me.data.email}</p>}
            <button
                type="button"
                onClick={signOut}
                className="self-start rounded border px-4 py-2"
            >
                Cerrar sesión
            </button>
        </main>
    )
}
