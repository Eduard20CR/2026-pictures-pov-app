import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import NotFound from './features/general/NotFound.tsx'
import { createBrowserRouter } from "react-router"
import { RouterProvider } from "react-router/dom"
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import EventPage from './features/event/pages/EventPage.tsx'
import { AuthProvider } from 'react-oidc-context'
import { oidcConfig } from './features/auth/authConfig.ts'
import AuthCallbackPage from './features/auth/pages/AuthCallbackPage.tsx'
import MyEventsPage from './features/my-events/pages/MyEventsPage.tsx'
import RequireAuth from './features/auth/components/RequireAuth.tsx'
import AppLayout from './app/AppLayout.tsx'
import CreateEventPage from './features/admin/pages/CreateEventPage.tsx'

const queryClient = new QueryClient()

const router = createBrowserRouter([
  {
    // Layout route: no path, it only wraps every page with the header and footer.
    element: <AppLayout />,
    children: [
      // Public routes: guests and the sign-in callback.
      { path: "/", element: <App /> },
      { path: "/events/:publicCode", element: <EventPage /> },
      { path: "/auth/callback", element: <AuthCallbackPage /> },

      // Private routes: everything inside RequireAuth needs a session.
      {
        element: <RequireAuth />,
        children: [
          { path: "/my-events", element: <MyEventsPage /> },
          { path: "/admin/events/new", element: <CreateEventPage /> },
        ],
      },

      { path: "*", element: <NotFound /> },
    ],
  },
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider {...oidcConfig}>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
      </QueryClientProvider>
    </AuthProvider>
  </StrictMode>,
)
