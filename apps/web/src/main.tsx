import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import NotFoundPage from './features/general/NotFoundPage.tsx'
import HomePage from './features/general/HomePage.tsx'
import { createBrowserRouter } from "react-router"
import { RouterProvider } from "react-router/dom"
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import GuestEventPage from './features/guest-event/pages/GuestEventPage.tsx'
import { AuthProvider } from 'react-oidc-context'
import { oidcConfig } from './features/auth/authConfig.ts'
import AuthCallbackPage from './features/auth/pages/AuthCallbackPage.tsx'
import MyEventsPage from './features/my-events/pages/MyEventsPage.tsx'
import MyEventPage from './features/my-events/pages/MyEventPage.tsx'
import RequireAuth from './features/auth/components/RequireAuth.tsx'
import RequireAdmin from './features/auth/components/RequireAdmin.tsx'
import AppLayout from './app/AppLayout.tsx'
import CreateEventPage from './features/admin/pages/CreateEventPage.tsx'
import AdminEventsPage from './features/admin/pages/AdminEventsPage.tsx'

const queryClient = new QueryClient()

const router = createBrowserRouter([
  {
    // Layout route: no path, it only wraps every page with the header and footer.
    element: <AppLayout />,
    children: [
      // Public routes: guests and the sign-in callback.
      { path: "/", element: <HomePage /> },
      { path: "/events/:publicCode", element: <GuestEventPage /> },
      { path: "/auth/callback", element: <AuthCallbackPage /> },

      // Private routes: everything inside RequireAuth needs a session.
      {
        element: <RequireAuth />,
        children: [
          { path: "/my-events", element: <MyEventsPage /> },
          { path: "/my-events/:id", element: <MyEventPage /> },

          // Admin routes: RequireAdmin refuses users outside the admins group.
          {
            element: <RequireAdmin />,
            children: [
              { path: "/admin/events", element: <AdminEventsPage /> },
              { path: "/admin/events/new", element: <CreateEventPage /> },
              // Any other /admin path: refused for non-admins, 404 for admins.
              { path: "/admin/*", element: <NotFoundPage /> },
            ],
          },
        ],
      },

      { path: "*", element: <NotFoundPage /> },
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
