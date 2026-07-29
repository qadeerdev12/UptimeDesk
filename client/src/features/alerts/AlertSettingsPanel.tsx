import type { FormEvent } from 'react'
import { Bell, Loader2, Mail, Trash2 } from 'lucide-react'
import type { AlertChannel, AlertChannelFormValues } from '../../types/monitor'
import { EmptyState } from '../../components/ui/EmptyState'
import { Field } from '../../components/ui/Field'

export function AlertSettingsPanel({
  channels,
  createError,
  deleteError,
  disabled,
  form,
  isCreating,
  isFetching,
  isUpdating,
  onCreate,
  onDelete,
  onFormChange,
  onToggle,
  updateError,
}: {
  channels: AlertChannel[]
  createError: Error | null
  deleteError: Error | null
  disabled: boolean
  form: AlertChannelFormValues
  isCreating: boolean
  isFetching: boolean
  isUpdating: boolean
  onCreate: (event: FormEvent<HTMLFormElement>) => void
  onDelete: (id: number) => void
  onFormChange: (form: AlertChannelFormValues) => void
  onToggle: (channel: AlertChannel) => void
  updateError: Error | null
}) {
  const error = createError ?? updateError ?? deleteError

  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm" id="alert-settings">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <Bell className="text-blue-600" size={18} />
            <h2 className="font-semibold text-slate-950">Alert settings</h2>
          </div>
          <p className="mt-1 text-sm text-slate-500">Choose where outage and recovery emails should go.</p>
        </div>
        {isFetching && <Loader2 className="animate-spin text-slate-400" size={18} />}
      </div>

      {error && (
        <p className="mt-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error.message}
        </p>
      )}

      <form className="mt-5 grid gap-3 md:grid-cols-[minmax(0,1fr)_150px_auto]" onSubmit={onCreate}>
        <Field label="Email destination">
          <input
            className="w-full rounded-md border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100 disabled:bg-slate-50"
            disabled={disabled || isCreating}
            onChange={(event) => onFormChange({ ...form, destination: event.target.value })}
            placeholder="alerts@example.com"
            type="email"
            value={form.destination}
          />
        </Field>
        <Field label="Cooldown">
          <input
            className="w-full rounded-md border border-slate-200 px-3 py-2 text-sm outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100 disabled:bg-slate-50"
            disabled={disabled || isCreating}
            min={1}
            max={1440}
            onChange={(event) => onFormChange({ ...form, cooldownMinutes: Number(event.target.value) })}
            type="number"
            value={form.cooldownMinutes}
          />
        </Field>
        <button
          className="self-end rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={disabled || isCreating || !form.destination.trim()}
          type="submit"
        >
          {isCreating ? 'Saving...' : 'Add alert'}
        </button>
      </form>

      <div className="mt-5 overflow-hidden rounded-lg border border-slate-200">
        {channels.length === 0 ? (
          <EmptyState title="No alert channels yet" message="Add an email address to receive outage and recovery notifications." />
        ) : (
          <div className="divide-y divide-slate-200">
            {channels.map((channel) => (
              <div className="flex flex-wrap items-center justify-between gap-3 p-4" key={channel.id}>
                <div className="min-w-0">
                  <div className="flex items-center gap-2 text-sm font-semibold text-slate-900">
                    <Mail size={16} />
                    <span className="truncate">{channel.destination}</span>
                  </div>
                  <p className="mt-1 text-xs text-slate-500">
                    {channel.enabled ? 'Enabled' : 'Disabled'} · {channel.cooldownMinutes} min cooldown
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    className="rounded-md border border-slate-200 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-60"
                    disabled={disabled || isUpdating}
                    onClick={() => onToggle(channel)}
                    type="button"
                  >
                    {channel.enabled ? 'Disable' : 'Enable'}
                  </button>
                  <button
                    aria-label={`Delete ${channel.destination}`}
                    className="rounded-md border border-red-200 p-2 text-red-600 hover:bg-red-50 disabled:opacity-60"
                    disabled={disabled}
                    onClick={() => onDelete(channel.id)}
                    type="button"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </section>
  )
}
