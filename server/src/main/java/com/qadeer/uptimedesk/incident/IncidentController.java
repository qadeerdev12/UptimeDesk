package com.qadeer.uptimedesk.incident;

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
    List<IncidentResponse> listActiveIncidents() {
        return incidentService.listActiveIncidents();
    }

    @GetMapping("/{id}")
    IncidentResponse getIncident(@PathVariable Long id) {
        return incidentService.getIncident(id);
    }

    @PostMapping("/{id}/acknowledge")
    IncidentResponse acknowledgeIncident(@PathVariable Long id) {
        return incidentService.acknowledgeIncident(id);
    }
}
