package com.qadeer.uptimedesk.dashboard;

import com.qadeer.uptimedesk.check.CheckStatus;

import java.time.Instant;
import java.util.List;

public record DashboardSummaryResponse(
        long totalMonitors,
        long activeMonitors,
        long downMonitors,
        long averageLatencyMs,
        double uptime24h,
        double uptime7d,
        double uptime30d,
        List<FailedCheckSummary> latestFailedChecks
) {
    public record FailedCheckSummary(
            Long id,
            Long monitorId,
            String monitorName,
            Instant checkedAt,
            Integer statusCode,
            long responseTimeMs,
            CheckStatus status,
            String errorMessage
    ) {
    }
}
