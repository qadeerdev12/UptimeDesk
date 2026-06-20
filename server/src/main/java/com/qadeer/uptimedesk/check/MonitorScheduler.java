package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class MonitorScheduler {

    private final MonitorRepository monitorRepository;
    private final MonitorCheckService monitorCheckService;

    public MonitorScheduler(MonitorRepository monitorRepository, MonitorCheckService monitorCheckService) {
        this.monitorRepository = monitorRepository;
        this.monitorCheckService = monitorCheckService;
    }

    @Scheduled(fixedDelay = 60_000)
    void checkDueMonitors() {
        for (Monitor monitor : monitorRepository.findByActiveTrue()) {
            if (isDue(monitor)) {
                monitorCheckService.check(monitor);
            }
        }
    }

    private boolean isDue(Monitor monitor) {
        if (monitor.getLastCheckedAt() == null) {
            return true;
        }

        Instant nextCheckAt = monitor.getLastCheckedAt().plus(Duration.ofMinutes(monitor.getIntervalMinutes()));
        return !Instant.now().isBefore(nextCheckAt);
    }
}
