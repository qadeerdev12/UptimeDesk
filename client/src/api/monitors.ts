import type { CheckResult, DashboardSummary, Monitor, MonitorFormValues } from '../types/monitor'

async function request<T>(url: string, options?: RequestInit) {
  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
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
