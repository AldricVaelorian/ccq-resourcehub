import '@testing-library/jest-dom/vitest'
import { cleanup, render, screen, within } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'

import { AppLayout } from './AppLayout'

afterEach(cleanup)

describe('AppLayout', () => {
  it('renders the shared landmarks and supplied page content', () => {
    // arrange & act
    render(
      <AppLayout>
        <h1>Testinhalt</h1>
      </AppLayout>,
    )

    // assert
    expect(screen.getByRole('banner')).toBeInTheDocument()
    expect(screen.getByRole('main')).toHaveAttribute('id', 'main-content')
    expect(screen.getByRole('heading', { name: 'Testinhalt' })).toBeInTheDocument()
    expect(screen.getByRole('contentinfo')).toHaveTextContent(
      'ResourceHub – Ressourcen einfach teilen und verwalten.',
    )
  })

  it('provides the expected primary navigation and announces the current page', () => {
    // arrange & act
    render(<AppLayout>Inhalt</AppLayout>)

    // assert
    const navigation = screen.getByRole('navigation', { name: 'Hauptnavigation' })
    const links = within(navigation).getAllByRole('link')
    expect(links).toHaveLength(4)
    expect(links).toEqual([
      expect.objectContaining({ textContent: 'Startseite' }),
      expect.objectContaining({ textContent: 'Ressourcen' }),
      expect.objectContaining({ textContent: 'Buchungen' }),
      expect.objectContaining({ textContent: 'Profil' }),
    ])
    expect(links.map((link) => link.getAttribute('href'))).toEqual([
      '/',
      '/resources',
      '/bookings',
      '/profile',
    ])
    expect(screen.getByRole('link', { name: 'Startseite', current: 'page' })).toHaveAttribute(
      'aria-current',
      'page',
    )
    expect(within(navigation).getAllByRole('link', { current: false })).toHaveLength(3)
  })

  it('links the keyboard skip control to the main content landmark', () => {
    // arrange & act
    render(<AppLayout>Inhalt</AppLayout>)

    // assert
    const skipLink = screen.getByRole('link', { name: 'Zum Hauptinhalt springen' })
    expect(skipLink).toHaveAttribute('href', '#main-content')
    expect(skipLink).toHaveClass('focus:not-sr-only')
    expect(screen.getByRole('main')).toHaveAttribute('id', 'main-content')
  })

  it('keeps navigation wrapping enabled for narrow viewports', () => {
    // arrange & act
    render(<AppLayout>Inhalt</AppLayout>)

    // assert
    const navigationList = within(
      screen.getByRole('navigation', { name: 'Hauptnavigation' }),
    ).getByRole('list')
    expect(navigationList).toHaveClass('flex-wrap')
  })
})
