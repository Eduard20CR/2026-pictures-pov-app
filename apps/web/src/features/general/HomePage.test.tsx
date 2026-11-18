// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { useAuth } from 'react-oidc-context'
import { afterEach, expect, test, vi } from 'vitest'
import HomePage from './HomePage'

vi.mock('react-oidc-context')

afterEach(cleanup)

const signinRedirect = vi.fn()

function renderHomeAs(user: { groups?: string[] } | null) {
    vi.mocked(useAuth).mockReturnValue({
        isAuthenticated: user !== null,
        user: user && { profile: { 'cognito:groups': user.groups } },
        signinRedirect,
    } as unknown as ReturnType<typeof useAuth>)

    render(
        <MemoryRouter>
            <HomePage />
        </MemoryRouter>,
    )
}

test('a signed-out visitor sees the app and can sign in', () => {
    renderHomeAs(null)

    screen.getByRole('heading', { name: 'Pictures POV' })
    fireEvent.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

    expect(signinRedirect).toHaveBeenCalled()
})

test('a signed-in user sees "Mis eventos" but not the admin link', () => {
    renderHomeAs({})

    expect(screen.getByRole('link', { name: 'Mis eventos' }).getAttribute('href')).toBe('/my-events')
    expect(screen.queryByRole('link', { name: 'Administrar eventos' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Iniciar sesión' })).toBeNull()
})

test('an administrator also sees the admin link', () => {
    renderHomeAs({ groups: ['admins'] })

    expect(screen.getByRole('link', { name: 'Administrar eventos' }).getAttribute('href')).toBe('/admin/events')
})
