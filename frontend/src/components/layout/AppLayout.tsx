import type { ReactNode } from 'react'

type AppLayoutProps = {
  children: ReactNode
}

const navigationItems = [
  { label: 'Startseite', href: '/', current: true },
  { label: 'Ressourcen', href: '/resources' },
  { label: 'Buchungen', href: '/bookings' },
  { label: 'Profil', href: '/profile' },
]

export function AppLayout({ children }: AppLayoutProps) {
  return (
    <div className="flex min-h-screen flex-col bg-slate-50 text-slate-950">
      <a
        className="sr-only z-50 rounded-md bg-white px-4 py-2 font-semibold text-primary-700 shadow focus:not-sr-only focus:fixed focus:left-4 focus:top-4"
        href="#main-content"
      >
        Zum Hauptinhalt springen
      </a>

      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl flex-col gap-4 px-4 py-4 sm:px-6 md:flex-row md:items-center md:justify-between lg:px-8">
          <a
            className="flex w-fit items-center gap-3 rounded-md focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-primary-600"
            href="/"
            aria-label="ResourceHub Startseite"
          >
            <span
              className="grid size-10 place-items-center rounded-xl bg-primary-600 text-lg font-bold text-white"
              aria-hidden="true"
            >
              RH
            </span>
            <span>
              <span className="block text-lg font-bold leading-tight">ResourceHub</span>
              <span className="block text-xs text-slate-500">Gemeinsam besser nutzen</span>
            </span>
          </a>

          <nav aria-label="Hauptnavigation">
            <ul className="flex flex-wrap gap-1">
              {navigationItems.map((item) => (
                <li key={item.href}>
                  <a
                    className={`block rounded-lg px-3 py-2 text-sm font-medium transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 ${
                      item.current
                        ? 'bg-primary-50 text-primary-700'
                        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-950'
                    }`}
                    href={item.href}
                    aria-current={item.current ? 'page' : undefined}
                  >
                    {item.label}
                  </a>
                </li>
              ))}
            </ul>
          </nav>
        </div>
      </header>

      <main id="main-content" className="mx-auto w-full max-w-7xl flex-1 px-4 py-10 sm:px-6 lg:px-8">
        {children}
      </main>

      <footer className="border-t border-slate-200 bg-white">
        <div className="mx-auto max-w-7xl px-4 py-6 text-sm text-slate-500 sm:px-6 lg:px-8">
          ResourceHub – Ressourcen einfach teilen und verwalten.
        </div>
      </footer>
    </div>
  )
}
