package com.qadeer.uptimedesk.alert;

import com.qadeer.uptimedesk.auth.UserIdentity;
import com.qadeer.uptimedesk.incident.Incident;
import com.qadeer.uptimedesk.monitor.Monitor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncidentAlertServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-29T10:00:00Z");

    private final AlertChannelRepository alertChannelRepository = mock(AlertChannelRepository.class);
    private final AlertEmailSender alertEmailSender = mock(AlertEmailSender.class);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final IncidentAlertService incidentAlertService = new IncidentAlertService(
            alertChannelRepository,
            alertEmailSender,
            clock
    );

    @Test
    void sendsIncidentOpenedEmailToEnabledOwnerChannels() {
        UserIdentity owner = new UserIdentity();
        owner.setId(7L);
        Monitor monitor = monitor(owner);
        Incident incident = incident(monitor);
        AlertChannel channel = emailChannel("qadeer@example.com");

        when(alertChannelRepository.findByOwnerIdAndEnabledTrueOrderByCreatedAtDesc(owner.getId()))
                .thenReturn(List.of(channel));
        when(alertEmailSender.send(org.mockito.ArgumentMatchers.any(AlertEmailMessage.class))).thenReturn(true);

        incidentAlertService.sendIncidentOpenedAlert(incident);

        ArgumentCaptor<AlertEmailMessage> messageCaptor = ArgumentCaptor.forClass(AlertEmailMessage.class);
        verify(alertEmailSender).send(messageCaptor.capture());
        verify(alertChannelRepository).save(channel);

        AlertEmailMessage message = messageCaptor.getValue();
        assertThat(message.to()).isEqualTo("qadeer@example.com");
        assertThat(message.subject()).isEqualTo("UptimeDesk alert: Portfolio API is down");
        assertThat(message.body()).contains("Portfolio API", "https://api.example.com/health", "threshold is 2");
        assertThat(channel.getLastIncidentAlertSentAt()).isEqualTo(NOW);
        assertThat(channel.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void skipsIncidentOpenedEmailWhenChannelIsInsideCooldownWindow() {
        UserIdentity owner = new UserIdentity();
        owner.setId(7L);
        Incident incident = incident(monitor(owner));
        AlertChannel channel = emailChannel("qadeer@example.com");
        channel.setCooldownMinutes(30);
        channel.setLastIncidentAlertSentAt(NOW.minusSeconds(60));

        when(alertChannelRepository.findByOwnerIdAndEnabledTrueOrderByCreatedAtDesc(owner.getId()))
                .thenReturn(List.of(channel));

        incidentAlertService.sendIncidentOpenedAlert(incident);

        verify(alertEmailSender, never()).send(org.mockito.ArgumentMatchers.any(AlertEmailMessage.class));
        verify(alertChannelRepository, never()).save(org.mockito.ArgumentMatchers.any(AlertChannel.class));
    }

    @Test
    void sendsIncidentResolvedEmailToEnabledOwnerChannels() {
        UserIdentity owner = new UserIdentity();
        owner.setId(7L);
        Monitor monitor = monitor(owner);
        Incident incident = incident(monitor);
        incident.setResolutionReason("Monitor recovered after a successful check.");
        AlertChannel channel = emailChannel("qadeer@example.com");

        when(alertChannelRepository.findByOwnerIdAndEnabledTrueOrderByCreatedAtDesc(owner.getId()))
                .thenReturn(List.of(channel));
        when(alertEmailSender.send(org.mockito.ArgumentMatchers.any(AlertEmailMessage.class))).thenReturn(true);

        incidentAlertService.sendIncidentResolvedAlert(incident);

        ArgumentCaptor<AlertEmailMessage> messageCaptor = ArgumentCaptor.forClass(AlertEmailMessage.class);
        verify(alertEmailSender).send(messageCaptor.capture());
        verify(alertChannelRepository).save(channel);

        AlertEmailMessage message = messageCaptor.getValue();
        assertThat(message.to()).isEqualTo("qadeer@example.com");
        assertThat(message.subject()).isEqualTo("UptimeDesk recovery: Portfolio API is back up");
        assertThat(message.body()).contains("Portfolio API", "https://api.example.com/health", "recovered");
        assertThat(channel.getLastRecoveryAlertSentAt()).isEqualTo(NOW);
        assertThat(channel.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void skipsIncidentOpenedAlertWhenMonitorHasNoOwner() {
        Incident incident = incident(monitor(null));

        incidentAlertService.sendIncidentOpenedAlert(incident);

        verify(alertChannelRepository, never()).findByOwnerIdAndEnabledTrueOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.anyLong());
        verify(alertEmailSender, never()).send(org.mockito.ArgumentMatchers.any(AlertEmailMessage.class));
    }

    private AlertChannel emailChannel(String destination) {
        AlertChannel channel = new AlertChannel();
        channel.setType(AlertChannelType.EMAIL);
        channel.setDestination(destination);
        channel.setEnabled(true);

        return channel;
    }

    private Incident incident(Monitor monitor) {
        Incident incident = new Incident();
        incident.setMonitor(monitor);
        incident.setOpeningReason("Monitor reached 2 consecutive failures; threshold is 2.");

        return incident;
    }

    private Monitor monitor(UserIdentity owner) {
        Monitor monitor = new Monitor();
        monitor.setOwner(owner);
        monitor.setName("Portfolio API");
        monitor.setUrl("https://api.example.com/health");

        return monitor;
    }
}
