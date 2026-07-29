package com.qadeer.uptimedesk.alert;

import com.qadeer.uptimedesk.incident.Incident;
import com.qadeer.uptimedesk.monitor.Monitor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class IncidentAlertService {
    private final AlertChannelRepository alertChannelRepository;
    private final AlertEmailSender alertEmailSender;
    private final Clock clock;

    public IncidentAlertService(
            AlertChannelRepository alertChannelRepository,
            AlertEmailSender alertEmailSender,
            Clock clock
    ) {
        this.alertChannelRepository = alertChannelRepository;
        this.alertEmailSender = alertEmailSender;
        this.clock = clock;
    }

    public void sendIncidentOpenedAlert(Incident incident) {
        sendToEmailChannels(
                incident,
                this::openedMessage,
                AlertChannel::getLastIncidentAlertSentAt,
                AlertChannel::setLastIncidentAlertSentAt
        );
    }

    public void sendIncidentResolvedAlert(Incident incident) {
        sendToEmailChannels(
                incident,
                this::resolvedMessage,
                AlertChannel::getLastRecoveryAlertSentAt,
                AlertChannel::setLastRecoveryAlertSentAt
        );
    }

    private void sendToEmailChannels(
            Incident incident,
            IncidentEmailMessageFactory messageFactory,
            Function<AlertChannel, Instant> lastSentAt,
            BiConsumer<AlertChannel, Instant> updateLastSentAt
    ) {
        Monitor monitor = incident.getMonitor();
        if (monitor == null || monitor.getOwner() == null || monitor.getOwner().getId() == null) {
            return;
        }

        Instant now = clock.instant();
        alertChannelRepository.findByOwnerIdAndEnabledTrueOrderByCreatedAtDesc(monitor.getOwner().getId())
                .stream()
                .filter(channel -> channel.getType() == AlertChannelType.EMAIL)
                .filter(channel -> cooldownHasElapsed(channel, lastSentAt.apply(channel), now))
                .forEach(channel -> sendAndRecord(messageFactory, incident, updateLastSentAt, now, channel));
    }

    private boolean cooldownHasElapsed(AlertChannel channel, Instant lastSentAt, Instant now) {
        return lastSentAt == null || !lastSentAt.plus(Duration.ofMinutes(channel.getCooldownMinutes())).isAfter(now);
    }

    private void sendAndRecord(
            IncidentEmailMessageFactory messageFactory,
            Incident incident,
            BiConsumer<AlertChannel, Instant> updateLastSentAt,
            Instant now,
            AlertChannel channel
    ) {
        boolean sent = alertEmailSender.send(messageFactory.create(channel, incident));
        if (sent) {
            updateLastSentAt.accept(channel, now);
            channel.setUpdatedAt(now);
            alertChannelRepository.save(channel);
        }
    }

    private AlertEmailMessage openedMessage(AlertChannel channel, Incident incident) {
        Monitor monitor = incident.getMonitor();
        String subject = "UptimeDesk alert: " + monitor.getName() + " is down";
        String body = "UptimeDesk detected an outage.\n\n"
                + "Monitor: " + monitor.getName() + "\n"
                + "URL: " + monitor.getUrl() + "\n"
                + "Reason: " + incident.getOpeningReason() + "\n"
                + "Opened at: " + incident.getOpenedAt();

        return new AlertEmailMessage(channel.getDestination(), subject, body);
    }

    private AlertEmailMessage resolvedMessage(AlertChannel channel, Incident incident) {
        Monitor monitor = incident.getMonitor();
        String subject = "UptimeDesk recovery: " + monitor.getName() + " is back up";
        String body = "UptimeDesk detected a recovery.\n\n"
                + "Monitor: " + monitor.getName() + "\n"
                + "URL: " + monitor.getUrl() + "\n"
                + "Reason: " + incident.getResolutionReason() + "\n"
                + "Resolved at: " + incident.getResolvedAt();

        return new AlertEmailMessage(channel.getDestination(), subject, body);
    }

    @FunctionalInterface
    private interface IncidentEmailMessageFactory {
        AlertEmailMessage create(AlertChannel channel, Incident incident);
    }
}
