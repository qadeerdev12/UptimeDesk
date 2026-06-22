package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.incident.IncidentTransition;
import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CheckResultControllerIntegrationTest {

    @Autowired
    private CheckResultRepository checkResultRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonitorRepository monitorRepository;

    @Test
    void getsCheckResultById() throws Exception {
        Monitor monitor = new Monitor();
        monitor.setName("Portfolio API");
        monitor.setUrl("https://example.com/health");
        Monitor savedMonitor = monitorRepository.save(monitor);

        CheckResult result = new CheckResult();
        result.setMonitor(savedMonitor);
        result.setStatusCode(500);
        result.setResponseTimeMs(240);
        result.setStatus(CheckStatus.FAILURE);
        result.setIncidentTransition(IncidentTransition.OPEN_INCIDENT);
        result.setIncidentReason("Monitor reached 2 consecutive failures; threshold is 2.");
        result.setErrorMessage("Server error");
        CheckResult savedResult = checkResultRepository.save(result);

        mockMvc.perform(get("/api/check-results/{id}", savedResult.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedResult.getId()))
                .andExpect(jsonPath("$.monitorId").value(savedMonitor.getId()))
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.responseTimeMs").value(240))
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.incidentTransition").value("OPEN_INCIDENT"))
                .andExpect(jsonPath("$.incidentReason").value("Monitor reached 2 consecutive failures; threshold is 2."))
                .andExpect(jsonPath("$.errorMessage").value("Server error"));
    }

    @Test
    void returnsNotFoundForMissingCheckResult() throws Exception {
        mockMvc.perform(get("/api/check-results/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
