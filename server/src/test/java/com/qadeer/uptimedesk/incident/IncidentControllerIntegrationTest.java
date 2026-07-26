package com.qadeer.uptimedesk.incident;

import com.qadeer.uptimedesk.check.CheckResult;
import com.qadeer.uptimedesk.check.CheckResultRepository;
import com.qadeer.uptimedesk.check.CheckStatus;
import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IncidentControllerIntegrationTest {

    @Autowired
    private CheckResultRepository checkResultRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonitorRepository monitorRepository;

    @Test
    void acknowledgesOpenIncident() throws Exception {
        Incident incident = incidentRepository.save(openIncident());

        mockMvc.perform(post("/api/incidents/{id}/acknowledge", incident.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(incident.getId()))
                .andExpect(jsonPath("$.monitorId").value(incident.getMonitor().getId()))
                .andExpect(jsonPath("$.monitorName").value("Portfolio API"))
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.acknowledgedAt", notNullValue()));

        Incident savedIncident = incidentRepository.findById(incident.getId()).orElseThrow();
        assertThat(savedIncident.getStatus()).isEqualTo(IncidentStatus.ACKNOWLEDGED);
        assertThat(savedIncident.getAcknowledgedAt()).isNotNull();
    }

    @Test
    void returnsNotFoundForMissingIncident() throws Exception {
        mockMvc.perform(post("/api/incidents/{id}/acknowledge", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Incident not found: 999"));
    }

    @Test
    void rejectsAcknowledgementForResolvedIncident() throws Exception {
        Incident incident = openIncident();
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(Instant.now());
        Incident savedIncident = incidentRepository.save(incident);

        mockMvc.perform(post("/api/incidents/{id}/acknowledge", savedIncident.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Resolved incidents cannot be acknowledged."));
    }

    private Incident openIncident() {
        Monitor monitor = monitorRepository.save(monitor("Portfolio API"));
        CheckResult checkResult = checkResultRepository.save(failedCheck(monitor));

        Incident incident = new Incident();
        incident.setMonitor(monitor);
        incident.setOpenedByCheckResult(checkResult);
        incident.setLatestCheckResult(checkResult);
        incident.setLastCheckedAt(checkResult.getCheckedAt());
        incident.setOpeningReason("Monitor reached 2 consecutive failures; threshold is 2.");

        return incident;
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
