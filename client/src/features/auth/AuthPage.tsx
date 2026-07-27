import { useState } from 'react'
import type { FormEvent } from 'react'
import { Loader2, ShieldCheck, Signal } from 'lucide-react'
import { Field } from '../../components/ui/Field'
import { StateBanner } from '../../components/ui/StateBanner'
import { useAuth } from '../../auth/AuthContext'

type AuthMode = 'login' | 'register'

export function AuthPage() {
  const { isConfigured, signIn, signUp } = useAuth()
  const [mode, setMode] = useState<AuthMode>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const isRegistering = mode === 'register'

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setMessage(null)
    setIsSubmitting(true)

    try {
      if (isRegistering) {
        await signUp(email, password)
        setMessage('Account created. Check your inbox if Supabase email confirmation is enabled.')
      } else {
        await signIn(email, password)
      }
    } catch (authError) {
      setError(authError instanceof Error ? authError.message : 'Authentication failed.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="min-h-screen bg-slate-50 px-5 py-8 text-slate-950">
      <div className="mx-auto grid min-h-[calc(100vh-4rem)] max-w-6xl items-center gap-8 lg:grid-cols-[minmax(0,1fr)_420px]">
        <section>
          <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-blue-600 text-white">
            <Signal size={24} />
          </div>
          <p className="mt-8 text-sm font-semibold uppercase tracking-wide text-blue-700">UptimeDesk</p>
          <h1 className="mt-3 max-w-2xl text-4xl font-semibold tracking-tight text-slate-950">
            Monitor your project health from a private workspace.
          </h1>
          <p className="mt-4 max-w-xl text-base leading-7 text-slate-600">
            Sign in to manage monitors, inspect incidents, and keep each user&apos;s project data separated.
          </p>

          <div className="mt-8 grid gap-3 sm:grid-cols-3">
            {['JWT protected API', 'User-owned monitors', 'Supabase sessions'].map((item) => (
              <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm" key={item}>
                <ShieldCheck className="text-blue-600" size={18} />
                <p className="mt-3 text-sm font-semibold text-slate-800">{item}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex rounded-md bg-slate-100 p-1 text-sm font-semibold">
            <button
              className={`flex-1 rounded px-3 py-2 ${mode === 'login' ? 'bg-white text-slate-950 shadow-sm' : 'text-slate-500'}`}
              onClick={() => setMode('login')}
              type="button"
            >
              Login
            </button>
            <button
              className={`flex-1 rounded px-3 py-2 ${mode === 'register' ? 'bg-white text-slate-950 shadow-sm' : 'text-slate-500'}`}
              onClick={() => setMode('register')}
              type="button"
            >
              Register
            </button>
          </div>

          <h2 className="mt-6 text-xl font-semibold text-slate-950">
            {isRegistering ? 'Create your account' : 'Welcome back'}
          </h2>
          <p className="mt-1 text-sm text-slate-500">
            {isRegistering
              ? 'Use the same Supabase project credentials planned for deployment.'
              : 'Use your Supabase Auth account to access your monitors.'}
          </p>

          {!isConfigured && (
            <div className="mt-5">
              <StateBanner tone="warning">
                Add <code>VITE_SUPABASE_URL</code> and <code>VITE_SUPABASE_ANON_KEY</code> before using auth.
              </StateBanner>
            </div>
          )}

          {error && (
            <div className="mt-5">
              <StateBanner tone="warning">{error}</StateBanner>
            </div>
          )}

          {message && (
            <div className="mt-5">
              <StateBanner tone="info">{message}</StateBanner>
            </div>
          )}

          <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
            <Field label="Email">
              <input
                className="w-full rounded-md border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@example.com"
                type="email"
                value={email}
              />
            </Field>

            <Field label="Password">
              <input
                className="w-full rounded-md border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                minLength={6}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="At least 6 characters"
                type="password"
                value={password}
              />
            </Field>

            <button
              className="inline-flex w-full items-center justify-center gap-2 rounded-md bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
              disabled={!isConfigured || isSubmitting}
              type="submit"
            >
              {isSubmitting && <Loader2 className="animate-spin" size={16} />}
              {isRegistering ? 'Create account' : 'Sign in'}
            </button>
          </form>
        </section>
      </div>
    </main>
  )
}
