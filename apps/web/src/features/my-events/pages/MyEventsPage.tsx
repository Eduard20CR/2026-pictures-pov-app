import { useMe } from '../../auth/hooks/useMe'
import { useSignOut } from '../../auth/hooks/useSignOut'

// Private page: RequireAuth guarantees there is a session here.
export default function MyEventsPage() {
    const me = useMe()
    const signOut = useSignOut()

    return (
        <main className="mx-auto flex w-full flex-1 max-w-md flex-col gap-4 p-4">
            <h1 className="text-2xl font-bold">Mis eventos</h1>
            {me.isPending && <p>Cargando tu cuenta...</p>}
            {me.isError && <p role="alert">No pudimos cargar tu cuenta. Intenta de nuevo.</p>}
            {me.data && <p>Sesión iniciada como {me.data.email}</p>}
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