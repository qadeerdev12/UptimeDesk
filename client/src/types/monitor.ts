export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'HEAD'

export type MonitorStatus = 'UNKNOWN' | 'UP' | 'DOWN'

export type CheckStatus = 'SUCCESS' | 'FAILURE'

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
