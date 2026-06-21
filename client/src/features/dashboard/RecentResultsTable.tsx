import { Loader2 } from 'lucide-react'
import { ResultBadge } from '../../components/ui/Badges'
import { EmptyState } from '../../components/ui/EmptyState'
import type { CheckResult } from '../../types/monitor'
import { formatDateTime } from '../../utils/date'

export function RecentResultsTable({
  checkResults,
  isFetching,
}: {
  checkResults: CheckResult[]
  isFetching: boolean
}) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="font-semibold text-slate-950">Recent check results</h2>
          <p className="text-sm text-slate-500">Latest checks for the selected monitor.</p>
        </div>
        {isFetching && <Loader2 className="animate-spin text-slate-400" size={18} />}
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
  )
}
