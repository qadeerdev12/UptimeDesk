import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Activity,
  AlertTriangle,
  BarChart3,
  Bell,
  CheckCircle2,
  Clock,
  Gauge,
  Globe2,
  LayoutDashboard,
  Loader2,
  Plus,
  RefreshCw,
  Search,
  Settings,
  ShieldCheck,
  Signal,
} from 'lucide-react'
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'HEAD'
type MonitorStatus = 'UNKNOWN' | 'UP' | 'DOWN'

type Monitor = {
  id: number
  name: string
  url: string
  method: HttpMethod
  expectedStatusCode: number
  intervalMinutes: number
  timeoutSeconds: number
  active: boolean
  status: MonitorStatus
  createdAt: string
  lastCheckedAt?: string
}

type CreateMonitorPayload = {
  name: string
  url: string
  method: HttpMethod
  expectedStatusCode: number
  intervalMinutes: number
  timeoutSeconds: number
}

const sampleMonitors: Monitor[] = [
  {
    id: 1,
    name: 'Portfolio Website',
    url: 'https://qadeerdev.com',
    method: 'GET',
    expectedStatusCode: 200,
    intervalMinutes: 5,
    timeoutSeconds: 5,
    active: true,
    status: 'UP',
    createdAt: new Date().toISOString(),
    lastCheckedAt: new Date().toISOString(),
  },
  {
    id: 2,
    name: 'Job Tracker API',
    url: 'https://jobtracker.example.com/api/health',
    method: 'GET',
    expectedStatusCode: 200,
    intervalMinutes: 5,
    timeoutSeconds: 5,
    active: true,
    status: 'UP',
    createdAt: new Date().toISOString(),
    lastCheckedAt: new Date(Date.now() - 1000 * 60 * 8).toISOString(),
  },
  {
    id: 3,
    name: 'Kanban API',
    url: 'https://kanban.example.com/api/health',
    method: 'GET',
    expectedStatusCode: 200,
    intervalMinutes: 1,
    timeoutSeconds: 5,
    active: true,
    status: 'DOWN',
    createdAt: new Date().toISOString(),
    lastCheckedAt: new Date(Date.now() - 1000 * 60 * 2).toISOString(),
  },
]

const latencyData = [
  { time: '09:00', latency: 142 },
  { time: '10:00', latency: 156 },
  { time: '11:00', latency: 149 },
  { time: '12:00', latency: 181 },
  { time: '13:00', latency: 176 },
  { time: '14:00', latency: 214 },
  { time: '15:00', latency: 198 },
  { time: '16:00', latency: 238 },
]

async function fetchMonitors() {
  const response = await fetch('/api/monitors')

  if (!response.ok) {
    throw new Error('Unable to load monitors')
  }

  return response.json() as Promise<Monitor[]>
}

async function createMonitor(payload: CreateMonitorPayload) {
  const response = await fetch('/api/monitors', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    throw new Error('Unable to create monitor')
  }

  return response.json() as Promise<Monitor>
}

function App() {
  const queryClient = useQueryClient()
  const [form, setForm] = useState<CreateMonitorPayload>({
    name: '',
    url: '',
    method: 'GET',
    expectedStatusCode: 200,
    intervalMinutes: 5,
    timeoutSeconds: 5,
  })

  const monitorsQuery = useQuery({
    queryKey: ['monitors'],
    queryFn: fetchMonitors,
    retry: false,
  })

  const monitors = monitorsQuery.data ?? sampleMonitors
  const usingSampleData = !monitorsQuery.data

  const createMonitorMutation = useMutation({
    mutationFn: createMonitor,
    onSuccess: () => {
      setForm({
        name: '',
        url: '',
        method: 'GET',
        expectedStatusCode: 200,
        intervalMinutes: 5,
        timeoutSeconds: 5,
      })
      queryClient.invalidateQueries({ queryKey: ['monitors'] })
    },
  })

  const stats = useMemo(() => {
    const up = monitors.filter((monitor) => monitor.status === 'UP').length
    const down = monitors.filter((monitor) => monitor.status === 'DOWN').length
    const uptime = monitors.length === 0 ? 0 : Math.round((up / monitors.length) * 10000) / 100

    return {
      total: monitors.length,
      up,
      down,
      uptime,
      avgLatency: 189,
    }
  }, [monitors])

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    createMonitorMutation.mutate(form)
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
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

      <main className="lg:pl-64">
        <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b border-slate-200 bg-white/90 px-5 backdrop-blur lg:px-8">
          <div>
            <h1 className="text-lg font-semibold text-slate-950">Dashboard</h1>
            <p className="text-sm text-slate-500">Track uptime, latency, and service health.</p>
          </div>
          <div className="flex items-center gap-3">
            <div className="hidden items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm text-slate-500 md:flex">
              <Search size={16} />
              Search monitors
            </div>
            <button className="inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700">
              <Plus size={16} />
              New monitor
            </button>
          </div>
        </header>

        <section className="space-y-6 p-5 lg:p-8">
          {usingSampleData && (
            <div className="rounded-md border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
              Showing sample monitors. Start the Spring Boot backend with <code>./mvnw spring-boot:run</code> to load real data.
            </div>
          )}

          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <MetricCard icon={Activity} label="Monitors" value={stats.total.toString()} detail={`${stats.up} healthy`} />
            <MetricCard icon={ShieldCheck} label="Uptime" value={`${stats.uptime}%`} detail="Current monitor health" />
            <MetricCard icon={Gauge} label="Avg latency" value={`${stats.avgLatency} ms`} detail="Last 24 hours" />
            <MetricCard icon={AlertTriangle} label="Active incidents" value={stats.down.toString()} detail="Needs attention" danger={stats.down > 0} />
          </div>

          <div className="grid gap-6 xl:grid-cols-[1fr_380px]">
            <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
              <div className="mb-5 flex items-center justify-between">
                <div>
                  <h2 className="font-semibold text-slate-950">Response time</h2>
                  <p className="text-sm text-slate-500">Average latency across monitored endpoints.</p>
                </div>
                <button className="inline-flex items-center gap-2 rounded-md border border-slate-200 px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50">
                  <RefreshCw size={15} />
                  Refresh
                </button>
              </div>

              <div className="h-72">
                <ResponsiveContainer height="100%" width="100%">
                  <AreaChart data={latencyData}>
                    <defs>
                      <linearGradient id="latency" x1="0" x2="0" y1="0" y2="1">
                        <stop offset="5%" stopColor="#2563eb" stopOpacity={0.25} />
                        <stop offset="95%" stopColor="#2563eb" stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid stroke="#e2e8f0" strokeDasharray="3 3" vertical={false} />
                    <XAxis axisLine={false} dataKey="time" tickLine={false} />
                    <YAxis axisLine={false} tickLine={false} />
                    <Tooltip />
                    <Area dataKey="latency" fill="url(#latency)" stroke="#2563eb" strokeWidth={2} type="monotone" />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </section>

            <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
              <h2 className="font-semibold text-slate-950">Create monitor</h2>
              <p className="mt-1 text-sm text-slate-500">Add a health endpoint for one of your projects.</p>

              <form className="mt-5 space-y-4" onSubmit={handleSubmit}>
                <Field label="Monitor name">
                  <input
                    className="input"
                    placeholder="Portfolio API"
                    value={form.name}
                    onChange={(event) => setForm({ ...form, name: event.target.value })}
                  />
                </Field>
                <Field label="Endpoint URL">
                  <input
                    className="input"
                    placeholder="https://example.com/api/health"
                    value={form.url}
                    onChange={(event) => setForm({ ...form, url: event.target.value })}
                  />
                </Field>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Method">
                    <select
                      className="input"
                      value={form.method}
                      onChange={(event) => setForm({ ...form, method: event.target.value as HttpMethod })}
                    >
                      {['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD'].map((method) => (
                        <option key={method}>{method}</option>
                      ))}
                    </select>
                  </Field>
                  <Field label="Expected">
                    <input
                      className="input"
                      type="number"
                      value={form.expectedStatusCode}
                      onChange={(event) => setForm({ ...form, expectedStatusCode: Number(event.target.value) })}
                    />
                  </Field>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Interval min">
                    <input
                      className="input"
                      min={1}
                      type="number"
                      value={form.intervalMinutes}
                      onChange={(event) => setForm({ ...form, intervalMinutes: Number(event.target.value) })}
                    />
                  </Field>
                  <Field label="Timeout sec">
                    <input
                      className="input"
                      min={1}
                      type="number"
                      value={form.timeoutSeconds}
                      onChange={(event) => setForm({ ...form, timeoutSeconds: Number(event.target.value) })}
                    />
                  </Field>
                </div>

                <button
                  className="flex w-full items-center justify-center gap-2 rounded-md bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                  disabled={createMonitorMutation.isPending || usingSampleData}
                  type="submit"
                >
                  {createMonitorMutation.isPending ? <Loader2 className="animate-spin" size={16} /> : <Plus size={16} />}
                  Save monitor
                </button>
              </form>
            </section>
          </div>

          <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
            <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
              <div>
                <h2 className="font-semibold text-slate-950">Monitored services</h2>
                <p className="text-sm text-slate-500">Your project endpoints and their latest status.</p>
              </div>
              {monitorsQuery.isFetching && <Loader2 className="animate-spin text-slate-400" size={18} />}
            </div>

            <div className="overflow-x-auto">
              <table className="w-full min-w-[760px] text-left text-sm">
                <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                  <tr>
                    <th className="px-5 py-3">Service</th>
                    <th className="px-5 py-3">URL</th>
                    <th className="px-5 py-3">Status</th>
                    <th className="px-5 py-3">Expected</th>
                    <th className="px-5 py-3">Interval</th>
                    <th className="px-5 py-3">Last check</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {monitors.map((monitor) => (
                    <tr className="hover:bg-slate-50" key={monitor.id}>
                      <td className="px-5 py-4 font-medium text-slate-950">{monitor.name}</td>
                      <td className="max-w-[300px] truncate px-5 py-4 text-slate-500">{monitor.url}</td>
                      <td className="px-5 py-4">
                        <StatusBadge status={monitor.status} />
                      </td>
                      <td className="px-5 py-4 text-slate-600">
                        {monitor.method} {monitor.expectedStatusCode}
                      </td>
                      <td className="px-5 py-4 text-slate-600">{monitor.intervalMinutes} min</td>
                      <td className="px-5 py-4 text-slate-500">
                        {monitor.lastCheckedAt ? new Date(monitor.lastCheckedAt).toLocaleTimeString() : 'Not checked'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </section>
      </main>
    </div>
  )
}

function MetricCard({
  danger = false,
  detail,
  icon: Icon,
  label,
  value,
}: {
  danger?: boolean
  detail: string
  icon: typeof Activity
  label: string
  value: string
}) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <div className={`flex h-10 w-10 items-center justify-center rounded-md ${danger ? 'bg-red-50 text-red-600' : 'bg-blue-50 text-blue-600'}`}>
          <Icon size={19} />
        </div>
        <BarChart3 className="text-slate-300" size={18} />
      </div>
      <p className="mt-5 text-sm font-medium text-slate-500">{label}</p>
      <p className="mt-1 text-3xl font-semibold tracking-tight text-slate-950">{value}</p>
      <p className="mt-1 flex items-center gap-1 text-sm text-slate-500">
        <Clock size={14} />
        {detail}
      </p>
    </div>
  )
}

function Field({ children, label }: { children: React.ReactNode; label: string }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium text-slate-700">{label}</span>
      {children}
    </label>
  )
}

function StatusBadge({ status }: { status: MonitorStatus }) {
  if (status === 'UP') {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700">
        <CheckCircle2 size={13} />
        Operational
      </span>
    )
  }

  if (status === 'DOWN') {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-700">
        <AlertTriangle size={13} />
        Down
      </span>
    )
  }

  return <span className="inline-flex rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-600">Unknown</span>
}

export default App
