import { BarChart3, Clock } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'

export function MetricCard({
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
