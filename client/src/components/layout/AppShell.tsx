import type { ReactNode } from 'react'
import {
  Activity,
  AlertTriangle,
  Bell,
  Globe2,
  LayoutDashboard,
  Plus,
  Search,
  Settings,
  Signal,
} from 'lucide-react'

export function AppShell({
  backendUnavailable,
  children,
  searchTerm,
  setSearchTerm,
}: {
  backendUnavailable: boolean
  children: ReactNode
  searchTerm: string
  setSearchTerm: (value: string) => void
}) {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      <Sidebar />
      <main className="lg:pl-64">
        <Topbar
          backendUnavailable={backendUnavailable}
          searchTerm={searchTerm}
          setSearchTerm={setSearchTerm}
        />
        {children}
      </main>
    </div>
  )
}

function Sidebar() {
  return (
    <aside className="fixed inset-y-0 left-0 hidden w-64 border-r border-slate-200 bg-white lg:block">
      <div className="flex h-16 items-center gap-3 border-b border-slate-200 px-6">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 text-white">
          <Signal size={20} />
        </div>
        <div>
          <p className="text-sm font-semibold text-slate-950">UptimeDesk</p>
          <p className="text-xs text-slate-500">API monitoring</p>
        </div>
      </div>

      <nav className="space-y-1 px-3 py-5 text-sm font-medium text-slate-600">
        {[
          ['Dashboard', LayoutDashboard],
          ['Monitors', Activity],
          ['Incidents', AlertTriangle],
          ['Status Pages', Globe2],
          ['Alerts', Bell],
          ['Settings', Settings],
        ].map(([label, Icon]) => (
          <button
            className={`flex w-full items-center gap-3 rounded-md px-3 py-2 text-left ${
              label === 'Dashboard' ? 'bg-blue-50 text-blue-700' : 'hover:bg-slate-100'
            }`}
            key={label as string}
          >
            <Icon size={18} />
            {label as string}
          </button>
        ))}
      </nav>
    </aside>
  )
}

function Topbar({
  backendUnavailable,
  searchTerm,
  setSearchTerm,
}: {
  backendUnavailable: boolean
  searchTerm: string
  setSearchTerm: (value: string) => void
}) {
  return (
    <header className="sticky top-0 z-10 flex min-h-16 flex-wrap items-center justify-between gap-3 border-b border-slate-200 bg-white/90 px-5 py-3 backdrop-blur lg:px-8">
      <div>
        <h1 className="text-lg font-semibold text-slate-950">Dashboard</h1>
        <p className="text-sm text-slate-500">Track uptime, latency, and service health.</p>
      </div>
      <div className="flex items-center gap-3">
        <label className="hidden items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm text-slate-500 md:flex">
          <Search size={16} />
          <input
            className="w-44 border-none bg-transparent text-slate-700 outline-none placeholder:text-slate-400"
            onChange={(event) => setSearchTerm(event.target.value)}
            placeholder="Search monitors"
            value={searchTerm}
          />
        </label>
        <button
          className="inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={backendUnavailable}
          onClick={() => document.getElementById('create-monitor')?.scrollIntoView({ behavior: 'smooth' })}
          type="button"
        >
          <Plus size={16} />
          New monitor
        </button>
      </div>
    </header>
  )
}
