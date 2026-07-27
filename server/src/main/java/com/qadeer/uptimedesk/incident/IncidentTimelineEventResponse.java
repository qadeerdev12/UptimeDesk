package com.qadeer.uptimedesk.incident;

import java.time.Instant;

public record IncidentTimelineEventResponse(
        String type,
        String label,
        Instant occurredAt,
        Long checkResultId,
        String message
) {
}
