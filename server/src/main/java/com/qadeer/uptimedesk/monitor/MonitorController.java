package com.qadeer.uptimedesk.monitor;

import com.qadeer.uptimedesk.check.CheckResultResponse;
import com.qadeer.uptimedesk.check.CheckResultRepository;
import com.qadeer.uptimedesk.check.MonitorCheckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitors")
public class MonitorController {

    private final MonitorRepository monitorRepository;
    private final CheckResultRepository checkResultRepository;
    private final MonitorCheckService monitorCheckService;

    public MonitorController(
            MonitorRepository monitorRepository,
            CheckResultRepository checkResultRepository,
            MonitorCheckService monitorCheckService
    ) {
        this.monitorRepository = monitorRepository;
        this.checkResultRepository = checkResultRepository;
        this.monitorCheckService = monitorCheckService;
    }

    @GetMapping
    List<MonitorResponse> listMonitors() {
        return monitorRepository.findAll()
                .stream()
                .map(MonitorResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    MonitorResponse getMonitor(@PathVariable Long id) {
        return MonitorResponse.from(findMonitor(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    MonitorResponse createMonitor(@Valid @RequestBody CreateMonitorRequest request) {
        Monitor monitor = new Monitor();
        applyCreateRequest(monitor, request);

        return MonitorResponse.from(monitorRepository.save(monitor));
    }

    @PutMapping("/{id}")
    MonitorResponse updateMonitor(@PathVariable Long id, @Valid @RequestBody UpdateMonitorRequest request) {
        Monitor monitor = findMonitor(id);
        monitor.setName(request.name());
        monitor.setUrl(request.url());
        monitor.setMethod(request.method());
        monitor.setExpectedStatusCode(request.expectedStatusCode());
        monitor.setIntervalMinutes(request.intervalMinutes());
        monitor.setTimeoutSeconds(request.timeoutSeconds());
        monitor.setExpectedKeyword(normalizeOptionalText(request.expectedKeyword()));
        monitor.setFailureThreshold(request.failureThreshold());
        monitor.setActive(request.active());

        return MonitorResponse.from(monitorRepository.save(monitor));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteMonitor(@PathVariable Long id) {
        Monitor monitor = findMonitor(id);
        monitorRepository.delete(monitor);
    }

    @GetMapping("/{id}/results")
    List<CheckResultResponse> listRecentResults(@PathVariable Long id) {
        findMonitor(id);

        return checkResultRepository.findTop20ByMonitorIdOrderByCheckedAtDesc(id)
                .stream()
                .map(CheckResultResponse::from)
                .toList();
    }

    @PostMapping("/{id}/run")
    CheckResultResponse runCheckNow(@PathVariable Long id) {
        Monitor monitor = findMonitor(id);
        return CheckResultResponse.from(monitorCheckService.check(monitor));
    }

    private Monitor findMonitor(Long id) {
        return monitorRepository.findById(id)
                .orElseThrow(() -> new MonitorNotFoundException(id));
    }

    private void applyCreateRequest(Monitor monitor, CreateMonitorRequest request) {
        monitor.setName(request.name());
        monitor.setUrl(request.url());
        monitor.setMethod(request.method());
        monitor.setExpectedStatusCode(request.expectedStatusCode());
        monitor.setIntervalMinutes(request.intervalMinutes());
        monitor.setTimeoutSeconds(request.timeoutSeconds());
        monitor.setExpectedKeyword(normalizeOptionalText(request.expectedKeyword()));
        monitor.setFailureThreshold(request.failureThreshold() == null ? 2 : request.failureThreshold());
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
