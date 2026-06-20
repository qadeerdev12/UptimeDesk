package com.qadeer.uptimedesk.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class ApiHealthController {

    @GetMapping("/api/health")
    Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "UptimeDesk",
                "timestamp", Instant.now()
        );
    }
}
