import { Loader2 } from 'lucide-react'
import { StatusBadge } from '../../components/ui/Badges'
import { EmptyState } from '../../components/ui/EmptyState'
import type { Monitor } from '../../types/monitor'
import { formatDateTime } from '../../utils/date'

export function MonitorTable({
  isFetching,
  monitors,
  onSelectMonitor,
  selectedMonitorId,
}: {
  isFetching: boolean
  monitors: Monitor[]
  onSelectMonitor: (monitor: Monitor) => void
  selectedMonitorId?: number
}) {
  return (
    <section className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 px-5 py-4">
        <div>
          <h2 className="font-semibold text-slate-950">Monitored services</h2>
          <p className="text-sm text-slate-500">Select a service to view, edit, run, or delete it.</p>
        </div>
        {isFetching && <Loader2 className="animate-spin text-slate-400" size={18} />}
      </div>

      {monitors.length === 0 ? (
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
              {monitors.map((monitor) => (
                <tr
                  className={`cursor-pointer hover:bg-slate-50 ${
                    selectedMonitorId === monitor.id ? 'bg-blue-50/70' : ''
                  }`}
                  key={monitor.id}
                  onClick={() => onSelectMonitor(monitor)}
                >
                  <td className="px-5 py-4 font-medium text-slate-950">{monitor.name}</td>
                  <td className="max-w-[320px] truncate px-5 py-4 text-slate-500">{monitor.url}</td>
                  <td className="px-5 py-4">
                    <StatusBadge monitor={monitor} />
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
  )
}
