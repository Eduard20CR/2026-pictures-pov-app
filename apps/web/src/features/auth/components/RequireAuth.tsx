import { Outlet } from 'react-router'
import { withAuthenticationRequired } from 'react-oidc-context'

// Layout route for private pages: the library sends signed-out users to Cognito.
const RequireAuth = withAuthenticationRequired(Outlet, {
    OnRedirecting: () => <p className="p-4 text-center">Redirigiendo al inicio de sesión...</p>,
})

export default RequireAuth