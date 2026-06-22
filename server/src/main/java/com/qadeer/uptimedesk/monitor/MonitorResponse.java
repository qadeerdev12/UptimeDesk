package com.qadeer.uptimedesk.monitor;

import java.time.Instant;
import java.util.Map;

public record MonitorResponse(
        Long id,
        String name,
        String url,
        HttpMethod method,
        int expectedStatusCode,
        int intervalMinutes,
        int timeoutSeconds,
        String expectedKeyword,
        Map<String, String> requestHeaders,
        int failureThreshold,
        int consecutiveFailures,
        boolean active,
        MonitorStatus status,
        Instant createdAt,
        Instant lastCheckedAt
) {
    public static MonitorResponse from(Monitor monitor) {
        return new MonitorResponse(
                monitor.getId(),
                monitor.getName(),
                monitor.getUrl(),
                monitor.getMethod(),
                monitor.getExpectedStatusCode(),
                monitor.getIntervalMinutes(),
                monitor.getTimeoutSeconds(),
                monitor.getExpectedKeyword(),
                monitor.getRequestHeaders(),
                monitor.getFailureThreshold(),
                monitor.getConsecutiveFailures(),
                monitor.isActive(),
                monitor.getStatus(),
                monitor.getCreatedAt(),
                monitor.getLastCheckedAt()
        );
    }
}
