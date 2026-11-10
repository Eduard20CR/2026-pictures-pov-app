// @vitest-environment jsdom
import { cleanup, render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { afterEach, expect, test } from 'vitest'
import AppLayout from './AppLayout'

afterEach(cleanup)

test('renders the page between the header and the footer', () => {
    const router = createMemoryRouter([
        {
            element: <AppLayout />,
            children: [{ path: '/', element: <p>Page content</p> }],
        },
    ])

    render(<RouterProvider router={router} />)

    // getBy* throws if the element is missing, so each line is already a check
    expect(screen.getByRole('banner').textContent).toContain('Pictures POV')
    screen.getByText('Page content')
    expect(screen.getByRole('contentinfo').textContent).toContain('Pictures POV')
})
