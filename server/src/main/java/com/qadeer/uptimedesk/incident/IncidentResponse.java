package com.qadeer.uptimedesk.incident;

import java.time.Instant;

public record IncidentResponse(
        Long id,
        Long monitorId,
        String monitorName,
        Long openedByCheckResultId,
        Long resolvedByCheckResultId,
        Long latestCheckResultId,
        IncidentStatus status,
        Instant openedAt,
        Instant lastCheckedAt,
        Instant acknowledgedAt,
        Instant resolvedAt,
        String openingReason,
        String resolutionReason
) {
    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getMonitor().getId(),
                incident.getMonitor().getName(),
                incident.getOpenedByCheckResult() == null ? null : incident.getOpenedByCheckResult().getId(),
                incident.getResolvedByCheckResult() == null ? null : incident.getResolvedByCheckResult().getId(),
                incident.getLatestCheckResult() == null ? null : incident.getLatestCheckResult().getId(),
                incident.getStatus(),
                incident.getOpenedAt(),
                incident.getLastCheckedAt(),
                incident.getAcknowledgedAt(),
                incident.getResolvedAt(),
                incident.getOpeningReason(),
                incident.getResolutionReason()
        );
    }
}
