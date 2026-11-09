import { useMe } from '../../auth/hooks/useMe'

// Private page: RequireAuth guarantees there is a session here.
export default function MyEventsPage() {
    const me = useMe()

    return (
        <main className="mx-auto flex min-h-dvh max-w-md flex-col gap-4 p-4">
            <h1 className="text-2xl font-bold">Mis eventos</h1>
            {me.isPending && <p>Cargando tu cuenta...</p>}
            {me.isError && <p role="alert">No pudimos cargar tu cuenta. Intenta de nuevo.</p>}
            {me.data && <p>Sesión iniciada como {me.data.email}</p>}
        </main>
    )
}