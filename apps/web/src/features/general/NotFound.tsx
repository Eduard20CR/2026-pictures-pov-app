import { Link } from 'react-router'

function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-2 p-4 text-center">
      <h1 className="text-6xl font-bold">404</h1>
      <p className="text-lg">Page not found</p>
      <Link to="/" className="mt-2 rounded border px-3 py-1">
        Go home
      </Link>
    </div>
  )
}

export default NotFound
