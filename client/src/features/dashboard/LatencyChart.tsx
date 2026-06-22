import { Loader2, RefreshCw } from 'lucide-react'
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

export type LatencyPoint = {
  time: string
  latency: number
}

export function LatencyChart({
  backendUnavailable,
  chartData,
  hasSelectedMonitor,
  isRunningCheck,
  onRunCheck,
}: {
  backendUnavailable: boolean
  chartData: LatencyPoint[]
  hasSelectedMonitor: boolean
  isRunningCheck: boolean
  onRunCheck: () => void
}) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="font-semibold text-slate-950">Response time</h2>
          <p className="text-sm text-slate-500">Recent latency for the selected monitor.</p>
        </div>
        <button
          className="inline-flex items-center gap-2 rounded-md border border-slate-200 px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={!hasSelectedMonitor || backendUnavailable || isRunningCheck}
          onClick={onRunCheck}
          type="button"
        >
          {isRunningCheck ? <Loader2 className="animate-spin" size={15} /> : <RefreshCw size={15} />}
          Run check
        </button>
      </div>

      <div className="h-72">
        {chartData.length === 0 ? (
          <div className="flex h-full items-center justify-center rounded-md border border-dashed border-slate-200 bg-slate-50 px-4 text-center">
            <div>
              <p className="font-medium text-slate-700">No latency data yet</p>
              <p className="mt-1 text-sm text-slate-500">Run a check to start plotting real response times.</p>
            </div>
          </div>
        ) : (
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
        )}
      </div>
    </section>
  )
}
