import type { FormEvent } from 'react'
import { Loader2, Save, X } from 'lucide-react'
import { Field } from '../../components/ui/Field'
import type { HttpMethod, MonitorFormValues } from '../../types/monitor'

export function MonitorForm({
  actionLabel,
  disabled,
  error,
  form,
  isSaving,
  onCancel,
  onChange,
  onSubmit,
  showActive = false,
}: {
  actionLabel: string
  disabled: boolean
  error: Error | null
  form: MonitorFormValues
  isSaving: boolean
  onCancel?: () => void
  onChange: (form: MonitorFormValues) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  showActive?: boolean
}) {
  return (
    <form className="mt-5 space-y-4" onSubmit={onSubmit}>
      <Field label="Monitor name">
        <input
          className="input"
          onChange={(event) => onChange({ ...form, name: event.target.value })}
          placeholder="Portfolio API"
          required
          value={form.name}
        />
      </Field>
      <Field label="Endpoint URL">
        <input
          className="input"
          onChange={(event) => onChange({ ...form, url: event.target.value })}
          placeholder="https://example.com/api/health"
          required
          type="url"
          value={form.url}
        />
      </Field>
      <div className="grid grid-cols-2 gap-3">
        <Field label="Method">
          <select
            className="input"
            onChange={(event) => onChange({ ...form, method: event.target.value as HttpMethod })}
            value={form.method}
          >
            {['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD'].map((method) => (
              <option key={method}>{method}</option>
            ))}
          </select>
        </Field>
        <Field label="Expected">
          <input
            className="input"
            max={599}
            min={100}
            onChange={(event) => onChange({ ...form, expectedStatusCode: Number(event.target.value) })}
            required
            type="number"
            value={form.expectedStatusCode}
          />
        </Field>
      </div>
      <div className="grid grid-cols-2 gap-3">
        <Field label="Interval min">
          <input
            className="input"
            min={1}
            onChange={(event) => onChange({ ...form, intervalMinutes: Number(event.target.value) })}
            required
            type="number"
            value={form.intervalMinutes}
          />
        </Field>
        <Field label="Timeout sec">
          <input
            className="input"
            min={1}
            onChange={(event) => onChange({ ...form, timeoutSeconds: Number(event.target.value) })}
            required
            type="number"
            value={form.timeoutSeconds}
          />
        </Field>
      </div>
      <Field label="Failures before down">
        <input
          className="input"
          max={10}
          min={1}
          onChange={(event) => onChange({ ...form, failureThreshold: Number(event.target.value) })}
          required
          type="number"
          value={form.failureThreshold}
        />
      </Field>

      {showActive && (
        <label className="flex items-center justify-between rounded-md border border-slate-200 px-3 py-2 text-sm">
          <span>
            <span className="block font-medium text-slate-800">Active monitor</span>
            <span className="text-slate-500">Include this endpoint in scheduled checks.</span>
          </span>
          <input
            checked={form.active}
            className="h-4 w-4"
            onChange={(event) => onChange({ ...form, active: event.target.checked })}
            type="checkbox"
          />
        </label>
      )}

      {error && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
          {error.message}
        </p>
      )}

      <div className="flex gap-3">
        <button
          className="flex flex-1 items-center justify-center gap-2 rounded-md bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={disabled}
          type="submit"
        >
          {isSaving ? <Loader2 className="animate-spin" size={16} /> : <Save size={16} />}
          {actionLabel}
        </button>
        {onCancel && (
          <button
            className="inline-flex items-center gap-2 rounded-md border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            onClick={onCancel}
            type="button"
          >
            <X size={16} />
            Cancel
          </button>
        )}
      </div>
    </form>
  )
}
