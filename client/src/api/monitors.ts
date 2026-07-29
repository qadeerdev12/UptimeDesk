import type { AlertChannel, AlertChannelFormValues, CheckResult, DashboardSummary, Incident, Monitor, MonitorFormValues } from '../types/monitor'
import { supabase } from '../auth/supabase'

async function request<T>(url: string, options?: RequestInit) {
  const accessToken = await getAccessToken()

  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...options?.headers,
    },
    ...options,
  })

  if (!response.ok) {
    const fallback = `Request failed with status ${response.status}`
    const message = await response.text().catch(() => fallback)
    throw new Error(message || fallback)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

async function getAccessToken() {
  if (!supabase) {
    return null
  }

  const { data } = await supabase.auth.getSession()

  return data.session?.access_token ?? null
}

export function fetchMonitors() {
  return request<Monitor[]>('/api/monitors')
}

export function createMonitor(payload: MonitorFormValues) {
  // The backend create endpoint does not accept the active flag yet, so only send creation fields.
  return request<Monitor>('/api/monitors', {
    method: 'POST',
    body: JSON.stringify({
      name: payload.name,
      url: payload.url,
      method: payload.method,
      expectedStatusCode: payload.expectedStatusCode,
      intervalMinutes: payload.intervalMinutes,
      timeoutSeconds: payload.timeoutSeconds,
      expectedKeyword: payload.expectedKeyword,
      requestHeaders: payload.requestHeaders,
      failureThreshold: payload.failureThreshold,
    }),
  })
}

export function updateMonitor({ id, payload }: { id: number; payload: MonitorFormValues }) {
  return request<Monitor>(`/api/monitors/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteMonitor(id: number) {
  return request<void>(`/api/monitors/${id}`, { method: 'DELETE' })
}

export function runMonitorCheck(id: number) {
  return request<CheckResult>(`/api/monitors/${id}/run`, { method: 'POST' })
}

export function fetchCheckResults(id: number) {
  return request<CheckResult[]>(`/api/monitors/${id}/results`)
}

export function fetchCheckResult(id: number) {
  return request<CheckResult>(`/api/check-results/${id}`)
}

export function fetchDashboardSummary() {
  return request<DashboardSummary>('/api/dashboard/summary')
}

export function fetchActiveIncidents() {
  return request<Incident[]>('/api/incidents/active')
}

export function fetchIncident(id: number) {
  return request<Incident>(`/api/incidents/${id}`)
}


export function fetchAlertChannels() {
  return request<AlertChannel[]>('/api/alert-channels')
}

export function createAlertChannel(payload: AlertChannelFormValues) {
  return request<AlertChannel>('/api/alert-channels', {
    method: 'POST',
    body: JSON.stringify({
      destination: payload.destination,
      cooldownMinutes: payload.cooldownMinutes,
    }),
  })
}

export function updateAlertChannel({ id, payload }: { id: number; payload: AlertChannelFormValues }) {
  return request<AlertChannel>(`/api/alert-channels/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteAlertChannel(id: number) {
  return request<void>(`/api/alert-channels/${id}`, { method: 'DELETE' })
}
