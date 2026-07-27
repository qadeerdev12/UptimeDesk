package com.qadeer.uptimedesk.dashboard;

import com.qadeer.uptimedesk.auth.UserIdentity;
import com.qadeer.uptimedesk.auth.UserIdentityRepository;
import com.qadeer.uptimedesk.check.CheckResult;
import com.qadeer.uptimedesk.check.CheckResultRepository;
import com.qadeer.uptimedesk.check.CheckStatus;
import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import com.qadeer.uptimedesk.monitor.MonitorStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerIntegrationTest {

    @Autowired
    private CheckResultRepository checkResultRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MonitorRepository monitorRepository;

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Test
    void returnsDashboardSummaryMetrics() throws Exception {
        UserIdentity owner = userIdentity("supabase-user-1", "qadeer@example.com");
        UserIdentity otherOwner = userIdentity("supabase-user-2", "other@example.com");
        Monitor upMonitor = monitor(owner, "Portfolio API", MonitorStatus.UP, true);
        Monitor downMonitor = monitor(owner, "Kanban API", MonitorStatus.DOWN, true);
        Monitor pausedMonitor = monitor(owner, "Paused API", MonitorStatus.UNKNOWN, false);
        Monitor otherMonitor = monitor(otherOwner, "Other API", MonitorStatus.DOWN, true);

        Instant now = Instant.now();
        checkResult(upMonitor, CheckStatus.SUCCESS, 200, 100, now.minus(Duration.ofHours(1)), null);
        checkResult(upMonitor, CheckStatus.SUCCESS, 200, 200, now.minus(Duration.ofDays(1)), null);
        checkResult(downMonitor, CheckStatus.FAILURE, 500, 300, now.minus(Duration.ofDays(2)), "Server error");
        checkResult(pausedMonitor, CheckStatus.SUCCESS, 200, 400, now.minus(Duration.ofDays(21)), null);
        checkResult(otherMonitor, CheckStatus.FAILURE, 500, 900, now.minus(Duration.ofHours(2)), "Hidden error");

        mockMvc.perform(get("/api/dashboard/summary")
                        .with(jwt().jwt(jwt -> jwt.subject("supabase-user-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMonitors").value(3))
                .andExpect(jsonPath("$.activeMonitors").value(2))
                .andExpect(jsonPath("$.downMonitors").value(1))
                .andExpect(jsonPath("$.averageLatencyMs").value(250))
                .andExpect(jsonPath("$.uptime30d").value(75.0))
                .andExpect(jsonPath("$.latestFailedChecks[0].monitorId").value(downMonitor.getId()))
                .andExpect(jsonPath("$.latestFailedChecks[0].monitorName").value("Kanban API"))
                .andExpect(jsonPath("$.latestFailedChecks[0].status").value("FAILURE"))
                .andExpect(jsonPath("$.latestFailedChecks[0].errorMessage").value("Server error"));
    }

    @Test
    void rejectsUnauthenticatedDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    private CheckResult checkResult(
            Monitor monitor,
            CheckStatus status,
            Integer statusCode,
            long responseTimeMs,
            Instant checkedAt,
            String errorMessage
    ) {
        CheckResult result = new CheckResult();
        result.setMonitor(monitor);
        result.setStatus(status);
        result.setStatusCode(statusCode);
        result.setResponseTimeMs(responseTimeMs);
        result.setCheckedAt(checkedAt);
        result.setErrorMessage(errorMessage);

        return checkResultRepository.save(result);
    }

    private UserIdentity userIdentity(String externalSubject, String email) {
        UserIdentity identity = new UserIdentity();
        identity.setExternalSubject(externalSubject);
        identity.setEmail(email);

        return userIdentityRepository.save(identity);
    }

    private Monitor monitor(UserIdentity owner, String name, MonitorStatus status, boolean active) {
        Monitor monitor = new Monitor();
        monitor.setOwner(owner);
        monitor.setName(name);
        monitor.setUrl("https://example.com/" + name.toLowerCase().replace(" ", "-"));
        monitor.setStatus(status);
        monitor.setActive(active);

        return monitorRepository.save(monitor);
    }
}
