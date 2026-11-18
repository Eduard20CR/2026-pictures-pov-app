import { Link } from 'react-router'
import { useAuth } from 'react-oidc-context'
import { useIsAdmin } from '../auth/hooks/useIsAdmin'

// Public landing page. Guests never need it: they arrive through the event QR code.
export default function HomePage() {
    const auth = useAuth()
    const isAdmin = useIsAdmin()

    return (
        <main className="mx-auto flex w-full max-w-md flex-1 flex-col justify-center gap-4 p-4 text-center">
            <h1 className="text-3xl font-bold">Pictures POV</h1>
            <p>Las fotos de tu evento, desde el punto de vista de cada invitado.</p>
            <p>Los invitados suben sus fotos escaneando el código QR del evento, sin crear una cuenta.</p>

            {auth.isAuthenticated ? (
                <nav className="flex flex-col gap-2">
                    <Link to="/my-events" className="rounded border px-4 py-2">Mis eventos</Link>
                    {isAdmin && (
                        <Link to="/admin/events" className="rounded border px-4 py-2">Administrar eventos</Link>
                    )}
                </nav>
            ) : (
                <button
                    type="button"
                    onClick={() => auth.signinRedirect()}
                    className="rounded border px-4 py-2"
                >
                    Iniciar sesión
                </button>
            )}
        </main>
    )
}
