package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import com.qadeer.uptimedesk.monitor.MonitorStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class MonitorCheckService {

    private final CheckResultRepository checkResultRepository;
    private final MonitorRepository monitorRepository;
    private final EndpointCheckClient endpointCheckClient;

    public MonitorCheckService(
            CheckResultRepository checkResultRepository,
            MonitorRepository monitorRepository,
            EndpointCheckClient endpointCheckClient
    ) {
        this.checkResultRepository = checkResultRepository;
        this.monitorRepository = monitorRepository;
        this.endpointCheckClient = endpointCheckClient;
    }

    public CheckResult check(Monitor monitor) {
        long startedAt = System.nanoTime();
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

        result.setResponseTimeMs(Duration.ofNanos(System.nanoTime() - startedAt).toMillis());
        monitor.setLastCheckedAt(Instant.now());
        monitorRepository.save(monitor);

        return checkResultRepository.save(result);
    }
}
