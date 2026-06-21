import { AlertTriangle, CheckCircle2 } from 'lucide-react'
import type { CheckStatus, MonitorStatus } from '../../types/monitor'

export function StatusBadge({ status }: { status: MonitorStatus }) {
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

export function ResultBadge({ status }: { status: CheckStatus }) {
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
