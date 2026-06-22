package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.incident.IncidentTransition;

import java.time.Instant;

public record CheckResultResponse(
        Long id,
        Long monitorId,
        Instant checkedAt,
        Integer statusCode,
        long responseTimeMs,
        CheckStatus status,
        IncidentTransition incidentTransition,
        String incidentReason,
        String errorMessage
) {
    public static CheckResultResponse from(CheckResult result) {
        return new CheckResultResponse(
                result.getId(),
                result.getMonitor().getId(),
                result.getCheckedAt(),
                result.getStatusCode(),
                result.getResponseTimeMs(),
                result.getStatus(),
                result.getIncidentTransition(),
                result.getIncidentReason(),
                result.getErrorMessage()
        );
    }
}
