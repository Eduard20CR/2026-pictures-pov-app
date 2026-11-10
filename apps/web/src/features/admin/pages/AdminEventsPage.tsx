import { Link, useSearchParams } from 'react-router'
import { useAdminEvents } from '../hooks/useAdminEvents'

// Admin page: every event on the platform, newest event date first, 20 per page.
export default function AdminEventsPage() {
    const [searchParams, setSearchParams] = useSearchParams()
    const page = Number(searchParams.get('page') ?? '0')
    const events = useAdminEvents(page)

    function goToPage(newPage: number) {
        setSearchParams({ page: String(newPage) })
    }

    if (events.isPending) {
        return <main className="flex-1 p-4">Cargando eventos...</main>
    }

    if (events.isError) {
        return <main className="flex-1 p-4" role="alert">No se pudieron cargar los eventos.</main>
    }

    const { items, totalPages } = events.data
    const isFirstPage = page === 0
    const isLastPage = page >= totalPages - 1

    return (
        <main className="mx-auto flex w-full max-w-3xl flex-1 flex-col gap-4 p-4">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold">Eventos</h1>
                <Link to="/admin/events/new" className="rounded border px-4 py-2">Crear evento</Link>
            </div>

            {items.length === 0 ? (
                <p>Todavía no hay eventos.</p>
            ) : (
                <table className="w-full text-left">
                    <thead>
                        <tr className="border-b">
                            <th className="py-2">Nombre</th>
                            <th className="py-2">Fecha</th>
                            <th className="py-2">Estado</th>
                            <th className="py-2">Dueño</th>
                        </tr>
                    </thead>
                    <tbody>
                        {items.map((event) => (
                            <tr key={event.id} className="border-b">
                                <td className="py-2">{event.name}</td>
                                <td className="py-2">{event.eventDate}</td>
                                <td className="py-2">{event.status}</td>
                                <td className="py-2">{event.ownerEmail ?? 'Sin dueño'}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}

            <div className="flex items-center gap-4">
                <button
                    onClick={() => goToPage(page - 1)}
                    disabled={isFirstPage}
                    className="rounded border px-4 py-2 disabled:opacity-50"
                >
                    Anterior
                </button>
                <span>Página {page + 1} de {Math.max(totalPages, 1)}</span>
                <button
                    onClick={() => goToPage(page + 1)}
                    disabled={isLastPage}
                    className="rounded border px-4 py-2 disabled:opacity-50"
                >
                    Siguiente
                </button>
            </div>
        </main>
    )
}
