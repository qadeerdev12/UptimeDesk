package com.qadeer.uptimedesk.incident;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/active")
    List<IncidentResponse> listActiveIncidents(Authentication authentication) {
        return incidentService.listActiveIncidents(authentication.getName());
    }

    @GetMapping("/{id}")
    IncidentResponse getIncident(@PathVariable Long id, Authentication authentication) {
        return incidentService.getIncident(id, authentication.getName());
    }

    @PostMapping("/{id}/acknowledge")
    IncidentResponse acknowledgeIncident(@PathVariable Long id, Authentication authentication) {
        return incidentService.acknowledgeIncident(id, authentication.getName());
    }
}
