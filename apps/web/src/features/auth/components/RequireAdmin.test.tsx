// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { useAuth } from 'react-oidc-context'
import { afterEach, expect, test, vi } from 'vitest'
import RequireAdmin from './RequireAdmin'

vi.mock('react-oidc-context')

afterEach(cleanup)

function renderAdminPageAs(groups: string[] | undefined) {
    vi.mocked(useAuth).mockReturnValue({
        user: { profile: { 'cognito:groups': groups } },
    } as unknown as ReturnType<typeof useAuth>)

    const router = createMemoryRouter(
        [{ element: <RequireAdmin />, children: [{ path: '/admin/events', element: <p>Admin page</p> }] }],
        { initialEntries: ['/admin/events'] },
    )
    render(<RouterProvider router={router} />)
}

test('an administrator sees the admin page', () => {
    renderAdminPageAs(['admins'])

    screen.getByText('Admin page')
})

test('a signed-in user who is not an administrator is refused', () => {
    renderAdminPageAs(undefined)

    expect(screen.queryByText('Admin page')).toBeNull()
    expect(screen.getByRole('alert').textContent).toContain('No autorizado')
})
