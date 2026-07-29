package com.qadeer.uptimedesk.alert;

import com.qadeer.uptimedesk.incident.Incident;
import com.qadeer.uptimedesk.monitor.Monitor;
import org.springframework.stereotype.Service;

@Service
public class IncidentAlertService {
    private final AlertChannelRepository alertChannelRepository;
    private final AlertEmailSender alertEmailSender;

    public IncidentAlertService(AlertChannelRepository alertChannelRepository, AlertEmailSender alertEmailSender) {
        this.alertChannelRepository = alertChannelRepository;
        this.alertEmailSender = alertEmailSender;
    }

    public void sendIncidentOpenedAlert(Incident incident) {
        sendToEmailChannels(incident, this::openedMessage);
    }

    public void sendIncidentResolvedAlert(Incident incident) {
        sendToEmailChannels(incident, this::resolvedMessage);
    }

    private void sendToEmailChannels(Incident incident, IncidentEmailMessageFactory messageFactory) {
        Monitor monitor = incident.getMonitor();
        if (monitor == null || monitor.getOwner() == null || monitor.getOwner().getId() == null) {
            return;
        }

        alertChannelRepository.findByOwnerIdAndEnabledTrueOrderByCreatedAtDesc(monitor.getOwner().getId())
                .stream()
                .filter(channel -> channel.getType() == AlertChannelType.EMAIL)
                .forEach(channel -> alertEmailSender.send(messageFactory.create(channel, incident)));
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
