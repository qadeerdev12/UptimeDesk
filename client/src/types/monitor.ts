export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'HEAD'

export type MonitorStatus = 'UNKNOWN' | 'UP' | 'DOWN'

export type CheckStatus = 'SUCCESS' | 'FAILURE'

export type IncidentTransition = 'NONE' | 'OPEN_INCIDENT' | 'RESOLVE_INCIDENT'

export type Monitor = {
  id: number
  name: string
  url: string
  method: HttpMethod
  expectedStatusCode: number
  intervalMinutes: number
  timeoutSeconds: number
  expectedKeyword?: string
  requestHeaders: Record<string, string>
  failureThreshold: number
  consecutiveFailures: number
  active: boolean
  status: MonitorStatus
  createdAt: string
  lastCheckedAt?: string
}

export type CheckResult = {
  id: number
  monitorId: number
  checkedAt: string
  statusCode?: number
  responseTimeMs: number
  status: CheckStatus
  incidentTransition: IncidentTransition
  incidentReason?: string
  errorMessage?: string
}

export type DashboardSummary = {
  totalMonitors: number
  activeMonitors: number
  downMonitors: number
  averageLatencyMs: number
  uptime24h: number
  uptime7d: number
  uptime30d: number
  latestFailedChecks: FailedCheckSummary[]
}

export type FailedCheckSummary = {
  id: number
  monitorId: number
  monitorName: string
  checkedAt: string
  statusCode?: number
  responseTimeMs: number
  status: CheckStatus
  errorMessage?: string
}

export type MonitorFormValues = {
  name: string
  url: string
  method: HttpMethod
  expectedStatusCode: number
  intervalMinutes: number
  timeoutSeconds: number
  expectedKeyword: string
  requestHeaders: Record<string, string>
  failureThreshold: number
  active: boolean
}
