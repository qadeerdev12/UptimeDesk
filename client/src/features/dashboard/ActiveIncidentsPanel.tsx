import { AlertTriangle, BellRing, CheckCircle2, Loader2 } from 'lucide-react'
import { EmptyState } from '../../components/ui/EmptyState'
import type { Incident } from '../../types/monitor'
import { formatDateTime } from '../../utils/date'

export function ActiveIncidentsPanel({
  incidents,
  isFetching,
}: {
  incidents: Incident[]
  isFetching: boolean
}) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-5 py-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Incidents</p>
          <h2 className="mt-1 font-semibold text-slate-950">Active incidents</h2>
        </div>

        {isFetching && (
          <span className="inline-flex items-center gap-2 rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700">
            <Loader2 className="animate-spin" size={13} />
            Refreshing
          </span>
        )}
      </div>

      {incidents.length === 0 ? (
        <EmptyState
          title="No active incidents"
          message="Open and acknowledged incidents will appear here when a monitor repeatedly fails."
        />
      ) : (
        <div className="divide-y divide-slate-100">
          {incidents.map((incident) => (
            <article className="p-5" key={incident.id}>
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <IncidentStatusBadge status={incident.status} />
                    <span className="text-xs font-medium text-slate-400">Incident #{incident.id}</span>
                  </div>
                  <h3 className="mt-2 font-semibold text-slate-950">Monitor #{incident.monitorId}</h3>
                </div>

                <div className="text-right text-xs text-slate-500">
                  <p className="font-medium text-slate-700">Opened</p>
                  <p>{formatDateTime(incident.openedAt)}</p>
                </div>
              </div>

              <p className="mt-3 text-sm text-slate-600">
                {incident.openingReason ?? 'The monitor crossed its configured failure threshold.'}
              </p>

              <dl className="mt-4 grid gap-3 text-sm sm:grid-cols-2">
                <div className="rounded-md bg-slate-50 px-3 py-2">
                  <dt className="text-xs font-semibold uppercase tracking-wide text-slate-400">Last checked</dt>
                  <dd className="mt-1 text-slate-700">{formatDateTime(incident.lastCheckedAt)}</dd>
                </div>
                <div className="rounded-md bg-slate-50 px-3 py-2">
                  <dt className="text-xs font-semibold uppercase tracking-wide text-slate-400">Latest check</dt>
                  <dd className="mt-1 text-slate-700">
                    {incident.latestCheckResultId ? `#${incident.latestCheckResultId}` : 'Not recorded'}
                  </dd>
                </div>
              </dl>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}

function IncidentStatusBadge({ status }: { status: Incident['status'] }) {
  if (status === 'ACKNOWLEDGED') {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-amber-50 px-2.5 py-1 text-xs font-semibold text-amber-700">
        <BellRing size={13} />
        Acknowledged
      </span>
    )
  }

  if (status === 'OPEN') {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-red-50 px-2.5 py-1 text-xs font-semibold text-red-700">
        <AlertTriangle size={13} />
        Open
      </span>
    )
  }

  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-semibold text-emerald-700">
      <CheckCircle2 size={13} />
      Resolved
    </span>
  )
}
