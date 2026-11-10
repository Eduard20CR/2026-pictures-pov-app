import { Link, Outlet } from 'react-router'

// Layout route: every page renders inside the Outlet, between the shared header and footer.
// It must live inside the router (not around RouterProvider) so Link and useNavigate work.
export default function AppLayout() {
    return (
        <div className="flex min-h-dvh flex-col">
            <header className="border-b p-4">
                <Link to="/" className="font-bold">
                    Pictures POV
                </Link>
            </header>

            <div className="flex flex-1 flex-col">
                <Outlet />
            </div>

            <footer className="border-t p-4 text-center text-sm">
                © {new Date().getFullYear()} Pictures POV
            </footer>
        </div>
    )
}
