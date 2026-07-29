package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.alert.IncidentAlertService;
import com.qadeer.uptimedesk.incident.IncidentRuleEngine;
import com.qadeer.uptimedesk.incident.Incident;
import com.qadeer.uptimedesk.incident.IncidentRepository;
import com.qadeer.uptimedesk.incident.IncidentStatus;
import com.qadeer.uptimedesk.incident.IncidentTransition;
import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import com.qadeer.uptimedesk.monitor.MonitorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorCheckServiceTest {

    @Mock
    private CheckResultRepository checkResultRepository;

    @Mock
    private MonitorRepository monitorRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private EndpointCheckClient endpointCheckClient;

    @Mock
    private IncidentAlertService incidentAlertService;

    @Test
    void keepsMonitorUpUntilFailureThresholdIsReached() {
        Monitor monitor = new Monitor();
        monitor.setFailureThreshold(2);

        when(endpointCheckClient.check(monitor)).thenReturn(EndpointCheck.failure(500, "Server error"));
        when(checkResultRepository.save(any(CheckResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(incidentRepository.findFirstByMonitorIdAndStatusInOrderByOpenedAtDesc(
                eq(monitor.getId()),
                eq(List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED))
        )).thenReturn(Optional.empty());

        MonitorCheckService service = new MonitorCheckService(
                checkResultRepository,
                incidentRepository,
                monitorRepository,
                endpointCheckClient,
                new IncidentRuleEngine(),
                incidentAlertService
        );

        CheckResult firstResult = service.check(monitor);

        assertThat(firstResult.getIncidentTransition()).isEqualTo(IncidentTransition.NONE);
        assertThat(monitor.getConsecutiveFailures()).isEqualTo(1);
        assertThat(monitor.getStatus()).isEqualTo(MonitorStatus.UNKNOWN);

        CheckResult secondResult = service.check(monitor);

        assertThat(secondResult.getIncidentTransition()).isEqualTo(IncidentTransition.OPEN_INCIDENT);
        assertThat(secondResult.getIncidentReason()).contains("threshold is 2");
        assertThat(monitor.getConsecutiveFailures()).isEqualTo(2);
        assertThat(monitor.getStatus()).isEqualTo(MonitorStatus.DOWN);

        ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository).save(incidentCaptor.capture());
        assertThat(incidentCaptor.getValue().getMonitor()).isEqualTo(monitor);
        assertThat(incidentCaptor.getValue().getOpenedByCheckResult()).isEqualTo(secondResult);
        assertThat(incidentCaptor.getValue().getOpeningReason()).contains("threshold is 2");
        verify(incidentAlertService).sendIncidentOpenedAlert(incidentCaptor.getValue());
    }

    @Test
    void successfulCheckResetsFailureCountAndMarksMonitorUp() {
        Monitor monitor = new Monitor();
        monitor.setId(42L);
        monitor.setFailureThreshold(2);
        monitor.setConsecutiveFailures(1);
        monitor.setStatus(MonitorStatus.DOWN);

        Incident existingIncident = new Incident();
        existingIncident.setStatus(IncidentStatus.OPEN);

        when(endpointCheckClient.check(monitor)).thenReturn(EndpointCheck.success(200));
        when(checkResultRepository.save(any(CheckResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(incidentRepository.findFirstByMonitorIdAndStatusInOrderByOpenedAtDesc(
                eq(monitor.getId()),
                eq(List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED))
        )).thenReturn(Optional.of(existingIncident));

        MonitorCheckService service = new MonitorCheckService(
                checkResultRepository,
                incidentRepository,
                monitorRepository,
                endpointCheckClient,
                new IncidentRuleEngine(),
                incidentAlertService
        );

        CheckResult result = service.check(monitor);

        assertThat(result.getStatus()).isEqualTo(CheckStatus.SUCCESS);
        assertThat(result.getIncidentTransition()).isEqualTo(IncidentTransition.RESOLVE_INCIDENT);
        assertThat(result.getIncidentReason()).contains("recovered");
        assertThat(monitor.getConsecutiveFailures()).isZero();
        assertThat(monitor.getStatus()).isEqualTo(MonitorStatus.UP);
        assertThat(existingIncident.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(existingIncident.getResolvedByCheckResult()).isEqualTo(result);
        assertThat(existingIncident.getLatestCheckResult()).isEqualTo(result);
        assertThat(existingIncident.getResolvedAt()).isEqualTo(result.getCheckedAt());
        assertThat(existingIncident.getResolutionReason()).contains("recovered");
        verify(incidentRepository).save(existingIncident);
        verify(incidentAlertService).sendIncidentResolvedAlert(existingIncident);
    }

    @Test
    void doesNotCreateDuplicateIncidentWhenActiveIncidentAlreadyExists() {
        Monitor monitor = new Monitor();
        monitor.setId(42L);
        monitor.setFailureThreshold(2);
        monitor.setConsecutiveFailures(1);

        when(endpointCheckClient.check(monitor)).thenReturn(EndpointCheck.failure(500, "Server error"));
        when(checkResultRepository.save(any(CheckResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(incidentRepository.findFirstByMonitorIdAndStatusInOrderByOpenedAtDesc(
                eq(monitor.getId()),
                eq(List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED))
        )).thenReturn(Optional.of(new Incident()));

        MonitorCheckService service = new MonitorCheckService(
                checkResultRepository,
                incidentRepository,
                monitorRepository,
                endpointCheckClient,
                new IncidentRuleEngine(),
                incidentAlertService
        );

        CheckResult result = service.check(monitor);

        assertThat(result.getIncidentTransition()).isEqualTo(IncidentTransition.OPEN_INCIDENT);
        verify(incidentRepository, never()).save(any(Incident.class));
        verify(incidentAlertService, never()).sendIncidentOpenedAlert(any(Incident.class));
        verify(incidentAlertService, never()).sendIncidentResolvedAlert(any(Incident.class));
    }

    @Test
    void keepsActiveIncidentOpenWhileMonitorRemainsDown() {
        Monitor monitor = new Monitor();
        monitor.setId(42L);
        monitor.setFailureThreshold(2);
        monitor.setConsecutiveFailures(2);
        monitor.setStatus(MonitorStatus.DOWN);

        Incident existingIncident = new Incident();
        existingIncident.setStatus(IncidentStatus.OPEN);

        when(endpointCheckClient.check(monitor)).thenReturn(EndpointCheck.failure(500, "Server error"));
        when(checkResultRepository.save(any(CheckResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(incidentRepository.findFirstByMonitorIdAndStatusInOrderByOpenedAtDesc(
                eq(monitor.getId()),
                eq(List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED))
        )).thenReturn(Optional.of(existingIncident));

        MonitorCheckService service = new MonitorCheckService(
                checkResultRepository,
                incidentRepository,
                monitorRepository,
                endpointCheckClient,
                new IncidentRuleEngine(),
                incidentAlertService
        );

        CheckResult result = service.check(monitor);

        assertThat(result.getIncidentTransition()).isEqualTo(IncidentTransition.NONE);
        assertThat(existingIncident.getStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(existingIncident.getLatestCheckResult()).isEqualTo(result);
        assertThat(existingIncident.getLastCheckedAt()).isEqualTo(result.getCheckedAt());
        verify(incidentRepository).save(existingIncident);
        verify(incidentAlertService, never()).sendIncidentResolvedAlert(any(Incident.class));
    }
}
