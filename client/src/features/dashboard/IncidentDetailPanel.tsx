import { BellRing, CheckCircle2, Clock3, FileText, Loader2, RadioTower } from 'lucide-react'
import type { ReactNode } from 'react'
import { DetailItem } from '../../components/ui/DetailItem'
import { EmptyState } from '../../components/ui/EmptyState'
import { StateBanner } from '../../components/ui/StateBanner'
import type { Incident, IncidentTimelineEvent } from '../../types/monitor'
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

          <IncidentTimeline events={incident.timelineEvents ?? []} />
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

function IncidentTimeline({ events }: { events: IncidentTimelineEvent[] }) {
  if (events.length === 0) {
    return (
      <div className="rounded-md border border-slate-200 p-4">
        <p className="font-semibold text-slate-950">Timeline</p>
        <p className="mt-1 text-sm text-slate-500">No timeline events have been recorded yet.</p>
      </div>
    )
  }

  return (
    <div>
      <p className="font-semibold text-slate-950">Timeline</p>
      <div className="mt-4 space-y-4">
        {events.map((event, index) => (
          <div className="relative flex gap-3" key={`${event.type}-${event.occurredAt}-${index}`}>
            <div className="flex flex-col items-center">
              <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-slate-100 text-slate-600">
                {timelineIcon(event.type)}
              </span>
              {index < events.length - 1 && <span className="mt-2 h-full w-px flex-1 bg-slate-200" />}
            </div>

            <div className="min-w-0 pb-2">
              <div className="flex flex-wrap items-center gap-2">
                <p className="font-medium text-slate-900">{event.label}</p>
                {event.checkResultId && (
                  <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-semibold text-slate-500">
                    Check #{event.checkResultId}
                  </span>
                )}
              </div>
              <p className="mt-1 text-xs text-slate-500">{formatDateTime(event.occurredAt)}</p>
              {event.message && <p className="mt-2 text-sm text-slate-600">{event.message}</p>}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

function timelineIcon(type: IncidentTimelineEvent['type']) {
  if (type === 'ACKNOWLEDGED') {
    return <BellRing size={15} />
  }

  if (type === 'RESOLVED') {
    return <CheckCircle2 size={15} />
  }

  if (type === 'LATEST_CHECK') {
    return <RadioTower size={15} />
  }

  return <Clock3 size={15} />
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
