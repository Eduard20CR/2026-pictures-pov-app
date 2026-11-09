import { useAuth } from 'react-oidc-context'
import { useMe } from '../../auth/hooks/useMe'

export default function MyEventsPage() {
    const auth = useAuth()
    const me = useMe()

    if (auth.isLoading) {
        return <p className="p-4 text-center">Cargando...</p>
    }

    if (!auth.isAuthenticated) {
        return (
            <main className="mx-auto flex min-h-dvh max-w-md flex-col items-center justify-center gap-4 p-4 text-center">
                <h1 className="text-2xl font-bold">Mis eventos</h1>
                <p>Inicia sesión para ver y administrar tus eventos.</p>
                <button
                    type="button"
                    onClick={() => void auth.signinRedirect()}
                    className="rounded bg-black px-4 py-2 text-white"
                >
                    Iniciar sesión
                </button>
            </main>
        )
    }

    return (
        <main className="mx-auto flex min-h-dvh max-w-md flex-col gap-4 p-4">
            <h1 className="text-2xl font-bold">Mis eventos</h1>
            {me.isPending && <p>Cargando tu cuenta...</p>}
            {me.isError && <p role="alert">No pudimos cargar tu cuenta. Intenta de nuevo.</p>}
            {me.data && <p>Sesión iniciada como {me.data.email}</p>}
        </main>
    )
}