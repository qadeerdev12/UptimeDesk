import { Clock3, FileText, Loader2 } from 'lucide-react'
import type { ReactNode } from 'react'
import { DetailItem } from '../../components/ui/DetailItem'
import { EmptyState } from '../../components/ui/EmptyState'
import { StateBanner } from '../../components/ui/StateBanner'
import type { Incident } from '../../types/monitor'
import { formatDateTime } from '../../utils/date'
import { IncidentStatusBadge } from './ActiveIncidentsPanel'

export function IncidentDetailPanel({
  incident,
  isFetching,
  selectedIncidentId,
}: {
  incident?: Incident
  isFetching: boolean
  selectedIncidentId?: number
}) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-5 py-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Incident detail</p>
          <h2 className="mt-1 font-semibold text-slate-950">
            {incident ? `Incident #${incident.id}` : 'Select an incident'}
          </h2>
        </div>

        {incident && <IncidentStatusBadge status={incident.status} />}
      </div>

      {isFetching && (
        <div className="px-5 pt-5">
          <StateBanner tone="info">
            <Loader2 className="animate-spin" size={16} />
            Loading incident detail.
          </StateBanner>
        </div>
      )}

      {!selectedIncidentId ? (
        <EmptyState
          title="No incident selected"
          message="Choose an active incident to inspect when it opened, which check triggered it, and whether it recovered."
        />
      ) : incident ? (
        <div className="space-y-5 p-5">
          <div>
            <h3 className="font-semibold text-slate-950">{incident.monitorName}</h3>
            <p className="mt-1 text-sm text-slate-500">Monitor #{incident.monitorId}</p>
          </div>

          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <DetailItem label="Opened" value={formatDateTime(incident.openedAt)} />
            <DetailItem label="Last checked" value={formatDateTime(incident.lastCheckedAt)} />
            <DetailItem label="Acknowledged" value={formatDateTime(incident.acknowledgedAt)} />
            <DetailItem label="Resolved" value={formatDateTime(incident.resolvedAt)} />
          </div>

          <div className="grid gap-3 sm:grid-cols-3">
            <DetailItem label="Opening check" value={`#${incident.openedByCheckResultId}`} />
            <DetailItem label="Latest check" value={formatOptionalId(incident.latestCheckResultId)} />
            <DetailItem label="Resolved by check" value={formatOptionalId(incident.resolvedByCheckResultId)} />
          </div>

          <div className="grid gap-4 lg:grid-cols-2">
            <ReasonBlock
              icon={<Clock3 size={16} />}
              label="Opening reason"
              value={incident.openingReason ?? 'No opening reason recorded.'}
            />
            <ReasonBlock
              icon={<FileText size={16} />}
              label="Resolution reason"
              value={incident.resolutionReason ?? 'Incident has not been resolved yet.'}
            />
          </div>
        </div>
      ) : (
        <EmptyState
          title="Incident unavailable"
          message="The selected incident could not be loaded. Refresh the dashboard and try again."
        />
      )}
    </section>
  )
}

function ReasonBlock({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div className="rounded-md bg-slate-50 p-4">
      <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-slate-400">
        {icon}
        {label}
      </div>
      <p className="mt-2 text-sm text-slate-700">{value}</p>
    </div>
  )
}

function formatOptionalId(value?: number) {
  return value ? `#${value}` : 'Not recorded'
}
