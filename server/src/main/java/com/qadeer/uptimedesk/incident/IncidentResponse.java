package com.qadeer.uptimedesk.incident;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
        String resolutionReason,
        List<IncidentTimelineEventResponse> timelineEvents
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
                incident.getResolutionReason(),
                timelineEventsFrom(incident)
        );
    }

    private static List<IncidentTimelineEventResponse> timelineEventsFrom(Incident incident) {
        List<IncidentTimelineEventResponse> events = new ArrayList<>();

        events.add(new IncidentTimelineEventResponse(
                "OPENED",
                "Incident opened",
                incident.getOpenedAt(),
                incident.getOpenedByCheckResult() == null ? null : incident.getOpenedByCheckResult().getId(),
                incident.getOpeningReason()
        ));

        if (incident.getAcknowledgedAt() != null) {
            events.add(new IncidentTimelineEventResponse(
                    "ACKNOWLEDGED",
                    "Incident acknowledged",
                    incident.getAcknowledgedAt(),
                    null,
                    "A user acknowledged the incident."
            ));
        }

        if (incident.getLatestCheckResult() != null && incident.getLastCheckedAt() != null) {
            events.add(new IncidentTimelineEventResponse(
                    "LATEST_CHECK",
                    "Latest check recorded",
                    incident.getLastCheckedAt(),
                    incident.getLatestCheckResult().getId(),
                    "The monitor check updated the incident state."
            ));
        }

        if (incident.getResolvedAt() != null) {
            events.add(new IncidentTimelineEventResponse(
                    "RESOLVED",
                    "Incident resolved",
                    incident.getResolvedAt(),
                    incident.getResolvedByCheckResult() == null ? null : incident.getResolvedByCheckResult().getId(),
                    incident.getResolutionReason()
            ));
        }

        return events;
    }
}
