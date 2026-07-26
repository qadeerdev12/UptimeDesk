package com.qadeer.uptimedesk.incident;

import com.qadeer.uptimedesk.check.CheckResult;
import com.qadeer.uptimedesk.check.CheckResultRepository;
import com.qadeer.uptimedesk.check.CheckStatus;
import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class IncidentRepositoryIntegrationTest {

    @Autowired
    private CheckResultRepository checkResultRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private MonitorRepository monitorRepository;

    @Test
    void persistsIncidentAndFindsOpenIncidentForMonitor() {
        Monitor monitor = monitorRepository.save(monitor("Portfolio API"));
        CheckResult failedCheck = checkResultRepository.save(failedCheck(monitor));

        Incident incident = new Incident();
        incident.setMonitor(monitor);
        incident.setOpenedByCheckResult(failedCheck);
        incident.setOpeningReason("Monitor reached 2 consecutive failures; threshold is 2.");
        incidentRepository.save(incident);

        assertThat(incidentRepository.findFirstByMonitorIdAndStatusInOrderByOpenedAtDesc(
                monitor.getId(),
                List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED)
        ))
                .isPresent()
                .get()
                .satisfies(savedIncident -> {
                    assertThat(savedIncident.getStatus()).isEqualTo(IncidentStatus.OPEN);
                    assertThat(savedIncident.getMonitor().getId()).isEqualTo(monitor.getId());
                    assertThat(savedIncident.getOpenedByCheckResult().getId()).isEqualTo(failedCheck.getId());
                    assertThat(savedIncident.getOpeningReason()).contains("threshold is 2");
                });
    }

    private CheckResult failedCheck(Monitor monitor) {
        CheckResult result = new CheckResult();
        result.setMonitor(monitor);
        result.setStatus(CheckStatus.FAILURE);
        result.setStatusCode(500);
        result.setResponseTimeMs(250);
        result.setErrorMessage("Server error");

        return result;
    }

    private Monitor monitor(String name) {
        Monitor monitor = new Monitor();
        monitor.setName(name);
        monitor.setUrl("https://example.com/" + name.toLowerCase().replace(" ", "-"));

        return monitor;
    }
}
