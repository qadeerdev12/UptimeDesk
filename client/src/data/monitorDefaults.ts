import type { CheckResult, Monitor, MonitorFormValues } from '../types/monitor'

export const emptyMonitorForm: MonitorFormValues = {
  name: '',
  url: '',
  method: 'GET',
  expectedStatusCode: 200,
  intervalMinutes: 5,
  timeoutSeconds: 5,
  expectedKeyword: '',
  failureThreshold: 2,
  active: true,
}

// Sample data keeps the UI explorable when the Spring Boot API is not running.
export const sampleMonitors: Monitor[] = [
  {
    id: 1,
    name: 'Portfolio Website',
    url: 'https://qadeerdev.com',
    method: 'GET',
    expectedStatusCode: 200,
    intervalMinutes: 5,
    timeoutSeconds: 5,
    expectedKeyword: '',
    failureThreshold: 2,
    consecutiveFailures: 0,
    active: true,
    status: 'UP',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 12).toISOString(),
    lastCheckedAt: new Date().toISOString(),
  },
  {
    id: 2,
    name: 'Job Tracker API',
    url: 'https://jobtracker.example.com/api/health',
    method: 'GET',
    expectedStatusCode: 200,
    intervalMinutes: 5,
    timeoutSeconds: 5,
    expectedKeyword: '',
    failureThreshold: 2,
    consecutiveFailures: 0,
    active: true,
    status: 'UP',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 8).toISOString(),
    lastCheckedAt: new Date(Date.now() - 1000 * 60 * 8).toISOString(),
  },
  {
    id: 3,
    name: 'Kanban API',
    url: 'https://kanban.example.com/api/health',
    method: 'GET',
    expectedStatusCode: 200,
    intervalMinutes: 1,
    timeoutSeconds: 5,
    expectedKeyword: 'OK',
    failureThreshold: 2,
    consecutiveFailures: 2,
    active: true,
    status: 'DOWN',
    createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 4).toISOString(),
    lastCheckedAt: new Date(Date.now() - 1000 * 60 * 2).toISOString(),
  },
]

export const sampleCheckResults: CheckResult[] = [
  {
    id: 101,
    monitorId: 1,
    checkedAt: new Date(Date.now() - 1000 * 60 * 2).toISOString(),
    statusCode: 200,
    responseTimeMs: 142,
    status: 'SUCCESS',
  },
  {
    id: 102,
    monitorId: 1,
    checkedAt: new Date(Date.now() - 1000 * 60 * 7).toISOString(),
    statusCode: 200,
    responseTimeMs: 156,
    status: 'SUCCESS',
  },
  {
    id: 103,
    monitorId: 1,
    checkedAt: new Date(Date.now() - 1000 * 60 * 12).toISOString(),
    statusCode: 200,
    responseTimeMs: 149,
    status: 'SUCCESS',
  },
]

export function toFormValues(monitor: Monitor): MonitorFormValues {
  return {
    name: monitor.name,
    url: monitor.url,
    method: monitor.method,
    expectedStatusCode: monitor.expectedStatusCode,
    intervalMinutes: monitor.intervalMinutes,
    timeoutSeconds: monitor.timeoutSeconds,
    expectedKeyword: monitor.expectedKeyword ?? '',
    failureThreshold: monitor.failureThreshold,
    active: monitor.active,
  }
}
