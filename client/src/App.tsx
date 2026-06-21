import { useMemo, useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Activity,
  AlertTriangle,
  BarChart3,
  Bell,
  CheckCircle2,
  Clock,
  Edit3,
  Gauge,
  Globe2,
  LayoutDashboard,
  Loader2,
  Plus,
  RefreshCw,
  Save,
  Search,
  Settings,
  ShieldCheck,
  Signal,
  Trash2,
  X,
} from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
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

type CheckResult = {
  id: number
  monitorId: number
  checkedAt: string
  statusCode?: number
  responseTimeMs: number
  status: 'SUCCESS' | 'FAILURE'
  errorMessage?: string
}

type MonitorFormValues = {
  name: string
  url: string
  method: HttpMethod
  expectedStatusCode: number
  intervalMinutes: number
  timeoutSeconds: number
  active: boolean
}

const emptyForm: MonitorFormValues = {
  name: '',
  url: '',
  method: 'GET',
  expectedStatusCode: 200,
  intervalMinutes: 5,
  timeoutSeconds: 5,
  active: true,
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
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 12).toISOString(),
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
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 8).toISOString(),
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
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 4).toISOString(),
    lastCheckedAt: new Date(Date.now() - 1000 * 60 * 2).toISOString(),
  },
]

const sampleCheckResults: CheckResult[] = [
  {
    id: 101,
    monitorId: 1,
    checkedAt: new Date(Date.now() - 1000 * 60 * 2).toISOString(),
    statusCode: 200,
    responseTimeMs: 142,
    status: 'SUCCESS',
  },
  {
    id: 102,
    monitorId: 1,
    checkedAt: new Date(Date.now() - 1000 * 60 * 7).toISOString(),
    statusCode: 200,
    responseTimeMs: 156,
    status: 'SUCCESS',
  },
  {
    id: 103,
    monitorId: 1,
    checkedAt: new Date(Date.now() - 1000 * 60 * 12).toISOString(),
    statusCode: 200,
    responseTimeMs: 149,
    status: 'SUCCESS',
  },
]

async function request<T>(url: string, options?: RequestInit) {
  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  })

  if (!response.ok) {
    const fallback = `Request failed with status ${response.status}`
    const message = await response.text().catch(() => fallback)
    throw new Error(message || fallback)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

function fetchMonitors() {
  return request<Monitor[]>('/api/monitors')
}

function createMonitor(payload: MonitorFormValues) {
  return request<Monitor>('/api/monitors', {
    method: 'POST',
    body: JSON.stringify({
      name: payload.name,
      url: payload.url,
      method: payload.method,
      expectedStatusCode: payload.expectedStatusCode,
      intervalMinutes: payload.intervalMinutes,
      timeoutSeconds: payload.timeoutSeconds,
    }),
  })
}

function updateMonitor({ id, payload }: { id: number; payload: MonitorFormValues }) {
  return request<Monitor>(`/api/monitors/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

function deleteMonitor(id: number) {
  return request<void>(`/api/monitors/${id}`, { method: 'DELETE' })
}

function runMonitorCheck(id: number) {
  return request<CheckResult>(`/api/monitors/${id}/run`, { method: 'POST' })
}

function fetchCheckResults(id: number) {
  return request<CheckResult[]>(`/api/monitors/${id}/results`)
}

function App() {
  const queryClient = useQueryClient()
  const [createForm, setCreateForm] = useState<MonitorFormValues>(emptyForm)
  const [editForm, setEditForm] = useState<MonitorFormValues>(emptyForm)
  const [selectedMonitorId, setSelectedMonitorId] = useState<number | null>(null)
  const [isEditing, setIsEditing] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')

  const monitorsQuery = useQuery({
    queryKey: ['monitors'],
    queryFn: fetchMonitors,
    retry: false,
  })

  const backendUnavailable = monitorsQuery.isError
  const monitors = useMemo(
    () => monitorsQuery.data ?? (backendUnavailable ? sampleMonitors : []),
    [backendUnavailable, monitorsQuery.data],
  )
  const selectedMonitor = monitors.find((monitor) => monitor.id === selectedMonitorId) ?? monitors[0]

  const checkResultsQuery = useQuery({
    queryKey: ['check-results', selectedMonitor?.id],
    queryFn: () => fetchCheckResults(selectedMonitor.id),
    enabled: Boolean(selectedMonitor && !backendUnavailable),
    retry: false,
  })

  const checkResults = useMemo(
    () => checkResultsQuery.data ?? (backendUnavailable ? sampleCheckResults : []),
    [backendUnavailable, checkResultsQuery.data],
  )

  const createMonitorMutation = useMutation({
    mutationFn: createMonitor,
    onSuccess: (monitor) => {
      setCreateForm(emptyForm)
      setSelectedMonitorId(monitor.id)
      queryClient.invalidateQueries({ queryKey: ['monitors'] })
    },
  })

  const updateMonitorMutation = useMutation({
    mutationFn: updateMonitor,
    onSuccess: (monitor) => {
      setIsEditing(false)
      setSelectedMonitorId(monitor.id)
      queryClient.invalidateQueries({ queryKey: ['monitors'] })
    },
  })

  const deleteMonitorMutation = useMutation({
    mutationFn: deleteMonitor,
    onSuccess: () => {
      setSelectedMonitorId(null)
      setIsEditing(false)
      queryClient.invalidateQueries({ queryKey: ['monitors'] })
    },
  })

  const runCheckMutation = useMutation({
    mutationFn: runMonitorCheck,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['monitors'] })
      if (selectedMonitor) {
        queryClient.invalidateQueries({ queryKey: ['check-results', selectedMonitor.id] })
      }
    },
  })

  const filteredMonitors = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase()

    if (!normalizedSearch) {
      return monitors
    }

    return monitors.filter((monitor) =>
      [monitor.name, monitor.url, monitor.status].some((value) =>
        value.toLowerCase().includes(normalizedSearch),
      ),
    )
  }, [monitors, searchTerm])

  const stats = useMemo(() => {
    const up = monitors.filter((monitor) => monitor.status === 'UP').length
    const down = monitors.filter((monitor) => monitor.status === 'DOWN').length
    const uptime = monitors.length === 0 ? 0 : Math.round((up / monitors.length) * 10000) / 100
    const avgLatency =
      checkResults.length === 0
        ? 0
        : Math.round(
            checkResults.reduce((total, result) => total + result.responseTimeMs, 0) /
              checkResults.length,
          )

    return { total: monitors.length, up, down, uptime, avgLatency }
  }, [checkResults, monitors])

  const chartData = useMemo(() => {
    if (checkResults.length === 0) {
      return [
        { time: '09:00', latency: 142 },
        { time: '10:00', latency: 156 },
        { time: '11:00', latency: 149 },
        { time: '12:00', latency: 181 },
        { time: '13:00', latency: 176 },
        { time: '14:00', latency: 214 },
      ]
    }

    return [...checkResults]
      .reverse()
      .slice(-8)
      .map((result) => ({
        time: formatTime(result.checkedAt),
        latency: result.responseTimeMs,
      }))
  }, [checkResults])

  function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    createMonitorMutation.mutate(createForm)
  }

  function handleUpdate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!selectedMonitor) {
      return
    }

    updateMonitorMutation.mutate({ id: selectedMonitor.id, payload: editForm })
  }

  function handleDelete() {
    if (!selectedMonitor) {
      return
    }

    const shouldDelete = window.confirm(`Delete ${selectedMonitor.name}? This cannot be undone.`)

    if (shouldDelete) {
      deleteMonitorMutation.mutate(selectedMonitor.id)
    }
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

        <section className="space-y-6 p-5 lg:p-8">
          {backendUnavailable && (
            <StateBanner tone="warning">
              Showing sample monitors because the Spring Boot backend is not reachable. Start the backend
              from <code>server/</code> with <code>./mvnw spring-boot:run</code> to manage real data.
            </StateBanner>
          )}

          {monitorsQuery.isLoading && (
            <StateBanner tone="info">
              <Loader2 className="animate-spin" size={16} />
              Loading monitors from the API.
            </StateBanner>
          )}

          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <MetricCard icon={Activity} label="Monitors" value={stats.total.toString()} detail={`${stats.up} healthy`} />
            <MetricCard icon={ShieldCheck} label="Uptime" value={`${stats.uptime}%`} detail="Current monitor health" />
            <MetricCard icon={Gauge} label="Avg latency" value={`${stats.avgLatency} ms`} detail="Selected monitor" />
            <MetricCard icon={AlertTriangle} label="Active incidents" value={stats.down.toString()} detail="Needs attention" danger={stats.down > 0} />
          </div>

          <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
            <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
              <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h2 className="font-semibold text-slate-950">Response time</h2>
                  <p className="text-sm text-slate-500">Recent latency for the selected monitor.</p>
                </div>
                <button
                  className="inline-flex items-center gap-2 rounded-md border border-slate-200 px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                  disabled={!selectedMonitor || backendUnavailable || runCheckMutation.isPending}
                  onClick={() => selectedMonitor && runCheckMutation.mutate(selectedMonitor.id)}
                  type="button"
                >
                  {runCheckMutation.isPending ? <Loader2 className="animate-spin" size={15} /> : <RefreshCw size={15} />}
                  Run check
                </button>
              </div>

              <div className="h-72">
                <ResponsiveContainer height="100%" width="100%">
                  <AreaChart data={chartData}>
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
              {selectedMonitor ? (
                <>
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Selected monitor</p>
                      <h2 className="mt-1 font-semibold text-slate-950">{selectedMonitor.name}</h2>
                      <p className="mt-1 break-all text-sm text-slate-500">{selectedMonitor.url}</p>
                    </div>
                    <StatusBadge status={selectedMonitor.status} />
                  </div>

                  {isEditing ? (
                    <MonitorForm
                      actionLabel="Save changes"
                      disabled={backendUnavailable || updateMonitorMutation.isPending}
                      error={updateMonitorMutation.error}
                      form={editForm}
                      isSaving={updateMonitorMutation.isPending}
                      onCancel={() => {
                        setIsEditing(false)
                        setEditForm(toFormValues(selectedMonitor))
                      }}
                      onChange={setEditForm}
                      onSubmit={handleUpdate}
                      showActive
                    />
                  ) : (
                    <>
                      <div className="mt-5 grid grid-cols-2 gap-3 text-sm">
                        <DetailItem label="Method" value={selectedMonitor.method} />
                        <DetailItem label="Expected" value={selectedMonitor.expectedStatusCode.toString()} />
                        <DetailItem label="Interval" value={`${selectedMonitor.intervalMinutes} min`} />
                        <DetailItem label="Timeout" value={`${selectedMonitor.timeoutSeconds} sec`} />
                        <DetailItem label="Active" value={selectedMonitor.active ? 'Yes' : 'No'} />
                        <DetailItem label="Last check" value={formatDateTime(selectedMonitor.lastCheckedAt)} />
                      </div>

                      <div className="mt-5 flex flex-wrap gap-3">
                        <button
                          className="inline-flex items-center gap-2 rounded-md border border-slate-200 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                          onClick={() => {
                            setEditForm(toFormValues(selectedMonitor))
                            setIsEditing(true)
                          }}
                          type="button"
                        >
                          <Edit3 size={15} />
                          Edit
                        </button>
                        <button
                          className="inline-flex items-center gap-2 rounded-md border border-red-200 px-3 py-2 text-sm font-semibold text-red-700 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-60"
                          disabled={backendUnavailable || deleteMonitorMutation.isPending}
                          onClick={handleDelete}
                          type="button"
                        >
                          {deleteMonitorMutation.isPending ? <Loader2 className="animate-spin" size={15} /> : <Trash2 size={15} />}
                          Delete
                        </button>
                      </div>

                      {deleteMonitorMutation.error && (
                        <p className="mt-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
                          {deleteMonitorMutation.error.message}
                        </p>
                      )}
                    </>
                  )}
                </>
              ) : (
                <EmptyState
                  title="No monitor selected"
                  message="Create a monitor or select one from the table to inspect its configuration."
                />
              )}
            </section>
          </div>

          <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
            <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
              <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-5 py-4">
                <div>
                  <h2 className="font-semibold text-slate-950">Monitored services</h2>
                  <p className="text-sm text-slate-500">Select a service to view, edit, run, or delete it.</p>
                </div>
                {monitorsQuery.isFetching && <Loader2 className="animate-spin text-slate-400" size={18} />}
              </div>

              {filteredMonitors.length === 0 ? (
                <EmptyState
                  message="Add your first API endpoint to start collecting uptime and latency data."
                  title="No monitors yet"
                />
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[860px] text-left text-sm">
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
                      {filteredMonitors.map((monitor) => (
                        <tr
                          className={`cursor-pointer hover:bg-slate-50 ${
                            selectedMonitor?.id === monitor.id ? 'bg-blue-50/70' : ''
                          }`}
                          key={monitor.id}
                          onClick={() => {
                            setSelectedMonitorId(monitor.id)
                            setEditForm(toFormValues(monitor))
                            setIsEditing(false)
                          }}
                        >
                          <td className="px-5 py-4 font-medium text-slate-950">{monitor.name}</td>
                          <td className="max-w-[320px] truncate px-5 py-4 text-slate-500">{monitor.url}</td>
                          <td className="px-5 py-4">
                            <StatusBadge status={monitor.status} />
                          </td>
                          <td className="px-5 py-4 text-slate-600">
                            {monitor.method} {monitor.expectedStatusCode}
                          </td>
                          <td className="px-5 py-4 text-slate-600">{monitor.intervalMinutes} min</td>
                          <td className="px-5 py-4 text-slate-500">{formatDateTime(monitor.lastCheckedAt)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>

            <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm" id="create-monitor">
              <h2 className="font-semibold text-slate-950">Create monitor</h2>
              <p className="mt-1 text-sm text-slate-500">Add a health endpoint for one of your projects.</p>

              <MonitorForm
                actionLabel="Save monitor"
                disabled={backendUnavailable || createMonitorMutation.isPending}
                error={createMonitorMutation.error}
                form={createForm}
                isSaving={createMonitorMutation.isPending}
                onChange={setCreateForm}
                onSubmit={handleCreate}
              />
            </section>
          </div>

          <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
            <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 className="font-semibold text-slate-950">Recent check results</h2>
                <p className="text-sm text-slate-500">Latest checks for the selected monitor.</p>
              </div>
              {checkResultsQuery.isFetching && <Loader2 className="animate-spin text-slate-400" size={18} />}
            </div>

            {checkResults.length === 0 ? (
              <EmptyState
                message="Run a check or wait for the scheduler to collect the first result."
                title="No check results yet"
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full min-w-[720px] text-left text-sm">
                  <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                    <tr>
                      <th className="px-4 py-3">Checked at</th>
                      <th className="px-4 py-3">Result</th>
                      <th className="px-4 py-3">Status code</th>
                      <th className="px-4 py-3">Latency</th>
                      <th className="px-4 py-3">Error</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {checkResults.map((result) => (
                      <tr key={result.id}>
                        <td className="px-4 py-3 text-slate-600">{formatDateTime(result.checkedAt)}</td>
                        <td className="px-4 py-3">
                          <ResultBadge status={result.status} />
                        </td>
                        <td className="px-4 py-3 text-slate-600">{result.statusCode ?? '-'}</td>
                        <td className="px-4 py-3 text-slate-600">{result.responseTimeMs} ms</td>
                        <td className="max-w-[360px] truncate px-4 py-3 text-slate-500">
                          {result.errorMessage ?? '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </section>
      </main>
    </div>
  )
}

function MonitorForm({
  actionLabel,
  disabled,
  error,
  form,
  isSaving,
  onCancel,
  onChange,
  onSubmit,
  showActive = false,
}: {
  actionLabel: string
  disabled: boolean
  error: Error | null
  form: MonitorFormValues
  isSaving: boolean
  onCancel?: () => void
  onChange: (form: MonitorFormValues) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  showActive?: boolean
}) {
  return (
    <form className="mt-5 space-y-4" onSubmit={onSubmit}>
      <Field label="Monitor name">
        <input
          className="input"
          onChange={(event) => onChange({ ...form, name: event.target.value })}
          placeholder="Portfolio API"
          required
          value={form.name}
        />
      </Field>
      <Field label="Endpoint URL">
        <input
          className="input"
          onChange={(event) => onChange({ ...form, url: event.target.value })}
          placeholder="https://example.com/api/health"
          required
          type="url"
          value={form.url}
        />
      </Field>
      <div className="grid grid-cols-2 gap-3">
        <Field label="Method">
          <select
            className="input"
            onChange={(event) => onChange({ ...form, method: event.target.value as HttpMethod })}
            value={form.method}
          >
            {['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD'].map((method) => (
              <option key={method}>{method}</option>
            ))}
          </select>
        </Field>
        <Field label="Expected">
          <input
            className="input"
            max={599}
            min={100}
            onChange={(event) => onChange({ ...form, expectedStatusCode: Number(event.target.value) })}
            required
            type="number"
            value={form.expectedStatusCode}
          />
        </Field>
      </div>
      <div className="grid grid-cols-2 gap-3">
        <Field label="Interval min">
          <input
            className="input"
            min={1}
            onChange={(event) => onChange({ ...form, intervalMinutes: Number(event.target.value) })}
            required
            type="number"
            value={form.intervalMinutes}
          />
        </Field>
        <Field label="Timeout sec">
          <input
            className="input"
            min={1}
            onChange={(event) => onChange({ ...form, timeoutSeconds: Number(event.target.value) })}
            required
            type="number"
            value={form.timeoutSeconds}
          />
        </Field>
      </div>

      {showActive && (
        <label className="flex items-center justify-between rounded-md border border-slate-200 px-3 py-2 text-sm">
          <span>
            <span className="block font-medium text-slate-800">Active monitor</span>
            <span className="text-slate-500">Include this endpoint in scheduled checks.</span>
          </span>
          <input
            checked={form.active}
            className="h-4 w-4"
            onChange={(event) => onChange({ ...form, active: event.target.checked })}
            type="checkbox"
          />
        </label>
      )}

      {error && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
          {error.message}
        </p>
      )}

      <div className="flex gap-3">
        <button
          className="flex flex-1 items-center justify-center gap-2 rounded-md bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={disabled}
          type="submit"
        >
          {isSaving ? <Loader2 className="animate-spin" size={16} /> : <Save size={16} />}
          {actionLabel}
        </button>
        {onCancel && (
          <button
            className="inline-flex items-center gap-2 rounded-md border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            onClick={onCancel}
            type="button"
          >
            <X size={16} />
            Cancel
          </button>
        )}
      </div>
    </form>
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
  icon: LucideIcon
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

function Field({ children, label }: { children: ReactNode; label: string }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium text-slate-700">{label}</span>
      {children}
    </label>
  )
}

function DetailItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-md bg-slate-50 px-3 py-2">
      <p className="text-xs font-medium uppercase tracking-wide text-slate-400">{label}</p>
      <p className="mt-1 break-all font-semibold text-slate-800">{value}</p>
    </div>
  )
}

function EmptyState({ message, title }: { message: string; title: string }) {
  return (
    <div className="p-8 text-center">
      <p className="font-semibold text-slate-900">{title}</p>
      <p className="mx-auto mt-1 max-w-md text-sm text-slate-500">{message}</p>
    </div>
  )
}

function StateBanner({ children, tone }: { children: ReactNode; tone: 'info' | 'warning' }) {
  const classes =
    tone === 'warning'
      ? 'border-amber-200 bg-amber-50 text-amber-900'
      : 'border-blue-200 bg-blue-50 text-blue-900'

  return (
    <div className={`flex flex-wrap items-center gap-2 rounded-md border px-4 py-3 text-sm ${classes}`}>
      {children}
    </div>
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

function ResultBadge({ status }: { status: CheckResult['status'] }) {
  if (status === 'SUCCESS') {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700">
        <CheckCircle2 size={13} />
        Success
      </span>
    )
  }

  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-700">
      <AlertTriangle size={13} />
      Failure
    </span>
  )
}

function toFormValues(monitor: Monitor): MonitorFormValues {
  return {
    name: monitor.name,
    url: monitor.url,
    method: monitor.method,
    expectedStatusCode: monitor.expectedStatusCode,
    intervalMinutes: monitor.intervalMinutes,
    timeoutSeconds: monitor.timeoutSeconds,
    active: monitor.active,
  }
}

function formatDateTime(value?: string) {
  if (!value) {
    return 'Not checked'
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat(undefined, {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

export default App
