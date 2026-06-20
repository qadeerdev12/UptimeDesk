package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import com.qadeer.uptimedesk.monitor.MonitorStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@Service
public class MonitorCheckService {

    private final CheckResultRepository checkResultRepository;
    private final MonitorRepository monitorRepository;

    public MonitorCheckService(CheckResultRepository checkResultRepository, MonitorRepository monitorRepository) {
        this.checkResultRepository = checkResultRepository;
        this.monitorRepository = monitorRepository;
    }

    public CheckResult check(Monitor monitor) {
        long startedAt = System.nanoTime();
        CheckResult result = new CheckResult();
        result.setMonitor(monitor);

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(monitor.getTimeoutSeconds()))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(monitor.getUrl()))
                    .timeout(Duration.ofSeconds(monitor.getTimeoutSeconds()))
                    .method(monitor.getMethod().name(), HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            boolean success = response.statusCode() == monitor.getExpectedStatusCode();

            result.setStatusCode(response.statusCode());
            result.setStatus(success ? CheckStatus.SUCCESS : CheckStatus.FAILURE);
            monitor.setStatus(success ? MonitorStatus.UP : MonitorStatus.DOWN);
        } catch (Exception ex) {
            result.setStatus(CheckStatus.FAILURE);
            result.setErrorMessage(ex.getMessage());
            monitor.setStatus(MonitorStatus.DOWN);
        }

        result.setResponseTimeMs(Duration.ofNanos(System.nanoTime() - startedAt).toMillis());
        monitor.setLastCheckedAt(Instant.now());
        monitorRepository.save(monitor);

        return checkResultRepository.save(result);
    }
}
