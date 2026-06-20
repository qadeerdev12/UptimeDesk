package com.qadeer.uptimedesk.check;

import java.time.Instant;

public record CheckResultResponse(
        Long id,
        Long monitorId,
        Instant checkedAt,
        Integer statusCode,
        long responseTimeMs,
        CheckStatus status,
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
                result.getErrorMessage()
        );
    }
}
