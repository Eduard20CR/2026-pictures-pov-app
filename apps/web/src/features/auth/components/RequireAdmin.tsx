import { Outlet } from 'react-router'
import { useIsAdmin } from '../hooks/useIsAdmin'

// Layout route for admin pages, nested inside RequireAuth: a session exists here.
export default function RequireAdmin() {
    if (!useIsAdmin()) {
        return <main className="flex-1 p-4" role="alert">No autorizado: esta página es solo para administradores.</main>
    }
    return <Outlet />
}
