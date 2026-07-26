package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.incident.IncidentDecision;
import com.qadeer.uptimedesk.incident.Incident;
import com.qadeer.uptimedesk.incident.IncidentRepository;
import com.qadeer.uptimedesk.incident.IncidentRuleEngine;
import com.qadeer.uptimedesk.incident.IncidentStatus;
import com.qadeer.uptimedesk.incident.IncidentTransition;
import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import com.qadeer.uptimedesk.monitor.MonitorStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class MonitorCheckService {

    private final CheckResultRepository checkResultRepository;
    private final IncidentRepository incidentRepository;
    private final MonitorRepository monitorRepository;
    private final EndpointCheckClient endpointCheckClient;
    private final IncidentRuleEngine incidentRuleEngine;

    public MonitorCheckService(
            CheckResultRepository checkResultRepository,
            IncidentRepository incidentRepository,
            MonitorRepository monitorRepository,
            EndpointCheckClient endpointCheckClient,
            IncidentRuleEngine incidentRuleEngine
    ) {
        this.checkResultRepository = checkResultRepository;
        this.incidentRepository = incidentRepository;
        this.monitorRepository = monitorRepository;
        this.endpointCheckClient = endpointCheckClient;
        this.incidentRuleEngine = incidentRuleEngine;
    }

    public CheckResult check(Monitor monitor) {
        long startedAt = System.nanoTime();
        MonitorStatus previousMonitorStatus = monitor.getStatus();
        CheckResult result = new CheckResult();
        result.setMonitor(monitor);

        EndpointCheck endpointCheck = endpointCheckClient.check(monitor);
        result.setStatusCode(endpointCheck.statusCode());
        result.setStatus(endpointCheck.success() ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
        result.setErrorMessage(endpointCheck.errorMessage());

        if (endpointCheck.success()) {
            monitor.setConsecutiveFailures(0);
            monitor.setStatus(MonitorStatus.UP);
        } else {
            int consecutiveFailures = monitor.getConsecutiveFailures() + 1;
            monitor.setConsecutiveFailures(consecutiveFailures);

            if (consecutiveFailures >= monitor.getFailureThreshold()) {
                monitor.setStatus(MonitorStatus.DOWN);
            }
        }

        IncidentDecision incidentDecision = incidentRuleEngine.evaluate(
                previousMonitorStatus,
                monitor.getStatus(),
                result.getStatus(),
                monitor.getConsecutiveFailures(),
                monitor.getFailureThreshold()
        );
        result.setIncidentTransition(incidentDecision.transition());
        result.setIncidentReason(incidentDecision.reason());

        result.setResponseTimeMs(Duration.ofNanos(System.nanoTime() - startedAt).toMillis());
        monitor.setLastCheckedAt(Instant.now());
        monitorRepository.save(monitor);
        CheckResult savedResult = checkResultRepository.save(result);

        if (incidentDecision.transition() == IncidentTransition.OPEN_INCIDENT) {
            openIncidentIfNeeded(monitor, savedResult, incidentDecision.reason());
        } else if (result.getStatus() == CheckStatus.FAILURE && monitor.getStatus() == MonitorStatus.DOWN) {
            keepActiveIncidentOpen(monitor, savedResult);
        }

        return savedResult;
    }

    private void openIncidentIfNeeded(Monitor monitor, CheckResult checkResult, String reason) {
        boolean activeIncidentExists = incidentRepository.findFirstByMonitorIdAndStatusInOrderByOpenedAtDesc(
                monitor.getId(),
                java.util.List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED)
        ).isPresent();

        if (activeIncidentExists) {
            return;
        }

        Incident incident = new Incident();
        incident.setMonitor(monitor);
        incident.setOpenedByCheckResult(checkResult);
        incident.setLatestCheckResult(checkResult);
        incident.setLastCheckedAt(checkResult.getCheckedAt());
        incident.setOpeningReason(reason);

        incidentRepository.save(incident);
    }

    private void keepActiveIncidentOpen(Monitor monitor, CheckResult checkResult) {
        incidentRepository.findFirstByMonitorIdAndStatusInOrderByOpenedAtDesc(
                        monitor.getId(),
                        java.util.List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED)
                )
                .ifPresent(incident -> {
                    incident.setLatestCheckResult(checkResult);
                    incident.setLastCheckedAt(checkResult.getCheckedAt());
                    incidentRepository.save(incident);
                });
    }
}
