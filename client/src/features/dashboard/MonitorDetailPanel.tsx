import type { FormEvent } from 'react'
import { Edit3, Loader2, Trash2 } from 'lucide-react'
import { StatusBadge } from '../../components/ui/Badges'
import { DetailItem } from '../../components/ui/DetailItem'
import { EmptyState } from '../../components/ui/EmptyState'
import type { Monitor, MonitorFormValues } from '../../types/monitor'
import { formatDateTime } from '../../utils/date'
import { MonitorForm } from './MonitorForm'

export function MonitorDetailPanel({
  backendUnavailable,
  deleteError,
  editForm,
  isDeleting,
  isEditing,
  isUpdating,
  monitor,
  onCancelEdit,
  onDelete,
  onEditFormChange,
  onStartEdit,
  onSubmitEdit,
  updateError,
}: {
  backendUnavailable: boolean
  deleteError: Error | null
  editForm: MonitorFormValues
  isDeleting: boolean
  isEditing: boolean
  isUpdating: boolean
  monitor?: Monitor
  onCancelEdit: () => void
  onDelete: () => void
  onEditFormChange: (form: MonitorFormValues) => void
  onStartEdit: () => void
  onSubmitEdit: (event: FormEvent<HTMLFormElement>) => void
  updateError: Error | null
}) {
  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      {monitor ? (
        <>
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Selected monitor</p>
              <h2 className="mt-1 font-semibold text-slate-950">{monitor.name}</h2>
              <p className="mt-1 break-all text-sm text-slate-500">{monitor.url}</p>
            </div>
            <StatusBadge status={monitor.status} />
          </div>

          {isEditing ? (
            <MonitorForm
              actionLabel="Save changes"
              disabled={backendUnavailable || isUpdating}
              error={updateError}
              form={editForm}
              isSaving={isUpdating}
              onCancel={onCancelEdit}
              onChange={onEditFormChange}
              onSubmit={onSubmitEdit}
              showActive
            />
          ) : (
            <>
              <div className="mt-5 grid grid-cols-2 gap-3 text-sm">
                <DetailItem label="Method" value={monitor.method} />
                <DetailItem label="Expected" value={monitor.expectedStatusCode.toString()} />
                <DetailItem label="Interval" value={`${monitor.intervalMinutes} min`} />
                <DetailItem label="Timeout" value={`${monitor.timeoutSeconds} sec`} />
                <DetailItem label="Failures" value={`${monitor.consecutiveFailures}/${monitor.failureThreshold}`} />
                <DetailItem label="Keyword" value={monitor.expectedKeyword || 'Not required'} />
                <DetailItem label="Headers" value={Object.keys(monitor.requestHeaders ?? {}).length.toString()} />
                <DetailItem label="Active" value={monitor.active ? 'Yes' : 'No'} />
                <DetailItem label="Last check" value={formatDateTime(monitor.lastCheckedAt)} />
              </div>

              <div className="mt-5 flex flex-wrap gap-3">
                <button
                  className="inline-flex items-center gap-2 rounded-md border border-slate-200 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                  onClick={onStartEdit}
                  type="button"
                >
                  <Edit3 size={15} />
                  Edit
                </button>
                <button
                  className="inline-flex items-center gap-2 rounded-md border border-red-200 px-3 py-2 text-sm font-semibold text-red-700 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-60"
                  disabled={backendUnavailable || isDeleting}
                  onClick={onDelete}
                  type="button"
                >
                  {isDeleting ? <Loader2 className="animate-spin" size={15} /> : <Trash2 size={15} />}
                  Delete
                </button>
              </div>

              {deleteError && (
                <p className="mt-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
                  {deleteError.message}
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
  )
}
