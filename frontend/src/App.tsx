import { AppLayout } from './components/layout/AppLayout'

function App() {
  return (
    <AppLayout>
      <section className="max-w-3xl" aria-labelledby="welcome-heading">
        <p className="mb-3 text-sm font-semibold uppercase tracking-widest text-primary-700">
          Willkommen
        </p>
        <h1 id="welcome-heading" className="text-3xl font-bold tracking-tight sm:text-4xl">
          Gemeinsam genutzte Ressourcen an einem Ort
        </h1>
        <p className="mt-4 max-w-2xl text-base leading-7 text-slate-600 sm:text-lg">
          Räume, Fahrzeuge, Geräte und Arbeitsplätze einfach finden, buchen und
          verwalten.
        </p>
      </section>
    </AppLayout>
  )
}

export default App
