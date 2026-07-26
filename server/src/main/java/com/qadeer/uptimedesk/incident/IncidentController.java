package com.qadeer.uptimedesk.incident;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping("/{id}/acknowledge")
    IncidentResponse acknowledgeIncident(@PathVariable Long id) {
        return incidentService.acknowledgeIncident(id);
    }
}
