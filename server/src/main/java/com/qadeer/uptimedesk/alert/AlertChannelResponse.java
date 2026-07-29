package com.qadeer.uptimedesk.alert;

import java.time.Instant;

public record AlertChannelResponse(
        Long id,
        AlertChannelType type,
        String destination,
        boolean enabled,
        int cooldownMinutes,
        Instant lastIncidentAlertSentAt,
        Instant lastRecoveryAlertSentAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static AlertChannelResponse from(AlertChannel channel) {
        return new AlertChannelResponse(
                channel.getId(),
                channel.getType(),
                channel.getDestination(),
                channel.isEnabled(),
                channel.getCooldownMinutes(),
                channel.getLastIncidentAlertSentAt(),
                channel.getLastRecoveryAlertSentAt(),
                channel.getCreatedAt(),
                channel.getUpdatedAt()
        );
    }
}
