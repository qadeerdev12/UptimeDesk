package com.qadeer.uptimedesk.dashboard;

import com.qadeer.uptimedesk.check.CheckResult;
import com.qadeer.uptimedesk.check.CheckResultRepository;
import com.qadeer.uptimedesk.check.CheckStatus;
import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import com.qadeer.uptimedesk.monitor.MonitorStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class DashboardSummaryService {

    private final CheckResultRepository checkResultRepository;
    private final Clock clock;
    private final MonitorRepository monitorRepository;

    public DashboardSummaryService(
            CheckResultRepository checkResultRepository,
            Clock clock,
            MonitorRepository monitorRepository
    ) {
        this.checkResultRepository = checkResultRepository;
        this.clock = clock;
        this.monitorRepository = monitorRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        List<Monitor> monitors = monitorRepository.findAll();
        List<CheckResult> thirtyDayResults = checkResultRepository.findByCheckedAtAfter(
                clock.instant().minus(Duration.ofDays(30))
        );
        List<CheckResult> latestFailedChecks = checkResultRepository.findTop5ByStatusOrderByCheckedAtDesc(
                CheckStatus.FAILURE
        );

        long activeMonitors = monitors.stream().filter(Monitor::isActive).count();
        long downMonitors = monitors.stream().filter(monitor -> monitor.getStatus() == MonitorStatus.DOWN).count();

        return new DashboardSummaryResponse(
                monitors.size(),
                activeMonitors,
                downMonitors,
                averageLatency(thirtyDayResults),
                uptimeForWindow(thirtyDayResults, Duration.ofDays(1)),
                uptimeForWindow(thirtyDayResults, Duration.ofDays(7)),
                uptimeForWindow(thirtyDayResults, Duration.ofDays(30)),
                latestFailedChecks.stream()
                        .map(this::toFailedCheckSummary)
                        .toList()
        );
    }

    private long averageLatency(List<CheckResult> results) {
        return Math.round(results.stream()
                .mapToLong(CheckResult::getResponseTimeMs)
                .average()
                .orElse(0));
    }

    private double uptimeForWindow(List<CheckResult> results, Duration window) {
        Instant windowStart = clock.instant().minus(window);
        List<CheckResult> windowResults = results.stream()
                .filter(result -> result.getCheckedAt().isAfter(windowStart))
                .toList();

        if (windowResults.isEmpty()) {
            return 0;
        }

        long successfulChecks = windowResults.stream()
                .filter(result -> result.getStatus() == CheckStatus.SUCCESS)
                .count();

        return Math.round(((double) successfulChecks / windowResults.size()) * 10000.0) / 100.0;
    }

    private DashboardSummaryResponse.FailedCheckSummary toFailedCheckSummary(CheckResult result) {
        return new DashboardSummaryResponse.FailedCheckSummary(
                result.getId(),
                result.getMonitor().getId(),
                result.getMonitor().getName(),
                result.getCheckedAt(),
                result.getStatusCode(),
                result.getResponseTimeMs(),
                result.getStatus(),
                result.getErrorMessage()
        );
    }
}
