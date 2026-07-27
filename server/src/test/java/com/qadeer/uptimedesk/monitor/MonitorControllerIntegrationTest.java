package com.qadeer.uptimedesk.monitor;

import com.qadeer.uptimedesk.auth.UserIdentity;
import com.qadeer.uptimedesk.auth.UserIdentityRepository;
import com.qadeer.uptimedesk.check.CheckResult;
import com.qadeer.uptimedesk.check.CheckResultRepository;
import com.qadeer.uptimedesk.check.CheckStatus;
import com.qadeer.uptimedesk.incident.Incident;
import com.qadeer.uptimedesk.incident.IncidentRepository;
import com.qadeer.uptimedesk.incident.IncidentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MonitorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CheckResultRepository checkResultRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Test
    void rejectsUnauthenticatedMonitorRequests() throws Exception {
        mockMvc.perform(get("/api/monitors"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsReadsUpdatesAndDeletesMonitor() throws Exception {
        String createPayload = """
                {
                  "name": "Portfolio API",
                  "url": "https://example.com/health",
                  "method": "GET",
                  "expectedStatusCode": 200,
                  "intervalMinutes": 5,
                  "timeoutSeconds": 5
                }
                """;

        int monitorId = mockMvc.perform(post("/api/monitors")
                        .with(jwtFor("supabase-user-1", "qadeer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Portfolio API"))
                .andExpect(jsonPath("$.url").value("https://example.com/health"))
                .andExpect(jsonPath("$.status").value("UNKNOWN"))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"id\":(\\d+).*", "$1")
                .transform(Integer::parseInt);

        mockMvc.perform(get("/api/monitors/{id}", monitorId)
                        .with(jwtFor("supabase-user-1", "qadeer@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(monitorId))
                .andExpect(jsonPath("$.name").value("Portfolio API"));

        String updatePayload = """
                {
                  "name": "Portfolio Website",
                  "url": "https://example.com/status",
                  "method": "GET",
                  "expectedStatusCode": 204,
                  "intervalMinutes": 10,
                  "timeoutSeconds": 3,
                  "expectedKeyword": "ready",
                  "requestHeaders": {
                    "X-Health-Check": "uptimedesk"
                  },
                  "failureThreshold": 2,
                  "active": false
                }
                """;

        mockMvc.perform(put("/api/monitors/{id}", monitorId)
                        .with(jwtFor("supabase-user-1", "qadeer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Portfolio Website"))
                .andExpect(jsonPath("$.expectedStatusCode").value(204))
                .andExpect(jsonPath("$.expectedKeyword").value("ready"))
                .andExpect(jsonPath("$.requestHeaders.X-Health-Check").value("uptimedesk"))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(delete("/api/monitors/{id}", monitorId)
                        .with(jwtFor("supabase-user-1", "qadeer@example.com")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/monitors/{id}", monitorId)
                        .with(jwtFor("supabase-user-1", "qadeer@example.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidMonitorUrl() throws Exception {
        String invalidPayload = """
                {
                  "name": "Broken Monitor",
                  "url": "not-a-url",
                  "method": "GET",
                  "expectedStatusCode": 200,
                  "intervalMinutes": 5,
                  "timeoutSeconds": 5
                }
                """;

        mockMvc.perform(post("/api/monitors")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hidesMonitorsOwnedByOtherUsers() throws Exception {
        UserIdentity owner = userIdentity("owner-user", "owner@example.com");
        Monitor monitor = new Monitor();
        monitor.setOwner(owner);
        monitor.setName("Private API");
        monitor.setUrl("https://example.com/private");
        Monitor savedMonitor = monitorRepository.save(monitor);

        mockMvc.perform(get("/api/monitors")
                        .with(jwtFor("other-user", "other@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/monitors/{id}", savedMonitor.getId())
                        .with(jwtFor("other-user", "other@example.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsMonitorsAsResponses() throws Exception {
        String createPayload = """
                {
                  "name": "Kanban API",
                  "url": "https://example.com/kanban/health",
                  "method": "GET",
                  "expectedStatusCode": 200,
                  "intervalMinutes": 1,
                  "timeoutSeconds": 5
                }
                """;

        mockMvc.perform(post("/api/monitors")
                        .with(jwtFor("supabase-user-1", "qadeer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/monitors")
                        .with(jwtFor("supabase-user-1", "qadeer@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Kanban API"));
    }

    @Test
    void listsMonitorIncidentHistory() throws Exception {
        Monitor monitor = new Monitor();
        monitor.setOwner(userIdentity("supabase-user-1", "qadeer@example.com"));
        monitor.setName("Portfolio API");
        monitor.setUrl("https://example.com/health");
        Monitor savedMonitor = monitorRepository.save(monitor);

        CheckResult failedCheck = new CheckResult();
        failedCheck.setMonitor(savedMonitor);
        failedCheck.setStatus(CheckStatus.FAILURE);
        failedCheck.setStatusCode(500);
        failedCheck.setResponseTimeMs(250);
        CheckResult savedCheck = checkResultRepository.save(failedCheck);

        Incident incident = new Incident();
        incident.setMonitor(savedMonitor);
        incident.setOpenedByCheckResult(savedCheck);
        incident.setLatestCheckResult(savedCheck);
        incident.setLastCheckedAt(savedCheck.getCheckedAt());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setOpeningReason("Monitor reached 2 consecutive failures; threshold is 2.");
        Incident savedIncident = incidentRepository.save(incident);

        mockMvc.perform(get("/api/monitors/{id}/incidents", savedMonitor.getId())
                        .with(jwtFor("supabase-user-1", "qadeer@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedIncident.getId()))
                .andExpect(jsonPath("$[0].monitorId").value(savedMonitor.getId()))
                .andExpect(jsonPath("$[0].openedByCheckResultId").value(savedCheck.getId()))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
    }

    private UserIdentity userIdentity(String externalSubject, String email) {
        UserIdentity identity = new UserIdentity();
        identity.setExternalSubject(externalSubject);
        identity.setEmail(email);

        return userIdentityRepository.save(identity);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtFor(
            String subject,
            String email
    ) {
        return jwt().jwt(jwt -> jwt.subject(subject).claim("email", email));
    }
}
