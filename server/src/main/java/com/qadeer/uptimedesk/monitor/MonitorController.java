package com.qadeer.uptimedesk.monitor;

import com.qadeer.uptimedesk.check.CheckResultResponse;
import com.qadeer.uptimedesk.check.CheckResultRepository;
import com.qadeer.uptimedesk.check.MonitorCheckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    List<Monitor> listMonitors() {
        return monitorRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Monitor createMonitor(@Valid @RequestBody CreateMonitorRequest request) {
        Monitor monitor = new Monitor();
        monitor.setName(request.name());
        monitor.setUrl(request.url());
        monitor.setMethod(request.method());
        monitor.setExpectedStatusCode(request.expectedStatusCode());
        monitor.setIntervalMinutes(request.intervalMinutes());
        monitor.setTimeoutSeconds(request.timeoutSeconds());

        return monitorRepository.save(monitor);
    }

    @GetMapping("/{id}/results")
    List<CheckResultResponse> listRecentResults(@PathVariable Long id) {
        return checkResultRepository.findTop20ByMonitorIdOrderByCheckedAtDesc(id)
                .stream()
                .map(CheckResultResponse::from)
                .toList();
    }

    @PostMapping("/{id}/run")
    CheckResultResponse runCheckNow(@PathVariable Long id) {
        Monitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Monitor not found"));

        return CheckResultResponse.from(monitorCheckService.check(monitor));
    }
}
