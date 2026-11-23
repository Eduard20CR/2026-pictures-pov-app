import { Link } from 'react-router'
import { useCurrentUser } from '../../auth/hooks/useCurrentUser'
import { useSignOut } from '../../auth/hooks/useSignOut'
import PendingInvitations from '../components/PendingInvitations'
import { useMyEvents } from '../hooks/useMyEvents'
import { ROLE_LABELS, STATUS_LABELS } from '../labels'

// Private page: RequireAuth guarantees there is a session here.
// Events where I hold an Accepted invitation, newest event date first, no pagination.
export default function MyEventsPage() {
    const currentUser = useCurrentUser()
    const events = useMyEvents()
    const signOut = useSignOut()

    return (
        <main className="mx-auto flex w-full max-w-3xl flex-1 flex-col gap-4 p-4">
            <h1 className="text-2xl font-bold">Mis eventos</h1>

            <PendingInvitations />

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
                                <td className="py-2">
                                    <Link to={`/my-events/${event.id}`} className="underline">{event.name}</Link>
                                </td>
                                <td className="py-2">{event.eventDate}</td>
                                <td className="py-2">{STATUS_LABELS[event.status]}</td>
                                <td className="py-2">{ROLE_LABELS[event.role]}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            ))}

            {currentUser.data && <p className="text-sm">Sesión iniciada como {currentUser.data.email}</p>}
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
