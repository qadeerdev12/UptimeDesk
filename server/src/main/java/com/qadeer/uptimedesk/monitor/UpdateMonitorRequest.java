package com.qadeer.uptimedesk.monitor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.util.Map;

public record UpdateMonitorRequest(
        @NotBlank String name,
        @NotBlank @URL String url,
        @NotNull HttpMethod method,
        @Min(100) @Max(599) int expectedStatusCode,
        @Min(1) int intervalMinutes,
        @Min(1) int timeoutSeconds,
        String expectedKeyword,
        Map<String, String> requestHeaders,
        @Min(1) @Max(10) int failureThreshold,
        boolean active
) {
}
