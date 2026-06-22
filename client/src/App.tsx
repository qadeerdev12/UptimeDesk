import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Activity, AlertTriangle, Gauge, Loader2, ShieldCheck } from 'lucide-react'
import {
  createMonitor,
  deleteMonitor,
  fetchCheckResults,
  fetchDashboardSummary,
  fetchMonitors,
  runMonitorCheck,
  updateMonitor,
} from './api/monitors'
import { AppShell } from './components/layout/AppShell'
import { MetricCard } from './components/ui/MetricCard'
import { StateBanner } from './components/ui/StateBanner'
import {
  emptyMonitorForm,
  sampleCheckResults,
  sampleMonitors,
  toFormValues,
} from './data/monitorDefaults'
import { LatencyChart } from './features/dashboard/LatencyChart'
import type { LatencyRange } from './features/dashboard/LatencyChart'
import { MonitorDetailPanel } from './features/dashboard/MonitorDetailPanel'
import { MonitorForm } from './features/dashboard/MonitorForm'
import { MonitorTable } from './features/dashboard/MonitorTable'
import { RecentResultsTable } from './features/dashboard/RecentResultsTable'
import type { CheckResult, DashboardSummary, Monitor, MonitorFormValues } from './types/monitor'
import { formatTime } from './utils/date'

function App() {
  const queryClient = useQueryClient()
  const [createForm, setCreateForm] = useState<MonitorFormValues>(emptyMonitorForm)
  const [editForm, setEditForm] = useState<MonitorFormValues>(emptyMonitorForm)
  const [selectedMonitorId, setSelectedMonitorId] = useState<number | null>(null)
  const [isEditing, setIsEditing] = useState(false)
  const [latencyRange, setLatencyRange] = useState<LatencyRange>('latest')
  const [searchTerm, setSearchTerm] = useState('')

  const monitorsQuery = useQuery({
    queryKey: ['monitors'],
    queryFn: fetchMonitors,
    retry: false,
  })

  const backendUnavailable = monitorsQuery.isError

  // When the API is unavailable, keep the dashboard useful with sample data.
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

  const dashboardSummaryQuery = useQuery({
    queryKey: ['dashboard-summary'],
    queryFn: fetchDashboardSummary,
    enabled: !backendUnavailable,
    retry: false,
  })

  const createMonitorMutation = useMutation({
    mutationFn: createMonitor,
    onSuccess: (monitor) => {
      setCreateForm(emptyMonitorForm)
      setSelectedMonitorId(monitor.id)
      queryClient.invalidateQueries({ queryKey: ['monitors'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
    },
  })

  const updateMonitorMutation = useMutation({
    mutationFn: updateMonitor,
    onSuccess: (monitor) => {
      setIsEditing(false)
      setSelectedMonitorId(monitor.id)
      queryClient.invalidateQueries({ queryKey: ['monitors'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
    },
  })

  const deleteMonitorMutation = useMutation({
    mutationFn: deleteMonitor,
    onSuccess: () => {
      setSelectedMonitorId(null)
      setIsEditing(false)
      queryClient.invalidateQueries({ queryKey: ['monitors'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
    },
  })

  const runCheckMutation = useMutation({
    mutationFn: runMonitorCheck,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['monitors'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] })
      if (selectedMonitor) {
        queryClient.invalidateQueries({ queryKey: ['check-results', selectedMonitor.id] })
      }
    },
  })

  const filteredMonitors = useMemo(
    () => filterMonitors(monitors, searchTerm),
    [monitors, searchTerm],
  )

  const stats = useMemo(
    () => calculateDashboardStats(monitors, checkResults, dashboardSummaryQuery.data),
    [checkResults, dashboardSummaryQuery.data, monitors],
  )

  const chartData = useMemo(
    () => buildLatencyChartData(checkResults, latencyRange),
    [checkResults, latencyRange],
  )

  function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    createMonitorMutation.mutate(createForm)
  }

  function handleUpdate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (selectedMonitor) {
      updateMonitorMutation.mutate({ id: selectedMonitor.id, payload: editForm })
    }
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
    <AppShell
      backendUnavailable={backendUnavailable}
      searchTerm={searchTerm}
      setSearchTerm={setSearchTerm}
    >
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
          <MetricCard icon={Activity} label="Monitors" value={stats.total.toString()} detail={`${stats.active} active`} />
          <MetricCard icon={ShieldCheck} label="Uptime" value={`${stats.uptime}%`} detail="Last 24 hours" />
          <MetricCard icon={Gauge} label="Avg latency" value={`${stats.avgLatency} ms`} detail="Last 30 days" />
          <MetricCard icon={AlertTriangle} label="Down monitors" value={stats.down.toString()} detail="Needs attention" danger={stats.down > 0} />
        </div>

        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
          <LatencyChart
            activeRange={latencyRange}
            backendUnavailable={backendUnavailable}
            chartData={chartData}
            hasSelectedMonitor={Boolean(selectedMonitor)}
            isRunningCheck={runCheckMutation.isPending}
            onRangeChange={setLatencyRange}
            onRunCheck={() => selectedMonitor && runCheckMutation.mutate(selectedMonitor.id)}
          />

          <MonitorDetailPanel
            backendUnavailable={backendUnavailable}
            deleteError={deleteMonitorMutation.error}
            editForm={editForm}
            isDeleting={deleteMonitorMutation.isPending}
            isEditing={isEditing}
            isUpdating={updateMonitorMutation.isPending}
            monitor={selectedMonitor}
            onCancelEdit={() => {
              setIsEditing(false)
              if (selectedMonitor) {
                setEditForm(toFormValues(selectedMonitor))
              }
            }}
            onDelete={handleDelete}
            onEditFormChange={setEditForm}
            onStartEdit={() => {
              if (selectedMonitor) {
                setEditForm(toFormValues(selectedMonitor))
                setIsEditing(true)
              }
            }}
            onSubmitEdit={handleUpdate}
            updateError={updateMonitorMutation.error}
          />
        </div>

        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
          <MonitorTable
            isFetching={monitorsQuery.isFetching}
            monitors={filteredMonitors}
            onSelectMonitor={(monitor) => {
              setSelectedMonitorId(monitor.id)
              setEditForm(toFormValues(monitor))
              setIsEditing(false)
            }}
            selectedMonitorId={selectedMonitor?.id}
          />

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

        <RecentResultsTable
          checkResults={checkResults}
          isFetching={checkResultsQuery.isFetching}
        />
      </section>
    </AppShell>
  )
}

function filterMonitors(monitors: Monitor[], searchTerm: string) {
  const normalizedSearch = searchTerm.trim().toLowerCase()

  if (!normalizedSearch) {
    return monitors
  }

  return monitors.filter((monitor) =>
    [monitor.name, monitor.url, monitor.status].some((value) =>
      value.toLowerCase().includes(normalizedSearch),
    ),
  )
}

function calculateDashboardStats(
  monitors: Monitor[],
  checkResults: CheckResult[],
  dashboardSummary?: DashboardSummary,
) {
  if (dashboardSummary) {
    return {
      active: dashboardSummary.activeMonitors,
      avgLatency: dashboardSummary.averageLatencyMs,
      down: dashboardSummary.downMonitors,
      total: dashboardSummary.totalMonitors,
      uptime: dashboardSummary.uptime24h,
    }
  }

  const up = monitors.filter((monitor) => monitor.status === 'UP').length
  const down = monitors.filter((monitor) => monitor.status === 'DOWN').length
  const uptime = monitors.length === 0 ? 0 : Math.round((up / monitors.length) * 10000) / 100
  const active = monitors.filter((monitor) => monitor.active).length
  const avgLatency =
    checkResults.length === 0
      ? 0
      : Math.round(
          checkResults.reduce((total, result) => total + result.responseTimeMs, 0) /
            checkResults.length,
        )

  return { active, total: monitors.length, down, uptime, avgLatency }
}

function buildLatencyChartData(checkResults: CheckResult[], range: LatencyRange) {
  return filterCheckResultsByRange(checkResults, range)
    .reverse()
    .slice(-8)
    .map((result) => ({
      time: formatTime(result.checkedAt),
      latency: result.responseTimeMs,
    }))
}

function filterCheckResultsByRange(checkResults: CheckResult[], range: LatencyRange) {
  if (range === 'latest') {
    return [...checkResults]
  }

  const now = Date.now()
  const rangeMs = {
    '24h': 24 * 60 * 60 * 1000,
    '7d': 7 * 24 * 60 * 60 * 1000,
    '30d': 30 * 24 * 60 * 60 * 1000,
  }[range]

  return checkResults.filter((result) => now - new Date(result.checkedAt).getTime() <= rangeMs)
}

export default App
